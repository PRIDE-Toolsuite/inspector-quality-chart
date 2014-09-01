package uk.ac.ebi.pride.toolsuite.chart.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author qingwei
 * Date: 03/07/13
 */
public class PridePlotUtils {
    public static String getTimeCost(long start, long end) {
        double time = (end - start) / 1000d;
        BigDecimal cost = new BigDecimal(time).setScale(2, RoundingMode.CEILING);
        return cost.toString();
    }
}
