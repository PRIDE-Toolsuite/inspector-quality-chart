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
import uk.ac.ebi.pride.utilities.util.Tuple;

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

    private Map<String, Tuple<Boolean, Boolean>> categories;

    public QuantitationChart(XYSeriesCollection dataset, PrideDataCategoryType prideDataType) {
        this(dataset, prideDataType, true);
    }

    public QuantitationChart(XYSeriesCollection dataset, PrideDataCategoryType prideDataType, boolean smallPlot) {
        super(PrideChartType.QUANTITATION_PEPTIDES, dataset, new XYSplineRenderer(), smallPlot);

        this.spectraSeriesList.addAll(dataset.getSeries());

        for(XYSeries serie: spectraSeriesList){
            seriesCollection.addSeries(serie);
        }

        categories = new HashMap<String, Tuple<Boolean, Boolean>>();

        for(int i=0; i < dataset.getSeriesCount(); i++){
            categories.put(dataset.getSeries(i).getKey().toString(), new Tuple<Boolean, Boolean>(true, true));
        }

        //Set first series of the variables
        spectraSeries = getSpectraSeries(prideDataType.getKey());
        if (spectraSeries != null) {
            seriesCollection.addSeries(spectraSeries);
        }

        refresh();
    }

    public Map<String, Tuple<Boolean, Boolean>> getOptionStudyList() {

        Map<String, Tuple<Boolean, Boolean>> optionList = new HashMap<String, Tuple<Boolean, Boolean>>();
        if(categories == null )
            return optionList;
        return categories;

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

    public void updateSpectraSeries(Boolean status, String key) {

        categories.get(key).setValue(status);
        refresh();
    }

    private void refresh() {

        XYSplineRenderer renderer = (XYSplineRenderer) getRenderer();
        int i = 0;
        for (String category: categories.keySet()) {

            if(!categories.get(category).getValue())
               renderer.setSeriesShapesVisible(i, false);
            i++;
            //renderer.setSeriesPaint(i, color);
        }

//        int seriesSize = seriesCollection.getSeries().size();
//        if (seriesSize > 3)
//            renderer.setSeriesVisibleInLegend(seriesSize - 3, false);
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
