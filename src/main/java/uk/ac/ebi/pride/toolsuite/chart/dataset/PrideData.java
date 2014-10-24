package uk.ac.ebi.pride.toolsuite.chart.dataset;

/**
 * @author Qingwei
 * @author ypriverol
 * Date: 17/06/13
 */
public class PrideData implements Comparable<PrideData> {

    private Double data;

    private PrideDataType type;

    private String category;

    public PrideData(Double data) {
        this(data,  PrideDataType.ALL);
    }

    public PrideData(Double data, PrideDataType type) {
        this.data = data;
        this.type = type;
    }

    public PrideData(Double data, String category){
        this(data, PrideDataType.ALL_SPECTRA);
        this.category = category;
    }

    public Double getData() {
        return data;
    }

    public void setData(Double data) {
        this.data = data;
    }

    public PrideDataType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrideData prideData = (PrideData) o;

        if (data != null ? !data.equals(prideData.data) : prideData.data != null) return false;
        if (type != prideData.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(PrideData o) {
        return data.compareTo(o.getData());
    }

    @Override
    public String toString() {
        return "PrideData{" +
                "data=" + data +
                ", type=" + type +
                '}';
    }
}
