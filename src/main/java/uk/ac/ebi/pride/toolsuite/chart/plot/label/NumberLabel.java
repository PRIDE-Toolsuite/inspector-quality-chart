package uk.ac.ebi.pride.toolsuite.chart.plot.label;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;

import java.text.DecimalFormat;

/**
 * <p>A label generator for the bar chats.</p>
 *
 * @author Antonio Fabregat
 * Date: 12-oct-2010
 * Time: 11:16:27
 */
public class NumberLabel implements XYItemLabelGenerator {
    private DecimalFormat numberFormat;

    public NumberLabel() {
        numberFormat = new DecimalFormat("###");
    }

    public NumberLabel(DecimalFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public String generateLabel(XYDataset dataset, int series, int item) {
        double value = dataset.getYValue(series, item);

        if (value == 0.0) {
            return "";
        } else {
            return numberFormat.format(value);
        }
    }
}
