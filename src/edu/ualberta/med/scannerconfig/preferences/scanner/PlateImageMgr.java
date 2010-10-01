package edu.ualberta.med.scannerconfig.preferences.scanner;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class PlateImageMgr {
    private static PlateImageMgr instance = null;

    /* please note that PALLET_IMAGE_DPI may change value */
    public static double PLATE_IMAGE_DPI = 300.0;

    public static final String PALLET_IMAGE_FILE = "plates.bmp";

    protected ListenerList listenerList = new ListenerList();

    private Image scannedImage;

    private boolean debug = true;

    protected PlateImageMgr() {
        if (debug) {
            scannedImage =
                new Image(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell().getDisplay(), PlateImageMgr.PALLET_IMAGE_FILE);
        } else {
            cleanAll();
        }
    }

    public static PlateImageMgr instance() {
        if (instance == null) {
            instance = new PlateImageMgr();
        }
        return instance;
    }

    public boolean hasImage() {
        return (scannedImage != null);
    }

    public Image getScannedImage() {
        return scannedImage;
    }

    public void cleanAll() {
        final File platesFile = new File(PlateImageMgr.PALLET_IMAGE_FILE);
        platesFile.delete();

        if (scannedImage != null) {
            scannedImage.dispose();
            scannedImage = null;
        }
    }

    public void scanPlateImage() {
        int brightness =
            ScannerConfigPlugin.getDefault().getPreferenceStore()
                .getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
        int contrast =
            ScannerConfigPlugin.getDefault().getPreferenceStore()
                .getInt(PreferenceConstants.SCANNER_CONTRAST);
        int debugLevel =
            ScannerConfigPlugin.getDefault().getPreferenceStore()
                .getInt(PreferenceConstants.DLL_DEBUG_LEVEL);

        cleanAll();
        notifyListeners(false);

        final int result =
            ScanLib.getInstance().slScanImage(debugLevel,
                (int) PlateImageMgr.PLATE_IMAGE_DPI, brightness, contrast, 0,
                0, 20, 20, PlateImageMgr.PALLET_IMAGE_FILE);

        if (result != ScanLib.SC_SUCCESS) {
            ScannerConfigPlugin.openAsyncError("Scanner error",
                ScanLib.getErrMsg(result));
            return;
        }

        Assert.isTrue((new File(PlateImageMgr.PALLET_IMAGE_FILE)).exists());

        scannedImage =
            new Image(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell().getDisplay(), PlateImageMgr.PALLET_IMAGE_FILE);
        notifyListeners(true);
    }

    public void addScannedImageChangeListener(IPlateImageListener listener) {
        listenerList.add(listener);
        if (debug) {
            notifyListeners(true);
        }
    }

    public void removeScannedImageChangeListener(IPlateImageListener listener) {
        listenerList.remove(listener);
    }

    private void notifyListeners(final boolean haveNewImage) {
        Object[] listeners = listenerList.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final IPlateImageListener l = (IPlateImageListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    if (haveNewImage) {
                        l.plateImageNew();
                    } else {
                        l.plateImageDeleted();
                    }
                }
            });
        }
    }
}
