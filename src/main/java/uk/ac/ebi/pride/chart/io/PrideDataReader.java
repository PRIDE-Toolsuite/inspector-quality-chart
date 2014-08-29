package uk.ac.ebi.pride.chart.io;

import uk.ac.ebi.pride.chart.PrideChartType;
import uk.ac.ebi.pride.chart.dataset.PrideHistogramDataSource;
import uk.ac.ebi.pride.chart.dataset.PrideXYDataSource;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * User: Qingwei
 * Date: 12/06/13
 */
public abstract class PrideDataReader {
    protected SortedMap<PrideChartType, PrideXYDataSource> xyDataSourceMap = new TreeMap<PrideChartType, PrideXYDataSource>();
    protected SortedMap<PrideChartType, PrideHistogramDataSource> histogramDataSourceMap = new TreeMap<PrideChartType, PrideHistogramDataSource>();
    protected SortedMap<PrideChartType, PrideDataException> errorMap = new TreeMap<PrideChartType, PrideDataException>();

    // peptides size in the experiment data.
    protected int peptideSize = 0;
    // identified spectra size in the experiment data.
    protected int identifiedSpectraSize = 0;
    // unidentified spectra size in the experiment data.
    protected int unidentifiedSpectraSize = 0;

    public void readData() {
        start();
        reading();
        end();
    }

    protected abstract void start();

    protected abstract void reading();

    protected abstract void end();

    public SortedMap<PrideChartType, PrideXYDataSource> getXYDataSourceMap() {
        return xyDataSourceMap;
    }

    public SortedMap<PrideChartType, PrideHistogramDataSource> getHistogramDataSourceMap() {
        return histogramDataSourceMap;
    }

    public SortedMap<PrideChartType, PrideDataException> getErrorMap() {
        return errorMap;
    }

    public int getPeptideSize() {
        return peptideSize;
    }

    public int getIdentifiedSpectraSize() {
        return identifiedSpectraSize;
    }

    public int getUnidentifiedSpectraSize() {
        return unidentifiedSpectraSize;
    }

    public int getSpectraSize() {
        return identifiedSpectraSize + unidentifiedSpectraSize;
    }
}
