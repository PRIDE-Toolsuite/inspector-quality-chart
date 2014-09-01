package uk.ac.ebi.pride.toolsuite.chart.io;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.*;
import uk.ac.ebi.pride.toolsuite.chart.utils.PridePlotUtils;
import uk.ac.ebi.pride.data.controller.DataAccessController;
import uk.ac.ebi.pride.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.data.core.Modification;
import uk.ac.ebi.pride.data.core.Peptide;
import uk.ac.ebi.pride.data.core.Protein;
import uk.ac.ebi.pride.data.core.Spectrum;
import uk.ac.ebi.pride.mol.MoleculeUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;


/**
 * @author qingwei
 * Date: 12/06/13
 */
public class DataAccessReader extends PrideDataReader {
    private static Logger logger = Logger.getLogger(DataAccessReader.class);
    private long start;

    private static final int DELTA_BIN_COUNT = 200;
    private static final double DELTA_MIN_BIN_WIDTH = 0.0005;
    private static final double PRE_MIN_BIN_WIDTH = 100;

    private String source = "DataAccessController";
    private DataAccessController controller;

    private boolean noPeptide = true;
    private boolean noSpectra = true;
    private boolean noTandemSpectra = true;

    private List<Double> deltaDomain = new ArrayList<Double>();
    private List<PrideData> deltaRange = new ArrayList<PrideData>();

    private Double[] peptidesDomain = new Double[6];
    private PrideData[] peptidesRange = new PrideData[6];

    private Double[] missedDomain = new Double[5];
    private PrideData[] missedRange = new PrideData[5];

    private Double[] preChargeDomain = new Double[8];
    private PrideData[] preChargeRange = new PrideData[8];

    private List<Double> preMassesDomain = new ArrayList<Double>();
    private List<PrideData> preMassesRange = new ArrayList<PrideData>();

    public DataAccessReader(DataAccessController controller) throws PrideDataException {
        if (controller == null) {
            throw new NullPointerException(source + " is null!");
        }
        this.controller = controller;

        readData();
    }

    @Override
    protected void start() {
        start = System.currentTimeMillis();
    }

    private Double calcDeltaMZ(Peptide peptide) {
        List<Double> modMassList = new ArrayList<Double>();
        for (Modification mod : peptide.getModifications()) {
            modMassList.add(mod.getMonoisotopicMassDelta().get(0));
        }

        return MoleculeUtilities.calculateDeltaMz(
                peptide.getSequence(),
                peptide.getPrecursorMz(),
                peptide.getPrecursorCharge(),
                modMassList
        );
    }

    private int calcMissedCleavages(Peptide peptide) {
        String sequence = peptide.getSequence();
        //Always remove the last K or R from sequence
        sequence = sequence.replaceAll("[K|R]$", "");

        //We assume the hypothesis KR|P
        sequence = sequence.replaceAll("[K|R]P", "");
        int initialLength = sequence.length();

        sequence = sequence.replaceAll("[K|R]", "");
        return initialLength - sequence.length();
    }

    private void readDelta(List<PrideData> deltaMZList) {
        if (noPeptide) {
            errorMap.put(PrideChartType.DELTA_MASS, new PrideDataException(PrideDataException.NO_PEPTIDE));
            return;
        }

        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        PrideEqualWidthHistogramDataSource dataSource = new PrideEqualWidthHistogramDataSource(
                deltaMZList.toArray(new PrideData[deltaMZList.size()]),
                true
        );
        double start = Double.MAX_VALUE;
        double end = Double.MIN_VALUE;
        double v;
        for (PrideData data : deltaMZList) {
            v = data.getData();
            if (v < start) {
                start = v;
            }
            if (v > end) {
                end = v;
            }
        }
        double binWidth = (end - start) / DELTA_BIN_COUNT;
        binWidth = binWidth < DELTA_MIN_BIN_WIDTH ? DELTA_MIN_BIN_WIDTH : binWidth;
        dataSource.appendBins(dataSource.generateBins(-DELTA_BIN_COUNT * binWidth, binWidth, DELTA_BIN_COUNT * 2));

        SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> histogramMap = dataSource.getHistogramMap();
        SortedMap<PrideHistogramBin, Integer> histogram;
        int maxFreq = 0;
        histogram = histogramMap.get(dataType);
        for (Integer size : histogram.values()) {
            if (size > maxFreq) {
                maxFreq = size;
            }
        }

        double relativeFreq;
        for (PrideHistogramBin bin : histogram.keySet()) {
            deltaDomain.add(bin.getStartBoundary());
            relativeFreq = maxFreq == 0 ? 0 : histogram.get(bin) * 1.0d / maxFreq;
            deltaRange.add(new PrideData(relativeFreq, dataType));
        }

        for (int i = 0; i < deltaRange.size(); i++) {
            if (deltaRange.get(i).getData() == null) {
                System.out.println(i);
            }
        }

        xyDataSourceMap.put(PrideChartType.DELTA_MASS, new PrideXYDataSource(
                deltaDomain.toArray(new Double[deltaDomain.size()]),
                deltaRange.toArray(new PrideData[deltaRange.size()]),
                dataType
        ));
    }

