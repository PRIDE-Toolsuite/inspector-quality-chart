package uk.ac.ebi.pride.toolsuite.chart.io;

/**
 * @author qingwei
 * Date: 21/06/13
 */
public class PrideDataException extends Exception {
    public static final String NO_PRE_CHARGE = "The experiment does not contain precursor charge values";
    public static final String NO_PROTEIN_QUANT = "No protein quantitation data reported";
    public static final String NO_MASSES = "The experiment does not contain precursor masses";
    public static final String NO_INTENSITY = "The experiment does not contain intensity values";
    public static final String NO_IDENTIFICATION = "The experiment does not contain identifications data";
    public static final String NO_MASCOT_SCORE = "No Mascot scores found";
    public static final String NO_CORRECT_SCORE = "The scores do not contain correct values";
    public static final String SEQUENCE_ASSOCIATED_ERROR = "Peptide sequences could not be associated to a MS 1 precursor";
    public static final String INVALID_SEQUENCE = "The experiment does not contain valid AminoAcid sequences";
    public static final String NO_SUMMARY = "Summary data could not be retrieved from the experiment";
    public static final String NO_CORRECT_CHARGE = "No correct charges has been found";


    // new error messages.
    public static final String NO_PEPTIDE = "Peptide not found";
    public static final String NO_SPECTRA = "Spectra not found";
    public static final String NO_TANDEM_SPECTRA = "MS/MS Spectra not found";
    public static final String NO_PRECURSOR_CHARGE = "Precursor charge not found";
    public static final String NO_PRECURSOR_MASS = "No correct mass has been found";

    public PrideDataException() {
    }

    public PrideDataException(String message) {
        super(message);
    }

    public PrideDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrideDataException(Throwable cause) {
        super(cause);
    }
}
