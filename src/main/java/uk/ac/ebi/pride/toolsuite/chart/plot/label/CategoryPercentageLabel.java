package uk.ac.ebi.pride.toolsuite.chart.plot.label;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

import java.text.DecimalFormat;

/**
 * @author qingwei
 * Date: 19/06/13
 */
public class CategoryPercentageLabel extends StandardCategoryItemLabelGenerator {
    private DecimalFormat numberFormat = null;

    public CategoryPercentageLabel() {
        this(new DecimalFormat("#.#"));
    }

    public CategoryPercentageLabel(DecimalFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public String generateLabel(CategoryDataset dataset, int row, int column) {
        long sum = 0;
        for (int i = 0; i < dataset.getColumnCount(); i++) {
            sum += dataset.getValue(row, i).longValue();
        }

        int value = dataset.getValue(row, column).intValue();
        double p = (value * 100.0) / (double) sum;

        if (p == 0) {
            return "";
        } else if (p < 1) {
            return "<1 %";
        } else if (numberFormat == null) {
            int out = (int) Math.round(p);
            return out + " %";
        } else {
            return numberFormat.format(p) + " %";
        }
    }
}
