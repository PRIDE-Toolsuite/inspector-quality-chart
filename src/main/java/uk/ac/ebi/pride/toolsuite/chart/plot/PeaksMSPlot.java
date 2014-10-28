package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.PrideDataType;
import uk.ac.ebi.pride.toolsuite.chart.plot.label.CategoryPercentageLabel;

import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

/**
*  @author qingwei
 * @author ypriverol
* Date: 14/06/13
*/
public class PeaksMSPlot extends PrideCategoryPlot {

    CategoryDataset dataset;

    public PeaksMSPlot(CategoryDataset dataset, PrideDataType dataType) {
        this(dataset, true, dataType);
    }

    public PeaksMSPlot(CategoryDataset dataset, boolean smallPlot, PrideDataType dataType) {
        super(PrideChartType.PEAKS_MS, dataset, smallPlot);
        this.dataset = dataset;

        BarRenderer renderer = (BarRenderer) getRenderer();
        renderer.setMaximumBarWidth(0.2);
        renderer.setBaseItemLabelGenerator(new CategoryPercentageLabel());
        renderer.setBaseItemLabelsVisible(true);

        this.dataset = dataset;

        // only display current data type series.
        for (PrideDataType type : dataType.getChildren()) {
            setVisible(false, type);
        }
        setVisible(true, dataType);
    }

    public void setVisible(boolean visible, PrideDataType dataType) {
        BarRenderer renderer = (BarRenderer) getRenderer();

        for (int i = 0; i < dataset.getRowCount(); i++) {
            if (dataset.getRowKey(i).equals(PrideDataType.ALL_SPECTRA)) {
                renderer.setSeriesPaint(i, Color.BLUE);
            } else if (dataset.getRowKey(i).equals(PrideDataType.IDENTIFIED_SPECTRA)) {
                renderer.setSeriesPaint(i, Color.RED);
            } else if (dataset.getRowKey(i).equals(PrideDataType.UNIDENTIFIED_SPECTRA)) {
                renderer.setSeriesPaint(i, Color.BLACK);
            }else if (dataset.getRowKey(i).equals(PrideDataType.IDENTIFIED_DECOY)) {
                renderer.setSeriesPaint(i, Color.GREEN);
            }else if (dataset.getRowKey(i).equals(PrideDataType.IDENTIFIED_TARGET)) {
                renderer.setSeriesPaint(i, Color.CYAN);
            }
            if (dataset.getRowKey(i).equals(dataType.getTitle())) {
                renderer.setSeriesVisible(i, visible);
            }
        }
    }

    @Override
    public Map<PrideDataType, Boolean> getOptionList() {
        Map<PrideDataType, Boolean> optionList = new TreeMap<PrideDataType, Boolean>();

        optionList.put(PrideDataType.IDENTIFIED_SPECTRA, false);
        optionList.put(PrideDataType.UNIDENTIFIED_SPECTRA, false);
        optionList.put(PrideDataType.ALL_SPECTRA, false);
        optionList.put(PrideDataType.IDENTIFIED_DECOY, false);
        optionList.put(PrideDataType.IDENTIFIED_TARGET, false);

        PrideDataType dataType;
        for (Object key : dataset.getRowKeys()) {
            dataType = PrideDataType.findBy((String) key);
            optionList.put(dataType, true);
        }

        return optionList;
    }

    @Override
    public boolean isMultiOptional() {
        return true;
    }


}
