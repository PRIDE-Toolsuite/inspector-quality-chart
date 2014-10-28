package uk.ac.ebi.pride.toolsuite.chart.io;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.*;
import uk.ac.ebi.pride.toolsuite.chart.utils.PridePlotUtils;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;
import uk.ac.ebi.pride.utilities.util.Tuple;

import java.util.*;


/**
 * DataAccess Reader is a class to do the analysis of protein and peptide statistics and chart generation.
 * @author qingwei
 * Date: 12/06/13
 */
public class DataAccessReader extends PrideDataReader {
    private static Logger logger = Logger.getLogger(DataAccessReader.class);
    private long start;

    private static final int DELTA_BIN_COUNT = 200;
    private static final double DELTA_MIN_BIN_WIDTH = 0.0005;
    private static final double PRE_MIN_BIN_WIDTH = 100;
    private static final double QUANT_MIN_BIN_WITH = 0.1;

    private String source = "DataAccessController";
    private DataAccessController controller;

    private boolean noPeptide = true;
    private boolean noSpectra = true;
    private boolean noTandemSpectra = true;
    private boolean decoyPresentInformation = false;

    private List<Double> deltaDomain = new ArrayList<Double>();
    private List<PrideData> deltaRange = new ArrayList<PrideData>();

    private Double[] peptidesDomain = new Double[6];
    private PrideData[] peptidesRange = new PrideData[6];

    private List<Double> missedDomain = new ArrayList<Double>();
    private List<PrideData> missedRange = new ArrayList<PrideData>();

    private Double[] preChargeDomain = new Double[8];
    private PrideData[] preChargeRange = new PrideData[8];

    private List<Double> preMassesDomain = new ArrayList<Double>();
    private List<PrideData> preMassesRange = new ArrayList<PrideData>();

    private List<Double> preQuantDomain = new ArrayList<Double>();
    private List<PrideData> preQuantRange = new ArrayList<PrideData>();

    private Map<Comparable, String> variables = new HashMap<Comparable, String>();
    private Map<Comparable, List<Double>> variablesStudy = new HashMap<Comparable, List<Double>>();

    public DataAccessReader(DataAccessController controller) throws PrideDataException {
        if (controller == null) {
            throw new NullPointerException(source + " is null!");
        }
        this.controller = controller;
        decoyPresentInformation = controller.hasDecoyInformation();

        readData();
    }

    @Override
    protected void start() {
        start = System.currentTimeMillis();
    }

