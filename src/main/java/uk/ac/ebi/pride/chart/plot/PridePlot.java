package uk.ac.ebi.pride.chart.plot;

import org.jfree.data.general.Dataset;
import uk.ac.ebi.pride.chart.PrideChartType;
import uk.ac.ebi.pride.chart.dataset.PrideDataType;

import java.util.Map;

/**
 * User: Qingwei
 * Date: 12/06/13
 */
public interface PridePlot {
    public PrideChartType getType();

    public boolean isSmallPlot();

    public String getTitle();

    public String getFullTitle();

    public String getDomainLabel();

    public String getRangeLabel();

    public boolean isLegend();

    public Map<PrideDataType, Boolean> getOptionList();

    public boolean isMultiOptional();
}
