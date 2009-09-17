package edu.ualberta.med.scannerconfig.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public PreferenceInitializer() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = ScannerConfigPlugin.getDefault()
            .getPreferenceStore();
        store.setDefault(PreferenceConstants.SCANNER_DPI, 300);
        store.setDefault(PreferenceConstants.SCANNER_BRIGHTNESS, 0);
        store.setDefault(PreferenceConstants.SCANNER_CONTRAST, 0);
        store.setDefault(PreferenceConstants.SCANNER_DRV_TYPE,
            PreferenceConstants.SCANNER_DRV_TYPE_TWAIN);

        for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_INFO.length; i++) {
            store.setDefault(PreferenceConstants.SCANNER_PALLET_INFO[i][1],
                false);
            store.setDefault(PreferenceConstants.SCANNER_PALLET_INFO[i][1],
                "PLATE" + (i + 1));
        }
    }

}
