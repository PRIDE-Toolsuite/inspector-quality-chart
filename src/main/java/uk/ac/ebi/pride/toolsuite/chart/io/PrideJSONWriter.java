package uk.ac.ebi.pride.toolsuite.chart.io;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.*;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author qingwei
 * Date: 20/06/13
 */
public class PrideJSONWriter {
    public static final String ERROR = "ErrorMessages";
    public static final String SERIES = "Series";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String X_AXIS = "XAxis";
    public static final String Y_AXIS = "YAxis";
    public static final String FREQUENCY = "Freq";
    public static final String INTENSITY = "Intensity";
    public static final String DELTA_MASSES = "deltaMasses";
    public static final String MISSED_CLEAVAGE = "MissedCleavage";
    public static final String IDENTIFIED_FREQUENCY = "idenFreq";
    public static final String UNIDENTIFIED_FREQUENCY = "unidenFreq";
    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String EXPERIMENT_SIZE = "experimentSize";
    public static final String IDENTIFIED_SPECTRA = "Identified Spectra";
    public static final String UNIDENTIFIED_SPECTRA = "Unidentified Spectra";

    private PrideDataReader reader;

    public PrideJSONWriter(PrideDataReader reader) {
        if (reader == null) {
            throw new NullPointerException("Please set a pride data reader instance.");
        }

        this.reader = reader;
    }

    private class Series {
        String id;
        String type = null;

        Collection<Object> xArray = null;
        Collection<Object> yArray = null;

        private Series(String id, String type, Collection<Object> xArray, Collection<Object> yArray) {
            this.id = id;
            this.type = type;
            this.xArray = xArray;
            this.yArray = yArray;
        }

        JSONObject getJSONObject() throws JSONException {
            JSONObject series = new JSONObject();

            series.put(ID, id);

            if (yArray != null) {
                series.put(Y_AXIS, yArray);
            }

            if (type != null) {
                series.put(TYPE, type);
            }

            if (xArray != null) {
                series.put(X_AXIS, xArray);
            }

            return series;
        }
    }

    private List<Object> getDomainValues(Double[] domainData) {
        List<Object> values = new ArrayList<Object>();

        Collections.addAll(values, domainData);

        return values;
    }

    private List<Object> getDomainValues(Double[] domainData, PrideData[] rangeData, PrideDataType dataType) {
        List<Object> values = new ArrayList<Object>();

        PrideData prideData;
        for (int i = 0; i < rangeData.length; i++) {
            prideData = rangeData[i];
            if (dataType != null && prideData.getType() == dataType) {
                values.add(domainData[i]);
            } else if (dataType == null) {
                values.add(domainData[i]);
            }
        }

        return values;
    }

    private List<Object> getRangeValues(Double[] rangeData) {
        List<Object> values = new ArrayList<Object>();

        Collections.addAll(values, rangeData);

        return values;
    }

    private List<Object> getRangeValues(Collection<Double> rangeData) {
        List<Object> values = new ArrayList<Object>();

        Collections.addAll(values, rangeData.toArray());

        return values;
    }

    private List<Object> getRangeValues(PrideData[] rangeData, PrideDataType dataType) {
        List<Object> values = new ArrayList<Object>();

        for (PrideData prideData : rangeData) {
            if (dataType != null && prideData.getType() == dataType) {
                values.add(prideData.getData());
            } else if (dataType == null) {
                values.add(prideData.getData());
            }
        }

        return values;
    }

    public JSONObject getDelta() throws JSONException {
        if (getErrorMessages(PrideChartType.DELTA_MASS) != null) {
            return getErrorMessages(PrideChartType.DELTA_MASS);
        }

        PrideXYDataSource dataSource = reader.getXYDataSourceMap().get(PrideChartType.DELTA_MASS);
        boolean hasData = false;
        if (dataSource != null) {
            for (PrideData data : dataSource.getRangeData()) {
                if (data.getData() > 0) {
                    hasData = true;
                    break;
                }
            }

            if (!hasData) {
                JSONObject obj = new JSONObject();
                obj.put(ERROR, new String[]{PrideDataException.NO_PRECURSOR_CHARGE});
                return obj;
            }

            Series series = new Series(
                    DELTA_MASSES, null,
                    getDomainValues(dataSource.getDomainData()),
                    getRangeValues(dataSource.getRangeData(), null)
            );

            JSONObject obj = new JSONObject();
            JSONArray seriesArray = new JSONArray();
            seriesArray.put(series.getJSONObject());
            obj.put(SERIES, seriesArray);
            obj.put(SEQUENCE_NUMBER, reader.getPeptideSize());

            return obj;
        }

        return null;
    }

    public JSONObject getPeptides() throws JSONException {
        if (getErrorMessages(PrideChartType.PEPTIDES_PROTEIN) != null) {
            return getErrorMessages(PrideChartType.PEPTIDES_PROTEIN);
        }

        PrideXYDataSource dataSource = reader.getXYDataSourceMap().get(PrideChartType.PEPTIDES_PROTEIN);
        if (dataSource != null) {
            Series series = new Series(
                    FREQUENCY, null,
                    getDomainValues(dataSource.getDomainData()),
                    getRangeValues(dataSource.getRangeData(), null)
            );

            JSONObject obj = new JSONObject();
            JSONArray seriesArray = new JSONArray();
            seriesArray.put(series.getJSONObject());
            obj.put(SERIES, seriesArray);

            return obj;
        }

        return null;
    }

