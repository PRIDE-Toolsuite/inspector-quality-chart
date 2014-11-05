package uk.ac.ebi.pride.toolsuite.chart.dataset;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author qingwei
 * @author ypriverol
 * Date: 14/06/13
 */
public enum PrideDataType {
    ALL                  (0xFFFF, "All"),
    ALL_SPECTRA          (0x0007, "All Spectra"),              //0011 = 0010 | 0001
    IDENTIFIED_SPECTRA   (0x0003, "Identified Spectra"),       //0010
    IDENTIFIED_DECOY     (0x0001, "Decoy Identification"),
    IDENTIFIED_TARGET    (0x0002, "Target Identification"),
    UNIDENTIFIED_SPECTRA (0x0004, "Unidentified Spectra");     //0001

    private String title;
    private int id;


    private PrideDataType(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public static PrideDataType findBy(int id) {
        PrideDataType type;
        switch (id) {
            case 0xFFFF:
                type = ALL;
                break;
            case 0x0007:
                type = ALL_SPECTRA;
                break;
            case 0x0003:
                type = IDENTIFIED_SPECTRA;
                break;
            case 0x0004:
                type = UNIDENTIFIED_SPECTRA;
                break;
            case 0x0002:
                type = IDENTIFIED_TARGET;
                break;
            case 0x0001:
                type = IDENTIFIED_DECOY;
                break;
            default:
                throw new IllegalArgumentException("Can not find PrideDataType.");
        }

        return type;
    }

    public static PrideDataType findBy(String title) {
        if (title.equalsIgnoreCase(ALL.title)) {
            return ALL;
        } else if (title.equalsIgnoreCase(ALL_SPECTRA.title)) {
            return ALL_SPECTRA;
        } else if (title.equalsIgnoreCase(IDENTIFIED_SPECTRA.title)) {
            return IDENTIFIED_SPECTRA;
        } else if (title.equalsIgnoreCase(UNIDENTIFIED_SPECTRA.title)) {
            return UNIDENTIFIED_SPECTRA;
        } else if(title.equalsIgnoreCase(IDENTIFIED_DECOY.title)){
            return IDENTIFIED_DECOY;
        }else if(title.equalsIgnoreCase(IDENTIFIED_TARGET.title)){
            return IDENTIFIED_TARGET;
        }
        return null;
        
    }



    /**
     * Check current data type compatible type or not.
     * For example: ALL_SPECTRA compatible with IDENTIFIED_SPECTRA.
     */
    public boolean compatible(PrideDataType type) {
        return (this.id & type.getId()) != 0;
    }

    /**
     * Check the target type is child of current type.
     */
    public boolean isChild(PrideDataType target) {
        return ((this.id & target.getId()) == target.getId() && this != target);
    }

    /**
     * Check the target type is the parent of current type.
     */
    public boolean isParent(PrideDataType target) {
        return ((this.id | target.getId()) == target.id && this != target);
    }

    public Collection<PrideDataType> getChildren() {
        Collection<PrideDataType> typeList = new ArrayList<PrideDataType>();

        for (PrideDataType type : values()) {
            if (isChild(type)) {
                typeList.add(type);
            }
        }

        return typeList;
    }

    public Collection<PrideDataType> getParents() {
        Collection<PrideDataType> typeList = new ArrayList<PrideDataType>();

        for (PrideDataType type : values()) {
            if (isParent(type)) {
                typeList.add(type);
            }
        }

        return typeList;
    }

    public boolean isLeaf() {
        return getChildren().size() == 0;
    }

    public boolean isRoot() {
        return getParents().size() == 0;
    }

    @Override
    public String toString() {
        return title;
    }
}
