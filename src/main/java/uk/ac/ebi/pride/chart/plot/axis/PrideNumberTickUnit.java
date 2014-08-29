package uk.ac.ebi.pride.chart.plot.axis;

import org.jfree.chart.axis.NumberTickUnit;

import java.text.NumberFormat;

/**
 * This is a boundary number tick unit, which means if the tick value great than the max value
 * the label is "> max"; while the tick value less than the min value, the label is "< min".
 * Others label is "value".
 *
* User: Qingwei
* Date: 13/06/13
*/
public class PrideNumberTickUnit extends NumberTickUnit {
    private int maxValue = Integer.MAX_VALUE;
    private int minValue = Integer.MIN_VALUE;

    public PrideNumberTickUnit(double size) {
        super(size);
    }

    public PrideNumberTickUnit(double size, NumberFormat formatter) {
        super(size, formatter);
    }

    public PrideNumberTickUnit(double size, NumberFormat formatter, int minorTickCount) {
        super(size, formatter, minorTickCount);
    }

    @Override
    public String valueToString(double value) {
        String str = super.valueToString(value);

        if (value == 0) {
            return str;
        }

        if (value > maxValue) {
            str = ">" + maxValue;
        } else if (value < minValue) {
            str = "<" + minValue;
        }

        return str;
    }

    public void setMaxValue(int maxValue) {
        if (maxValue <= this.minValue) {
            throw new IllegalArgumentException("new maxValue " + maxValue + " less than or equals minValue " + minValue);
        }

        this.maxValue = maxValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMinValue(int minValue) {
        if (minValue >= this.maxValue) {
            throw new IllegalArgumentException("new minValue " + minValue + " great than or equals maxValue " + maxValue);
        }

        this.minValue = minValue;
    }

    public int getMinValue() {
        return minValue;
    }
}
