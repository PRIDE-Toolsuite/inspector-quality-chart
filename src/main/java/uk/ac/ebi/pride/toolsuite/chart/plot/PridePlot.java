package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.data.general.Dataset;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.dataset.PrideDataType;

import java.util.Map;

/**
 * @author qingwei
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
