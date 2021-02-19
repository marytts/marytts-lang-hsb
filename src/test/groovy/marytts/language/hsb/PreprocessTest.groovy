package marytts.language.hsb

import org.testng.annotations.Test

class PreprocessTest {

    @Test
    void testGetExpandedNumber() {
        def input = 9
        def expected = 'dźewjeć'
        def preprocess = new Preprocess()
        def actual = preprocess.getExpandedNumber(input)
        assert expected == actual
    }
}
