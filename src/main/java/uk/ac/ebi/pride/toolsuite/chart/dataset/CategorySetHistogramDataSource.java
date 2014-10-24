package uk.ac.ebi.pride.toolsuite.chart.dataset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author ypriverol
 *
 */
public class CategorySetHistogramDataSource implements PrideDataSource {

    protected SortedSet<PrideHistogramBin> bins = new TreeSet<PrideHistogramBin>();

    protected PrideData[] values;

    private boolean calcAllSpectra = false;

    private SortedMap<String, SortedMap<PrideHistogramBin, Integer>> histMap;

    private Set<String> dataTypeList = new HashSet<String>();   // list all data type which stored in current data source.
    private boolean displayDataTypeList = false;

    public CategorySetHistogramDataSource(PrideData[] values) {
        this.values = values;

        for (PrideData value : values) {
            dataTypeList.add(value.getCategory());
        }

    }

    public Set<PrideDataType> getDataTypeList() {
        return null;
    }

    public boolean isCalcAllSpectra() {
        return calcAllSpectra;
    }

    public Iterator<PrideHistogramBin> iterator() {
        return bins.iterator();
    }


    public void appendBins(Collection<PrideHistogramBin> bins) {
        for (PrideHistogramBin bin : bins) {
            appendBin(bin);
        }
    }

    public void removeBins(double lowerBound, double upperBound) {
        if (upperBound <= lowerBound) {
            throw new IllegalArgumentException("the upperBound <= lowerBound");
        }

        SortedSet<PrideHistogramBin> newBins = new TreeSet<PrideHistogramBin>();
        for (PrideHistogramBin bin : bins) {
            if (bin.getStartBoundary() >= lowerBound && bin.getEndBoundary() < upperBound) {
                continue;
            }
            newBins.add(bin);
        }

        this.bins = newBins;
    }

    public void clearBins() {
        bins.clear();
    }

    public int getBinCount() {
        return bins.size();
    }

    public PrideHistogramBin getFirstBin() {
        return bins.first();
    }

    public double getStart() {
        return getFirstBin().getStartBoundary();
    }

    public PrideHistogramBin getLastBin() {
        return bins.last();
    }

    public double getEnd() {
        return getLastBin().getEndBoundary();
    }

    private SortedMap<PrideHistogramBin, Integer> createEmptyHistogram() {
        SortedMap<PrideHistogramBin, Integer> histogram = new TreeMap<PrideHistogramBin, Integer>();
        for (PrideHistogramBin bin : bins) {
            histogram.put(bin, 0);
        }
        return histogram;
    }

    public SortedMap<String, SortedMap<PrideHistogramBin, Integer>> getHistogramMap() {

        if (histMap != null) {
            return histMap;
        }

        this.histMap = new TreeMap<String, SortedMap<PrideHistogramBin, Integer>>();

        for (PrideData d : values) {
            for (PrideHistogramBin bin : bins) {
                if (d.getData() >= bin.getStartBoundary() && d.getData() < bin.getEndBoundary()) {
                    SortedMap<PrideHistogramBin, Integer> idHistogram = histMap.get(d.getCategory());
                    if (idHistogram == null) {
                        idHistogram = createEmptyHistogram();
                        histMap.put(d.getCategory(), idHistogram);
                    }
                    idHistogram.put(bin, idHistogram.get(bin) + 1);
                    break;
                }
            }
        }

        return histMap;
    }

    public PrideData[] getValues() {
        return values;
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

        PrideHistogramBin lastBin = bins.isEmpty() ? null : bins.last();

        if (lastBin != null && bin.getStartBoundary() < lastBin.getEndBoundary()) {
            throw new IllegalArgumentException("There exists overlap between the last bin " + lastBin +
                    " and the new append bin " + bin);
        }

        this.bins.add(bin);


    }
}
