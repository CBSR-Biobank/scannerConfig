package edu.ualberta.med.scannerconfig.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "edu.ualberta.med.scannerconfig.dialogs.messages"; //$NON-NLS-1$
    public static String InputDialog_Cancel_button;
    public static String InputDialog_OK_button;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
