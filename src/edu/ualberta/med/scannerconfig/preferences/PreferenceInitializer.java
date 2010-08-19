package edu.ualberta.med.scannerconfig.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ScannerConfigPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.SCANNER_DPI, 600);
		store.setDefault(PreferenceConstants.SCANNER_PALLET_PROFILES, "");
		store.setDefault(PreferenceConstants.SCANNER_BRIGHTNESS, 0);
		store.setDefault(PreferenceConstants.SCANNER_CONTRAST, 0);
		store.setDefault(PreferenceConstants.SCANNER_DRV_TYPE,
				PreferenceConstants.SCANNER_DRV_TYPE_TWAIN);
		store.setDefault(PreferenceConstants.LIBDMTX_EDGE_THRESH, 5);
		store.setDefault(PreferenceConstants.LIBDMTX_SCAN_GAP, 0.085);
		store.setDefault(PreferenceConstants.LIBDMTX_SQUARE_DEV, 15);
		store.setDefault(PreferenceConstants.LIBDMTX_CORRECTIONS, 10);
		store.setDefault(PreferenceConstants.LIBDMTX_CELL_DISTANCE, 0.345);

		for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_ENABLED.length; i++) {
			store.setDefault(PreferenceConstants.SCANNER_PALLET_ENABLED[i],
					false);
		}
	}

}
