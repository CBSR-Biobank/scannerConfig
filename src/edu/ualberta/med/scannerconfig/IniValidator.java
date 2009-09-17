package edu.ualberta.med.scannerconfig;

import java.io.File;

import org.ini4j.Ini;
import org.ini4j.Wini;

import edu.ualberta.med.scanlib.ScanLib;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class IniValidator {

    private static final String INI_FILE_NAME = "scanlib.ini";

    private ScannerConfigPlugin scannerConfigPlugin;

    public IniValidator() {
        scannerConfigPlugin = ScannerConfigPlugin.getDefault();

        try {
            loadFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() throws Exception {
        Wini ini;

        File f = new File(INI_FILE_NAME);
        if (!f.exists()) {
            f.createNewFile();
        }
        ini = new Wini(f);

        Ini.Section section = ini.get("scanner");
        if (section != null) {
            setBrightness(section.get("brightness", Integer.class));
            setContrast(section.get("contrast", Integer.class));
        }

        for (int p = 1; p < PreferenceConstants.SCANNER_PALLET_ENABLED.length; p++) {
            if (!scannerConfigPlugin.getPreferenceStore().getBoolean(
                PreferenceConstants.SCANNER_PALLET_ENABLED[p - 1]))
                continue;

            ScannerRegion iniRegion = null;
            Double left = null;
            Double top = null;
            Double right = null;
            Double bottom = null;

            section = ini.get("plate-" + p);
            if (section != null) {
                left = section.get("left", Double.class);
                top = section.get("top", Double.class);
                right = section.get("right", Double.class);
                bottom = section.get("bottom", Double.class);

                if ((left != null) && (top != null) && (right != null)
                    && (bottom != null)) {
                    iniRegion = new ScannerRegion("" + p, left, top, right,
                        bottom);
                }
            }

            setPlate(p, iniRegion);
        }
    }

    private void setBrightness(Integer iniBrightness) throws Exception {
        int brightness = scannerConfigPlugin.getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_BRIGHTNESS);

        if (iniBrightness.equals(brightness))
            return;

        int res = ScanLib.getInstance().slConfigScannerBrightness(brightness);
        if (res < ScanLib.SC_SUCCESS) {
            throw new Exception("Brightness configuration: "
                + ScanLib.getErrMsg(res));
        }
    }

    private void setContrast(Integer iniContrast) throws Exception {
        int contrast = scannerConfigPlugin.getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_CONTRAST);

        if (iniContrast.equals(contrast))
            return;

        int res = ScanLib.getInstance().slConfigScannerContrast(contrast);
        if (res < ScanLib.SC_SUCCESS) {
            throw new Exception("Contrast cofiguration: "
                + ScanLib.getErrMsg(res));
        }
    }

    private void setPlate(int palletId, ScannerRegion iniRegion)
        throws Exception {
        ScannerRegion configRegion = new ScannerRegion("" + palletId,
            scannerConfigPlugin.getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][0]),
            scannerConfigPlugin.getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][0]),
            scannerConfigPlugin.getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][0]),
            scannerConfigPlugin.getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][0]));

        if ((iniRegion == null) || !configRegion.equal(iniRegion)) {
            int res = ScanLib.getInstance().slConfigPlateFrame(palletId,
                configRegion.left, configRegion.top, configRegion.right,
                configRegion.bottom);
            if (res < ScanLib.SC_SUCCESS) {
                throw new Exception("Contrast cofiguration: "
                    + ScanLib.getErrMsg(res));
            }
        }
    }

}
