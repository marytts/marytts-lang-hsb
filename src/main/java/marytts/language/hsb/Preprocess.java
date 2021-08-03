package marytts.language.hsb;

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
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class Preprocess extends InternalModule {

    static final ULocale locale = new ULocale.Builder().setLanguage("hsb").build();
    private RuleBasedNumberFormat ruleBasedNumberFormat;
    private NumberFormat numberFormat;

    public Preprocess() throws MaryConfigurationException {
        super("Preprocess", MaryDataType.TOKENS, MaryDataType.WORDS, locale.toLocale());
        initNumberExpansion("formatRules.txt");
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
        expandAllNumbers(doc);
        MaryData result = new MaryData(getOutputType(), d.getLocale());
        result.setDocument(doc);
        return result;
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
