package uk.ac.ebi.pride.toolsuite.chart.dataset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author qingwei
 * @author ypriverol
 * Date: 20/06/13
 */
public class PrideEqualWidthHistogramDataSource extends PrideHistogramDataSource {

    public PrideEqualWidthHistogramDataSource(PrideData[] values, boolean calcAllSpectra, boolean categoryType) {
        super(values, calcAllSpectra, categoryType);
    }

    /**
     * generate a couple of histogram bins, which size is binCount. Every bin's width are same
     * and first bin's lower bound is start.
     *
     * [start, start+binWidth), [start+binWidth, start+binWidth+binWidth) ....
     */
    public Collection<PrideHistogramBin> generateBins(double start, double binWidth, int binCount) {
        if (binWidth <= 0) {
            throw new IllegalArgumentException("Bin width should be great than 0");
        }
        if (binCount < 0) {
            throw new IllegalArgumentException("Bin count should be great than 0");
        }

        Collection<PrideHistogramBin> newBins = new ArrayList<PrideHistogramBin>();

        double lowerBound = start;
        double higherBound;
        for (int i = 0; i < binCount; i++) {
            higherBound = lowerBound + binWidth;
            newBins.add(new PrideHistogramBin(lowerBound, higherBound));
            lowerBound = higherBound;
        }

        return newBins;
    }

    /**
     * The bin width should be integer multiple of granularity.
     */
    public Collection<PrideHistogramBin> generateGranularityBins(double start, int binCount, int granularity) {
        if (binCount <= 0) {
            throw new IllegalArgumentException("Bin count should be great than 0");
        }

        double end = Double.MIN_VALUE;
        for (PrideData value : values) {
            int v = value.getData().intValue();
            if (v > end) {
                end = v;
            }
        }

        int binWidth = (int) Math.ceil((end - start) / binCount);
        int remainder = binWidth % granularity == 0 ? 0 : granularity;
        binWidth = binWidth / granularity * granularity + remainder;

        return generateBins(start, binWidth, binCount);
    }

    public Collection<PrideHistogramBin> generateBins(double start, double binWidth) {
        if (binWidth <= 0) {
            throw new IllegalArgumentException("Bin width should be great than 0");
        }

        double end = Double.MIN_VALUE;
        for (PrideData value : values) {
            int v = value.getData().intValue();
            if (v > end) {
                end = v;
            }
        }

        int binCount = (int)Math.ceil((end - start) / binWidth);
        return generateBins(start, binWidth, binCount);
    }

    public double getBinWidth() {
        if (bins.isEmpty()) {
            throw new UnsupportedOperationException("Current histogram bin collection is empty.");
        }

        return bins.first().getBinWidth();
    }

    public void appendBin(PrideHistogramBin bin) {
        if (! bins.isEmpty() &&
            ! new BigDecimal(bin.getBinWidth()).setScale(2, RoundingMode.CEILING).equals(new BigDecimal(bins.first().getBinWidth()).setScale(2, RoundingMode.CEILING))) {
            throw new IllegalArgumentException("the bin width not be same with exists bin width.");
        }

        super.appendBin(bin);
    }
}
