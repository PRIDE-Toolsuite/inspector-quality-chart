package uk.ac.ebi.pride.chart.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>PMD Quartiles file reader.</p>
 *
 * @author Antonio Fabregat
 * Date: 07-oct-2010
 * Time: 15:52:30
 */
public class QuartilesReader {
    private QuartilesType type;

    private List<Integer> points = new ArrayList<Integer>();

    /**
     * Contains the Q1 quartiles values from the file
     */
    private List<Double> q1Values = new ArrayList<Double>();

    /**
     * Contains the Q2 quartiles values from the file
     */
    private List<Double> q2Values = new ArrayList<Double>();

    /**
     * Contains the Q3 quartiles values from the file
     */
    private List<Double> q3Values = new ArrayList<Double>();

    /**
     * <p> Creates an instance of this QuartilesReader object, setting all fields as per description below.</p>
     */
    public QuartilesReader(QuartilesType type) {
        if (type != QuartilesType.NONE) {
            String fileName = type.getFileName();
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            initialize(br);
        }

        this.type = type;
    }

    /**
     * Loads the data from the specified BufferedReader
     *
     * @param br the buffered reader of the file with the PMD quartiles chartData
     */
    private void initialize(BufferedReader br){
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line.startsWith("\"V1\",\"V2\",\"V3\"")) {
                    continue;
                }

                String[] values = line.split(",");
                points.add(Integer.valueOf(values[0].replaceAll("\"", "")));
                q1Values.add(Double.valueOf(values[1]));
                q2Values.add(Double.valueOf(values[2]));
                q3Values.add(Double.valueOf(values[3]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public QuartilesType getType() {
        return type;
    }

    public int size() {
        return q1Values.size();
    }

    public List<Integer> getPoints() {
        return points;
    }

    /**
     * Returns the PMD Q1 quartile data
     *
     * @return the PMD Q1 quartile data
     */
    public List<Double> getQ1Values() {
        return q1Values;
    }

    /**
     * Returns the PMD Q2 quartile data
     *
     * @return the PMD Q2 quartile data
     */
    public List<Double> getQ2Values() {
        return q2Values;
    }

    /**
     * Returns the PMD Q3 quartile data
     *
     * @return the PMD Q3 quartile data
     */
    public List<Double> getQ3Values() {
        return q3Values;
    }
}