package marytts.language.hsb

import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class PreprocessTest {

    @DataProvider
    Object[][] numbers() {
        [
                [0, 'nul'],
                [1, 'jedyn'],
                [2, 'dwaj'],
                [3, 'tři'],
                [9, 'dźewjeć']
        ]
    }

    @Test(dataProvider = 'numbers')
    void testGetExpandedNumber(Integer input, String expected) {
        def preprocess = new Preprocess()
        def actual = preprocess.getExpandedNumber(input)
        assert expected == actual
    }
}