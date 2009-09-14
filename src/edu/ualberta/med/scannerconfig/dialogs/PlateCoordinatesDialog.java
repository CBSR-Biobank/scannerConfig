package edu.ualberta.med.scannerconfig.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.ualberta.med.scannerconfig.ScannerRegion;

public class PlateCoordinatesDialog extends Dialog {

    private Label statusLabel;

    private ComboViewer plateComboViewer;

    private ScannerRegion[] regions;

    private Text[] coordTexts;

    protected PlateCoordinatesDialog(Shell parentShell, ScannerRegion[] regions) {
        super(parentShell);
        this.regions = regions;
        coordTexts = new Text[regions.length];
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
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        Composite lowerComp = new Composite(contents, SWT.NONE);
        lowerComp.setLayout(new GridLayout(2, false));
        lowerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createCoordsComp(lowerComp);
        createCanvasComp(lowerComp);

        return contents;
    }

    private Composite createCoordsComp(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(new GridLayout(2, false));
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        plateComboViewer = new ComboViewer(new CCombo(comp, SWT.BORDER));
        plateComboViewer.setContentProvider(new ArrayContentProvider());
        plateComboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ScannerRegion) element).name;
            }
        });
        plateComboViewer.setInput(regions);
        plateComboViewer.setSelection((ISelection) regions[0]);

        Label l = new Label(comp, SWT.NONE);
        l.setText("Left");
        coordTexts[0] = new Text(comp, SWT.BORDER);
        coordTexts[0].setText("" + regions[0].left);

        l = new Label(comp, SWT.NONE);
        l.setText("Top");
        coordTexts[1] = new Text(comp, SWT.BORDER);
        coordTexts[1].setText("" + regions[0].top);

        l = new Label(comp, SWT.NONE);
        l.setText("Right");
        coordTexts[2] = new Text(comp, SWT.BORDER);
        coordTexts[2].setText("" + regions[0].right);

        l = new Label(comp, SWT.NONE);
        l.setText("Bottom");
        coordTexts[3] = new Text(comp, SWT.BORDER);
        coordTexts[3].setText("" + regions[0].bottom);

        return comp;
    }

    private Composite createCanvasComp(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(new GridLayout(1, false));
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return comp;
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
