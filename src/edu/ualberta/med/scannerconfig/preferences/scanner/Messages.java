package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "edu.ualberta.med.scannerconfig.preferences.scanner.messages"; //$NON-NLS-1$
    public static String Decoding_cell_distance;
    public static String Decoding_corrections;
    public static String Decoding_debug_level;
    public static String Decoding_edge_treshold;
    public static String Decoding_scan_gap;
    public static String Decoding_square_deviation;
    public static String Scanner_brightness_label;
    public static String Scanner_contrast_label;
    public static String Scanner_driver_type_label;
    public static String Scanner_scann_error_title;
    public static String Scanner_select_label;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
