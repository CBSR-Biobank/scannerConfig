package edu.ualberta.med.scannerconfig.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static final I18n i18n = I18nFactory
        .getI18n(PreferenceInitializer.class);

    @SuppressWarnings("nls")
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = ScannerConfigPlugin.getDefault()
            .getPreferenceStore();
        store.setDefault(PreferenceConstants.SCANNER_DPI, 600);
        store.setDefault(PreferenceConstants.SCANNER_PALLET_PROFILES, "");
        store.setDefault(PreferenceConstants.SCANNER_BRIGHTNESS, 0);
        store.setDefault(PreferenceConstants.SCANNER_CONTRAST, 0);
        store.setDefault(PreferenceConstants.SCANNER_DRV_TYPE,
            PreferenceConstants.SCANNER_DRV_TYPE_NONE);
        store.setDefault(PreferenceConstants.LIBDMTX_EDGE_THRESH, 5);
        store.setDefault(PreferenceConstants.LIBDMTX_SCAN_GAP, 0.085);
        store.setDefault(PreferenceConstants.LIBDMTX_SQUARE_DEV, 15);
        store.setDefault(PreferenceConstants.LIBDMTX_CORRECTIONS, 10);

        for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_ENABLED.length; i++) {
            store.setDefault(PreferenceConstants.SCANNER_PALLET_ENABLED[i],
                false);
        }
        for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_CONFIG.length; i++) {
            store.setDefault(PreferenceConstants.SCANNER_PALLET_ORIENTATION[i],
                false);
        }
        for (int i = 0; i < PreferenceConstants.SCANNER_PLATE_BARCODES.length; i++) {
            store.setDefault(PreferenceConstants.SCANNER_PLATE_BARCODES[i],
                i18n.tr("PLATE") + (i + 1));
        }
        for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_ORIENTATION.length; i++) {
            store.setDefault(PreferenceConstants.SCANNER_PALLET_ORIENTATION[i],
                PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE);
        }
        for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS.length; i++) {
            store.setDefault(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS[i],
                PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12);
        }
    }
}
