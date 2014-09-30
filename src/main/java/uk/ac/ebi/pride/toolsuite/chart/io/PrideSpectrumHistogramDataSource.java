package uk.ac.ebi.pride.toolsuite.chart.io;

import uk.ac.ebi.pride.toolsuite.chart.dataset.*;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

import java.util.*;

/**
 * @author qingwei
 * Date: 02/07/13
 */
public class PrideSpectrumHistogramDataSource extends PrideEqualWidthHistogramDataSource {
    private class PridePeak {
        double mz;
        double intensity;
        PrideDataType dataType;

        private PridePeak(double mz, double intensity, PrideDataType dataType) {
            this.mz = mz;
            this.intensity = intensity;
            this.dataType = dataType;
        }
    }

    private List<PridePeak> peakList = new ArrayList<PridePeak>();

    // If add data into data source, modify set true. Which means histogram map need to regenerate again.
    private boolean modify = true;
    private SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Double>> histMap;


    public PrideSpectrumHistogramDataSource(boolean calcAllSpectra) {
        super(new PrideData[0], calcAllSpectra);
    }

    public void addSpectrum(Spectrum spectrum, PrideDataType dataType) {
        double[] mzArray = spectrum.getMzBinaryDataArray().getDoubleArray();
        double[] intensityArray = spectrum.getIntensityBinaryDataArray().getDoubleArray();

        PridePeak peak;
        for (int i = 0; i < mzArray.length; i++) {
            peak = new PridePeak(
                    mzArray[i],
                    i < intensityArray.length ? intensityArray[i] : 0d,
                    dataType
            );
            peakList.add(peak);
        }

        modify = true;
    }

    public void addPeak(double mz, double intensity, PrideDataType dataType) {
        PridePeak peak = new PridePeak(mz, intensity, dataType);
        peakList.add(peak);

        modify = true;
    }

    @Override
    public Collection<PrideHistogramBin> generateBins(double start, double binWidth) {
        if (binWidth <= 0) {
            throw new IllegalArgumentException("Bin width should be great than 0");
        }

        double end = Double.MIN_VALUE;
        for (PridePeak peak : peakList) {
            double v = peak.mz;
            if (v > end) {
                end = v;
            }
        }

        int binCount = (int)Math.ceil((end - start) / binWidth);
        return generateBins(start, binWidth, binCount);
    }

    private SortedMap<PrideHistogramBin, Double> createEmptyHistogram() {
        SortedMap<PrideHistogramBin, Double> histogram = new TreeMap<PrideHistogramBin, Double>();
        for (PrideHistogramBin bin : bins) {
            histogram.put(bin, 0.0);
        }
        return histogram;
    }

    @Deprecated
    public SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> getHistogramMap() {
        throw new UnsupportedOperationException("Please use getIntensityMap() method.");
    }

    public SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Double>> getIntensityMap() {
        if (! modify) {
            return histMap;
        }

        this.histMap = new TreeMap<PrideDataType, SortedMap<PrideHistogramBin, Double>>();

        SortedMap<PrideHistogramBin, Double> idHistogram;
        SortedMap<PrideHistogramBin, Double> unHistogram;
        SortedMap<PrideHistogramBin, Double> allHistogram = createEmptyHistogram();
        histMap.put(PrideDataType.ALL_SPECTRA, allHistogram);

        PrideHistogramBin bin;
        PrideHistogramBin[] binArray = bins.toArray(new PrideHistogramBin[bins.size()]);
        int offset;
        for (PridePeak peak : peakList) {
            offset = (int)Math.floor(peak.mz);
            if (offset < binArray.length) {
                bin = binArray[offset];
            } else {
                bin = binArray[binArray.length - 1];
            }

            if (peak.dataType == PrideDataType.IDENTIFIED_SPECTRA) {
                idHistogram = histMap.get(PrideDataType.IDENTIFIED_SPECTRA);
                if (idHistogram == null) {
                    idHistogram = createEmptyHistogram();
                    histMap.put(PrideDataType.IDENTIFIED_SPECTRA, idHistogram);
                }
                idHistogram.put(bin, idHistogram.get(bin) + peak.intensity);
            } else if (peak.dataType == PrideDataType.UNIDENTIFIED_SPECTRA) {
                unHistogram = histMap.get(PrideDataType.UNIDENTIFIED_SPECTRA);
                if (unHistogram == null) {
                    unHistogram = createEmptyHistogram();
                    histMap.put(PrideDataType.UNIDENTIFIED_SPECTRA, unHistogram);
                }
                unHistogram.put(bin, unHistogram.get(bin) + peak.intensity);
            } else if (peak.dataType == PrideDataType.ALL_SPECTRA) {
                allHistogram.put(bin, allHistogram.get(bin) + peak.intensity);
            }

            if (isCalcAllSpectra()) {
                allHistogram.put(bin, allHistogram.get(bin) + peak.intensity);
            }
        }

        modify = false;
        return histMap;
    }
}
