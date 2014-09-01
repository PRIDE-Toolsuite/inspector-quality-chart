package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.PrideDataType;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * @author qingwei
 * Date: 14/06/13
 */
public class AverageMSPlot extends PrideXYPlot {
    // store all series displayed on the plot.
    private XYSeriesCollection seriesCollection = new XYSeriesCollection();

    // pre-store all spectra series based on input dataset.
    private List<XYSeries> spectraSeriesList = new ArrayList<XYSeries>();

    // used to store the special spectral series.
    private XYSeries spectraSeries;

    public AverageMSPlot(XYSeriesCollection dataset, PrideDataType spectraType) {
        this(dataset, spectraType, true);
    }

    public AverageMSPlot(XYSeriesCollection dataset, PrideDataType spectraType, boolean smallPlot) {
        super(PrideChartType.AVERAGE_MS, dataset, new XYLineAndShapeRenderer(true, false), smallPlot);

        setDomainZeroBaselineVisible(false);
        getDomainAxis().setLowerBound(0);

        XYSeries series;
        for (Object o : dataset.getSeries()) {
            series = (XYSeries) o;
            if (series.getItemCount() != 0) {
                this.spectraSeriesList.add(series);
            }
        }

        spectraSeries = getSpectraSeries(spectraType);
        if (spectraSeries != null) {
            seriesCollection.addSeries(spectraSeries);
        }

        setDataset(seriesCollection);
    }

    private XYSeries getSpectraSeries(PrideDataType dataType) {
        String seriesKey = dataType.getTitle();

        for (XYSeries series : spectraSeriesList) {
            if (series.getKey().equals(seriesKey)) {
                return series;
            }
        }
        return null;
    }

    public void updateSpectraSeries(PrideDataType dataType) {
        if (spectraSeries.getKey().equals(dataType.getTitle())) {
            return;
        }

        XYSeries series = getSpectraSeries(dataType);
        if (series == null) {
            // can not find series in internal spectra series list.
            return;
        }

        seriesCollection.removeSeries(spectraSeries);
        spectraSeries = series;
        seriesCollection.addSeries(spectraSeries);

        setDataset(seriesCollection);
    }

    public void setDomainUnitSize(double domainUnitSize) {
        NumberAxis domainAxis = (NumberAxis) getDomainAxis();
        domainAxis.setTickUnit(new NumberTickUnit(domainUnitSize, new DecimalFormat("###,###")));
    }

    public void setRangeUnitSize(double rangeUnitSize) {
        NumberAxis rangeAxis = (NumberAxis) getRangeAxis();
        rangeAxis.setTickUnit(new NumberTickUnit(rangeUnitSize, new DecimalFormat("#,###,###")));
    }

    public Map<PrideDataType, Boolean> getOptionList() {
        Map<PrideDataType, Boolean> optionList = new TreeMap<PrideDataType, Boolean>();

        optionList.put(PrideDataType.IDENTIFIED_SPECTRA, false);
        optionList.put(PrideDataType.UNIDENTIFIED_SPECTRA, false);
        optionList.put(PrideDataType.ALL_SPECTRA, false);

        PrideDataType dataType;
        for (XYSeries series : spectraSeriesList) {
            dataType = PrideDataType.findBy((String) series.getKey());
            optionList.put(dataType, true);
        }

        return optionList;
    }

    @Override
    public boolean isMultiOptional() {
        return false;
    }
}
