package uk.ac.ebi.pride.chart.io;

import org.junit.Test;
import uk.ac.ebi.pride.chart.PrideChartType;
import uk.ac.ebi.pride.chart.dataset.*;
import uk.ac.ebi.pride.data.controller.DataAccessController;
import uk.ac.ebi.pride.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.SortedMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

/**
 * User: qingwei
 * Date: 25/06/13
 */
public class DataAccessReaderTest {

//    private File jsonFile = new File("testset/old_2.json");

//    private File prideXMLFile = new File("testset/PRIDE_Exp_Complete_Ac_1643.xml");
//    private File jsonFile = new File("testset/old_1643.json");

    private DataAccessReader dataReader;
    private JSONReader jsonReader;
//    private ElderJSONReader jsonReader;

    public DataAccessReaderTest() throws Exception {
        URL url = DataAccessReaderTest.class.getClassLoader().getResource("new_2.json");
        File jsonFile = new File(url.toURI());
        url = DataAccessReaderTest.class.getClassLoader().getResource("PRIDE_Exp_Complete_Ac_2.xml");
        File prideXMLFile = new File(url.toURI());
        DataAccessController controller = new PrideXmlControllerImpl(prideXMLFile);
        dataReader = new DataAccessReader(controller);
        jsonReader = new JSONReader(jsonFile);
//        jsonReader = new ElderJSONReader(jsonFile);
    }

    @Test
    public void testPeptides() throws Exception {
        PrideChartType type = PrideChartType.PEPTIDES_PROTEIN;

        PrideXYDataSource prideDataSource = dataReader.getXYDataSourceMap().get(type);
        PrideXYDataSource jsonDataSource = jsonReader.getXYDataSourceMap().get(type);

        assertArrayEquals(prideDataSource.getDomainData(), jsonDataSource.getDomainData());
        assertArrayEquals(prideDataSource.getRangeData(), jsonDataSource.getRangeData());
    }

    @Test
    public void testDelta() throws Exception {
        PrideChartType type = PrideChartType.DELTA_MASS;

        PrideXYDataSource prideDataSource = dataReader.getXYDataSourceMap().get(type);
        PrideXYDataSource jsonDataSource = jsonReader.getXYDataSourceMap().get(type);

        double threshold = 0.1;
        for (int i = 0; i < prideDataSource.getDomainData().length; i++) {
            assertTrue(Math.abs(prideDataSource.getDomainData()[i] - jsonDataSource.getDomainData()[i]) < threshold);
        }
    }

    @Test
    public void testMissedCleavages() throws Exception {
        PrideChartType type = PrideChartType.MISSED_CLEAVAGES;

        PrideXYDataSource prideDataSource = dataReader.getXYDataSourceMap().get(type);
        PrideXYDataSource jsonDataSource = jsonReader.getXYDataSourceMap().get(type);

        assertArrayEquals(prideDataSource.getDomainData(), jsonDataSource.getDomainData());
        double sum = 0;
        for (PrideData data : jsonDataSource.getRangeData()) {
            sum += data.getData();
        }

        // tolerance is <1%
        for (int i = 0; i < prideDataSource.getRangeData().length; i++) {
            assertTrue(Math.abs(prideDataSource.getRangeData()[i].getData() - jsonDataSource.getRangeData()[i].getData()) < sum * 0.01);
        }
    }

