package edu.ualberta.med.scannerconfig.preferences.scanner.plateposition;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLibResult;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class PlateImageMgr {
    private static PlateImageMgr instance = null;

    public static final int PLATE_IMAGE_DPI = 300;

    public static final String PALLET_IMAGE_FILE = "plates.bmp"; //$NON-NLS-1$

    protected ListenerList listenerList = new ListenerList();

    private static final I18n i18n = I18nFactory.getI18n(PlateImageMgr.class);
    private Image scannedImage;

    private boolean debug = false;

    protected PlateImageMgr() {
        if (debug) {
            scannedImage = new Image(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell().getDisplay(),
                PlateImageMgr.PALLET_IMAGE_FILE);
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
        File platesFile = new File(PlateImageMgr.PALLET_IMAGE_FILE);
        if (platesFile.exists()) {
            platesFile.delete();
        }

        if (scannedImage != null) {
            scannedImage.dispose();
            scannedImage = null;
        }
    }

    @SuppressWarnings("nls")
    public void scanPlateImage() {
        if (ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getString(PreferenceConstants.SCANNER_DRV_TYPE)
            .equals(PreferenceConstants.SCANNER_DRV_TYPE_NONE)) {
            ScannerConfigPlugin
                .openAsyncError(
                    i18n.tr("Scanner Driver Not Selected"),
                    i18n.tr("Please select and configure the scanner in preferences"));
            return;
        }

        int brightness = ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
        int contrast = ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getInt(PreferenceConstants.SCANNER_CONTRAST);
        int debugLevel = ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getInt(PreferenceConstants.DLL_DEBUG_LEVEL);

        cleanAll();
        notifyListeners(false);

        final ScanLibResult result = ScanLib.getInstance().scanFlatbed(
            debugLevel, PlateImageMgr.PLATE_IMAGE_DPI, brightness, contrast,
            PlateImageMgr.PALLET_IMAGE_FILE);

        if (result.getResultCode() != ScanLib.SC_SUCCESS) {
            ScannerConfigPlugin.openAsyncError(i18n.tr("Scanner error"),
                result.getMessage());
            return;
        }

        Assert.isTrue((new File(PlateImageMgr.PALLET_IMAGE_FILE)).exists());

        scannedImage = new Image(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell().getDisplay(),
            PlateImageMgr.PALLET_IMAGE_FILE);
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
