package uk.ac.ebi.pride.toolsuite.chart.plot;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;

import java.awt.*;

/**
 * @author qingwei
 * @author ypriverol
 * Date: 12/06/13
 */
public abstract class PrideXYPlot extends XYPlot implements PridePlot {

    private PrideChartType type;
    private boolean smallPlot;

    protected PrideXYPlot(PrideChartType type, XYDataset dataset, XYItemRenderer renderer, boolean smallPlot) {
        super(dataset, new NumberAxis(type.getDomainLabel()), new NumberAxis(type.getRangeLabel()), renderer);
        this.type = type;

        setOrientation(PlotOrientation.VERTICAL);
        setDomainZeroBaselineVisible(true);
        setBackgroundAlpha(0f);
        setDomainCrosshairPaint(Color.red);
        setRangeCrosshairPaint(Color.blue);
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
