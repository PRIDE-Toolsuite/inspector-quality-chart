package uk.ac.ebi.pride.chart.dataset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Qingwei
 * Date: 12/06/13
 */
public class PrideXYDataSource implements PrideDataSource {
    private Double[] domainData;
    private PrideData[] rangeData;
    private PrideDataType dataType;   // data source data type.

    private Set<PrideDataType> dataTypeList = new HashSet<PrideDataType>();   // list all data type which stored in current data source.

    public PrideXYDataSource(Double[] domainData, PrideData[] rangeData, PrideDataType dataType) {
        if (domainData.length != rangeData.length) {
            throw new IllegalArgumentException("Input data not correct!");
        }

        for (PrideData value : rangeData) {
            if (! value.getType().compatible(dataType)) {
                throw new IllegalArgumentException("There exists incompatible value " + value + " in range array!");
            }
        }

        this.dataType = dataType;
        dataTypeList.add(dataType);
        this.domainData = domainData;
        this.rangeData = rangeData;
    }

    /**
     * @return list all data type which stored in current data source.
     */
    public Set<PrideDataType> getDataTypeList() {
        return dataTypeList;
    }

    public Double[] getDomainData() {
        return domainData;
    }

    public PrideData[] getRangeData() {
        return rangeData;
    }

    public PrideDataType getDataType() {
        return dataType;
    }

    public PrideXYDataSource filter(PrideDataType type) {
        if (! this.dataType.compatible(type)) {
            // current data source type not compatible with filter type. return empty.
            return null;
        }

        List<Double> filterDomainData = new ArrayList<Double>();
        List<PrideData> filterRangeData = new ArrayList<PrideData>();

        PrideData data;
        for (int i = 0; i < rangeData.length; i++) {
            data = rangeData[i];
            if (data.getType().equals(type)) {
                filterDomainData.add(domainData[i]);
                filterRangeData.add(rangeData[i]);
            }
        }

        int size = filterDomainData.size();
        if (size == 0) {
            return null;
        } else {
            return new PrideXYDataSource(
                    filterDomainData.toArray(new Double[size]),
                    filterRangeData.toArray(new PrideData[size]),
                    type
            );
        }
    }
}