    public JSONObject getMissed() throws JSONException {
        if (getErrorMessages(PrideChartType.MISSED_CLEAVAGES) != null) {
            return getErrorMessages(PrideChartType.MISSED_CLEAVAGES);
        }

        PrideXYDataSource dataSource = reader.getXYDataSourceMap().get(PrideChartType.MISSED_CLEAVAGES);
        if (dataSource != null) {
            Series series = new Series(
                    MISSED_CLEAVAGE, null,
                    getDomainValues(dataSource.getDomainData()),
                    getRangeValues(dataSource.getRangeData(), null)
            );

            JSONObject obj = new JSONObject();
            JSONArray seriesArray = new JSONArray();
            seriesArray.put(series.getJSONObject());
            obj.put(SERIES, seriesArray);

            return obj;
        }

        return null;
    }

    public JSONObject getAvg() throws JSONException {
        if (getErrorMessages(PrideChartType.AVERAGE_MS) != null) {
            return getErrorMessages(PrideChartType.AVERAGE_MS);
        }

        PrideSpectrumHistogramDataSource dataSource = (PrideSpectrumHistogramDataSource) reader.getHistogramDataSourceMap().get(PrideChartType.AVERAGE_MS);
        if (dataSource != null) {
            SortedMap<PrideHistogramBin, Double> idPrideHistogram = dataSource.getIntensityMap().get(PrideDataType.IDENTIFIED_SPECTRA);
            SortedMap<PrideHistogramBin, Double> unPrideHistogram = dataSource.getIntensityMap().get(PrideDataType.UNIDENTIFIED_SPECTRA);

            List<Series> seriesList = new ArrayList<Series>();
            if (unPrideHistogram == null) {
                seriesList.add(new Series(UNIDENTIFIED_SPECTRA, UNIDENTIFIED_SPECTRA, null, null));
            } else {
                seriesList.add(
                        new Series(
                                UNIDENTIFIED_SPECTRA, UNIDENTIFIED_SPECTRA,
                                getDomainValues(unPrideHistogram.keySet()),
                                getRangeValues(unPrideHistogram.values())
                        )
                );
            }

            if (idPrideHistogram == null) {
                seriesList.add(new Series(IDENTIFIED_SPECTRA, IDENTIFIED_SPECTRA, null, null));
            } else {
                seriesList.add(
                        new Series(
                                IDENTIFIED_SPECTRA, IDENTIFIED_SPECTRA,
                                getDomainValues(idPrideHistogram.keySet()),
                                getRangeValues(idPrideHistogram.values())
                        )
                );
            }

            JSONObject obj = new JSONObject();
            for (Series series : seriesList) {
                obj.append(SERIES, series.getJSONObject());
            }
            obj.put(EXPERIMENT_SIZE, reader.getSpectraSize());

            return obj;
        }

        return null;
    }

    public JSONObject getPreCharge() throws JSONException {
        if (getErrorMessages(PrideChartType.PRECURSOR_CHARGE) != null) {
            return getErrorMessages(PrideChartType.PRECURSOR_CHARGE);
        }

        PrideXYDataSource dataSource = reader.getXYDataSourceMap().get(PrideChartType.PRECURSOR_CHARGE);
        if (dataSource != null) {
            Series series = new Series(
                    IDENTIFIED_SPECTRA, IDENTIFIED_SPECTRA,
                    getDomainValues(dataSource.getDomainData()),
                    getRangeValues(dataSource.getRangeData(), null)
            );

            JSONObject obj = new JSONObject();
            JSONArray seriesArray = new JSONArray();
            seriesArray.put(series.getJSONObject());
            obj.put(SERIES, seriesArray);
            obj.put(EXPERIMENT_SIZE, reader.getSpectraSize());

            return obj;
        }

        return null;
    }

