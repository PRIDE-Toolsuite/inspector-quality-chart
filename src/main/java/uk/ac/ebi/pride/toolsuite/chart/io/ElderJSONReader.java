package uk.ac.ebi.pride.toolsuite.chart.io;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qingwei
 * Date: 21/06/13
 */
public class ElderJSONReader extends PrideDataReader {
    Logger logger = Logger.getLogger(ElderJSONReader.class);
    private Map<PrideChartType, JSONObject> jsonMap = new TreeMap<PrideChartType, JSONObject>();

    private final String source = "JSON";

    public static final String ERROR_MSG_SPLIT_CHAR = "\n";

    private final String ERROR = "ErrorMessages";
    private final String SERIES = "Series";
    private final String ID = "id";
    private final String X_AXIS = "XAxis";
    private final String Y_AXIS = "YAxis";
    private final String IDENTIFIED_FREQUENCY = "idenFreq";
    private final String UNIDENTIFIED_FREQUENCY = "unidenFreq";

    public ElderJSONReader(String jsonString, PrideChartType chartType) {
        try {
            JSONObject json = new JSONObject(jsonString);
            try {
                checkErrorMsg(json);
            } catch (PrideDataException e) {
                errorMap.put(chartType, e);
            }
            jsonMap.put(chartType, json);
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        readData();
    }

    public static PrideChartType getChartType(int id) {
        PrideChartType type = null;
        switch (id) {
            case 1:
                type = PrideChartType.PEAK_INTENSITY;
                break;
            case 2:
                type = PrideChartType.PRECURSOR_CHARGE;
                break;
            case 3:
                type = PrideChartType.AVERAGE_MS;
                break;
            case 4:
                type = PrideChartType.PRECURSOR_MASSES;
                break;
            case 5:
                type = PrideChartType.PEPTIDES_PROTEIN;
                break;
            case 6:
                type = PrideChartType.PEAKS_MS;
                break;
            case 7:
                type = PrideChartType.DELTA_MASS;
                break;
            case 8:
                type = PrideChartType.MISSED_CLEAVAGES;
                break;
        }

        return type;
    }

    public static int getChartID(PrideChartType chartType) {
        int id = -1;
        switch (chartType) {
            case PEAK_INTENSITY:
                id = 1;
                break;
            case PRECURSOR_CHARGE:
                id = 2;
                break;
            case AVERAGE_MS:
                id = 3;
                break;
            case PRECURSOR_MASSES:
                id = 4;
                break;
            case PEPTIDES_PROTEIN:
                id = 5;
                break;
            case PEAKS_MS:
                id = 6;
                break;
            case DELTA_MASS:
                id = 7;
                break;
            case MISSED_CLEAVAGES:
                id = 8;
                break;
        }

        return id;
    }

    /**
     * The jsonFile is txt file, which export from database.
     * Reference testset directory json files.
     */
    public ElderJSONReader(File jsonFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonFile));

            String line;
            String[] items;
            JSONObject json;
            PrideChartType chartType;
            while ((line = reader.readLine()) != null) {
                items = line.split(", ");
                json = new JSONObject(items[1]);
                chartType = getChartType(Integer.parseInt(items[0]));
                try {
                    checkErrorMsg(json);
                } catch (PrideDataException e) {
                    errorMap.put(chartType, e);
                }
                jsonMap.put(chartType, json);
            }

