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
    }

    public void loadFromFile() throws Exception {
        Wini ini;

        File f = new File(INI_FILE_NAME);
        if (!f.exists()) {
            f.createNewFile();
        }
        ini = new Wini(f);

        Ini.Section section = ini.get("scanner");
        if (section != null) {
            setBrightness(section.get("brightness"));
            setContrast(section.get("contrast"));
        }

        for (int p = 1; p < PreferenceConstants.SCANNER_PALLET_ENABLED.length; p++) {
            section = ini.get("plate-" + p);
            if (section != null) {
                Double left = section.get("left", Double.class);
                Double top = section.get("top", Double.class);
                Double right = section.get("right", Double.class);
                Double bottom = section.get("bottom", Double.class);
            }

            if ((left != null) && (top != null) && (right != null)
                && (bottom != null)) {

                setPallet(p, left.doubleValue(), top.doubleValue(), right
                    .doubleValue(), bottom.doubleValue());
            }
        }
    }

    private void setBrightness(String iniBrightnessStr) throws Exception {
        int iniBrightness = 1001;
        int brightness = scannerConfigPlugin.getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_BRIGHTNESS);

        try {
            iniBrightness = Integer.valueOf(iniBrightnessStr);
        } catch (NumberFormatException ex) {
            // do nothing
        }

        if (brightness != iniBrightness) {
            int res = ScanLib.getInstance().slConfigScannerBrightness(
                brightness);
            if (res < ScanLib.SC_SUCCESS) {
                throw new Exception("Brightness configuration: "
                    + ScanLib.getErrMsg(res));
            }
        }
    }

    private void setContrast(String iniContrastStr) throws Exception {
        int iniContrast = 1001;
        int contrast = scannerConfigPlugin.getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_CONTRAST);

        try {
            iniContrast = Integer.valueOf(iniContrastStr);
        } catch (NumberFormatException ex) {
            // do nothing
        }

        if (contrast != iniContrast) {
            int res = ScanLib.getInstance().slConfigScannerContrast(contrast);
            if (res < ScanLib.SC_SUCCESS) {
                throw new Exception("Contrast cofiguration: "
                    + ScanLib.getErrMsg(res));
            }
        }
    }

}
