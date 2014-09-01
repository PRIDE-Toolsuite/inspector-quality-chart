package uk.ac.ebi.pride.toolsuite.chart;

import java.util.*;

/**
 * @author qingwei
 * Date: 11/06/13
 */
public class PrideChartSummary {
    private TreeSet<PrideChartType> typeSet = new TreeSet<PrideChartType>();

    public PrideChartSummary() {}

    public static PrideChartSummary PROTEIN_SUMMARY = new PrideChartSummary();
    public static PrideChartSummary SPECTRA_SUMMARY = new PrideChartSummary();
    public static PrideChartSummary PROJECT_SUMMARY = new PrideChartSummary();

    static {
        PROTEIN_SUMMARY.add(PrideChartType.PEPTIDES_PROTEIN);

        SPECTRA_SUMMARY.add(PrideChartType.DELTA_MASS);
        SPECTRA_SUMMARY.add(PrideChartType.MISSED_CLEAVAGES);
        SPECTRA_SUMMARY.add(PrideChartType.AVERAGE_MS);
        SPECTRA_SUMMARY.add(PrideChartType.PRECURSOR_CHARGE);
        SPECTRA_SUMMARY.add(PrideChartType.PRECURSOR_MASSES);
        SPECTRA_SUMMARY.add(PrideChartType.PEAKS_MS);
        SPECTRA_SUMMARY.add(PrideChartType.PEAK_INTENSITY);

        PROJECT_SUMMARY.addAll(PROTEIN_SUMMARY);
        PROJECT_SUMMARY.addAll(SPECTRA_SUMMARY);
    }

    public void add(PrideChartType type) {
        typeSet.add(type);
    }

    public void addAll(Collection<PrideChartType> types) {
        typeSet.addAll(types);
    }

    public void addAll(PrideChartSummary summary) {
        addAll(summary.getAll());
    }

    public boolean contains(PrideChartType type) {
        return typeSet.contains(type);
    }

    public PrideChartType get(Integer order) {
        return (PrideChartType) typeSet.toArray()[order - 1];
    }

    public PrideChartType get(String name) {
        PrideChartType type = PrideChartType.valueOf(name);

        if (contains(type)) {
            return type;
        } else {
            return null;
        }
    }

    public Collection<PrideChartType> getAll() {
        return Collections.unmodifiableCollection(typeSet);
    }

    public void remove(PrideChartType type) {
        typeSet.remove(type);
    }

    public void clear() {
        typeSet.clear();
    }
}