    private void readPeptide(int[] peptideBars) {
        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        if (noPeptide) {
            errorMap.put(PrideChartType.PEPTIDES_PROTEIN, new PrideDataException(PrideDataException.NO_PEPTIDE));
            return;
        }

        for (int i = 0; i < peptideBars.length; i++) {
            peptidesRange[i] = new PrideData(peptideBars[i] + 0.0, dataType);
        }

        xyDataSourceMap.put(PrideChartType.PEPTIDES_PROTEIN, new PrideXYDataSource(
                peptidesDomain,
                peptidesRange,
                dataType
        ));
    }

    private void readMissed(int[] missedBars) {
        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        if (noPeptide) {
            errorMap.put(PrideChartType.MISSED_CLEAVAGES, new PrideDataException(PrideDataException.NO_PEPTIDE));
            return;
        }

        for (int i = 0; i < missedBars.length; i++) {
            missedRange[i] = new PrideData(missedBars[i] + 0.0, PrideDataType.ALL_SPECTRA);
        }

        xyDataSourceMap.put(PrideChartType.MISSED_CLEAVAGES, new PrideXYDataSource(
                missedDomain,
                missedRange,
                dataType
        ));
    }

    private void readAvg(PrideSpectrumHistogramDataSource dataSource) {
        if (noTandemSpectra) {
            errorMap.put(PrideChartType.AVERAGE_MS, new PrideDataException(PrideDataException.NO_TANDEM_SPECTRA));
            return;
        }

        dataSource.appendBins(dataSource.generateBins(0, 1));

        histogramDataSourceMap.put(PrideChartType.AVERAGE_MS, dataSource);
    }

    private void readPreCharge(int[] preChargeBars) {
        if (noSpectra) {
            errorMap.put(PrideChartType.PRECURSOR_CHARGE, new PrideDataException(PrideDataException.NO_SPECTRA));
            return;
        }

        boolean hasCharge = false;
        for (int i = 0; i < preChargeBars.length; i++) {
            preChargeRange[i] = new PrideData(preChargeBars[i] + 0.0, PrideDataType.IDENTIFIED_SPECTRA);
            if (preChargeBars[i] > 0.0) {
                hasCharge = true;
            }
        }
        if (!hasCharge) {
            errorMap.put(PrideChartType.PRECURSOR_CHARGE, new PrideDataException(PrideDataException.NO_PRECURSOR_CHARGE));
            return;
        }

        xyDataSourceMap.put(PrideChartType.PRECURSOR_CHARGE, new PrideXYDataSource(
                preChargeDomain,
                preChargeRange,
                PrideDataType.IDENTIFIED_SPECTRA
        ));
    }

