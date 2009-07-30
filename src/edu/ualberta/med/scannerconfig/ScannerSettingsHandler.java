package edu.ualberta.med.scannerconfig;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;

public class ScannerSettingsHandler extends AbstractHandler implements IHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ConfigDialog configDialog = new ConfigDialog(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), SWT.NONE);
        configDialog.open();
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setFocus();
        return null;
    }
}
