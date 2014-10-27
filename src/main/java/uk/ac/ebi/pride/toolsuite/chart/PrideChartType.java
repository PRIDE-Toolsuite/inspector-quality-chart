package uk.ac.ebi.pride.toolsuite.chart;

/**
 * @author qingwei
 * Date: 12/06/13
 */
public enum PrideChartType {
    DELTA_MASS        ("Delta m/z",                    "Delta m/z",                                         "Experimental m/z - Theoretical m/z",    "Relative Frequency",  false),
    PEPTIDES_PROTEIN  ("Peptides per Protein",         "Number of Peptides Identified per Protein",         "Number of Peptides",                    "Frequency",           false),
    MISSED_CLEAVAGES  ("Missed Tryptic Cleavages",      "Number of Missed Tryptic Cleavages",               "Missed Cleavages",                      "Frequency",           false),
    AVERAGE_MS        ("Average MS/MS Spectrum",       "Average MS/MS Spectrum",                            "m/z",                                   "Intensity",           true),
    PRECURSOR_CHARGE  ("Precursor Ion Charge",         "Precursor Ion Charge Distribution",                 "Precursor Ion Charge",                  "Frequency",           true),
    PRECURSOR_MASSES  ("Precursor Ion Masses",         "Distribution of Precursor Ion Masses",              "Mass (Daltons)",                        "Relative Frequency",  true),
    PEAKS_MS          ("Peaks per MS/MS Spectrum",     "Number of Peaks per MS/MS Spectrum",                "Number of Peaks",                       "Frequency",           false),
    PEAK_INTENSITY    ("Peak Intensity",               "Peak Intensity Distribution",                       "Intensity",                             "Frequency",           true),
    QUANTITATION_PEPTIDES("Number of Peptides by Ratio", "Number of Peptides by Ratio",                     "Ratio",                    "Number of Peptides",           true),
    ;

    private String title;
    private String fullTitle;
    private String domainLabel;
    private String rangeLabel;
    private boolean legend;

    private PrideChartType(String title, String fullTitle, String domainLabel, String rangeLabel, boolean legend) {
        this.title = title;
        this.fullTitle = fullTitle;
        this.domainLabel = domainLabel;
        this.rangeLabel = rangeLabel;
        this.legend = legend;
    }

    public String getTitle() {
        return title;
    }

    public String getFullTitle() {
        return fullTitle;
    }

    public String getDomainLabel() {
        return domainLabel;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public boolean isLegend() {
        return legend;
    }

    public PrideChartType previous() {
        int index = ordinal();
        int previous = index - 1 < 0 ? 0 : index - 1;

        return values()[previous];
    }

    public PrideChartType next() {
        int index = ordinal();
        int next = index + 1 < values().length ? index + 1 : index;

        return values()[next];
    }
}
