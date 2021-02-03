package marytts.language.hsb;

import marytts.datatypes.MaryDataType;
import marytts.modules.InternalModule;

import java.util.Locale;

public class Preprocess extends InternalModule {

    public Preprocess() {
        super("Preprocess", MaryDataType.TOKENS, MaryDataType.WORDS, Locale.forLanguageTag("hsb"));
    }
}
