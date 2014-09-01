package uk.ac.ebi.pride.toolsuite.chart.io;

import org.apache.log4j.Logger;
import org.json.JSONException;
import uk.ac.ebi.pride.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

import java.io.File;

import static uk.ac.ebi.pride.toolsuite.chart.utils.PridePlotConstants.NEW_LINE;

/**
 * @author qingwei
 * Date: 01/07/13
 */
public class PrideJSONWriterRun {
    private static Logger logger = Logger.getLogger(PrideJSONWriterRun.class);

    private PrideJSONWriter writer;

    public PrideJSONWriterRun(PrideDataReader reader) {
        this.writer = new PrideJSONWriter(reader);
    }

    public String toJSONString() throws JSONException {
        StringBuilder sb = new StringBuilder();
        logger.debug("Begin write JSON.");

        sb.append("1, ").append(writer.getPeakIntensity().toString()).append(NEW_LINE);
        logger.debug("Write Peak Intensity.");
        sb.append("2, ").append(writer.getPreCharge().toString()).append(NEW_LINE);
        logger.debug("Write precursor charge.");
        sb.append("3, ").append(writer.getAvg().toString()).append(NEW_LINE);
        logger.debug("Write average.");
        sb.append("4, ").append(writer.getPreMasses().toString()).append(NEW_LINE);
        logger.debug("Write precursor masses.");
        sb.append("5, ").append(writer.getPeptides().toString()).append(NEW_LINE);
        logger.debug("Write Peptides.");
        sb.append("6, ").append(writer.getPeaksMS().toString()).append(NEW_LINE);
        logger.debug("Write peaks per ms/ms.");
        sb.append("7, ").append(writer.getDelta().toString()).append(NEW_LINE);
        logger.debug("Write delta m/z.");
        sb.append("8, ").append(writer.getMissed().toString()).append(NEW_LINE);
        logger.debug("Write missed cleavages.");

        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
//        PrideDataReader reader = new ElderJSONReader(new File("testset/old_1643.json"));
//        PrideDataReader reader = new ElderJSONReader(new File("testset/old_10.json"));
//        PrideDataReader reader = new ElderJSONReader(new File("testset/old_2.json"));

//        PrideDataReader reader = new DataAccessReader(new PrideXmlControllerImpl(new File("testset/PRIDE_Exp_Complete_Ac_2.xml")));
//        PrideDataReader reader = new DataAccessReader(new PrideXmlControllerImpl(new File("PRIDE_Exp_Complete_Ac_10.xml")));
//        PrideDataReader reader = new DataAccessReader(new PrideXmlControllerImpl(new File("testset/PRIDE_Exp_Complete_Ac_18249.xml")));
 //       PrideJSONWriterRun run = new PrideJSONWriterRun(reader);

  //      System.out.println(run.toJSONString());

    }
}
