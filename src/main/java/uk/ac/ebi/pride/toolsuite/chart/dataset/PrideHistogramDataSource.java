package uk.ac.ebi.pride.toolsuite.chart.dataset;

import java.util.*;

/**
* Based on Histogram bins list {@link #bins}, system translate the {@link #values}
* into a histogram. Every bin, the value should in [bin_lowBound, bin_highBound) range,
* which means the low bound value is included and high bound value is excluded.
*
* Notice:
* NOT exists overlap between the different histogram bins.
*
* @author qingwei
* Date: 14/06/13
*/
public class PrideHistogramDataSource implements PrideDataSource {

    protected SortedSet<PrideHistogramBin> bins = new TreeSet<PrideHistogramBin>();

    protected PrideData[] values;

    private boolean calcAllSpectra = false;

    private SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> histMap;

    private Set<PrideDataType> dataTypeList = new HashSet<PrideDataType>();   // list all data type which stored in current data source.

    public PrideHistogramDataSource(PrideData[] values, boolean calcAllSpectra) {
        this.calcAllSpectra = calcAllSpectra;
        this.values = values;

        for (PrideData value : values) {
            dataTypeList.add(value.getType());
        }

        if (calcAllSpectra) {
            dataTypeList.add(PrideDataType.ALL_SPECTRA);
        }
    }

    public Set<PrideDataType> getDataTypeList() {
        return dataTypeList;
    }

    public boolean isCalcAllSpectra() {
        return calcAllSpectra;
    }

    public Iterator<PrideHistogramBin> iterator() {
        return bins.iterator();
    }

    /**
     * add a bin to the end of set.
     */
    public void appendBin(PrideHistogramBin bin) {
        PrideHistogramBin lastBin = bins.isEmpty() ? null : bins.last();

        if (lastBin != null && bin.getStartBoundary() < lastBin.getEndBoundary()) {
            throw new IllegalArgumentException("There exists overlap between the last bin " + lastBin +
                    " and the new append bin " + bin);
        }

        this.bins.add(bin);
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

    public SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> getHistogramMap() {
        if (histMap != null) {
            return histMap;
        }

        this.histMap = new TreeMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>>();

        SortedMap<PrideHistogramBin, Integer> idHistogram;
        SortedMap<PrideHistogramBin, Integer> unHistogram;
        SortedMap<PrideHistogramBin, Integer> targetHistogram;
        SortedMap<PrideHistogramBin, Integer> decoyHistogram;

        SortedMap<PrideHistogramBin, Integer> allHistogram = createEmptyHistogram();
        if(dataTypeList.contains(PrideDataType.ALL_SPECTRA) || calcAllSpectra)
            histMap.put(PrideDataType.ALL_SPECTRA, allHistogram);

        for (PrideData d : values) {
            for (PrideHistogramBin bin : bins) {
                if (d.getData() >= bin.getStartBoundary() && d.getData() < bin.getEndBoundary()) {
                    if (d.getType() == PrideDataType.IDENTIFIED_SPECTRA) {
                        idHistogram = histMap.get(PrideDataType.IDENTIFIED_SPECTRA);
                        if (idHistogram == null) {
                            idHistogram = createEmptyHistogram();
                            histMap.put(PrideDataType.IDENTIFIED_SPECTRA, idHistogram);
                        }
                        idHistogram.put(bin, idHistogram.get(bin) + 1);
                    } else if (d.getType() == PrideDataType.UNIDENTIFIED_SPECTRA) {
                        unHistogram = histMap.get(PrideDataType.UNIDENTIFIED_SPECTRA);
                        if (unHistogram == null) {
                            unHistogram = createEmptyHistogram();
                            histMap.put(PrideDataType.UNIDENTIFIED_SPECTRA, unHistogram);
                        }
                        unHistogram.put(bin, unHistogram.get(bin) + 1);
                    } else if (d.getType() == PrideDataType.IDENTIFIED_TARGET) {
                        targetHistogram = histMap.get(PrideDataType.IDENTIFIED_TARGET);
                        if (targetHistogram == null) {
                            targetHistogram = createEmptyHistogram();
                            histMap.put(PrideDataType.IDENTIFIED_TARGET, targetHistogram);
                        }
                        targetHistogram.put(bin, targetHistogram.get(bin) + 1);
                    }else if (d.getType() == PrideDataType.IDENTIFIED_DECOY) {
                        decoyHistogram = histMap.get(PrideDataType.IDENTIFIED_DECOY);
                        if (decoyHistogram == null) {
                            decoyHistogram = createEmptyHistogram();
                            histMap.put(PrideDataType.IDENTIFIED_DECOY, decoyHistogram);
                        }
                        decoyHistogram.put(bin, decoyHistogram.get(bin) + 1);

                    }else if (d.getType() == PrideDataType.ALL_SPECTRA) {
                        allHistogram.put(bin, allHistogram.get(bin) + 1);
                    }

                    if (calcAllSpectra) {
                        allHistogram.put(bin, allHistogram.get(bin) + 1);
                    }

                    break;
                }
            }
        }

        return histMap;
    }

    public PrideData[] getValues() {
        return values;
    }


}
