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
import uk.ac.ebi.pride.toolsuite.chart.dataset.*;
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

    private XYSeries spectraSeries;

    private Collection<String> categories;

    public QuantitationChart(XYSeriesCollection dataset, PrideDataCategoryType prideDataType) {
        this(dataset, prideDataType, true);
    }

    public QuantitationChart(XYSeriesCollection dataset, PrideDataCategoryType prideDataType, boolean smallPlot) {
        super(PrideChartType.QUANTITATION_PEPTIDES, dataset, new XYSplineRenderer(), smallPlot);

        this.spectraSeriesList.addAll(dataset.getSeries());

        for(XYSeries serie: spectraSeriesList){
            seriesCollection.addSeries(serie);
        }

        categories = new ArrayList<String>();

        for(int i=0; i < dataset.getSeriesCount(); i++){
            categories.add(dataset.getSeries(i).getKey().toString());
        }

        //Set first series of the variables
        spectraSeries = getSpectraSeries(prideDataType.getKey());
        if (spectraSeries != null) {
            seriesCollection.addSeries(spectraSeries);
        }

        refresh();
    }

    public Map<String, Boolean> getOptionStudyList() {

        Map<String, Boolean> optionList = new TreeMap<String, Boolean>();
        for(String category: categories)
            optionList.put(category,true);
        return optionList;
    }

    @Override
    public boolean isMultiOptional() {
        return true;
    }

    private XYSeries getSpectraSeries(String key) {
        for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
            if (seriesCollection.getSeries(i).getKey().equals(key)) {
                return seriesCollection.getSeries(i);
            }
        }
        return null;
    }

    public void updateSpectraSeries(String key) {
        if (spectraSeries.getKey().equals(key)) {
            return;
        }

        XYSeries series = getSpectraSeries(key);
        if (series == null) {
            // can not find series in internal spectra series list.
            return;
        }

        seriesCollection.removeSeries(spectraSeries);
        spectraSeries = series;
        seriesCollection.addSeries(spectraSeries);
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

            // setting QuartilesType.NONE color

            color = Color.RED;


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

    @Override
    public Map<PrideDataType, Boolean> getOptionList() {
        return Collections.emptyMap();
    }

    public static PrideDataCategoryType getDefaultCategory(PrideXYDataSource dataSource){
        if (dataSource != null && !dataSource.getCategoryDataType().isEmpty()){
            return new PrideDataCategoryType(dataSource.getCategoryDataType().iterator().next());
        }
        return null;
    }
}
