package marytts.language.hsb

import org.apache.commons.csv.CSVFormat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class PreprocessTest {

    @DataProvider
    Object[][] numbers() {
        def stream = this.getClass().getResourceAsStream('numbers.csv')
        def reader = stream.newReader('UTF-8')
        def csvParser = CSVFormat.DEFAULT.parse(reader)
        return csvParser.collect { record ->
            [new BigDecimal(record.get(0)), record.get(1)]
        }
    }

    @Test(dataProvider = 'numbers')
    void testGetExpandedNumber(BigDecimal input, String expected) {
        def preprocess = new Preprocess()
        def actual = preprocess.getExpandedNumber(input)
        assert expected == actual
    }
}
