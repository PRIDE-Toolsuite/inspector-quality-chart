package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.PrideDataType;
import uk.ac.ebi.pride.toolsuite.chart.plot.axis.PrideNumberTickUnit;
import uk.ac.ebi.pride.toolsuite.chart.plot.label.XYPercentageLabel;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author qingwei
 * Date: 14/06/13
 */
public class MissedCleavagesPlot extends PrideXYPlot {

    XYDataset dataset;

    public MissedCleavagesPlot(XYDataset dataset, PrideDataType dataType) {
        this(dataset, true, dataType);
    }

    public MissedCleavagesPlot(XYDataset dataset, boolean smallPlot, PrideDataType dataType) {

        super(PrideChartType.MISSED_CLEAVAGES, new XYBarDataset(dataset, 0.2), new XYBarRenderer(), smallPlot);

        this.dataset = dataset;

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

        // only display current data type series.
        for (PrideDataType type : dataType.getChildren()) {
            setVisible(false, type);
        }
        setVisible(true, dataType);
    }

    public void setVisible(boolean visible, PrideDataType dataType) {

        XYBarRenderer renderer = (XYBarRenderer) getRenderer();

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
