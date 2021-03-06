package uk.ac.ebi.pride.toolsuite.chart.io;


import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.*;
import uk.ac.ebi.pride.toolsuite.chart.utils.PridePlotUtils;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.filter.AccessionFilter;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;
import uk.ac.ebi.pride.utilities.util.Tuple;

import java.util.*;


/**
 * DataAccess Reader is a class to do the analysis of protein and peptide statistics and chart generation.
 * @author qingwei
 * @author ypriverol
 *
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
    private AccessionFilter<String> filter;

    private boolean noPeptide = true;
    private boolean noSpectra = true;
    private boolean noTandemSpectra = true;

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
        this(controller, null);
    }

    public DataAccessReader(DataAccessController controller, AccessionFilter<String> filter) throws PrideDataException {
        if (controller == null) {
            throw new NullPointerException(source + " is null!");
        }
        this.controller = controller;
        this.filter = filter;

        readData();
    }

    @Override
    protected void start() {
        start = System.currentTimeMillis();
    }

    @Override
    protected void reading() {
        boolean hasDecoyInformation = controller.hasDecoyInformation();

        for (int i = 0; i < 6; i++) {
            peptidesDomain[i] = i + 1.0;
        }

        //If mzTab and contains Quantitation data
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
        List<PrideData> missedBars = new ArrayList<PrideData>();
        int[] preChargeBars = new int[8];

        List<PrideData> deltaMZList = new ArrayList<PrideData>();
        List<PrideData> preMassedList = new ArrayList<PrideData>();

        Protein protein;
        List<Peptide> peptideList;

        Map<Comparable, Tuple<Boolean, Boolean>> spectrumDecoy = new HashMap<Comparable, Tuple<Boolean, Boolean>>();

        for (Comparable proteinId : controller.getProteinIds()) {

            String proteinAccession = controller.getProteinAccession(proteinId);
            if (filter == null || filter.apply(proteinAccession)) {
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
                        if (hasDecoyInformation) {
                            if (peptide.getPeptideEvidence().isDecoy()) {
                                deltaMZList.add(new PrideData(deltaMZ, PrideDataType.IDENTIFIED_DECOY));
                            } else {
                                deltaMZList.add(new PrideData(deltaMZ, PrideDataType.IDENTIFIED_TARGET));
                            }
                        }

                    }
                    // fill missed cleavages
                    missedCleavages = MoleculeUtilities.calcMissedCleavages(peptide.getSequence());
                    if (missedCleavages < 4) {
                        missedBars.add(new PrideData(missedCleavages + 0.0d, PrideDataType.IDENTIFIED_SPECTRA));
                        if (hasDecoyInformation)
                            if (peptide.getPeptideEvidence().isDecoy())
                                missedBars.add(new PrideData(missedCleavages + 0.0d, PrideDataType.IDENTIFIED_DECOY));
                            else
                                missedBars.add(new PrideData(missedCleavages + 0.0d, PrideDataType.IDENTIFIED_TARGET));

                    } else {
                        missedBars.add(new PrideData(4 + 0.0d, PrideDataType.IDENTIFIED_SPECTRA));
                        if (hasDecoyInformation)
                            if (peptide.getPeptideEvidence().isDecoy())
                                missedBars.add(new PrideData(4 + 0.0d, PrideDataType.IDENTIFIED_DECOY));
                            else
                                missedBars.add(new PrideData(4 + 0.0d, PrideDataType.IDENTIFIED_TARGET));
                    }

                    if (hasDecoyInformation && controller.hasSpectrum()) {
                        Comparable id = controller.getSpectrumIdForPeptide(peptide.getSpectrumIdentification().getId());
                        if (id != null) {
                            Tuple<Boolean, Boolean> status = new Tuple<Boolean, Boolean>(false, false);
                            if (spectrumDecoy.containsKey(id))
                                status = spectrumDecoy.get(id);
                            if (peptide.getPeptideEvidence().isDecoy()) {
                                status.setValue(true);
                            } else {
                                status.setKey(true);
                            }
                            spectrumDecoy.put(id, status);
                        }
                    }
                }

                //If mzTab and contains Quantitation data
                if (controller.getType().equals(DataAccessController.Type.MZTAB) && controller.hasQuantData()) {
                    List<QuantPeptide> quantPeptides = controller.getProteinById(proteinId).getQuantPeptides();
                    for (QuantPeptide peptide : quantPeptides) {
                        QuantScore quantScore = peptide.getQuantScore();
                        for (Comparable studyValueKey : quantScore.getStudyVariableScores().keySet()) {
                            if (quantScore.getStudyVariableScores().get(studyValueKey) != null) {
                                variablesStudy.get(studyValueKey).add(quantScore.getStudyVariableScores().get(studyValueKey));
                            }
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
        Iterator<Comparable> itIds = controller.getSpectrumIds().iterator();
        while(itIds != null && itIds.hasNext()) {
            Comparable spectrumId = itIds.next();
            noSpectra = false;
            spectrum = controller.getSpectrumById(spectrumId);
            List<Peptide> peptides = spectrum.getPeptide();
            boolean isIdentified = spectrumDecoy.containsKey(spectrumId);

            // precursor charge and mass.
            if (peptides == null) {
                preCharge = DataAccessUtilities.getPrecursorCharge(spectrum.getPrecursors());
                preMZ = DataAccessUtilities.getPrecursorMz(spectrum);
            } else {
                preCharge = peptides.get(0).getPrecursorCharge();
                preMZ = peptides.get(0).getPrecursorMz();
            }
            // Charge State must be less than 8, but some files can have the annotation wrong
            if (preCharge != null && preCharge < 8 && preCharge > 0) {
                // Identified spectrum.
                preChargeBars[preCharge - 1]++;
            }

            if (preMZ != null && preMZ > -1 && preCharge != null && preCharge < 8) {
                preMassedList.add(new PrideData(preMZ * preCharge,
                        isIdentified ? PrideDataType.IDENTIFIED_SPECTRA : PrideDataType.UNIDENTIFIED_SPECTRA
                ));
                if(hasDecoyInformation){
                    if(spectrumDecoy.containsKey(spectrum.getId())){
                        Tuple<Boolean, Boolean> status = spectrumDecoy.get(spectrumId);
                        if(status.getKey())
                            preMassedList.add(new PrideData(preMZ * preCharge, PrideDataType.IDENTIFIED_TARGET));
                        if(status.getValue())
                            preMassedList.add(new PrideData(preMZ * preCharge, PrideDataType.IDENTIFIED_DECOY));
                    }
                }
            }
            if (isIdentified) {
                identifiedSpectraSize++;
                dataType = PrideDataType.IDENTIFIED_SPECTRA;
            } else {
                unidentifiedSpectraSize++;
                dataType = PrideDataType.UNIDENTIFIED_SPECTRA;
            }

            if (controller.getSpectrumMsLevel(spectrumId) == 2) {
                noTandemSpectra = false;
                peaksMSList.add(new PrideData(spectrum.getMzBinaryDataArray().getDoubleArray().length + 0.0d, PrideDataType.ALL_SPECTRA));
                peaksMSList.add(new PrideData(spectrum.getMzBinaryDataArray().getDoubleArray().length + 0.0d, dataType));
                if(hasDecoyInformation){
                    if(spectrumDecoy.containsKey(spectrum.getId())){
                        Tuple<Boolean, Boolean> status = spectrumDecoy.get(spectrumId);
                        if(status.getKey())
                            peaksMSList.add(new PrideData(spectrum.getMzBinaryDataArray().getDoubleArray().length + 0.0d, PrideDataType.IDENTIFIED_TARGET));
                        if(status.getValue())
                            peaksMSList.add(new PrideData(spectrum.getMzBinaryDataArray().getDoubleArray().length + 0.0d, PrideDataType.IDENTIFIED_DECOY));
                    }
                }

                avgDataSource.addSpectrum(spectrum, dataType);
                double[] itArray = spectrum.getIntensityBinaryDataArray().getDoubleArray();

                for (double v : itArray) {
                    peaksIntensityList.add(new PrideData(v, dataType));
                    if(hasDecoyInformation){
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

        // release memory.
        controller = null;

        readPeptide(peptideBars);
        readDelta(deltaMZList);
        readMissed(missedBars);

        readPreCharge(preChargeBars);
        readPreMasses(preMassedList);

        readAvg(avgDataSource);
        readPeakMS(peaksMSList);
        readPeakIntensity(peaksIntensityList);
        readQuantitation(variables, variablesStudy);
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

    /**
     * Read the Delta Mz for all the peptides.
     * @param deltaMZList
     */
    private void readDelta(List<PrideData> deltaMZList) {
        if (noPeptide || noSpectra) {
            errorMap.put(PrideChartType.DELTA_MASS, new PrideDataException(PrideDataException.NO_PRE_CHARGE));
            return;
        }

        PrideEqualWidthHistogramDataSource dataSource = new PrideEqualWidthHistogramDataSource(deltaMZList.toArray(new PrideData[deltaMZList.size()]),true, false);

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

    /**
     * Read delta Peptides per Proteins Chart.
     * @param peptideBars
     */
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

    /**
     * Read miscleavage information and convert to histogram representation.
     * @param missedCleavages
     */
    private void readMissed(List<PrideData> missedCleavages) {

        if (noPeptide) {
            errorMap.put(PrideChartType.MISSED_CLEAVAGES, new PrideDataException(PrideDataException.NO_IDENTIFICATION));
            return;
        }

        PrideEqualWidthHistogramDataSource dataSource = new PrideEqualWidthHistogramDataSource(
                missedCleavages.toArray(new PrideData[missedCleavages.size()]),
                false, false
        );
        dataSource.appendBins(dataSource.generateGranularityBins(0d, 4, 1));

        histogramDataSourceMap.put(PrideChartType.MISSED_CLEAVAGES, dataSource);

    }

    /**
     * Read Quantitation Information
     * @param studyVariables
     * @param values
     */
    private void readQuantitation(Map<Comparable, String> studyVariables, Map<Comparable, List<Double>> values) {

        if (noPeptide) {
            errorMap.put(PrideChartType.QUANTITATION_PEPTIDES, new PrideDataException(PrideDataException.NO_PROTEIN_QUANT));
            return;
        }

        if (variablesStudy.isEmpty()) {
            errorMap.put(PrideChartType.QUANTITATION_PEPTIDES, new PrideDataException(PrideDataException.NO_PROTEIN_QUANT));
            return;
        }

        List<PrideData> preQuantList = new ArrayList<PrideData>();

//        PrideEqualWidthHistogramDataSource dataSource = new PrideEqualWidthHistogramDataSource(preQuantList.toArray(new PrideData[preQuantList.size()]), true, true);
//        dataSource.appendBins(dataSource.generateGranularityBins(minValue, 10, (int)(maxValue-minValue/10)));

        double start = Double.MAX_VALUE;
        double end = Double.MIN_VALUE;
        double v;


        List<Double> totalValues = new ArrayList<Double>();
        for(Comparable studyId: studyVariables.keySet()){
            List<Double> valueStudy = values.get(studyId);
            for (Double data : valueStudy) {
                v = data;
                PrideData pridata = new PrideData(data, studyVariables.get(studyId));
                preQuantList.add(pridata);
                if (v < start) {
                    start = v;
                }
                if (v > end) {
                    end = v;
                }
            }
            totalValues.addAll(valueStudy);
        }
        double[] distribution = new double[totalValues.size()];
        for(int i=0; i < totalValues.size(); i++)
            distribution[0] = totalValues.get(i);

        EmpiricalDistribution empiricalDistribution = new EmpiricalDistribution(20);
        empiricalDistribution.load(distribution);


        PrideEqualWidthHistogramDataSource dataSource = new PrideEqualWidthHistogramDataSource(preQuantList.toArray(new PrideData[preQuantList.size()]), false, true);

        for(int i = 0; i < empiricalDistribution.getUpperBounds().length -1; i++){
            dataSource.appendBin(new PrideHistogramBin(empiricalDistribution.getUpperBounds()[i],empiricalDistribution.getUpperBounds()[i+1]));
        }

        SortedMap<String, SortedMap<PrideHistogramBin, Integer>> histogramMap = dataSource.getCategoryHistogramMap();
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
                preQuantDomain.toArray(new Double[preQuantDomain.size()]),
                preQuantRange.toArray(new PrideData[preQuantRange.size()]),
                PrideDataType.IDENTIFIED_SPECTRA
        ));
    }

    /**
     * Read Avg Information for all the spectra
     * @param dataSource
     */
    private void readAvg(PrideSpectrumHistogramDataSource dataSource) {
        if (noTandemSpectra) {
            errorMap.put(PrideChartType.AVERAGE_MS, new PrideDataException(PrideDataException.NO_TANDEM_SPECTRA));
            return;
        }

        dataSource.appendBins(dataSource.generateBins(0, 1));

        histogramDataSourceMap.put(PrideChartType.AVERAGE_MS, dataSource);
    }

    /**
     * Read precursor charge information and generate the Charts
     * @param preChargeBars
     */
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

    /**
     * Read the precursor mass information and generate the Charts
     * @param preMassedList
     */
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
                true, false
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
                false,false
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
                true, false
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
    protected void end() {
        logger.debug("create data set cost: " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()));
    }
}
