package edu.ualberta.med.scannerconfig.preferences.scanner.profiles;

import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class ProfileManager {
    private static ProfileManager instance = null;

    private HashMap<String, ProfileSettings> profiles =
        new HashMap<String, ProfileSettings>();

    private static final I18n i18n = I18nFactory.getI18n(ProfileManager.class);

    public static final String ALL_PROFILE_NAME = i18n.tr("All"); //$NON-NLS-1$

    protected ProfileManager() {
        reloadProfiles();
    }

    public static ProfileManager instance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    @SuppressWarnings("nls")
    public void reloadProfiles() {
        try {
            profiles = loadProfilesFromString();
        } catch (Exception e) {
            ScannerConfigPlugin.openAsyncError(
                i18n.tr("Profile Manager Settings"),
                i18n.tr("Could not load profile settings"));
        }
    }

    @SuppressWarnings("nls")
    private String profilesToString() {
        String out = "";
        for (String key : profiles.keySet()) {
            ProfileSettings profile = profiles.get(key);
            int[] words = profile.toWords();
            out += key + "," + words[0] + "," + words[1] + "," + words[2] + ";";
        }
        return out;
    }

    @SuppressWarnings("nls")
    public static HashMap<String, ProfileSettings> loadProfilesFromString()
        throws Exception {
        HashMap<String, ProfileSettings> profilesMap =
            new HashMap<String, ProfileSettings>();

        ProfileSettings allProfile = new ProfileSettings(ALL_PROFILE_NAME);
        allProfile.setAll();
        profilesMap.put(allProfile.getName(), allProfile);

        IPreferenceStore store = ScannerConfigPlugin.getDefault()
            .getPreferenceStore();
        String profileString = store
            .getString(PreferenceConstants.SCANNER_PALLET_PROFILES);

        String entries[] = profileString.split(";");
        if (entries.length < 0)
            return profilesMap;

        for (String entry : entries) {
            String elements[] = entry.split(",");
            if (elements.length < 4) {
                continue;
            }
            String key = elements[0];
            if (key.equals(ALL_PROFILE_NAME))
                continue;
            int[] settingsAsInts = new int[] { Integer.parseInt(elements[1]),
                Integer.parseInt(elements[2]), Integer.parseInt(elements[3]) };
            profilesMap.put(key, new ProfileSettings(key, settingsAsInts));
        }
        return profilesMap;
    }

    public ProfileSettings getProfile(String name) {
        return profiles.get(name);

    }

    public void addNewProfile(String name) {
        profiles.put(name, new ProfileSettings(name));
    }

    public void removeProfile(String name) {
        profiles.remove(name);
    }

    public HashMap<String, ProfileSettings> getProfiles() {
        return profiles;
    }

    public void saveProfiles() {
        IPreferenceStore store = ScannerConfigPlugin.getDefault()
            .getPreferenceStore();
        store.setValue(PreferenceConstants.SCANNER_PALLET_PROFILES,
            profilesToString());
    }

}
