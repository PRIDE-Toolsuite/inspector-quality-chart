package uk.ac.ebi.pride.toolsuite.chart.dataset;

/**
 * @author ypriverol
 */
public class PrideDataCategoryType {

    public static PrideDataCategoryType QUANTITATION_PEPTIDE = new PrideDataCategoryType("Quantative peptide");

    public String key;

    public PrideDataCategoryType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