    public JSONObject getPreMasses() throws JSONException {
        if (getErrorMessages(PrideChartType.PRECURSOR_MASSES) != null) {
            return getErrorMessages(PrideChartType.PRECURSOR_MASSES);
        }

        PrideXYDataSource dataSource = reader.getXYDataSourceMap().get(PrideChartType.PRECURSOR_MASSES);
        if (dataSource != null) {
            List<Series> seriesList = new ArrayList<Series>();
            List<Object> unSpectraList = getRangeValues(dataSource.getRangeData(), PrideDataType.UNIDENTIFIED_SPECTRA);
            List<Object> unDomainList = getDomainValues(dataSource.getDomainData(), dataSource.getRangeData(), PrideDataType.UNIDENTIFIED_SPECTRA);
            List<Object> idSpectraList = getRangeValues(dataSource.getRangeData(), PrideDataType.IDENTIFIED_SPECTRA);
            List<Object> idDomainList = getDomainValues(dataSource.getDomainData(), dataSource.getRangeData(), PrideDataType.IDENTIFIED_SPECTRA);

            if (unSpectraList.size() == 0) {
                seriesList.add(new Series(UNIDENTIFIED_SPECTRA, UNIDENTIFIED_SPECTRA, null, null));
            } else {
                seriesList.add(
                        new Series(
                                UNIDENTIFIED_SPECTRA, UNIDENTIFIED_SPECTRA,
                                unDomainList,
                                unSpectraList
                        )
                );
            }

            if (idSpectraList.size() == 0) {
                seriesList.add(new Series(IDENTIFIED_SPECTRA, IDENTIFIED_SPECTRA, null, null));
            } else {
                seriesList.add(
                        new Series(
                                IDENTIFIED_SPECTRA, IDENTIFIED_SPECTRA,
                                idDomainList,
                                idSpectraList
                        )
                );
            }

            JSONObject obj = new JSONObject();
            for (Series series : seriesList) {
                obj.append(SERIES, series.getJSONObject());
            }
            obj.put(UNIDENTIFIED_FREQUENCY, reader.getUnidentifiedSpectraSize());
            obj.put(IDENTIFIED_FREQUENCY, reader.getIdentifiedSpectraSize());

            return obj;
        }

        return null;
    }

    private List<Object> getDomainValues(Collection<PrideHistogramBin> bins) {
        List<Object> values = new ArrayList<Object>();

        for (PrideHistogramBin bin : bins) {
            values.add(bin.toString(new DecimalFormat("#")));
        }

        return values;
    }

    private Collection<Object> getIntegerRangeValues(Collection<Integer> data) {
        List<Object> values = new ArrayList<Object>();

        values.addAll(data);

        return values;
    }

    public JSONObject getPeaksMS() throws JSONException {
        if (getErrorMessages(PrideChartType.PEAKS_MS) != null) {
            return getErrorMessages(PrideChartType.PEAKS_MS);
        }

        PrideHistogramDataSource dataSource = reader.getHistogramDataSourceMap().get(PrideChartType.PEAKS_MS);

        if (dataSource != null) {
            SortedMap<PrideHistogramBin, Integer> histogram = dataSource.getHistogramMap().get(PrideDataType.ALL_SPECTRA);
            Series series = new Series(
                    INTENSITY, null,
                    getDomainValues(histogram.keySet()),
                    getIntegerRangeValues(histogram.values())
            );

            JSONObject obj = new JSONObject();
            JSONArray seriesArray = new JSONArray();
            seriesArray.put(series.getJSONObject());
            obj.put(SERIES, seriesArray);
            obj.put(EXPERIMENT_SIZE, reader.getSpectraSize());

            return obj;
        }

        return null;
    }

    public JSONObject getPeakIntensity() throws JSONException {
        if (getErrorMessages(PrideChartType.PEAK_INTENSITY) != null) {
            return getErrorMessages(PrideChartType.PEAK_INTENSITY);
        }

        PrideHistogramDataSource dataSource = reader.getHistogramDataSourceMap().get(PrideChartType.PEAK_INTENSITY);

        if (dataSource != null) {
            SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> histogramMap = dataSource.getHistogramMap();
            List<Series> seriesList = new ArrayList<Series>();

            SortedMap<PrideHistogramBin, Integer> histogram = histogramMap.get(PrideDataType.IDENTIFIED_SPECTRA);
            if (histogram != null) {
                seriesList.add(new Series(
                        IDENTIFIED_SPECTRA, IDENTIFIED_SPECTRA,
                        getDomainValues(histogram.keySet()),
                        getIntegerRangeValues(histogram.values())
                ));
            } else {
                seriesList.add(new Series(
                        IDENTIFIED_SPECTRA, IDENTIFIED_SPECTRA,
                        null,
                        null
                ));
            }

            histogram = histogramMap.get(PrideDataType.UNIDENTIFIED_SPECTRA);
            if (histogram != null) {
                seriesList.add(new Series(
                        UNIDENTIFIED_SPECTRA, UNIDENTIFIED_SPECTRA,
                        getDomainValues(histogram.keySet()),
                        getIntegerRangeValues(histogram.values())
                ));
            } else {
                seriesList.add(new Series(
                        UNIDENTIFIED_SPECTRA, UNIDENTIFIED_SPECTRA,
                        null,
                        null
                ));
            }

            JSONObject obj = new JSONObject();
            for (Series series : seriesList) {
                obj.append(SERIES, series.getJSONObject());
            }
            obj.put(EXPERIMENT_SIZE, reader.getSpectraSize());

            return obj;
        }

        return null;
    }

    private JSONObject getErrorMessages(PrideChartType chartType) throws JSONException {
        if (!reader.getErrorMap().containsKey(chartType)) {
            return null;
        }

        JSONObject obj = new JSONObject();
        String errorMsg = reader.getErrorMap().get(chartType).getMessage();
        String[] errorItems = errorMsg.split(ElderJSONReader.ERROR_MSG_SPLIT_CHAR);
        obj.put(ERROR, errorItems);

        return obj;
    }

}
