package uk.ac.ebi.pride.chart.dataset;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.ebi.pride.chart.io.PrideSpectrumHistogramDataSource;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.SortedMap;

/**
 * User: Qingwei
 * Date: 13/06/13
 */
public class PrideDatasetFactory {
    private PrideDatasetFactory() {}

    private static XYSeries getSeries(PrideXYDataSource dataSource) {
        XYSeries series = new XYSeries(dataSource.getDataType().toString());
        Double[] domainValues = dataSource.getDomainData();
        PrideData[] rangeValues = dataSource.getRangeData();

        for (int i = 0; i < domainValues.length; i++) {
            series.add(domainValues[i], rangeValues[i].getData());
        }

        return series;
    }

    public static XYSeriesCollection getXYDataset(PrideXYDataSource dataSource) {
        XYSeries series = getSeries(dataSource.filter(dataSource.getDataType()));

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        Collection<PrideDataType> subTypes = dataSource.getDataType().getChildren();
        for (PrideDataType subType : subTypes) {
            PrideXYDataSource filterDataSource = dataSource.filter(subType);
            if (filterDataSource != null) {
                series = getSeries(filterDataSource);
                dataset.addSeries(series);
            }
        }

        return dataset;
    }

    public static XYSeriesCollection getXYDataset(PrideSpectrumHistogramDataSource dataSource) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Double>> histMap = dataSource.getIntensityMap();
        XYSeries series;
        SortedMap<PrideHistogramBin, Double> histogram;
        for (PrideDataType dataType : histMap.keySet()) {
            series = new XYSeries(dataType.getTitle());
            histogram = histMap.get(dataType);
            for (PrideHistogramBin bin : histogram.keySet()) {
                series.add(bin.getStartBoundary(), histogram.get(bin));
            }
            dataset.addSeries(series);
        }

        return dataset;
    }

    public static CategoryDataset getHistogramDataset(PrideHistogramDataSource dataSource) {
        SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> histogramMap = dataSource.getHistogramMap();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String category;
        String seriesKey;
        SortedMap<PrideHistogramBin, Integer> histogram;
        for (PrideDataType dataType : histogramMap.keySet()) {
            histogram = histogramMap.get(dataType);
            seriesKey = dataType.getTitle();
            for (PrideHistogramBin bin : histogram.keySet()) {
                category = bin.getEndBoundary() == Integer.MAX_VALUE ? ">" + bin.getStartBoundary() : bin.toString(new DecimalFormat("#"));
                dataset.addValue(histogram.get(bin), seriesKey, category);
            }
        }

        return dataset;
    }
}
