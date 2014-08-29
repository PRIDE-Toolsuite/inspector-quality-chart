package uk.ac.ebi.pride.chart.dataset;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Extend org.jfree.data.statistics.PrideHistogramBin, add hashCode and comparable capability.
 *
 * Notice: Not exists overlap between different PrideHistogramBin.
 *
 * User: qingwei
 * Date: 19/06/13
 */
public class PrideHistogramBin implements Cloneable, Serializable, Comparable<PrideHistogramBin> {
    /** For serialization. */
    private static final long serialVersionUID = 2791734099087438571L;

    /** The number of items in the bin. */
    private int count;

    /** The start boundary. */
    private double startBoundary;

    /** The end boundary. */
    private double endBoundary;

    /**
     * Creates a new bin.
     *
     * @param startBoundary  the start boundary.
     * @param endBoundary  the end boundary.
     */
    public PrideHistogramBin(double startBoundary, double endBoundary) {
        if (startBoundary >= endBoundary) {
            throw new IllegalArgumentException(
                    "PrideHistogramBin():  startBoundary >= endBoundary.");
        }
        this.count = 0;
        this.startBoundary = startBoundary;
        this.endBoundary = endBoundary;
    }

    /**
     * Returns the number of items in the bin.
     *
     * @return The item count.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Increments the item count.
     */
    public void incrementCount() {
        this.count++;
    }

    /**
     * Returns the start boundary.
     *
     * @return The start boundary.
     */
    public double getStartBoundary() {
        return this.startBoundary;
    }

    public void setStartBoundary(double startBoundary) {
        this.startBoundary = startBoundary;
    }

    /**
     * Returns the end boundary.
     *
     * @return The end boundary.
     */
    public double getEndBoundary() {
        return this.endBoundary;
    }

    public void setEndBoundary(double endBoundary) {
        this.endBoundary = endBoundary;
    }

    public double getMedian() {
        return startBoundary + getBinWidth() / 2;
    }

    /**
     * Returns the bin width.
     *
     * @return The bin width.
     */
    public double getBinWidth() {
        return this.endBoundary - this.startBoundary;
    }

    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the object to test against.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof PrideHistogramBin) {
            PrideHistogramBin bin = (PrideHistogramBin) obj;
            boolean b0 = bin.startBoundary == this.startBoundary;
            boolean b1 = bin.endBoundary == this.endBoundary;
            boolean b2 = bin.count == this.count;
            return b0 && b1 && b2;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = count;
        temp = startBoundary != +0.0d ? Double.doubleToLongBits(startBoundary) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = endBoundary != +0.0d ? Double.doubleToLongBits(endBoundary) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Returns a clone of the bin.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException not thrown by this class.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int compareTo(PrideHistogramBin o) {
        double v = this.startBoundary - o.startBoundary;
        if (v > 0) {
            return 1;
        } else if (v < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return startBoundary + "-" + endBoundary;
    }

    public String toString(DecimalFormat format) {
        return format.format(startBoundary) + "-" + format.format(endBoundary);
    }
}
