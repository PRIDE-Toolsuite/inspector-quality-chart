package uk.ac.ebi.pride.chart.io;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.pride.chart.utils.PridePlotConstants;

import java.io.*;

/**
 * User: Qingwei
 * Date: 11/06/13
 */
public class PrideJSONReader extends PrideDataReader {
    private Logger logger = Logger.getLogger(PrideJSONReader.class);
    private String source = "JSON";

    private JSONObject json = null;

    private void init(JSONObject json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON Object can not set null!");
        }

        this.json = json;
    }

    public PrideJSONReader(JSONObject json) {
        init(json);
    }

    public PrideJSONReader(String jsonString) {
        try {
            init(new JSONObject(jsonString));
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }

    public PrideJSONReader(File jsonFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(PridePlotConstants.NEW_LINE);
            }

            init(new JSONObject(sb.toString()));
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void start() {
        // do noting.
    }

    @Override
    protected void reading() {

    }

    @Override
    protected void end() {
        // do noting.
    }
}
