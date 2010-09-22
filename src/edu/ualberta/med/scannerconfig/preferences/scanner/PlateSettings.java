package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateGrid.Orientation;
import edu.ualberta.med.scannerconfig.widgets.AdvancedRadioGroupFieldEditor;
import edu.ualberta.med.scannerconfig.widgets.PlateGridWidget;
import edu.ualberta.med.scannerconfig.widgets.PlateGridWidgetListener;

public class PlateSettings extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage, PlateGridWidgetListener {

    protected ListenerList changeListeners = new ListenerList();

    protected int plateId;
    private boolean isEnabled;

    private Text[] textControls;
    private Canvas canvas;
    private PlateGridWidget plateBoundsWidget;

    private BooleanFieldEditor enabledFieldEditor;
    private AdvancedRadioGroupFieldEditor orientationFieldEditor;
    private Button scanBtn;
    private Button refreshBtn;

    private Orientation orientation;

    private Label statusLabel;

    public PlateSettings(int plateId) {
        super(GRID);
        this.plateId = plateId;

        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
    }

    @Override
    public void setValid(boolean enable) {
        super.setValid(enable);

        getContainer().updateButtons();
        Button applyButton = getApplyButton();
        if (applyButton != null)
            applyButton.setEnabled(true);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
    }

    @Override
    protected Control createContents(final Composite parent) {

        Composite top = new Composite(parent, SWT.NONE);
        top.setLayout(new GridLayout(2, false));
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Control s = super.createContents(top);
        GridData gd = (GridData) s.getLayoutData();
        if (gd == null) {
            gd = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
            s.setLayoutData(gd);
        }
        gd.verticalAlignment = SWT.BEGINNING;

        Composite right = new Composite(top, SWT.NONE);
        right.setLayout(new GridLayout(1, false));
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 200;
        right.setLayoutData(gd);

        createCanvasComp(right);

        statusLabel = new Label(right, SWT.BORDER);
        statusLabel
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite buttonComposite = new Composite(right, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));

        scanBtn = new Button(buttonComposite, SWT.NONE);
        scanBtn.setText("Scan");
        scanBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                PlateImage.instance().scanPlateImage();
            }
        });
        refreshBtn = new Button(buttonComposite, SWT.NONE);
        refreshBtn.setText("Refresh");
        refreshBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                PlateSettings.this.notifyChangeListener(
                    PlateSettingsListener.PLATE_BASE_REFRESH, 0);
            }
        });

        if (System.getProperty("os.name").startsWith("Windows")
            && ScannerConfigPlugin.getDefault().getPlateEnabled(plateId)) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }

        return top;
    }

    @Override
    protected void createFieldEditors() {
        DoubleFieldEditor fe;
        String[] labels =
            { "Left", "Top", "Right", "Bottom", "Cell Gap Horizontal",
                "Cell Gap Vertical" };

        enabledFieldEditor =
            new BooleanFieldEditor(
                PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1],
                "Enable", getFieldEditorParent());
        addField(enabledFieldEditor);
        ((Button) enabledFieldEditor
            .getDescriptionControl(getFieldEditorParent()))
            .addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setEnabled(enabledFieldEditor.getBooleanValue());
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });

        String[] prefsArr =
            PreferenceConstants.SCANNER_PALLET_CONFIG[plateId - 1];

        textControls = new Text[6];

        for (int i = 0; i < 6; ++i) {
            fe =
                new DoubleFieldEditor(prefsArr[i], labels[i] + ":",
                    getFieldEditorParent());
            fe.setValidRange(0, 20);
            addField(fe);
            textControls[i] = fe.getTextControl(getFieldEditorParent());
            textControls[i].addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    PlateSettings.this.notifyChangeListener(
                        PlateSettingsListener.PLATE_BASE_TEXT_CHANGE, 0);
                }

            });
        }

        orientationFieldEditor =
            new AdvancedRadioGroupFieldEditor(
                PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateId - 1],
                "Orientation", 2, new String[][] {
                    { "Landscape", "Landscape" }, { "Portrait", "Portrait" } },
                getFieldEditorParent(), true);
        addField(orientationFieldEditor);
        orientationFieldEditor
            .setPropertyChangeListener(new IPropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    PlateSettings.this.notifyChangeListener(
                        PlateSettingsListener.PLATE_BASE_ORIENTATION, event
                            .getNewValue().equals("Portrait") ? 1 : 0);

                }
            });
    }

    /* create canvas and plate widget */
    private void createCanvasComp(Composite parent) {

        canvas = new Canvas(parent, SWT.BORDER | SWT.NO_BACKGROUND);
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        canvas.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

        IPreferenceStore prefs =
            ScannerConfigPlugin.getDefault().getPreferenceStore();

        orientation =
            prefs
                .getBoolean(PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateId - 1]) ? Orientation.PORTRAIT
                : Orientation.LANDSCAPE;

        plateBoundsWidget = new PlateGridWidget(this, canvas);
        plateBoundsWidget.addPlateWidgetChangeListener(this);
    }

    @Override
    public void sizeChanged() {
        statusLabel.setText("Align the green grid with the barcodes");
        PlateGrid r = plateBoundsWidget.getPlateRegion();
        textControls[0].setText(String.valueOf(r.left));
        textControls[1].setText(String.valueOf(r.top));
        textControls[2].setText(String.valueOf(r.width));
        textControls[3].setText(String.valueOf(r.height));
        textControls[4].setText(String.valueOf(r.gapX));
        textControls[5].setText(String.valueOf(r.gapY));
        orientation = r.orientation;
    }

    private String formatInput(String s) {
        try {
            Double.parseDouble(s);
            return s;
        } catch (NumberFormatException e) {
            return "0";
        }
    }

    public double getLeft() {
        return Double.parseDouble(formatInput(textControls[0].getText()));
    }

    public double getTop() {
        return Double.parseDouble(formatInput(textControls[1].getText()));
    }

    public double getRight() {
        return Double.parseDouble(formatInput(textControls[2].getText()));
    }

    public double getBottom() {
        return Double.parseDouble(formatInput(textControls[3].getText()));
    }

    public double getGapX() {
        return Double.parseDouble(formatInput(textControls[4].getText()));
    }

    public double getGapY() {
        return Double.parseDouble(formatInput(textControls[5].getText()));
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    private void setEnabled(boolean enabled) {
        isEnabled = enabled;

        for (int i = 0; i < 6; ++i) {
            if (textControls[i] != null)
                textControls[i].setEnabled(enabled);
        }

        orientationFieldEditor.setEnabled(isEnabled, getFieldEditorParent());

        if (isEnabled) {
            statusLabel.setText("A scan is required");
        } else {
            statusLabel.setText("Plate is not enabled");
        }

        notifyChangeListener(PlateSettingsListener.PLATE_BASE_ENABLED,
            enabled ? 1 : 0);

    }

    public void removePlateBaseChangeListener(PlateSettingsListener listener) {
        changeListeners.remove(listener);
    }

    public void addPlateBaseChangeListener(PlateSettingsListener listener) {
        changeListeners.add(listener);
    }

    private void notifyChangeListener(final int message, final int detail) {
        Object[] listeners = changeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final PlateSettingsListener l =
                (PlateSettingsListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {

                    Event e = new Event();
                    e.type = message;
                    e.detail = detail;
                    l.plateGridChange(e);
                }
            });
        }
    }

    private void saveSettings() {
        setEnabled(enabledFieldEditor.getBooleanValue());
    }

    @Override
    public boolean performOk() {
        saveSettings();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        saveSettings();
        super.performApply();
    }
}
