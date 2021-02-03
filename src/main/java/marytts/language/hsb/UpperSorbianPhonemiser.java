package marytts.language.hsb;

import marytts.exceptions.MaryConfigurationException;

import java.io.IOException;

public class UpperSorbianPhonemiser extends marytts.modules.JPhonemiser {
    public UpperSorbianPhonemiser() throws IOException, MaryConfigurationException {
        super("hsb.");
    }
}
