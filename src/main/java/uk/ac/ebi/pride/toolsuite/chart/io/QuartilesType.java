package uk.ac.ebi.pride.toolsuite.chart.io;

/**
 * @author Antonio Fabregat
 * @author ypriverol
 * Date: 9-mar-2011
 * Time: 16:30:07
 */
public enum QuartilesType {
    NONE("No quartiles reference", null),
    PRIDE("PRIDE reference", "pridePMDQuartiles.csv"),
    HUMAN("Human reference", "humanPMDQuartiles.csv"),
    MOUSE("Mouse reference", "mousePMDQuartiles.csv");

    private String reference;
    private String fileName;

    QuartilesType(String reference, String fileName) {
        this.reference = reference;
        this.fileName = fileName;
    }

    public String getReference() {
        return reference;
    }

    public String getFileName(){
        return fileName;
    }

    public static QuartilesType getQuartilesType(String label){
        for (QuartilesType type : values()) {
            if(type.getReference().equals(label))
                return type;
        }
        return null;
    }
}