    private Double calcDeltaMZ(Peptide peptide) {
        List<Double> modMassList = new ArrayList<Double>();
        for (Modification mod : peptide.getModifications()) {
            if(mod.getMonoisotopicMassDelta().size() > 0)
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

        PrideEqualWidthHistogramDataSource dataSource = new PrideEqualWidthHistogramDataSource(deltaMZList.toArray(new PrideData[deltaMZList.size()]),true);

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

        SortedMap<PrideHistogramBin, Integer> identhistogram;
        int maxFreq = 0;

        identhistogram = histogramMap.get(PrideDataType.IDENTIFIED_SPECTRA);
        for (Integer size : identhistogram.values()) {
            if (size > maxFreq) {
                maxFreq = size;
            }
        }

        double relativeFreq;
        for(PrideDataType dataType: histogramMap.keySet()){
            identhistogram = histogramMap.get(dataType);
            for (PrideHistogramBin bin : identhistogram.keySet()) {
                deltaDomain.add(bin.getStartBoundary());
                relativeFreq = maxFreq == 0 ? 0 : identhistogram.get(bin) * 1.0d / maxFreq;
                deltaRange.add(new PrideData(relativeFreq, dataType));
            }
        }

        SortedMap<PrideHistogramBin, Integer> targethistogram;
        int targetmaxFreq = 0;

        targethistogram = histogramMap.get(PrideDataType.IDENTIFIED_TARGET);
        if(targethistogram != null){
            for (Integer size : targethistogram.values()) {
                if (size > targetmaxFreq) {
                    targetmaxFreq = size;
                }
            }

            double targetFreq;
            for (PrideHistogramBin bin : targethistogram.keySet()) {
                deltaDomain.add(bin.getStartBoundary());
                targetFreq = targetmaxFreq == 0 ? 0 : targethistogram.get(bin) * 1.0d / targetmaxFreq;
                deltaRange.add(new PrideData(targetFreq, PrideDataType.IDENTIFIED_TARGET));
            }
        }


        SortedMap<PrideHistogramBin, Integer> decoyHistogram;
        int decoymaxFreq = 0;

        decoyHistogram = histogramMap.get(PrideDataType.IDENTIFIED_DECOY);
        if(decoyHistogram != null){
            for (Integer size : decoyHistogram.values()) {
                if (size > decoymaxFreq) {
                    decoymaxFreq = size;
                }
            }

            double decoyFreq;
            for (PrideHistogramBin bin : decoyHistogram.keySet()) {
                deltaDomain.add(bin.getStartBoundary());
                decoyFreq = decoymaxFreq == 0 ? 0 : decoyHistogram.get(bin) * 1.0d / decoymaxFreq;
                deltaRange.add(new PrideData(decoyFreq, PrideDataType.IDENTIFIED_DECOY));
            }
        }


        for (int i = 0; i < deltaRange.size(); i++) {
            if (deltaRange.get(i).getData() == null) {
                System.out.println(i);
            }
        }

        xyDataSourceMap.put(PrideChartType.DELTA_MASS, new PrideXYDataSource(
                deltaDomain.toArray(new Double[deltaDomain.size()]),
                deltaRange.toArray(new PrideData[deltaRange.size()]),
                PrideDataType.IDENTIFIED_SPECTRA
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

    private void readMissed(int[] missedBars, int[] decoyMissedBar, int[] tarjetMissedBar, boolean hasDecoyInformation) {
        PrideDataType dataType = PrideDataType.IDENTIFIED_SPECTRA;

        if (noPeptide) {
            errorMap.put(PrideChartType.MISSED_CLEAVAGES, new PrideDataException(PrideDataException.NO_PEPTIDE));
            return;
        }

        for (int i = 0; i < missedBars.length; i++) {
            missedRange.add(new PrideData(missedBars[i] + 0.0, PrideDataType.IDENTIFIED_SPECTRA));
            missedDomain.add(i + 0.0);
        }

        if(hasDecoyInformation){
            for (int i = 0; i < decoyMissedBar.length; i++) {
                missedRange.add(new PrideData(decoyMissedBar[i] + 0.0, PrideDataType.IDENTIFIED_DECOY));
                missedDomain.add(i + 0.0);
            }

            for (int i = 0; i < tarjetMissedBar.length; i++) {
                missedRange.add(new PrideData(tarjetMissedBar[i] + 0.0, PrideDataType.IDENTIFIED_TARGET));
                missedDomain.add(i + 0.0);
            }
        }

        xyDataSourceMap.put(PrideChartType.MISSED_CLEAVAGES, new PrideXYDataSource(
                missedDomain.toArray(new Double[missedDomain.size()]),
                missedRange.toArray(new PrideData[missedRange.size()]),
                dataType
        ));
    }

    private void readQuantitation(Map<Comparable, String> studyVariables, Map<Comparable, List<Double>> values) {

        if (noPeptide) {
            errorMap.put(PrideChartType.QUANTITATION_PEPTIDES, new PrideDataException(PrideDataException.NO_PROTEIN_QUANT));
            return;
        }

        if (variablesStudy.isEmpty()) {
            errorMap.put(PrideChartType.QUANTITATION_PEPTIDES, new PrideDataException(PrideDataException.NO_PROTEIN_QUANT));
            return;
        }
        if (noSpectra) {
            errorMap.put(PrideChartType.PRECURSOR_MASSES, new PrideDataException(PrideDataException.NO_SPECTRA));
            return;
        }
        List<PrideData> preQuantList = new ArrayList<PrideData>();
        for(Comparable studyId: studyVariables.keySet()){
            List<Double> valueStudy = values.get(studyId);
            for(Double value: valueStudy){
                PrideData pridata = new PrideData(value, studyVariables.get(studyId));
                preQuantList.add(pridata);

            }
        }
        CategorySetHistogramDataSource dataSource = new CategorySetHistogramDataSource(preQuantList.toArray(new PrideData[preQuantList.size()]));
        dataSource.appendBins(dataSource.generateBins(0d, PRE_MIN_BIN_WIDTH));

        SortedMap<String, SortedMap<PrideHistogramBin, Integer>> histogramMap = dataSource.getHistogramMap();
        for(String variable: histogramMap.keySet()){
            SortedMap<PrideHistogramBin, Integer> histogram = histogramMap.get(variable);
            if (histogram != null) {
                int studyCount = 0;
                for (PrideHistogramBin bin : histogram.keySet()) {
                    studyCount += histogram.get(bin);
                }
                for (PrideHistogramBin bin : histogram.keySet()) {
                    preQuantDomain.add(bin.getStartBoundary());
                    preQuantRange.add(new PrideData(
                            histogram.get(bin) * 1.0d / studyCount,
                            variable
                    ));
                }
            }
        }
        xyDataSourceMap.put(PrideChartType.QUANTITATION_PEPTIDES, new PrideXYDataSource(
                preQuantDomain.toArray(new Double[preMassesDomain.size()]),
                preQuantRange.toArray(new PrideData[preMassesRange.size()]),
                PrideDataType.ALL_SPECTRA
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
        SortedMap<PrideHistogramBin, Integer> decoyHistogram = histogramMap.get(PrideDataType.IDENTIFIED_DECOY);
        SortedMap<PrideHistogramBin, Integer> targetHistogram = histogramMap.get(PrideDataType.IDENTIFIED_TARGET);
        SortedMap<PrideHistogramBin, Integer> allHistogram = histogramMap.get(PrideDataType.ALL_SPECTRA);

        int targetCount = 0;
        if (targetHistogram != null) {
            for (PrideHistogramBin bin : targetHistogram.keySet()) {
                targetCount += targetHistogram.get(bin);
            }
            for (PrideHistogramBin bin : targetHistogram.keySet()) {
                preMassesDomain.add(bin.getStartBoundary());
                preMassesRange.add(new PrideData(
                        targetHistogram.get(bin) * 1.0d / targetCount,
                        PrideDataType.IDENTIFIED_TARGET
                ));
            }
        }

        int decoyCount = 0;
        if (decoyHistogram != null) {
            for (PrideHistogramBin bin : decoyHistogram.keySet()) {
                decoyCount += decoyHistogram.get(bin);
            }
            for (PrideHistogramBin bin : decoyHistogram.keySet()) {
                preMassesDomain.add(bin.getStartBoundary());
                preMassesRange.add(new PrideData(
                        decoyHistogram.get(bin) * 1.0d / decoyCount,
                        PrideDataType.IDENTIFIED_DECOY
                ));
            }
        }

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
        dataSource.appendBin(new PrideHistogramBin(10,  100));
        dataSource.appendBin(new PrideHistogramBin(100, 300));
        dataSource.appendBin(new PrideHistogramBin(300, 500));
        dataSource.appendBin(new PrideHistogramBin(500, 700));
        dataSource.appendBin(new PrideHistogramBin(700, 900));
        dataSource.appendBin(new PrideHistogramBin(900, 1000));
        dataSource.appendBin(new PrideHistogramBin(1000, 3000));
        dataSource.appendBin(new PrideHistogramBin(3000, 6000));
        dataSource.appendBin(new PrideHistogramBin(6000, 10000));
        dataSource.appendBin(new PrideHistogramBin(10000, Integer.MAX_VALUE));

        histogramDataSourceMap.put(PrideChartType.PEAK_INTENSITY, dataSource);
    }

    @Override
    protected void reading() {
        for (int i = 0; i < 6; i++) {
            peptidesDomain[i] = i + 1.0;
        }

        //If mzTab and contains quantitation data
        if(controller.getType().equals(DataAccessController.Type.MZTAB) && controller.hasQuantData()){
            for(Comparable idStudyVariable: controller.getStudyVariables().keySet()){
                variables.put(idStudyVariable, controller.getStudyVariables().get(idStudyVariable).getDescription());
                variablesStudy.put(idStudyVariable, new ArrayList<Double>());
            }
        }

        /*for (int i = 0; i < 5; i++) {
            missedDomain[i] = i + 0.0;
        }*/

        for (int i = 0; i < 8; i++) {
            preChargeDomain[i] = i + 1.0;
        }

        int[] peptideBars = new int[6];
        int[] missedBars = new int[5];
        int[] decoyMissedBar  = new int[5];
        int[] targetMissedBar = new int[5];
        int[] preChargeBars = new int[8];

        List<PrideData> deltaMZList = new ArrayList<PrideData>();
        List<PrideData> preMassedList = new ArrayList<PrideData>();

        Protein protein;
        List<Peptide> peptideList;

        Map<Comparable, Tuple<Boolean, Boolean>> spectrumDecoy = new HashMap<Comparable, Tuple<Boolean, Boolean>>();

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
                    if(controller.hasDecoyInformation()){
                        if(peptide.getPeptideEvidence().isDecoy()){
                            deltaMZList.add(new PrideData(deltaMZ, PrideDataType.IDENTIFIED_DECOY));
                        }else{
                            deltaMZList.add(new PrideData(deltaMZ, PrideDataType.IDENTIFIED_TARGET));
                        }
                    }

                }

                // fill missed cleavages
                missedCleavages = calcMissedCleavages(peptide);
                if (missedCleavages < 4) {
                    missedBars[missedCleavages]++;
                    if(controller.hasDecoyInformation())
                        if(peptide.getPeptideEvidence().isDecoy())
                            decoyMissedBar[missedCleavages]++;
                        else
                            targetMissedBar[missedCleavages]++;

                } else {
                    missedBars[4]++;
                    if(controller.hasDecoyInformation())
                        if(peptide.getPeptideEvidence().isDecoy())
                            decoyMissedBar[4]++;
                        else
                            targetMissedBar[4]++;
                }
                if(controller.hasDecoyInformation() && controller.hasSpectrum()){
                    Comparable id = controller.getSpectrumIdForPeptide(peptide.getSpectrumIdentification().getId());
                    if(id != null){
                        Tuple<Boolean, Boolean> status = new Tuple<Boolean, Boolean>(false,false);
                        if(spectrumDecoy.containsKey(id))
                            status = spectrumDecoy.get(id);
                        if (peptide.getPeptideEvidence().isDecoy()){
                            status.setValue(true);
                        }else{
                            status.setKey(true);
                        }
                        spectrumDecoy.put(id,status);
                    }
                }
            }

            //If mzTab and contains quantitation data
            if(controller.getType().equals(DataAccessController.Type.MZTAB) && controller.hasQuantData()){
                List<QuantPeptide> quantPeptides = controller.getProteinById(proteinId).getQuantPeptides();
                for(QuantPeptide peptide: quantPeptides){
                    QuantScore quantScore = peptide.getQuantScore();
                    for(Comparable studyValueKey: quantScore.getStudyVariableScores().keySet()){
                        if(quantScore.getStudyVariableScores().get(studyValueKey) != null){
                            variablesStudy.get(studyValueKey).add(quantScore.getStudyVariableScores().get(studyValueKey));
                        }
                    }
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
            List<Peptide> peptides = spectrum.getPeptide();

            // precursor charge and mass.
            if (peptides == null) {
                preCharge = DataAccessUtilities.getPrecursorCharge(spectrum.getPrecursors());
                preMZ = DataAccessUtilities.getPrecursorMz(spectrum);
            } else {
                preCharge = peptides.get(0).getPrecursorCharge();
                preMZ = peptides.get(0).getPrecursorMz();
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
                if(controller.hasDecoyInformation()){
                    if(spectrumDecoy.containsKey(spectrum.getId())){
                        Tuple<Boolean, Boolean> status = spectrumDecoy.get(spectrumId);
                        if(status.getKey())
                            preMassedList.add(new PrideData(preMZ * preCharge, PrideDataType.IDENTIFIED_TARGET));
                        if(status.getValue())
                            preMassedList.add(new PrideData(preMZ * preCharge, PrideDataType.IDENTIFIED_DECOY));
                    }
                }
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
                    if(controller.hasDecoyInformation()){
                        if(spectrumDecoy.containsKey(spectrum.getId())){
                            Tuple<Boolean, Boolean> status = spectrumDecoy.get(spectrumId);
                            if(status.getKey())
                                peaksIntensityList.add(new PrideData(v, PrideDataType.IDENTIFIED_TARGET));
                            if(status.getValue())
                                peaksIntensityList.add(new PrideData(v, PrideDataType.IDENTIFIED_DECOY));
                        }
                    }
                }
            }

        }

        boolean hasDecoyInformation = controller.hasDecoyInformation();
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
        readMissed(missedBars, decoyMissedBar, targetMissedBar, hasDecoyInformation);

        readPreCharge(preChargeBars);
        readPreMasses(preMassedList);

        readAvg(avgDataSource);
        readPeakMS(peaksMSList);
        readPeakIntensity(peaksIntensityList);
        readQuantitation(variables, variablesStudy);
    }

    @Override
    protected void end() {
        logger.debug("create data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
    }
}