            reader.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        readData();
    }

    /**
     * Record json file content, which like:
     * 1, ....
     * 2, ....
     * 8, {"Series":[{"id":"MissedCleavage","YAxis":[443,94,5],"XAxis":[0,1,2]}]}
     */
    public ElderJSONReader(String[] jsonFileContent) {
        try {
            String[] items;
            JSONObject json;
            PrideChartType chartType;
            for (String line : jsonFileContent) {
                items = line.split(", ");
                json = new JSONObject(items[1]);
                chartType = getChartType(Integer.parseInt(items[0]));
                try {
                    checkErrorMsg(json);
                } catch (PrideDataException e) {
                    errorMap.put(chartType, e);
                }
                jsonMap.put(chartType, json);
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        readData();
    }

    @Override
    protected void start() {
        // do noting.
    }

    private void checkErrorMsg(JSONObject object) throws PrideDataException {
        try {
            StringBuilder sb = new StringBuilder();
            JSONArray msgList = object.getJSONArray(ERROR);
            sb.append(msgList.getString(0));
            for (int i = 1; i < msgList.length(); i++) {
                sb.append(ERROR_MSG_SPLIT_CHAR).append(msgList.getString(i));
            }

            throw new PrideDataException(sb.toString());
        } catch (JSONException e) {
            // do nothing.
        }
    }

    private Double[] getDomainData(JSONArray array) throws JSONException {
        List<Double> dataArray = new ArrayList<Double>();
        for (int i = 0; i < array.length(); i++) {
            dataArray.add(array.getDouble(i));
        }

        return dataArray.toArray(new Double[dataArray.size()]);
    }

    private PrideData[] getRangeData(JSONArray array, PrideDataType dataType) throws JSONException {
        List<PrideData> dataArray = new ArrayList<PrideData>();
        PrideData data;
        for (int i = 0; i < array.length(); i++) {
            data = new PrideData(array.getDouble(i), dataType);
            dataArray.add(data);
        }

        return dataArray.toArray(new PrideData[dataArray.size()]);
    }

    private void fillData(Double[] srcDomainData, PrideData[] srcRangeData,
                          Double[] targetDomainData, PrideData[] targetRangeData) {
        for (int i = 0; i < targetDomainData.length - 1; i++) {
            for (int j = 0; j < srcDomainData.length; j++) {
                if (srcDomainData[j].equals(targetDomainData[i])) {
                    targetRangeData[i] = srcRangeData[j];
                    break;
                }
            }
        }

        Double lastDomainData = targetDomainData[targetDomainData.length - 1];
        PrideData lastRangeData = targetRangeData[targetRangeData.length - 1];
        for (int i = 0; i < srcDomainData.length; i++) {
            if (srcDomainData[i] >= lastDomainData) {
                lastRangeData.setData(lastRangeData.getData() + srcRangeData[i].getData());
            }
        }
    }

    private void readDelta(JSONObject json) throws JSONException {
        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        peptideSize = json.getInt("sequenceNumber");
        JSONObject series = json.getJSONArray(SERIES).getJSONObject(0);
        Double[] domainData = getDomainData(series.getJSONArray(X_AXIS));
        PrideData[] rangeData = getRangeData(series.getJSONArray(Y_AXIS), dataType);
        PrideXYDataSource dataSource = new PrideXYDataSource(domainData, rangeData, dataType);
        xyDataSourceMap.put(PrideChartType.DELTA_MASS, dataSource);
    }

    private void readPeptide(JSONObject json) throws JSONException {
        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        JSONObject series = json.getJSONArray(SERIES).getJSONObject(0);
        Double[] domainData = getDomainData(series.getJSONArray(X_AXIS));
        PrideData[] rangeData = getRangeData(series.getJSONArray(Y_AXIS), dataType);

        // put data into six bars.
        Double[] sixDomainData = new Double[6];
        PrideData[] sixRangeData = new PrideData[6];
        for (int i = 0; i < 6; i++) {
            sixDomainData[i] = i + 1.0;
            sixRangeData[i] = new PrideData(0.0, dataType);
        }
        fillData(domainData, rangeData, sixDomainData, sixRangeData);

        PrideXYDataSource dataSource = new PrideXYDataSource(sixDomainData, sixRangeData, dataType);
        xyDataSourceMap.put(PrideChartType.PEPTIDES_PROTEIN, dataSource);
    }

    private void readMissed(JSONObject json) throws JSONException {
        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        JSONObject series = json.getJSONArray(SERIES).getJSONObject(0);
        Double[] domainData = getDomainData(series.getJSONArray(X_AXIS));
        PrideData[] rangeData = getRangeData(series.getJSONArray(Y_AXIS), dataType);

        // put data into five bars.
        Double[] fiveDomainData = new Double[5];
        PrideData[] fiveRangeData = new PrideData[5];
        for (int i = 0; i < 5; i++) {
            fiveDomainData[i] = i + 0.0;
            fiveRangeData[i] = new PrideData(0.0, dataType);
        }
        fillData(domainData, rangeData, fiveDomainData, fiveRangeData);

        PrideXYDataSource dataSource = new PrideXYDataSource(fiveDomainData, fiveRangeData, dataType);
        xyDataSourceMap.put(PrideChartType.MISSED_CLEAVAGES, dataSource);
    }

    private void addPeaks(PrideSpectrumHistogramDataSource dataSource, List<Double> domainList, List<PrideData> rangeList) {
        for (int i = 0; i < domainList.size(); i++) {
            dataSource.addPeak(
                    domainList.get(i),
                    rangeList.get(i).getData(),
                    rangeList.get(i).getType()
            );
        }
    }

    private void readAvg(JSONObject json) throws JSONException {
        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        List<Double> domainDataList0 = new ArrayList<Double>();
        List<PrideData> rangeDataList0 = new ArrayList<PrideData>();
        JSONObject series0 = json.getJSONArray(SERIES).getJSONObject(0);
        PrideDataType type0 = PrideDataType.findBy(series0.getString(ID));
        if (series0.has(X_AXIS)) {
            domainDataList0.addAll(Arrays.asList(getDomainData(series0.getJSONArray(X_AXIS))));
            rangeDataList0.addAll(Arrays.asList(getRangeData(series0.getJSONArray(Y_AXIS), type0)));
        }

        List<Double> domainDataList1 = new ArrayList<Double>();
        List<PrideData> rangeDataList1 = new ArrayList<PrideData>();
        JSONObject series1 = json.getJSONArray(SERIES).getJSONObject(1);
        PrideDataType type1 = PrideDataType.findBy(series1.getString(ID));
        if (series1.has(X_AXIS)) {
            domainDataList1.addAll(Arrays.asList(getDomainData(series1.getJSONArray(X_AXIS))));
            rangeDataList1.addAll(Arrays.asList(getRangeData(series1.getJSONArray(Y_AXIS), type1)));
        }

        List<Double> domainDataList = new ArrayList<Double>();
        List<PrideData> rangeDataList = new ArrayList<PrideData>();
        if (! domainDataList0.isEmpty() && domainDataList1.isEmpty()) {
            domainDataList.addAll(Arrays.asList(getDomainData(series0.getJSONArray(X_AXIS))));
            rangeDataList.addAll(Arrays.asList(getRangeData(series0.getJSONArray(Y_AXIS), dataType)));
        } else if (domainDataList0.isEmpty() && ! domainDataList1.isEmpty()) {
            domainDataList.addAll(Arrays.asList(getDomainData(series1.getJSONArray(X_AXIS))));
            rangeDataList.addAll(Arrays.asList(getRangeData(series1.getJSONArray(Y_AXIS), dataType)));
        } else if (! domainDataList0.isEmpty() && ! domainDataList1.isEmpty()) {
            for (int i = 0; i < domainDataList0.size(); i++) {
                domainDataList.add(domainDataList0.get(i));
                rangeDataList.add(new PrideData(rangeDataList0.get(i).getData() + rangeDataList1.get(i).getData(), dataType));
            }
        }

        PrideSpectrumHistogramDataSource dataSource = new PrideSpectrumHistogramDataSource(false);
        addPeaks(dataSource, domainDataList0, rangeDataList0);
        addPeaks(dataSource, domainDataList1, rangeDataList1);
        addPeaks(dataSource, domainDataList, rangeDataList);

        dataSource.appendBins(dataSource.generateBins(0, 1));
        histogramDataSourceMap.put(PrideChartType.AVERAGE_MS, dataSource);
    }

    private void readPreCharge(JSONObject json) throws JSONException {
        PrideDataType dataType = PrideDataType.IDENTIFIED_SPECTRA;

        JSONObject series = json.getJSONArray(SERIES).getJSONObject(0);
        Double[] domainData = getDomainData(series.getJSONArray(X_AXIS));
        PrideData[] rangeData = getRangeData(series.getJSONArray(Y_AXIS), dataType);

        // put data into 8 bars
        Double[] eightDomainData = new Double[8];
        PrideData[] eightRangeData = new PrideData[8];
        for (int i = 0; i < 8; i++) {
            eightDomainData[i] = i + 1.0;
            eightRangeData[i] = new PrideData(0.0, dataType);
        }
        fillData(domainData, rangeData, eightDomainData, eightRangeData);

        PrideXYDataSource dataSource = new PrideXYDataSource(eightDomainData, eightRangeData, dataType);
        xyDataSourceMap.put(PrideChartType.PRECURSOR_CHARGE, dataSource);
    }

    private void readPreMasses(JSONObject json) throws JSONException {
        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        identifiedSpectraSize = json.getInt(IDENTIFIED_FREQUENCY);
        unidentifiedSpectraSize = json.getInt(UNIDENTIFIED_FREQUENCY);

        List<Double> domainDataList0 = new ArrayList<Double>();
        List<PrideData> rangeDataList0 = new ArrayList<PrideData>();
        JSONObject series0 = json.getJSONArray(SERIES).getJSONObject(0);
        PrideDataType type0 = PrideDataType.findBy(series0.getString(ID));
        if (series0.has(X_AXIS)) {
            domainDataList0.addAll(Arrays.asList(getDomainData(series0.getJSONArray(X_AXIS))));
            rangeDataList0.addAll(Arrays.asList(getRangeData(series0.getJSONArray(Y_AXIS), type0)));
        }

        List<Double> domainDataList1 = new ArrayList<Double>();
        List<PrideData> rangeDataList1 = new ArrayList<PrideData>();
        JSONObject series1 = json.getJSONArray(SERIES).getJSONObject(1);
        PrideDataType type1 = PrideDataType.findBy(series1.getString(ID));
        if (series1.has(X_AXIS)) {
            domainDataList1.addAll(Arrays.asList(getDomainData(series1.getJSONArray(X_AXIS))));
            rangeDataList1.addAll(Arrays.asList(getRangeData(series1.getJSONArray(Y_AXIS), type1)));
        }

        List<Double> domainDataList = new ArrayList<Double>();
        List<PrideData> rangeDataList = new ArrayList<PrideData>();
        if (! domainDataList0.isEmpty() && domainDataList1.isEmpty()) {
            domainDataList.addAll(Arrays.asList(getDomainData(series0.getJSONArray(X_AXIS))));
            rangeDataList.addAll(Arrays.asList(getRangeData(series0.getJSONArray(Y_AXIS), dataType)));
        } else if (domainDataList0.isEmpty() && ! domainDataList1.isEmpty()) {
            domainDataList.addAll(Arrays.asList(getDomainData(series1.getJSONArray(X_AXIS))));
            rangeDataList.addAll(Arrays.asList(getRangeData(series1.getJSONArray(Y_AXIS), dataType)));
        } else if (! domainDataList0.isEmpty() && ! domainDataList1.isEmpty()) {
            int max = Math.max(domainDataList0.size(), domainDataList1.size());
            double identified;
            double unidentified;
            double offset;
            for (int i = 0; i < max; i++) {
                identified = i < rangeDataList0.size() ? rangeDataList0.get(i).getData() : 0.0;
                unidentified = i < rangeDataList1.size() ? rangeDataList1.get(i).getData() : 0.0;

                offset = i < domainDataList0.size() ? domainDataList0.get(i) : domainDataList1.get(i);
                domainDataList.add(offset);
                rangeDataList.add(new PrideData(
                        (identified * identifiedSpectraSize  + unidentified * unidentifiedSpectraSize) / (identifiedSpectraSize + unidentifiedSpectraSize),
                        dataType
                ));
            }
        }

        List<Double> totalDomainDataList = new ArrayList<Double>();
        List<PrideData> totalRangeDataList = new ArrayList<PrideData>();
        totalDomainDataList.addAll(domainDataList0);
        totalDomainDataList.addAll(domainDataList1);
        totalDomainDataList.addAll(domainDataList);
        totalRangeDataList.addAll(rangeDataList0);
        totalRangeDataList.addAll(rangeDataList1);
        totalRangeDataList.addAll(rangeDataList);
        Double[] domainData = totalDomainDataList.toArray(new Double[totalDomainDataList.size()]);
        PrideData[] rangeData = totalRangeDataList.toArray(new PrideData[totalRangeDataList.size()]);
        PrideXYDataSource dataSource = new PrideXYDataSource(domainData, rangeData, dataType);
        xyDataSourceMap.put(PrideChartType.PRECURSOR_MASSES, dataSource);
    }

    private PrideHistogramBin parseBin(String label) {
        label = label.replaceAll(",", "");
        Pattern pattern = Pattern.compile("(\\d+)-(\\d+)");
        Matcher matcher = pattern.matcher(label);

        if (matcher.find()) {
            int lowerBound = Integer.parseInt(matcher.group(1));
            int upperBound = Integer.parseInt(matcher.group(2));
            return new PrideHistogramBin(lowerBound, upperBound);
        }

        pattern = Pattern.compile(">(\\d+)");
        matcher = pattern.matcher(label);
        if (matcher.find()) {
            int lowerBound = Integer.parseInt(matcher.group(1));
            int upperBound = Integer.MAX_VALUE;
            return new PrideHistogramBin(lowerBound, upperBound);
        }

        return null;
    }

    private List<PrideHistogramBin> parseBins(JSONArray array) throws JSONException {
        List<PrideHistogramBin> bins = new ArrayList<PrideHistogramBin>();

        String binLabel;
        PrideHistogramBin bin;
        for (int i = 0; i < array.length(); i++) {
            binLabel = array.getString(i);
            bin = parseBin(binLabel);
            if (bin != null) {
                bins.add(bin);
            }
        }

        return bins;
    }

    private List<Integer> parseCounts(JSONArray array) throws JSONException {
        List<Integer> countList = new ArrayList<Integer>();

        for (int i = 0; i < array.length(); i++) {
            countList.add(array.getInt(i));
        }

        return countList;
    }

    private void readPeakMS(JSONObject json) throws JSONException {
        PrideDataType dataType = PrideDataType.ALL_SPECTRA;

        JSONObject series = json.getJSONArray(SERIES).getJSONObject(0);
        List<PrideHistogramBin> bins = parseBins(series.getJSONArray(X_AXIS));
        List<Integer> countList = parseCounts(series.getJSONArray(Y_AXIS));

        List<PrideData> values = new ArrayList<PrideData>();
        int count;
        for (int i = 0; i < countList.size(); i++) {
            count = countList.get(i);
            for (int j = 0; j < count; j++) {
                values.add(new PrideData(bins.get(i).getStartBoundary() + 0.0d, dataType));
            }
        }

        PrideHistogramDataSource dataSource = new PrideHistogramDataSource(values.toArray(new PrideData[values.size()]), false);
        for (PrideHistogramBin bin : bins) {
            dataSource.appendBin(bin);
        }
        histogramDataSourceMap.put(PrideChartType.PEAKS_MS, dataSource);
    }

    private void addSeries(List<PrideData> values, List<PrideHistogramBin> bins, List<Integer> countList, PrideDataType type) throws JSONException {
        int count;
        for (int i = 0; i < countList.size(); i++) {
            count = countList.get(i);
            for (int j = 0; j < count; j++) {
                values.add(new PrideData(bins.get(i).getStartBoundary() + 0.0d, type));
            }
        }
    }

    private void readPeakIntensity(JSONObject json) throws JSONException {
        List<PrideData> values = new ArrayList<PrideData>();

        JSONObject series = json.getJSONArray(SERIES).getJSONObject(0);
        PrideDataType type = PrideDataType.findBy(series.getString(ID));
        List<PrideHistogramBin> bins = parseBins(series.getJSONArray(X_AXIS));
        List<Integer> countList = parseCounts(series.getJSONArray(Y_AXIS));
        addSeries(values, bins, countList, type);

        series = json.getJSONArray(SERIES).getJSONObject(1);
        type = PrideDataType.findBy(series.getString(ID));
        bins = parseBins(series.getJSONArray(X_AXIS));
        countList = parseCounts(series.getJSONArray(Y_AXIS));
        addSeries(values, bins, countList, type);

        PrideHistogramDataSource dataSource = new PrideHistogramDataSource(values.toArray(new PrideData[values.size()]), true);
        for (PrideHistogramBin bin : bins) {
            dataSource.appendBin(bin);
        }

        histogramDataSourceMap.put(PrideChartType.PEAK_INTENSITY, dataSource);
    }

    @Override
    protected void reading() {
        try {
            for (PrideChartType type : jsonMap.keySet()) {
                if (errorMap.containsKey(type)) {
                    continue;
                }

                switch (type) {
                    case DELTA_MASS:
                        readDelta(jsonMap.get(type));
                        break;
                    case PEPTIDES_PROTEIN:
                        readPeptide(jsonMap.get(type));
                        break;
                    case MISSED_CLEAVAGES:
                        readMissed(jsonMap.get(type));
                        break;
                    case AVERAGE_MS:
                        readAvg(jsonMap.get(type));
                        break;
                    case PRECURSOR_CHARGE:
                        readPreCharge(jsonMap.get(type));
                        break;
                    case PRECURSOR_MASSES:
                        readPreMasses(jsonMap.get(type));
                        break;
                    case PEAKS_MS:
                        readPeakMS(jsonMap.get(type));
                        break;
                    case PEAK_INTENSITY:
                        readPeakIntensity(jsonMap.get(type));
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void end() {
        // do noting.
    }
}
