package edu.ualberta.med.scannerconfig.preferences.scanner;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scanlib.ScanLib;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.dialogs.CalibrateDialog;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.widgets.PalletImageWidget;

public class PalletBase extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    // 15 minutes
    private static final long LAST_MODIFIED_EXCEEDED_TIME_MILLIS = 15 * 60 * 1000;

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
    protected Control createContents(final Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayout(new GridLayout(2, false));
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        File palletsFile = new File(PalletImageWidget.PALLET_IMAGE_FILE);
        if (System.currentTimeMillis() - palletsFile.lastModified() > LAST_MODIFIED_EXCEEDED_TIME_MILLIS) {
            palletsFile.delete();
        }

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
        b.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
                    @Override
                    public void run() {
                        final int result = ScanLib.getInstance().slScanImage(
                            (int) PalletImageWidget.PALLET_IMAGE_DPI, 0, 0, 0,
                            0, PalletImageWidget.PALLET_IMAGE_FILE);

                        parent.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                if (result != ScanLib.SC_SUCCESS) {
                                    MessageDialog.openError(PlatformUI
                                        .getWorkbench()
                                        .getActiveWorkbenchWindow().getShell(),
                                        "Scanner error", ScanLib
                                            .getErrMsg(result));
                                    return;
                                }

                                File palletsFile = new File(
                                    PalletImageWidget.PALLET_IMAGE_FILE);
                                if (palletsFile.exists()) {
                                    for (int i = 0; i < 4; ++i) {
                                        textControls[i].setEnabled(true);
                                    }
                                    if (palletImageWidget != null) {
                                        palletImageWidget.redraw();
                                        palletImageWidget.update();
                                    }
                                }
                            }
                        });
                    }
                });

            }
        });

        if (!palletsFile.exists()) {
            for (int i = 0; i < 4; ++i) {
                textControls[i].setEnabled(false);
            }
        }

        return top;
    }

    @Override
    protected void createFieldEditors() {
        DoubleFieldEditor fe;
        String[] labels = { "", "Left", "Top", "Right", "Bottom" };

        addField(new BooleanFieldEditor(
            PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][0],
            "Enable", getFieldEditorParent()));

        for (int i = 1; i <= 4; ++i) {
            fe = new DoubleFieldEditor(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][i],
                labels[i], getFieldEditorParent());
            fe.setValidRange(0, 20);
            addField(fe);
            textControls[i - 1] = fe.getTextControl(getFieldEditorParent());

            textControls[i - 1].addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (palletImageWidget == null)
                        return;
                    try {
                        Text source = (Text) e.getSource();

                        if (source == textControls[0]) {
                            palletImageWidget.assignRegionLeft(Double
                                .parseDouble(source.getText()));
                        } else if (source == textControls[1]) {
                            palletImageWidget.assignRegionTop(Double
                                .parseDouble(source.getText()));
                        } else if (source == textControls[2]) {
                            palletImageWidget.assignRegionRight(Double
                                .parseDouble(source.getText()));
                        } else if (source == textControls[3]) {
                            palletImageWidget.assignRegionBottom(Double
                                .parseDouble(source.getText()));
                        }
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

        IPreferenceStore prefs = ScannerConfigPlugin.getDefault()
            .getPreferenceStore();

        ScannerRegion sr = new ScannerRegion(
            "" + palletId,
            ScannerConfigPlugin.getDefault().getPreferenceStore().getDouble(
                PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][0]),
            prefs
                .getDouble(PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][1]),
            prefs
                .getDouble(PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][2]),
            prefs
                .getDouble(PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1][3]));

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
        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
    }

    @Override
    public boolean performOk() {
        double left = Double.valueOf(textControls[0].getText());
        double top = Double.valueOf(textControls[1].getText());
        double right = Double.valueOf(textControls[2].getText());
        double bottom = Double.valueOf(textControls[3].getText());

        ScanLib.getInstance().slConfigPlateFrame(palletId, left, top, right,
            bottom);
        new CalibrateDialog(palletId);
        return super.performOk();

    }

}
