package edu.ualberta.med.scannerconfig.preferences.scanner;

import java.io.File;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class PlateScannedImage {
    private static PlateScannedImage instance = null;

    /* please note that PALLET_IMAGE_DPI may change value */
    public static double PLATE_IMAGE_DPI = 300.0;

    public static final String PALLET_IMAGE_FILE = "plates.bmp";

    protected ListenerList changeListeners = new ListenerList();

    private Image scannedImage;

    protected PlateScannedImage() {
        cleanAll();
    }

    public static PlateScannedImage instance() {
        if (instance == null) {
            instance = new PlateScannedImage();
        }
        return instance;
    }

    public boolean exists() {
        return (scannedImage != null);
    }

    public Image getScannedImage() {
        return scannedImage;
    }

    public void cleanAll() {
        final File platesFile = new File(PlateScannedImage.PALLET_IMAGE_FILE);
        platesFile.delete();

        if (scannedImage != null) {
            scannedImage.dispose();
            scannedImage = null;
        }
    }

    public void scanPlateImage() {
        int brightness = ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
        int contrast = ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getInt(PreferenceConstants.SCANNER_CONTRAST);
        int debugLevel = ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getInt(PreferenceConstants.DLL_DEBUG_LEVEL);

        cleanAll();
        final int result = ScanLib.getInstance().slScanImage(debugLevel,
            (int) PlateScannedImage.PLATE_IMAGE_DPI, brightness, contrast, 0,
            0, 20, 20, PlateScannedImage.PALLET_IMAGE_FILE);

        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
            .getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (result != ScanLib.SC_SUCCESS) {
                        MessageDialog.openError(PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getShell(),
                            "Scanner error", ScanLib.getErrMsg(result));
                        notifyChangeListener(false);
                        return;
                    }
                    if ((new File(PlateScannedImage.PALLET_IMAGE_FILE))
                        .exists()) {
                        scannedImage = new Image(
                            PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow().getShell()
                                .getDisplay(),
                            PlateScannedImage.PALLET_IMAGE_FILE);
                        notifyChangeListener(true);
                        return;
                    }
                    notifyChangeListener(false);
                }
            });
    }

    public void addScannedImageChangeListener(ChangeListener listener) {
        this.changeListeners.add(listener);
    }

    public void removeScannedImageChangeListener(ChangeListener listener) {
        this.changeListeners.remove(listener);
    }

    private void notifyChangeListener(final boolean scanned) {
        Object[] listeners = this.changeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ChangeListener l = (ChangeListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {

                    Event e = new Event();
                    e.type = ChangeListener.IMAGE_SCANNED;
                    e.detail = scanned ? 1 : 0;
                    l.change(e);
                }
            });
        }
    }
}
