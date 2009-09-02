package edu.ualberta.med.scannerconfig.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class PlateCoordinatesDialog extends Dialog {

    private Label statusLabel;

    protected PlateCoordinatesDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control contents = super.createButtonBar(parent);
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        Composite contents = new Composite(parentComposite, SWT.NONE);
        contents.setLayout(new GridLayout(1, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        statusLabel = new Label(contents, SWT.NONE);
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return contents;
    }

    protected void setStatusMessage(String text, Color systemColor) {
        if ((statusLabel != null) && !statusLabel.isDisposed()) {
            statusLabel.setText(text);
            statusLabel.setForeground(systemColor);
        }
    }

    protected void setStatusMessage(String msg) {
        setStatusMessage(msg, Display.getCurrent().getSystemColor(
            SWT.COLOR_BLACK));
    }
}
