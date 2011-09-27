package edu.ualberta.med.scannerconfig.handler;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "edu.ualberta.med.scannerconfig.handler.messages"; //$NON-NLS-1$
    public static String SelectSource_error_msg;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