    private void readPreMasses(List<PrideData> preMassedList) {
        if (noSpectra) {
            errorMap.put(PrideChartType.PRECURSOR_MASSES, new PrideDataException(PrideDataException.NO_SPECTRA));
            return;
        }

        boolean hasCharge = false;
        for (int i = 0; i < preMassedList.size(); i++) {
            if (preMassedList.get(i).getData() > 0.0) {
                hasCharge = true;
            }
        }

        if (!hasCharge) {
            errorMap.put(PrideChartType.PRECURSOR_MASSES, new PrideDataException(PrideDataException.NO_PRECURSOR_MASS));
            return;
        }

        PrideEqualWidthHistogramDataSource dataSource = new PrideEqualWidthHistogramDataSource(
                preMassedList.toArray(new PrideData[preMassedList.size()]),
                true
        );
        dataSource.appendBins(dataSource.generateBins(0d, PRE_MIN_BIN_WIDTH));

        SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> histogramMap = dataSource.getHistogramMap();
        SortedMap<PrideHistogramBin, Integer> idHistogram = histogramMap.get(PrideDataType.IDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Integer> unHistogram = histogramMap.get(PrideDataType.UNIDENTIFIED_SPECTRA);
        SortedMap<PrideHistogramBin, Integer> allHistogram = histogramMap.get(PrideDataType.ALL_SPECTRA);

        int identifiedCount = 0;
        if (idHistogram != null) {
            for (PrideHistogramBin bin : idHistogram.keySet()) {
                identifiedCount += idHistogram.get(bin);
            }
            for (PrideHistogramBin bin : idHistogram.keySet()) {
                preMassesDomain.add(bin.getStartBoundary());
                preMassesRange.add(new PrideData(
                        idHistogram.get(bin) * 1.0d / identifiedCount,
                        PrideDataType.IDENTIFIED_SPECTRA
                ));
            }
        }

        int unidentifiedCount = 0;
        if (unHistogram != null) {
            for (PrideHistogramBin bin : unHistogram.keySet()) {
                unidentifiedCount += unHistogram.get(bin);
            }
            for (PrideHistogramBin bin : unHistogram.keySet()) {
                preMassesDomain.add(bin.getStartBoundary());
                preMassesRange.add(new PrideData(
                        unHistogram.get(bin) * 1.0d / unidentifiedCount,
                        PrideDataType.UNIDENTIFIED_SPECTRA
                ));
            }
        }

        int allCount = identifiedCount + unidentifiedCount;
        if (allCount != 0) {
            for (PrideHistogramBin bin : allHistogram.keySet()) {
                preMassesDomain.add(bin.getStartBoundary());
                preMassesRange.add(new PrideData(
                        allHistogram.get(bin) * 1.0d / allCount,
                        PrideDataType.ALL_SPECTRA
                ));
            }
        }

        xyDataSourceMap.put(PrideChartType.PRECURSOR_MASSES, new PrideXYDataSource(
                preMassesDomain.toArray(new Double[preMassesDomain.size()]),
                preMassesRange.toArray(new PrideData[preMassesRange.size()]),
                PrideDataType.ALL_SPECTRA
        ));
    }

    private void readPeakMS(List<PrideData> peaksMSList) {
        if (noTandemSpectra) {
            errorMap.put(PrideChartType.PEAKS_MS, new PrideDataException(PrideDataException.NO_TANDEM_SPECTRA));
            return;
        }

        PrideEqualWidthHistogramDataSource dataSource = new PrideEqualWidthHistogramDataSource(
                peaksMSList.toArray(new PrideData[peaksMSList.size()]),
                false
        );
        dataSource.appendBins(dataSource.generateGranularityBins(0d, 10, 50));

        histogramDataSourceMap.put(PrideChartType.PEAKS_MS, dataSource);
    }

    private void readPeakIntensity(List<PrideData> peaksIntensityList) {
        if (noTandemSpectra) {
            errorMap.put(PrideChartType.PEAK_INTENSITY, new PrideDataException(PrideDataException.NO_TANDEM_SPECTRA));
            return;
        }

        PrideHistogramDataSource dataSource = new PrideHistogramDataSource(
                peaksIntensityList.toArray(new PrideData[peaksIntensityList.size()]),
                true
        );
        dataSource.appendBin(new PrideHistogramBin(0, 5));
        dataSource.appendBin(new PrideHistogramBin(10, 100));
        dataSource.appendBin(new PrideHistogramBin(100, 1000));
        dataSource.appendBin(new PrideHistogramBin(1000, 10000));
        dataSource.appendBin(new PrideHistogramBin(10000, Integer.MAX_VALUE));

        histogramDataSourceMap.put(PrideChartType.PEAK_INTENSITY, dataSource);
    }

    @Override
    protected void reading() {
        for (int i = 0; i < 6; i++) {
            peptidesDomain[i] = i + 1.0;
        }

        for (int i = 0; i < 5; i++) {
            missedDomain[i] = i + 0.0;
        }

        for (int i = 0; i < 8; i++) {
            preChargeDomain[i] = i + 1.0;
        }

        int[] peptideBars = new int[6];
        int[] missedBars = new int[5];
        int[] preChargeBars = new int[8];

        List<PrideData> deltaMZList = new ArrayList<PrideData>();
        List<PrideData> preMassedList = new ArrayList<PrideData>();

        Protein protein;
        List<Peptide> peptideList;
        for (Comparable proteinId : controller.getProteinIds()) {
            protein = controller.getProteinById(proteinId);
            peptideList = protein.getPeptides();
            // fill peptides per protein
            int size = peptideList.size();
            if (size < 6) {
                peptideBars[size - 1]++;
            } else {
                peptideBars[5]++;
            }

            int missedCleavages;
            Double deltaMZ;
            for (Peptide peptide : peptideList) {
                noPeptide = false;
                peptideSize++;

                // fill delta m/z histogram.
                deltaMZ = calcDeltaMZ(peptide);
                if (deltaMZ != null) {
                    deltaMZList.add(new PrideData(deltaMZ, PrideDataType.IDENTIFIED_SPECTRA));
                }

                // fill missed cleavages
                missedCleavages = calcMissedCleavages(peptide);
                if (missedCleavages < 4) {
                    missedBars[missedCleavages]++;
                } else {
                    missedBars[4]++;
                }
            }

        }

        // spectrum level statistics.
        Integer preCharge;
        Double preMZ;
        Peptide peptide;
        Spectrum spectrum;
        List<PrideData> peaksMSList = new ArrayList<PrideData>();
        List<PrideData> peaksIntensityList = new ArrayList<PrideData>();
        PrideSpectrumHistogramDataSource avgDataSource = new PrideSpectrumHistogramDataSource(true);

        PrideDataType dataType;
        for (Comparable spectrumId : controller.getSpectrumIds()) {
            noSpectra = false;
            spectrum = controller.getSpectrumById(spectrumId);
            peptide = spectrum.getPeptide();

            // precursor charge and mass.
            if (peptide == null) {
                preCharge = DataAccessUtilities.getPrecursorCharge(spectrum.getPrecursors());
                preMZ = DataAccessUtilities.getPrecursorMz(spectrum);
            } else {
                preCharge = peptide.getPrecursorCharge();
                preMZ = peptide.getPrecursorMz();
            }
            // Charge State must be less than 8, but some files can have the annotation wrong
            if (preCharge != null && preCharge < 8 && controller.isIdentifiedSpectrum(spectrumId)) {
                // Identified spectrum.
                preChargeBars[preCharge - 1]++;
            }

            if (preMZ != null && preMZ > -1 && preCharge != null && preCharge < 8) {
                preMassedList.add(new PrideData(preMZ * preCharge,
                        controller.isIdentifiedSpectrum(spectrumId) ? PrideDataType.IDENTIFIED_SPECTRA : PrideDataType.UNIDENTIFIED_SPECTRA
                ));
            }

            if (controller.isIdentifiedSpectrum(spectrumId)) {
                identifiedSpectraSize++;
                dataType = PrideDataType.IDENTIFIED_SPECTRA;
            } else {
                unidentifiedSpectraSize++;
                dataType = PrideDataType.UNIDENTIFIED_SPECTRA;
            }

            if (controller.getSpectrumMsLevel(spectrumId) == 2) {
                noTandemSpectra = false;
                peaksMSList.add(new PrideData(spectrum.getMzBinaryDataArray().getDoubleArray().length + 0.0d, PrideDataType.ALL_SPECTRA));
                avgDataSource.addSpectrum(spectrum, dataType);

                for (double v : spectrum.getIntensityBinaryDataArray().getDoubleArray()) {
                    peaksIntensityList.add(new PrideData(v, dataType));
                }
            }
        }

        // release memory.
        controller = null;

//        start = System.currentTimeMillis();
//        readPeptide(peptideBars);
//        logger.debug("create peptide data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
//
//        start = System.currentTimeMillis();
//        readDelta(deltaMZList);
//        logger.debug("create delta mz data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
//
//        start = System.currentTimeMillis();
//        readMissed(missedBars);
//        logger.debug("create missed cleavages data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
//
//        start = System.currentTimeMillis();
//        readPreCharge(preChargeBars);
//        logger.debug("create precursor charge data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
//
//        start = System.currentTimeMillis();
//        readPreMasses(preMassedList);
//        logger.debug("create precursor masses data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
//
//        start = System.currentTimeMillis();
//        readAvg(avgDataSource);
//        logger.debug("create average ms/ms data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
//
//        start = System.currentTimeMillis();
//        readPeakMS(peaksMSList);
//        logger.debug("create peaks per ms/ms data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
//
//        start = System.currentTimeMillis();
//        readPeakIntensity(peaksIntensityList);
//        logger.debug("create peak intensity data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));

        readPeptide(peptideBars);
        readDelta(deltaMZList);
        readMissed(missedBars);

        readPreCharge(preChargeBars);
        readPreMasses(preMassedList);

        readAvg(avgDataSource);
        readPeakMS(peaksMSList);
        readPeakIntensity(peaksIntensityList);
    }

    @Override
    protected void end() {
        logger.debug("create data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
    }
}
