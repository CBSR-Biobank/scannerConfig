package edu.ualberta.med.scannerconfig.preferences.profiles;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class ProfileManager {
	private static ProfileManager instance = null;

	private HashMap<String, TriIntC> profiles = new HashMap<String, TriIntC>();

	protected ProfileManager() {
		reloadProfiles();
	}

	public static ProfileManager instance() {
		if (instance == null) {
			instance = new ProfileManager();
		}
		return instance;
	}

	public void reloadProfiles() {
		profiles = loadProfilesFromString();
	}

	private String strip(String s) {

		if (s == null)
			return null;

		String result = "";
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != ",".charAt(0) && s.charAt(i) != ";".charAt(0))
				result += s.charAt(i);
		}
		return result;
	}

	private String profilesToString() {
		String out = "";
		Set<?> entries = profiles.entrySet();
		Iterator<?> it = entries.iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) it.next();
			out += strip((String) entry.getKey()) + ","
					+ ((TriIntC) entry.getValue()).getValues()[0] + ","
					+ ((TriIntC) entry.getValue()).getValues()[1] + ","
					+ ((TriIntC) entry.getValue()).getValues()[2] + ";";
		}
		return out;
	}

	public TriIntC getTriIntProfile(String name) {
		TriIntC triInt = loadProfilesFromString().get(name);
		if (triInt == null) {
			triInt = new TriIntC();
			triInt.setAll();
			System.err
					.println("Warning:loading non-existant profile (Defaulting to ALL)");
		}
		return triInt;

	}

	public static HashMap<String, TriIntC> loadProfilesFromString() {
		HashMap<String, TriIntC> profilesMap = new HashMap<String, TriIntC>();

		TriIntC allTriIntC = new TriIntC();
		allTriIntC.setAll();
		profilesMap.put("All", allTriIntC);

		IPreferenceStore store = ScannerConfigPlugin.getDefault()
				.getPreferenceStore();
		String profileString = store
				.getString(PreferenceConstants.SCANNER_PALLET_PROFILES);

		String entries[] = profileString.split(";");
		if (entries.length < 0)
			return profilesMap;

		for (String entry : entries) {
			String elements[] = entry.split(",");
			if (elements.length < 4)
				break;
			String key = elements[0];
			if (key.equals("All"))
				continue;
			int a = Integer.parseInt(elements[1]);
			int b = Integer.parseInt(elements[2]);
			int c = Integer.parseInt(elements[3]);
			profilesMap.put(key, new TriIntC(a, b, c));
		}
		return profilesMap;
	}

	public TriIntC getProfile(String name) {
		return profiles.get(name);

	}

	public void addNewProfile(String name) {
		profiles.put(name, new TriIntC());
	}

	public void removeProfile(String name) {
		profiles.remove(name);
	}

	public HashMap<String, TriIntC> getProfiles() {
		return profiles;
	}

	public void saveProfiles() {
		IPreferenceStore store = ScannerConfigPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(
				PreferenceConstants.SCANNER_PALLET_PROFILES,
				profilesToString());
	}

}
