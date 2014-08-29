package uk.ac.ebi.pride.chart.plot;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import uk.ac.ebi.pride.chart.PrideChartType;
import uk.ac.ebi.pride.chart.dataset.PrideDataType;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: Qingwei
 * Date: 12/06/13
 */
public class DeltaMZPlot extends PrideXYPlot {
    public DeltaMZPlot(XYDataset dataset) {
        this(dataset, new XYLineAndShapeRenderer(true, false), true);
    }

    public DeltaMZPlot(XYDataset dataset, XYItemRenderer renderer, boolean smallPlot) {
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
    }

    @Override
    public Map<PrideDataType, Boolean> getOptionList() {
        return new TreeMap<PrideDataType, Boolean>();
    }

    @Override
    public boolean isMultiOptional() {
        return false;
    }
}
