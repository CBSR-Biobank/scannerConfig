package edu.ualberta.med.scannerconfig;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.ualberta.med.scanlib.ScanCell;
import edu.ualberta.med.scanlib.ScanLib;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScannerConfigPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "scannerConfig";

    // The shared instance
    private static ScannerConfigPlugin plugin;

    private static int SQUARE_DEV = 10;

    private static int THRESHOLD = 50;

    private static int SCAN_GAP = 20;

    /**
     * The constructor
     */
    public ScannerConfigPlugin() {
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            System.loadLibrary("scanlib");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static ScannerConfigPlugin getDefault() {
        return plugin;
    }

    public void initialize() {
    }

    public static void scanImage(double left, double top, double right,
        double bottom, String filename) throws Exception {
        int dpi = getDefault().getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_DPI);
        int brightness = getDefault().getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_BRIGHTNESS);
        int contrast = getDefault().getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_CONTRAST);
        int res = ScanLib.getInstance().slScanImage(0, dpi, brightness,
            contrast, left, top, right, bottom, filename);

        if (res < ScanLib.SC_SUCCESS) {
            throw new Exception("Could not decode image. "
                + ScanLib.getErrMsg(res));
        }
    }

    public static void scanPlate(int plateNumber, String filename)
        throws Exception {

        String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[plateNumber - 1];

        ScannerRegion region = new ScannerRegion("" + plateNumber, getDefault()
            .getPreferenceStore().getDouble(prefsArr[0]), getDefault()
            .getPreferenceStore().getDouble(prefsArr[1]), getDefault()
            .getPreferenceStore().getDouble(prefsArr[2]), getDefault()
            .getPreferenceStore().getDouble(prefsArr[3]));
        scanImage(region.left, region.top, region.right, region.bottom,
            filename);
    }

    public static ScanCell[][] scan(int plateNumber) throws Exception {
        int dpi = getDefault().getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_DPI);
        int brightness = getDefault().getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_BRIGHTNESS);
        int contrast = getDefault().getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_CONTRAST);

        String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[plateNumber - 1];

        ScannerRegion region = new ScannerRegion("" + plateNumber, getDefault()
            .getPreferenceStore().getDouble(prefsArr[0]), getDefault()
            .getPreferenceStore().getDouble(prefsArr[1]), getDefault()
            .getPreferenceStore().getDouble(prefsArr[2]), getDefault()
            .getPreferenceStore().getDouble(prefsArr[3]));

        int res = ScanLib.getInstance().slDecodePlate(0, dpi, brightness,
            contrast, plateNumber, region.left, region.top, region.right,
            region.bottom, SCAN_GAP, SQUARE_DEV, THRESHOLD);

        if (res < ScanLib.SC_SUCCESS) {
            throw new Exception("Could not decode image. "
                + ScanLib.getErrMsg(res));
        }
        return ScanCell.getScanLibResults();
    }

    public boolean getPlateEnabled(int plateId) {
        Assert.isTrue((plateId > 0)
            && (plateId <= PreferenceConstants.SCANNER_PALLET_ENABLED.length),
            "plate id is invalid: " + plateId);
        return getPreferenceStore().getBoolean(
            PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1]);
    }

    public int getPlateCount() {
        int result = 0;
        for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_ENABLED.length; ++i) {
            if (getPreferenceStore().getBoolean(
                PreferenceConstants.SCANNER_PALLET_ENABLED[i]))
                ++result;
        }
        return result;
    }

    public static int getPlatesMax() {
        return PreferenceConstants.SCANNER_PALLET_ENABLED.length;
    }

    public int getDpi() {
        return getPreferenceStore().getInt(PreferenceConstants.SCANNER_DPI);
    }

    public int getBrightness() {
        return getPreferenceStore().getInt(
            PreferenceConstants.SCANNER_BRIGHTNESS);
    }

    public int getContrast() {
        return getPreferenceStore()
            .getInt(PreferenceConstants.SCANNER_CONTRAST);
    }

    /**
     * Display an error message
     */
    public static void openError(String title, String message) {
        MessageDialog.openError(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), title, message);
    }

    /**
     * Display an error message asynchronously
     */
    public static void openAsyncError(final String title, final String message) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell(), title, message);
            }
        });
    }
}
