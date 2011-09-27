package edu.ualberta.med.scannerconfig.preferences.scanner.plateposition;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.messages"; //$NON-NLS-1$
    public static String PlateImageMgr_scanner_drive_selection_error;
    public static String PlateImageMgr_scanner_drive_selection_msg;
    public static String PlateImageMgr_scanner_error_title;
    public static String PlateSettings_align_label;
    public static String PlateSettings_barcode_label;
    public static String PlateSettings_barcode_label_bis;
    public static String PlateSettings_bottom_label;
    public static String PlateSettings_enable_check_label;
    public static String PlateSettings_gap_horizontal_label;
    public static String PlateSettings_gap_vertical_label;
    public static String PlateSettings_left_label;
    public static String PlateSettings_not_enabled_msg;
    public static String PlateSettings_orientation_label;
    public static String PlateSettings_refresh_label;
    public static String PlateSettings_right_label;
    public static String PlateSettings_scan_button_label;
    public static String PlateSettings_scan_status_msg;
    public static String PlateSettings_top_label;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
