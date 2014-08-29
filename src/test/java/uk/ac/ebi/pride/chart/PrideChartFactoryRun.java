package uk.ac.ebi.pride.chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import uk.ac.ebi.pride.chart.io.*;
import uk.ac.ebi.pride.data.controller.DataAccessController;
import uk.ac.ebi.pride.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.data.controller.impl.ControllerImpl.MzMLControllerImpl;
import uk.ac.ebi.pride.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

import java.awt.*;
import java.io.File;
import java.util.*;

/**
 * User: qingwei
 * Date: 21/06/13
 */
public class PrideChartFactoryRun {
    private void drawChart(PrideDataReader reader, PrideChartType chartType) {
        JFreeChart chart = PrideChartFactory.getChart(reader, chartType);

        if (chart == null) {
            return;
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        ApplicationFrame mainFrame = new ApplicationFrame("test");
        mainFrame.setContentPane(chartPanel);

        mainFrame.pack();
        RefineryUtilities.centerFrameOnScreen(mainFrame);
        mainFrame.setVisible(true);
    }

    public void drawChartList(PrideDataReader reader, PrideChartSummary summary) throws Exception {
        for (PrideChartType chartType : summary.getAll()) {
            drawChart(reader, chartType);
        }
    }

    public static void main(String[] args) throws Exception {
        PrideChartFactoryRun run = new PrideChartFactoryRun();

//        File jsonFile = new File("testset/new_2.json");
//        File jsonFile = new File("testset/new_1643.json");
        File jsonFile = new File("new_10.json");
        run.drawChartList(new JSONReader(jsonFile), PrideChartSummary.PROJECT_SUMMARY);

//        File jsonFile = new File("testset/old_2.json");
//        File jsonFile = new File("testset/old_1643.json");
//        File jsonFile = new File("testset/old_10.json");
//        run.drawChartList(new ElderJSONReader(jsonFile), PrideChartSummary.PROJECT_SUMMARY);

//        File prideXMLFile = new File("testset/PRIDE_Exp_Complete_Ac_2.xml");
//        run.drawChartList(new DataAccessReader(new PrideXmlControllerImpl(prideXMLFile)), PrideChartSummary.PROJECT_SUMMARY);

//        File mzMLFile = new File("testset/mzml-example.mzML");
//        run.drawChartList(new DataAccessReader(new MzMLControllerImpl(mzMLFile)), PrideChartSummary.PROJECT_SUMMARY);

//        File mzidFile = new File("c:\\work\\example-files\\mzidentml_files\\mgf-mapped\\F001261.mzid");
//        java.util.List<File> mgfList = new ArrayList<File>();
//        mgfList.add(new File("c:\\work\\example-files\\mzidentml_files\\mgf-mapped\\data-no-pitc-34-filter.mgf"));
//        MzIdentMLControllerImpl controller = new MzIdentMLControllerImpl(mzidFile);
//        controller.addMSController(mgfList);
//        run.drawChartList(new DataAccessReader(controller), PrideChartSummary.PROJECT_SUMMARY);

//        File mzidFile = new File("c:\\work\\example-files\\mzidentml_files\\CPTAC\\H20120518_JQ_CPTAC2_COMPREF4_IMAC_01.mzIdentML");
//        MzIdentMLControllerImpl controller = new MzIdentMLControllerImpl(mzidFile);
//        run.drawChartList(new DataAccessReader(controller), PrideChartSummary.PROJECT_SUMMARY);
    }
}
