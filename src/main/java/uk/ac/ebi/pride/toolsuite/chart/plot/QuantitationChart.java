package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.CategorySetHistogramDataSource;
import uk.ac.ebi.pride.toolsuite.chart.dataset.PrideDataType;
import uk.ac.ebi.pride.toolsuite.chart.io.QuartilesReader;
import uk.ac.ebi.pride.toolsuite.chart.io.QuartilesType;
import uk.ac.ebi.pride.toolsuite.chart.plot.axis.PrideNumberTickUnit;
import uk.ac.ebi.pride.toolsuite.chart.plot.label.CategoryPercentageLabel;
import uk.ac.ebi.pride.toolsuite.chart.plot.label.XYPercentageLabel;

import java.awt.*;
import java.awt.List;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author ypriverol
 */
public class QuantitationChart extends PrideXYPlot {
    // pre-store all spectra series based on input dataset.
    private java.util.List<XYSeries> spectraSeriesList = new ArrayList<XYSeries>();

    // store all series displayed on the plot.
    private XYSeriesCollection seriesCollection = new XYSeriesCollection();

    // used to store the special spectral series.
    private XYSeries spectraSeries;

    private java.util.List<XYSeries> quartilesSeries;

    private QuartilesType quartilesType;

    private java.util.List<XYSeries> noneQuartilesSeries;
    private java.util.List<XYSeries> humanQuartilesSeries;
    private java.util.List<XYSeries> mouseQuartilesSeries;
    private java.util.List<XYSeries> prideQuartilesSeries;

    public QuantitationChart(XYSeriesCollection dataset, PrideDataType spectraType) {
        this(dataset, spectraType, QuartilesType.NONE, true);
    }

    public QuantitationChart(XYSeriesCollection dataset, PrideDataType spectraType, QuartilesType quartilesType, boolean smallPlot) {
        super(PrideChartType.QUANTITATION_PEPTIDES, dataset, new XYSplineRenderer(), smallPlot);
        this.spectraSeriesList.addAll(dataset.getSeries());

        noneQuartilesSeries = new ArrayList<XYSeries>();
        humanQuartilesSeries = getQuartilesSeries(QuartilesType.HUMAN);
        mouseQuartilesSeries = getQuartilesSeries(QuartilesType.MOUSE);
        prideQuartilesSeries = getQuartilesSeries(QuartilesType.PRIDE);

        spectraSeries = getSpectraSeries(spectraType);
        if (spectraSeries != null) {
            seriesCollection.addSeries(spectraSeries);
        }

        switch (quartilesType) {
            case HUMAN:
                this.quartilesSeries = humanQuartilesSeries;
                break;
            case MOUSE:
                this.quartilesSeries = mouseQuartilesSeries;
                break;
            case PRIDE:
                this.quartilesSeries = prideQuartilesSeries;
                break;
            default:
                this.quartilesSeries = noneQuartilesSeries;
        }

        for (XYSeries series : quartilesSeries) {
            seriesCollection.addSeries(series);
        }
        this.quartilesType = quartilesType;

        refresh();
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

    private XYSeries getSpectraSeries(PrideDataType dataType) {
        String seriesKey = dataType.getTitle();

        for (XYSeries series : spectraSeriesList) {
            if (series.getKey().equals(seriesKey)) {
                return series;
            }
        }
        return null;
    }

    public void updateSpectraSeries(PrideDataType dateType) {
        if (spectraSeries.getKey().equals(dateType.getTitle())) {
            return;
        }

        XYSeries series = getSpectraSeries(dateType);
        if (series == null) {
            // can not find series in internal spectra series list.
            return;
        }

        seriesCollection.removeSeries(spectraSeries);
        spectraSeries = series;
        seriesCollection.addSeries(spectraSeries);
        refresh();
    }

    private java.util.List<XYSeries> getQuartilesSeries(QuartilesType type) {
        java.util.List<XYSeries> series = new ArrayList<XYSeries>();

        if (type == QuartilesType.NONE) {
            return series;
        }

        double upperMargin = getDomainAxis().getUpperBound();
        double point;
        XYSeries q1 = new XYSeries("Quartiles"); //Q1
        XYSeries q2 = new XYSeries(type.getReference()); //Q2
        XYSeries q3 = new XYSeries("Quartiles"); //Q3

        QuartilesReader reader = new QuartilesReader(type);
        for (int j = 0; j < reader.size(); j++) {
            point = reader.getPoints().get(j);
            if (point > upperMargin) {
                break;
            }
            try {
                q1.add(point, reader.getQ1Values().get(j));
                q2.add(point, reader.getQ2Values().get(j));
                q3.add(point, reader.getQ3Values().get(j));
            } catch (IndexOutOfBoundsException e) {
                q1.add(point, new Double(0.0));
                q2.add(point, new Double(0.0));
                q3.add(point, new Double(0.0));
            }
        }
        series.add(q1);
        series.add(q2);
        series.add(q3);

        return series;
    }

    public void updateQuartilesType(QuartilesType type) {
        if (this.quartilesType == type) {
            return;
        }

        for (XYSeries series : quartilesSeries) {
            seriesCollection.removeSeries(series);
        }

        switch (type) {
            case HUMAN:
                this.quartilesSeries = humanQuartilesSeries;
                break;
            case MOUSE:
                this.quartilesSeries = mouseQuartilesSeries;
                break;
            case PRIDE:
                this.quartilesSeries = prideQuartilesSeries;
                break;
            default:
                this.quartilesSeries = noneQuartilesSeries;
        }

        for (XYSeries series : quartilesSeries) {
            seriesCollection.addSeries(series);
        }
        this.quartilesType = type;

        refresh();
    }

    private void refresh() {
        setDataset(seriesCollection);
        XYSplineRenderer renderer = (XYSplineRenderer) getRenderer();

        String seriesKey;
        Color color;
        for (int i = 0; i < getSeriesCount(); i++) {
            renderer.setSeriesShapesVisible(i, false);
            seriesKey = (String) getDataset().getSeriesKey(i);

            if (seriesKey.equals(PrideDataType.IDENTIFIED_SPECTRA.getTitle()) ||
                    seriesKey.equals(PrideDataType.UNIDENTIFIED_SPECTRA.getTitle()) ||
                    seriesKey.equals(PrideDataType.ALL_SPECTRA.getTitle())) {
                color = Color.RED;
            } else if (seriesKey.equals(QuartilesType.HUMAN.getReference())) {
                color = new Color(70,130,180);
            } else if (seriesKey.equals(QuartilesType.MOUSE.getReference())) {
                color = new Color(95,158,160);
            } else if (seriesKey.equals(QuartilesType.PRIDE.getReference())) {
                color = new Color(0,0,255);
            } else if (seriesKey.equals("Quartiles")) {
                color = new Color(95,158,160);
            } else {
                // setting QuartilesType.NONE color
                color = Color.WHITE;
            }

            renderer.setSeriesPaint(i, color);
        }

        int seriesSize = seriesCollection.getSeries().size();
        if (seriesSize > 3)
            renderer.setSeriesVisibleInLegend(seriesSize - 3, false);
    }

    public void setDomainUnitSize(double domainUnitSize) {
        NumberAxis domainAxis = (NumberAxis) getDomainAxis();
        domainAxis.setTickUnit(new NumberTickUnit(domainUnitSize, new DecimalFormat("###,###")));
    }

    public void setRangeUnitSize(double rangeUnitSize) {
        NumberAxis rangeAxis = (NumberAxis) getRangeAxis();
        rangeAxis.setTickUnit(new NumberTickUnit(rangeUnitSize, new DecimalFormat("0.000")));
    }


}
