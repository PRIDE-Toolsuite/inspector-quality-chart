package uk.ac.ebi.pride.toolsuite.chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import uk.ac.ebi.pride.toolsuite.chart.io.*;

import java.awt.*;
import java.io.File;
import java.net.URL;


/**
 * @author qingwei
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
        URL url = PrideChartFactoryRun.class.getClassLoader().getResource("new_10.json");
        File jsonFile = new File(url.toURI());
        run.drawChartList(new JSONReader(jsonFile), PrideChartSummary.PROJECT_SUMMARY);
    }
}
