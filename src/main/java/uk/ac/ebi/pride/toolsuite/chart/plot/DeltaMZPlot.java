package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.PrideDataType;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author qingwei
 * Date: 12/06/13
 */
public class DeltaMZPlot extends PrideXYPlot {

    private XYDataset dataset;

    public DeltaMZPlot(XYDataset dataset, PrideDataType datatype) {
        this(dataset, new XYLineAndShapeRenderer(true, false), true, datatype);
    }

    public DeltaMZPlot(XYDataset dataset, XYItemRenderer renderer, boolean smallPlot, PrideDataType dataType) {

        super(PrideChartType.DELTA_MASS, dataset, renderer, smallPlot);

        setDomainZeroBaselineVisible(true);
        setBackgroundAlpha(0f);
        setDomainGridlinePaint(Color.red);
        setRangeGridlinePaint(Color.blue);
        setDomainGridlinesVisible(true);
        setRangeGridlinesVisible(true);

        NumberAxis domainAxis = (NumberAxis) getDomainAxis();
        domainAxis.setAutoTickUnitSelection(true);
        domainAxis.setNumberFormatOverride(new DecimalFormat("#.##"));

        NumberAxis rangeAxis = (NumberAxis) getRangeAxis();
        rangeAxis.setAutoTickUnitSelection(false);
        rangeAxis.setTickUnit(new NumberTickUnit(0.25, new DecimalFormat("0.00")));

        this.dataset = dataset;

        // only display current data type series.
        for (PrideDataType type : dataType.getChildren()) {
            setVisible(false, type);
        }
        setVisible(true, dataType);
    }

    public void setVisible(boolean visible, PrideDataType dataType) {

        XYItemRenderer renderer = (XYItemRenderer) getRenderer();

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            if (dataset.getSeriesKey(i).equals(PrideDataType.IDENTIFIED_SPECTRA)) {
                renderer.setSeriesPaint(i, Color.RED);
            }else if (dataset.getSeriesKey(i).equals(PrideDataType.IDENTIFIED_DECOY)) {
                renderer.setSeriesPaint(i, Color.GREEN);
            }else if (dataset.getSeriesKey(i).equals(PrideDataType.IDENTIFIED_TARGET)) {
                renderer.setSeriesPaint(i, Color.CYAN);
            }
            if (dataset.getSeriesKey(i).equals(dataType.getTitle())) {
                renderer.setSeriesVisible(i, visible);
            }
        }
    }

    @Override
    public Map<PrideDataType, Boolean> getOptionList() {
        Map<PrideDataType, Boolean> optionList = new TreeMap<PrideDataType, Boolean>();

        optionList.put(PrideDataType.IDENTIFIED_SPECTRA, false);
        optionList.put(PrideDataType.IDENTIFIED_DECOY, false);
        optionList.put(PrideDataType.IDENTIFIED_TARGET, false);

        PrideDataType dataType;
        for (int i=0; i < dataset.getSeriesCount(); i++) {
            String key = (String) dataset.getSeriesKey(i);
            dataType = PrideDataType.findBy(key);
            optionList.put(dataType, true);
        }

        return optionList;
    }

    @Override
    public boolean isMultiOptional() {
        return true;
    }

}
