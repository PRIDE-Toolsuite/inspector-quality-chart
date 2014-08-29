package uk.ac.ebi.pride.chart.plot.label;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;

import java.text.DecimalFormat;

/**
 * User: Qingwei
 * Date: 13/06/13
 */
public class XYPercentageLabel implements XYItemLabelGenerator {
    private DecimalFormat numberFormat = new DecimalFormat("#.#");

    // Whether show less than one percent value. If false, the label will display < 1%.
    private boolean onePercent = false;

    public XYPercentageLabel() {
    }

    public String generateLabel(XYDataset dataset, int series, int item) {
        double sum = 0;

        for (int i = 0; i < dataset.getItemCount(series); i++) {
            sum += dataset.getYValue(series, i);
        }

        if (sum == 0) {
            return "";
        }

        double value = dataset.getYValue(series, item);
        double p = (value * 100.0) / sum;

        if (p == 0) {
            return "";
        } else if (p < 1 && !onePercent) {
            return "<1 %";
        } else {
            return numberFormat.format(p) + " %";
        }
    }

    public void setOnePercent(boolean onePercent) {
        this.onePercent = onePercent;
    }
}