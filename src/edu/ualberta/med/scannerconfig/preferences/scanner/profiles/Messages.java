package edu.ualberta.med.scannerconfig.preferences.scanner.profiles;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "edu.ualberta.med.scannerconfig.preferences.scanner.profiles.messages"; //$NON-NLS-1$
    public static String ProfileManager_load_error_msg;
    public static String ProfileManager_load_error_title;
    public static String ProfilePreferences_add_label;
    public static String ProfilePreferences_delete_label;
    public static String ProfilePreferences_profile_label;
    public static String ProfilePreferences_profile_msg;
    public static String ProfilePreferences_select_all_label;
    public static String ProfilePreferences_unselect_all_label;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
