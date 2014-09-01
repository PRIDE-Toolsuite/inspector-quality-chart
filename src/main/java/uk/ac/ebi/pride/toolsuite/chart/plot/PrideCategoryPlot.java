package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;

import java.awt.*;

/**
 * @author qingwei
 * Date: 19/06/13
 */
public abstract class PrideCategoryPlot extends CategoryPlot implements PridePlot {
    private PrideChartType type;
    private boolean smallPlot;

    protected PrideCategoryPlot(PrideChartType type, CategoryDataset dataset, boolean smallPlot) {
        super(dataset, new CategoryAxis(type.getDomainLabel()), new NumberAxis(type.getRangeLabel()), new BarRenderer());
        this.type = type;

        setOrientation(PlotOrientation.VERTICAL);
        setBackgroundAlpha(0f);
        setDomainGridlinePaint(Color.red);
        setRangeGridlinePaint(Color.blue);
        setDomainGridlinesVisible(true);
        setRangeGridlinesVisible(true);
        getRangeAxis().setUpperMargin(0.15);

        this.smallPlot = smallPlot;
    }

    @Override
    public boolean isSmallPlot() {
        return smallPlot;
    }

    @Override
    public PrideChartType getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return type.getTitle();
    }

    @Override
    public String getFullTitle() {
        return type.getFullTitle();
    }

    @Override
    public String getDomainLabel() {
        return type.getDomainLabel();
    }

    @Override
    public String getRangeLabel() {
        return type.getRangeLabel();
    }

    @Override
    public boolean isLegend() {
        return type.isLegend();
    }
}
