package uk.ac.ebi.pride.chart.plot;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import uk.ac.ebi.pride.chart.PrideChartType;
import uk.ac.ebi.pride.chart.dataset.PrideDataType;
import uk.ac.ebi.pride.chart.plot.axis.PrideNumberTickUnit;
import uk.ac.ebi.pride.chart.plot.label.XYPercentageLabel;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: Qingwei
 * Date: 12/06/13
 */
public class PeptidesProteinPlot extends PrideXYPlot {
    public PeptidesProteinPlot(XYDataset dataset) {
        this(dataset, true);
    }

    public PeptidesProteinPlot(XYDataset dataset, boolean smallPlot) {
        super(PrideChartType.PEPTIDES_PROTEIN, new XYBarDataset(dataset, 0.5), new XYBarRenderer(), smallPlot);

        XYBarRenderer renderer = (XYBarRenderer) getRenderer();
        renderer.setBaseItemLabelGenerator(new XYPercentageLabel());
        renderer.setBaseItemLabelsVisible(true);

        NumberAxis domainAxis = (NumberAxis) getDomainAxis();
        PrideNumberTickUnit unit = new PrideNumberTickUnit(1, new DecimalFormat("0"));
        int barCount = dataset.getItemCount(0);
        unit.setMaxValue(barCount - 1);
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
