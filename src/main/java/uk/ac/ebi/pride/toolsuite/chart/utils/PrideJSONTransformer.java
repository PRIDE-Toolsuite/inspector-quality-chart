package uk.ac.ebi.pride.toolsuite.chart.utils;

import org.apache.log4j.Logger;
import org.json.JSONException;
import uk.ac.ebi.pride.toolsuite.chart.io.DataAccessReader;
import uk.ac.ebi.pride.toolsuite.chart.io.PrideDataException;
import uk.ac.ebi.pride.toolsuite.chart.io.PrideDataReader;
import uk.ac.ebi.pride.toolsuite.chart.io.PrideJSONWriter;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static uk.ac.ebi.pride.toolsuite.chart.utils.PridePlotConstants.NEW_LINE;

/**
 * @author qingwei
 * Date: 03/07/13
 */
public class PrideJSONTransformer {
    private static Logger logger = Logger.getLogger(PrideJSONTransformer.class);

    private enum FileType {PRIDE_XML, mzIdentML}

    private void writeJSONFile(PrideJSONWriter writer, File outFile) throws JSONException, IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("1, ").append(writer.getPeakIntensity().toString()).append(NEW_LINE);
        sb.append("2, ").append(writer.getPreCharge().toString()).append(NEW_LINE);
        sb.append("3, ").append(writer.getAvg().toString()).append(NEW_LINE);
        sb.append("4, ").append(writer.getPreMasses().toString()).append(NEW_LINE);
        sb.append("5, ").append(writer.getPeptides().toString()).append(NEW_LINE);
        sb.append("6, ").append(writer.getPeaksMS().toString()).append(NEW_LINE);
        sb.append("7, ").append(writer.getDelta().toString()).append(NEW_LINE);
        sb.append("8, ").append(writer.getMissed().toString());

        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
        out.write(sb.toString());
        out.close();
    }

    private String getFileSize(File file) {
        double size = file.length() / 1024d / 1024;
        return new BigDecimal(size).setScale(2, RoundingMode.CEILING).toString() + "(MB)";
    }

    public void transform(File inFile, File outDir) throws PrideDataException, IOException, JSONException {
        PrideDataReader reader;
        PrideJSONWriter writer;
        DataAccessController controller = null;
        long start;
        long end;

        String inFileName = inFile.getName();
        String outFileName = inFileName.replaceAll("\\.(xml|mzid)$", ".json");
        File outFile = new File(outDir, outFileName);

        FileType type = null;
        if (inFileName.endsWith("xml")) {
            type = FileType.PRIDE_XML;
        } else if (inFileName.endsWith("mzid")) {
            type = FileType.mzIdentML;
        }

        if (type == null) {
            return;
        }

        logger.debug("Begin load " + type + " file: " + inFile.getName() + ", file size: " + getFileSize(inFile));
        start = System.currentTimeMillis();
        switch (type) {
            case PRIDE_XML:
                controller = new PrideXmlControllerImpl(inFile);
                break;
            case mzIdentML:
                controller = new MzIdentMLControllerImpl(inFile);
                break;
        }
        end = System.currentTimeMillis();
        logger.debug("End load " + type + " file: " + inFile.getName() + ". Cost " + PridePlotUtils.getTimeCost(start, end) + "(s)");

        reader = new DataAccessReader(controller);

        start = System.currentTimeMillis();
        writer = new PrideJSONWriter(reader);
        writeJSONFile(writer, outFile);
        logger.debug("End export json file: " + outFile.getName() + ". Cost " + PridePlotUtils.getTimeCost(start, System.currentTimeMillis()) + "(s)\n\n\n");
    }

    public static void main(String[] args) {
        File inDir = new File(args[0]);
        File outDir = new File(args[1]);

        PrideJSONTransformer transformer = new PrideJSONTransformer();

        if (! outDir.exists()) {
            outDir.mkdirs();
        }

        for (File inFile : inDir.listFiles()) {
            if (inFile.isDirectory()) {
                continue;
            }

            try {
                transformer.transform(inFile, outDir);
            } catch (PrideDataException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
