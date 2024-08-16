package marytts.language.hsb;

import com.google.common.base.Charsets;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.exceptions.MaryConfigurationException;
import marytts.modules.InternalModule;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class Preprocess extends InternalModule {

    static final ULocale locale = new ULocale.Builder().setLanguage("hsb").build();
    private Map<String, String> abbreviations;
    private Map<String, String> symbols;
    private RuleBasedNumberFormat ruleBasedNumberFormat;
    private NumberFormat numberFormat;

    public Preprocess() throws MaryConfigurationException {
        super("Preprocess", MaryDataType.TOKENS, MaryDataType.WORDS, locale.toLocale());
        initNumberExpansion("formatRules.txt");
        initSymbolExpansion("symbols.csv");
        initAbbreviationExpansion("abbreviations.csv");
    }

    private void initAbbreviationExpansion(String resourceName) throws MaryConfigurationException {
        try {
            abbreviations = new HashMap<>();
            InputStream abbreviationsStream = this.getClass().getResourceAsStream(resourceName);
            InputStreamReader abbreviationsReader = new InputStreamReader(abbreviationsStream, Charsets.UTF_8);
            CSVParser csv = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                    .setHeader("abbreviation", "expansion")
                    .build()
                    .parse(abbreviationsReader);
            for (CSVRecord record : csv) {
                String abbreviation = record.get("abbreviation");
                String expansion = record.get("expansion");
                abbreviations.put(abbreviation, expansion);
            }
        } catch (Exception exception) {
            throw new MaryConfigurationException(String.format("Could not load abbreviations from %s.%s", this.getClass().getCanonicalName(), resourceName), exception);
        }
    }

    private void initSymbolExpansion(String resourceName) throws MaryConfigurationException {
        try {
            symbols = new HashMap<>();
            InputStream symbolsStream = this.getClass().getResourceAsStream(resourceName);
            InputStreamReader symbolsReader = new InputStreamReader(symbolsStream, Charsets.UTF_8);
            CSVParser csv = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                    .setHeader("symbol", "expansion")
                    .build()
                    .parse(symbolsReader);
            for (CSVRecord record : csv) {
                String symbol = record.get("symbol");
                String expansion = record.get("expansion");
                symbols.put(symbol, expansion);
            }
        } catch (Exception exception) {
            throw new MaryConfigurationException(String.format("Could not load symbols from %s.%s", this.getClass().getCanonicalName(), resourceName), exception);
        }
    }

    private void initNumberExpansion(String resourceName) throws MaryConfigurationException {
        try {
            InputStream formatRulesStream = this.getClass().getResourceAsStream(resourceName);
            String formatRules = IOUtils.toString(formatRulesStream, StandardCharsets.UTF_8);
            ruleBasedNumberFormat = new RuleBasedNumberFormat(formatRules, locale);
            numberFormat = NumberFormat.getNumberInstance(locale);
        } catch (Exception exception) {
            throw new MaryConfigurationException(String.format("Could not load format rules from %s.%s", this.getClass().getCanonicalName(), resourceName), exception);
        }
    }

    public MaryData process(MaryData d) {
        Document doc = d.getDocument();
        expandAllAbbreviations(doc);
        expandAllSymbols(doc);
        expandAllNumbers(doc);
        MaryData result = new MaryData(getOutputType(), d.getLocale());
        result.setDocument(doc);
        return result;
    }

    private void expandAllAbbreviations(Document document) {
        TreeWalker treeWalker = ((DocumentTraversal) document).createTreeWalker(document, NodeFilter.SHOW_ELEMENT,
                new NameNodeFilter(MaryXML.TOKEN), false);
        Element token;
        while ((token = (Element) treeWalker.nextNode()) != null) {
            String tokenText = MaryDomUtils.tokenText(token);
            String expandedAbbreviation = expandAbbreviation(tokenText);
            if (expandedAbbreviation != tokenText) {
                MaryDomUtils.setTokenText(token, expandedAbbreviation);
            }
        }
    }

    protected String expandAbbreviation(String abbreviation) {
        if (abbreviations.containsKey(abbreviation))
            return abbreviations.get(abbreviation);
        else
            return abbreviation;
    }

    private void expandAllSymbols(Document document) {
        TreeWalker treeWalker = ((DocumentTraversal) document).createTreeWalker(document, NodeFilter.SHOW_ELEMENT,
                new NameNodeFilter(MaryXML.TOKEN), false);
        Element token;
        while ((token = (Element) treeWalker.nextNode()) != null) {
            String tokenText = MaryDomUtils.tokenText(token);
            String expandedSymbol = expandSymbol(tokenText);
            if (expandedSymbol != tokenText) {
                MaryDomUtils.setTokenText(token, expandedSymbol);
            }
        }
    }

    protected String expandSymbol(String symbol) {
        if (symbols.containsKey(symbol))
            return symbols.get(symbol);
        else
            return symbol;
    }

    private void expandAllNumbers(Document document) {
        TreeWalker treeWalker = ((DocumentTraversal) document).createTreeWalker(document, NodeFilter.SHOW_ELEMENT,
                new NameNodeFilter(MaryXML.TOKEN), false);
        Element token;
        while ((token = (Element) treeWalker.nextNode()) != null) {
            String tokenText = MaryDomUtils.tokenText(token);
            Number number = parseNumber(tokenText);
            if (number != null) {
                String spelledOutNumber = spelloutNumber(number);
                MaryDomUtils.setTokenText(token, spelledOutNumber);
            }
        }
    }

    protected Number parseNumber(String token) {
        try {
            return numberFormat.parse(token);
        } catch (ParseException e) {
            return null;
        }
    }

    protected String spelloutNumber(Number number) {
        return ruleBasedNumberFormat.format(number);
    }

}
