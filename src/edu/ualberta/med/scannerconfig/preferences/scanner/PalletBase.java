package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.widgets.PalletImageWidget;

public class PalletBase extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    protected int palletId;

    private Text[] textControls;

    private PalletImageWidget palletImageWidget;

    public PalletBase(int palletId) {
        super(GRID);
        this.palletId = palletId;
        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
        textControls = new Text[4];
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayout(new GridLayout(2, false));
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Control s = super.createContents(top);
        GridData gd = (GridData) s.getLayoutData();
        if (gd == null) {
            s.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
        } else {
            gd.verticalAlignment = SWT.BEGINNING;
        }

        Composite right = new Composite(top, SWT.NONE);
        right.setLayout(new GridLayout(1, false));
        right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createCanvasComp(right);

        Button b = new Button(right, SWT.NONE);
        b.setText("Scan");

        return top;
    }

    @Override
    protected void createFieldEditors() {
        DoubleFieldEditor fe;
        String[] labels = { "Left", "Top", "Right", "Bottom" };

        for (int i = 0; i < 4; ++i) {
            fe = new DoubleFieldEditor(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][i],
                labels[i], getFieldEditorParent());
            fe.setValidRange(0, 20);
            addField(fe);
            textControls[i] = fe.getTextControl(getFieldEditorParent());

            textControls[i].addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    try {
                        Double left = Double.parseDouble(textControls[0]
                            .getText());
                        Double top = Double.parseDouble(textControls[0]
                            .getText());
                        Double right = Double.parseDouble(textControls[0]
                            .getText());
                        Double bottom = Double.parseDouble(textControls[0]
                            .getText());
                        if (palletImageWidget != null)
                            palletImageWidget.assignRegion(left, top, right,
                                bottom);
                    } catch (NumberFormatException ex) {
                        // do nothing
                    }
                }
            });
        }

    }

    private Composite createCanvasComp(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(new GridLayout(1, false));
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Canvas canvas = new Canvas(comp, SWT.BORDER);
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ScannerRegion sr = new ScannerRegion("" + palletId, ScannerConfigPlugin
            .getDefault().getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][0]),
            ScannerConfigPlugin.getDefault().getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][1]),
            ScannerConfigPlugin.getDefault().getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][2]),
            ScannerConfigPlugin.getDefault().getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][3]));

        Color c = null;

        switch (palletId) {
        case 1:
            c = new Color(Display.getDefault(), 0, 0xFF, 0);
            break;
        case 2:
            c = new Color(Display.getDefault(), 0xFF, 0, 0);
            break;
        case 3:
            c = new Color(Display.getDefault(), 0xFF, 0xFF, 0);
            break;
        case 4:
            c = new Color(Display.getDefault(), 0, 0xFF, 0xFF);
            break;
        case 5:
            c = new Color(Display.getDefault(), 0, 0, 0xFF);
            break;
        default:
            Assert.isTrue(false, "Invalid value for palletId: " + palletId);
        }

        palletImageWidget = new PalletImageWidget(comp, SWT.NONE, canvas, sr,
            c, textControls);
        return comp;
    }

    @Override
    public void init(IWorkbench workbench) {

    }

}
