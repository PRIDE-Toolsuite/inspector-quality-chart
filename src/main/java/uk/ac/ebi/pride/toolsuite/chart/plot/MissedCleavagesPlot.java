package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.PrideDataType;
import uk.ac.ebi.pride.toolsuite.chart.plot.axis.PrideNumberTickUnit;
import uk.ac.ebi.pride.toolsuite.chart.plot.label.XYPercentageLabel;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author qingwei
 * Date: 14/06/13
 */
public class MissedCleavagesPlot extends PrideXYPlot {
    public MissedCleavagesPlot(XYDataset dataset) {
        this(dataset, true);
    }

    public MissedCleavagesPlot(XYDataset dataset, boolean smallPlot) {
        super(PrideChartType.MISSED_CLEAVAGES, new XYBarDataset(dataset, 0.5), new XYBarRenderer(), smallPlot);

        setDomainZeroBaselineVisible(false);

        XYBarRenderer renderer = (XYBarRenderer) getRenderer();
        renderer.setBaseItemLabelGenerator(new XYPercentageLabel());
        renderer.setBaseItemLabelsVisible(true);

        NumberAxis domainAxis = (NumberAxis) getDomainAxis();
        PrideNumberTickUnit unit = new PrideNumberTickUnit(1, new DecimalFormat("0"));
        int barCount = dataset.getItemCount(0);
        unit.setMaxValue(barCount - 2);
        domainAxis.setTickUnit(unit);

        NumberAxis rangeAxis = (NumberAxis) getRangeAxis();
        rangeAxis.setMinorTickCount(barCount);
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
