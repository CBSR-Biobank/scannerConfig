package edu.ualberta.med.scannerconfig.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scanlib.ScanLib;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class CalibrateDialog extends ProgressMonitorDialog {

    public CalibrateDialog(final int palletId) {
        super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

        String dpiString = ScannerConfigPlugin.getDefault()
            .getPreferenceStore().getString(PreferenceConstants.SCANNER_DPI);

        if (dpiString.length() == 0) {
            ScannerConfigPlugin.openAsyncError("Preferences Error",
                "bad value in preferences for scanner DPI");
            return;
        }

        final int dpi = Integer.valueOf(dpiString);

        try {
            run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) {
                    try {
                        monitor.beginTask("Configuring pallet " + palletId
                            + " position...", IProgressMonitor.UNKNOWN);

                        int scanlibReturn = ScanLib.getInstance()
                            .slCalibrateToPlate(dpi, palletId);

                        if (scanlibReturn != ScanLib.SC_SUCCESS) {
                            ScannerConfigPlugin.openAsyncError(
                                "Calibration Error", ScanLib
                                    .getErrMsg(scanlibReturn));
                            ScanLib.getInstance().slConfigPlateFrame(palletId,
                                0, 0, 0, 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        monitor.done();
                    }

                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
