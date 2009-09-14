package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
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
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class PalletBase extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    protected int palletId;

    private Text position;

    public PalletBase(int palletId) {
        super(GRID);
        this.palletId = palletId;
        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
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
        }

    }

    private Composite createCanvasComp(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(new GridLayout(1, false));
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Canvas canvas = new Canvas(comp, SWT.BORDER | SWT.V_SCROLL
            | SWT.H_SCROLL | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);

        position = new Text(comp, SWT.BORDER);
        position.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
            false));

        Display display = parent.getDisplay();

        // paintSurface = new PaintSurface(canvas, position, new Color(display,
        // 255, 255, 255));
        // canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        //
        // toolSettings = new ToolSettings();
        // toolSettings.commonForegroundColor = new Color(display, 0, 0, 0);
        // toolSettings.commonBackgroundColor = new Color(display, 255, 255,
        // 255);
        //
        // tool.data = new RectangleTool(toolSettings, paintSurface);
        // paintSurface.setPaintSession((PaintTool) tool.data);

        return comp;
    }

    @Override
    public void init(IWorkbench workbench) {

    }

}
