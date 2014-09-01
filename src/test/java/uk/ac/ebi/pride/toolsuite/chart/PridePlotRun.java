package uk.ac.ebi.pride.toolsuite.chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import uk.ac.ebi.pride.toolsuite.chart.dataset.*;
import uk.ac.ebi.pride.toolsuite.chart.io.QuartilesType;
import uk.ac.ebi.pride.toolsuite.chart.plot.*;
import uk.ac.ebi.pride.toolsuite.chart.dataset.PrideHistogramBin;

import java.awt.*;
import java.util.Random;

/**
* @author qingwei
* Date: 13/06/13
*/
public class PridePlotRun {

    private void drawChart(PrideXYPlot plot) {
        if (plot != null) {
            JFreeChart chart = PrideChartFactory.getChart(plot);
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(500, 300));
            ApplicationFrame mainFrame = new ApplicationFrame("test");
            mainFrame.setContentPane(chartPanel);

            mainFrame.pack();
            RefineryUtilities.centerFrameOnScreen(mainFrame);
            mainFrame.setVisible(true);
        }
    }

    private void drawChart(PrideCategoryPlot plot) {
        if (plot != null) {
            JFreeChart chart = PrideChartFactory.getChart(plot);
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(500, 300));
            ApplicationFrame mainFrame = new ApplicationFrame("test");
            mainFrame.setContentPane(chartPanel);

            mainFrame.pack();
            RefineryUtilities.centerFrameOnScreen(mainFrame);
            mainFrame.setVisible(true);
        }
    }

    private void createChart(Double[] domainData, PrideData[] rangeData, PrideChartType type) {
        PrideXYDataSource dataSource;

        PrideXYPlot plot;
        switch (type) {
            case DELTA_MASS:
                dataSource = new PrideXYDataSource(domainData, rangeData, PrideDataType.ALL_SPECTRA);
                plot = new DeltaMZPlot(PrideDatasetFactory.getXYDataset(dataSource));
                break;
            case PEPTIDES_PROTEIN:
                dataSource = new PrideXYDataSource(domainData, rangeData, PrideDataType.ALL_SPECTRA);
                plot = new PeptidesProteinPlot(PrideDatasetFactory.getXYDataset(dataSource));
                break;
            case MISSED_CLEAVAGES:
                dataSource = new PrideXYDataSource(domainData, rangeData, PrideDataType.ALL_SPECTRA);
                plot = new MissedCleavagesPlot(PrideDatasetFactory.getXYDataset(dataSource));
                break;
            case AVERAGE_MS:
                dataSource = new PrideXYDataSource(domainData, rangeData, PrideDataType.ALL_SPECTRA);
                plot = new AverageMSPlot(PrideDatasetFactory.getXYDataset(dataSource), PrideDataType.UNIDENTIFIED_SPECTRA);
                AverageMSPlot avgPlot = (AverageMSPlot) plot;
                avgPlot.updateSpectraSeries(PrideDataType.IDENTIFIED_SPECTRA);
//                avgPlot.updateSpectraSeries(PrideDataType.ALL_SPECTRA);
                break;
            case PRECURSOR_CHARGE:
                dataSource = new PrideXYDataSource(domainData, rangeData, PrideDataType.IDENTIFIED_SPECTRA);
                plot = new PrecursorChargePlot(PrideDatasetFactory.getXYDataset(dataSource));
                break;
            case PRECURSOR_MASSES:
                dataSource = new PrideXYDataSource(domainData, rangeData, PrideDataType.ALL_SPECTRA);
                plot = new PrecursorMassesPlot(PrideDatasetFactory.getXYDataset(dataSource), PrideDataType.IDENTIFIED_SPECTRA);
                PrecursorMassesPlot massesPlot = (PrecursorMassesPlot)plot ;
                massesPlot.updateQuartilesType(QuartilesType.HUMAN);
//                massesPlot.updateQuartilesType(QuartilesType.NONE);
//                massesPlot.updateSpectraSeries(PrideDataType.ALL_SPECTRA);
                break;
            default:
                plot = null;
        }

        drawChart(plot);
    }

    private void createChart(PrideData[] data, PrideChartType type) {
        PrideHistogramDataSource dataSource;

        PrideCategoryPlot plot;
        switch (type) {
            case PEAKS_MS:
                dataSource = new PrideEqualWidthHistogramDataSource(data, false);
                dataSource.appendBins(((PrideEqualWidthHistogramDataSource)dataSource).generateBins(0, 400, 8));
                plot = new PeaksMSPlot(PrideDatasetFactory.getHistogramDataset(dataSource));
                break;
            case PEAK_INTENSITY:
                dataSource = new PrideHistogramDataSource(data, true);
                dataSource.appendBin(new PrideHistogramBin(1, 100));
                dataSource.appendBin(new PrideHistogramBin(100, 1000));
                dataSource.appendBin(new PrideHistogramBin(1000, 2000));
                dataSource.appendBin(new PrideHistogramBin(2000, Integer.MAX_VALUE));
                plot = new PeakIntensityPlot(PrideDatasetFactory.getHistogramDataset(dataSource), PrideDataType.ALL_SPECTRA);

                PeakIntensityPlot peakPlot = (PeakIntensityPlot) plot;
                peakPlot.setVisible(true, PrideDataType.IDENTIFIED_SPECTRA);
                peakPlot.setVisible(true, PrideDataType.UNIDENTIFIED_SPECTRA);
                peakPlot.setVisible(false, PrideDataType.ALL_SPECTRA);
                break;
            default:
                plot = null;
        }

        drawChart(plot);
    }

    private PrideData[] generateRangeData(double minY, double maxY, int count) {
        return generateRangeData(minY, maxY, count, null);
    }

    private PrideData[] generateRangeData(double minY, double maxY, int count, PrideDataType type) {
        PrideData[] values = new PrideData[count];

        int high = (int)((maxY - minY) * 1000);

        Random yRandom = new Random();
        double y;
        PrideData value;
        PrideDataType randomType;
        for (int i = 0; i < count; i++) {
            y = minY + yRandom.nextInt(high) / 1000d;
            if (type == null) {
                double tempY = y;
                while (tempY < 10) {
                    tempY *= 10;
                }
                randomType = (int)tempY % 2 == 0 ? PrideDataType.IDENTIFIED_SPECTRA : PrideDataType.UNIDENTIFIED_SPECTRA;
            } else {
                randomType = type;
            }

            value = new PrideData(y, randomType);
            values[i] = value;
        }

        return values;
    }

    private Double[] generateDomainData(double minX, double maxX, int count) {
        Double[] data = new Double[count];

        int step = (int)((maxX - minX) * 1000) / count;
        if (step == 0) {
            if (minX < 0) {
                step = -1;
            } else {
                step = 1;
            }
        }

        double x;
        for (int i = 0; i < count; i++) {
            x = minX + step * i / 1000d;
            data[i] = x;
        }

        return data;
    }

    private Double[] generateDomainData(int minX, int maxX, int count) {
        Double[] data = new Double[count];

        int step = (maxX - minX) / count;
        step = step < 1 ? 1 : step;

        double x;
        for (int i = 0; i < count; i++) {
            x = minX + step * i;
            data[i] = x;
        }

        return data;
    }

    public static void main(String[] args) {
        PridePlotRun run = new PridePlotRun();

        // test XY style plots.
        Double[] domainData;
        PrideData[] rangeData;

        domainData = run.generateDomainData(-0.1, 0.1, 100);
        rangeData = run.generateRangeData(0.2, 1, 100);
        run.createChart(domainData, rangeData, PrideChartType.DELTA_MASS);

        domainData = run.generateDomainData(1, 6, 6);
        rangeData = run.generateRangeData(20, 300, 6);
        run.createChart(domainData, rangeData, PrideChartType.PEPTIDES_PROTEIN);

        domainData = run.generateDomainData(0, 4, 5);
        rangeData = run.generateRangeData(0, 125, 5);
        run.createChart(domainData, rangeData, PrideChartType.MISSED_CLEAVAGES);

        domainData = run.generateDomainData(5, 2000, 40);
        rangeData = run.generateRangeData(200, 300000, 40);
        run.createChart(domainData, rangeData, PrideChartType.AVERAGE_MS);

        domainData = run.generateDomainData(1, 8, 8);
        rangeData = run.generateRangeData(0, 100, 8, PrideDataType.IDENTIFIED_SPECTRA);
        run.createChart(domainData, rangeData, PrideChartType.PRECURSOR_CHARGE);

        domainData = run.generateDomainData(0, 3500, 20);
        rangeData = run.generateRangeData(0, 0.125, 20);
        run.createChart(domainData, rangeData, PrideChartType.PRECURSOR_MASSES);

        // test histogram plots.
        PrideData[] xData;

        xData = run.generateRangeData(0, 3000, 1000);
        run.createChart(xData, PrideChartType.PEAKS_MS);
//
        xData = run.generateRangeData(0, 5000, 1000);
        run.createChart(xData, PrideChartType.PEAK_INTENSITY);
    }
}
