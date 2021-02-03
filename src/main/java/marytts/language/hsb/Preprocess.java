package marytts.language.hsb;

import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.modules.InternalModule;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;


public class Preprocess extends InternalModule {

    static final ULocale locale = new ULocale.Builder().setLanguage("hsb").build();
    private RuleBasedNumberFormat ruleBasedNumberFormat;

    public Preprocess() {
        super("Preprocess", MaryDataType.TOKENS, MaryDataType.WORDS, locale.toLocale());
        ruleBasedNumberFormat = new RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT);
    }

    public MaryData process(MaryData d) throws Exception {
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
            if (tokenText.matches("\\d+")) {
                Double number = Double.parseDouble(tokenText);
                MaryDomUtils.setTokenText(token, getExpandedNumber(number));
            }
        }
    }

    private String getExpandedNumber(Double number) {
        return ruleBasedNumberFormat.format(number);
    }
}
