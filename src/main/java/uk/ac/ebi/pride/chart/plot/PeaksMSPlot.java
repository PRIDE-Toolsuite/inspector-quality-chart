package uk.ac.ebi.pride.chart.plot;

import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import uk.ac.ebi.pride.chart.PrideChartType;
import uk.ac.ebi.pride.chart.dataset.PrideDataType;
import uk.ac.ebi.pride.chart.plot.label.CategoryPercentageLabel;

import java.util.Map;
import java.util.TreeMap;

/**
* User: Qingwei
* Date: 14/06/13
*/
public class PeaksMSPlot extends PrideCategoryPlot {
    public PeaksMSPlot(CategoryDataset dataset) {
        this(dataset, true);
    }

    public PeaksMSPlot(CategoryDataset dataset, boolean smallPlot) {
        super(PrideChartType.PEAKS_MS, dataset, smallPlot);

        BarRenderer renderer = (BarRenderer) getRenderer();
        renderer.setBaseItemLabelGenerator(new CategoryPercentageLabel());
        renderer.setBaseItemLabelsVisible(true);
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
