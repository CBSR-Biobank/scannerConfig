package edu.ualberta.med.scannerconfig;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "edu.ualberta.med.scannerconfig.messages"; //$NON-NLS-1$
    public static String ScannerConfigPlugin_decode_image_error_msg;
    public static String ScannerConfigPlugin_decode_plate_error_msg;
    public static String ScannerConfigPlugin_scan_error_msg;
    public static String ScannerConfigPlugin_scan_flatbed_error_msg;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