    @Test
    public void testAvg() throws Exception {
        PrideChartType type = PrideChartType.AVERAGE_MS;

        PrideSpectrumHistogramDataSource prideDataSource = (PrideSpectrumHistogramDataSource) dataReader.getHistogramDataSourceMap().get(type);
        PrideSpectrumHistogramDataSource jsonDataSource = (PrideSpectrumHistogramDataSource) jsonReader.getHistogramDataSourceMap().get(type);

        SortedMap<PrideHistogramBin, Double> idPrideHistogram = prideDataSource.getIntensityMap().get(PrideDataType.IDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Double> unPrideHistogram = prideDataSource.getIntensityMap().get(PrideDataType.UNIDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Double> allPrideHistogram = prideDataSource.getIntensityMap().get(PrideDataType.ALL_SPECTRA);

        SortedMap<PrideHistogramBin, Double> idJSONHistogram = prideDataSource.getIntensityMap().get(PrideDataType.IDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Double> unJSONHistogram = prideDataSource.getIntensityMap().get(PrideDataType.UNIDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Double> allJSONHistogram = prideDataSource.getIntensityMap().get(PrideDataType.ALL_SPECTRA);

        int min = Math.min(idPrideHistogram.keySet().size(), idJSONHistogram.keySet().size());
//        for (int i = 0; i < min; i++) {
//            assertEquals(idPrideDataSource.getDomainData()[i], idJSONDataSource.getDomainData()[i]);
//            assertTrue(Math.abs(idPrideDataSource.getRangeData()[i].getData() - idJSONDataSource.getRangeData()[i].getData()) < 0.01);
//        }
//
//        min = Math.min(unPrideDataSource.getDomainData().length, unJSONDataSource.getDomainData().length);
//        for (int i = 0; i < min; i++) {
//            assertEquals(unPrideDataSource.getDomainData()[i], unJSONDataSource.getDomainData()[i]);
//            assertTrue(Math.abs(unPrideDataSource.getRangeData()[i].getData() - unJSONDataSource.getRangeData()[i].getData()) < 0.01);
//        }
//
//        min = Math.min(allPrideDataSource.getDomainData().length, allJSONDataSource.getDomainData().length);
//        for (int i = 0; i < min; i++) {
//            assertEquals(allPrideDataSource.getDomainData()[i], allJSONDataSource.getDomainData()[i]);
//            assertTrue(Math.abs(allPrideDataSource.getRangeData()[i].getData() - allJSONDataSource.getRangeData()[i].getData()) < 0.01);
//        }
    }

    @Test
    public void testPrecursorCharge() throws Exception {
        PrideChartType type = PrideChartType.PRECURSOR_CHARGE;

        PrideXYDataSource prideDataSource = dataReader.getXYDataSourceMap().get(type);
        PrideXYDataSource jsonDataSource = jsonReader.getXYDataSourceMap().get(type);

        assertArrayEquals(prideDataSource.getDomainData(), jsonDataSource.getDomainData());
        assertArrayEquals(prideDataSource.getRangeData(), jsonDataSource.getRangeData());
    }

    @Test
    public void testPrecursorMasses() throws Exception {
        PrideChartType type = PrideChartType.PRECURSOR_MASSES;

        PrideXYDataSource prideDataSource = dataReader.getXYDataSourceMap().get(type);
        PrideXYDataSource jsonDataSource = jsonReader.getXYDataSourceMap().get(type);

        PrideXYDataSource idPrideDataSource = prideDataSource.filter(PrideDataType.IDENTIFIED_SPECTRA);
        PrideXYDataSource unPrideDataSource = prideDataSource.filter(PrideDataType.UNIDENTIFIED_SPECTRA);
        PrideXYDataSource allPrideDataSource = prideDataSource.filter(PrideDataType.ALL_SPECTRA);

        PrideXYDataSource idJSONDataSource = jsonDataSource.filter(PrideDataType.IDENTIFIED_SPECTRA);
        PrideXYDataSource unJSONDataSource = jsonDataSource.filter(PrideDataType.UNIDENTIFIED_SPECTRA);
        PrideXYDataSource allJSONDataSource = jsonDataSource.filter(PrideDataType.ALL_SPECTRA);

        int min;
        if (idPrideDataSource != null) {
            min = Math.min(idPrideDataSource.getDomainData().length, idJSONDataSource.getDomainData().length);
            for (int i = 0; i < min; i++) {
                assertEquals(idPrideDataSource.getDomainData()[i], idJSONDataSource.getDomainData()[i]);
                assertTrue(Math.abs(idPrideDataSource.getRangeData()[i].getData() - idJSONDataSource.getRangeData()[i].getData()) < 0.01);
            }
        }

        if (unPrideDataSource != null) {
            min = Math.min(unPrideDataSource.getDomainData().length, unJSONDataSource.getDomainData().length);
            for (int i = 0; i < min; i++) {
                assertEquals(unPrideDataSource.getDomainData()[i], unJSONDataSource.getDomainData()[i]);
                assertTrue(Math.abs(unPrideDataSource.getRangeData()[i].getData() - unJSONDataSource.getRangeData()[i].getData()) < 0.01);
            }
        }


        min = Math.min(allPrideDataSource.getDomainData().length, allJSONDataSource.getDomainData().length);
        for (int i = 0; i < min; i++) {
            assertEquals(allPrideDataSource.getDomainData()[i], allJSONDataSource.getDomainData()[i]);
            assertTrue(Math.abs(allPrideDataSource.getRangeData()[i].getData() - allJSONDataSource.getRangeData()[i].getData()) < 0.01);
        }
    }

    private void compareHistogram(SortedMap<PrideHistogramBin, Integer> prideHistogram,
                                  SortedMap<PrideHistogramBin, Integer> jsonHistogram) {
        assertArrayEquals(prideHistogram.keySet().toArray(), jsonHistogram.keySet().toArray());

        Collection<Integer> prideFrequencyList = prideHistogram.values();
        Collection<Integer> jsonFrequencyList = jsonHistogram.values();

        assertArrayEquals(prideFrequencyList.toArray(), jsonFrequencyList.toArray());
    }

    @Test
    public void testPeaksMS() throws Exception {
        PrideChartType type = PrideChartType.PEAKS_MS;

        PrideHistogramDataSource prideDataSource = dataReader.getHistogramDataSourceMap().get(type);
        PrideHistogramDataSource jsonDataSource = jsonReader.getHistogramDataSourceMap().get(type);

        SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> prideHistogram = prideDataSource.getHistogramMap();
        SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> jsonHistogram = jsonDataSource.getHistogramMap();

        compareHistogram(prideHistogram.get(PrideDataType.ALL_SPECTRA), jsonHistogram.get(PrideDataType.ALL_SPECTRA));
    }

    @Test
    public void testPeaksIntensity() throws Exception {
        PrideChartType type = PrideChartType.PEAK_INTENSITY;

        PrideHistogramDataSource prideDataSource = dataReader.getHistogramDataSourceMap().get(type);
        PrideHistogramDataSource jsonDataSource = jsonReader.getHistogramDataSourceMap().get(type);

        SortedMap<PrideHistogramBin, Integer> idPrideHistogram = prideDataSource.getHistogramMap().get(PrideDataType.IDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Integer> unPrideHistogram = prideDataSource.getHistogramMap().get(PrideDataType.UNIDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Integer> allPrideHistogram = prideDataSource.getHistogramMap().get(PrideDataType.ALL_SPECTRA);

        SortedMap<PrideHistogramBin, Integer> idJSONHistogram = jsonDataSource.getHistogramMap().get(PrideDataType.IDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Integer> unJSONHistogram = jsonDataSource.getHistogramMap().get(PrideDataType.UNIDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Integer> allJSONHistogram = jsonDataSource.getHistogramMap().get(PrideDataType.ALL_SPECTRA);

//        compareHistogram(idPrideHistogram, idJSONHistogram);
//        compareHistogram(unPrideHistogram, unJSONHistogram);
//        compareHistogram(allPrideHistogram, allJSONHistogram);
    }
}
