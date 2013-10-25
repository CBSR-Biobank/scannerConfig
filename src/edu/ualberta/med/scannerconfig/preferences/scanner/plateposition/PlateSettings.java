package edu.ualberta.med.scannerconfig.preferences.scanner.plateposition;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.scannerconfig.FlatbedImageScan;
import edu.ualberta.med.scannerconfig.IScanImageListener;
import edu.ualberta.med.scannerconfig.PlateGrid;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.widgets.AdvancedRadioGroupFieldEditor;
import edu.ualberta.med.scannerconfig.widgets.IPlateGridWidgetListener;
import edu.ualberta.med.scannerconfig.widgets.PlateGridWidget;

public class PlateSettings extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage, IScanImageListener, IPlateGridWidgetListener {

    private static Logger log = LoggerFactory.getLogger(PlateSettings.class.getName());

    private static final I18n i18n = I18nFactory.getI18n(PlateSettings.class);

    @SuppressWarnings("nls")
    private enum Settings {
        LEFT() {
            @Override
            public String toString() {
                return i18n.tr("Left");
            }
        },
        TOP() {
            @Override
            public String toString() {
                return i18n.tr("Top");
            }
        },
        RIGHT() {
            @Override
            public String toString() {
                return i18n.tr("Right");
            }
        },
        BOTTOM() {
            @Override
            public String toString() {
                return i18n.tr("Bottom");
            }
        },
        BARCODE() {
            @Override
            public String toString() {
                return i18n.tr("Barcode");
            }
        }
    };

    public enum PlateOrientation {
        LANDSCAPE(PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE),
        PORTRAIT(PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT);

        private final String label;

        private PlateOrientation(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static PlateOrientation getFromString(String value) {
            if (LANDSCAPE.label.equals(value)) {
                return LANDSCAPE;
            }
            else if (PORTRAIT.label.equals(value)) {
                return PORTRAIT;
            }
            return null;
        }
    };

    public enum PlateDimensions {
        DIM_ROWS_8_COLS_12(new ImmutablePair<Integer, Integer>(8, 12)),
        DIM_ROWS_10_COLS_10(new ImmutablePair<Integer, Integer>(10, 10));

        private final ImmutablePair<Integer, Integer> dimensions;

        private PlateDimensions(ImmutablePair<Integer, Integer> dimensions) {
            this.dimensions = dimensions;
        }

        public Pair<Integer, Integer> getDimensions() {
            return dimensions;
        }

        public Integer getRows() {
            return dimensions.left;
        }

        public Integer getCols() {
            return dimensions.right;
        }

        public static PlateDimensions getFromString(String value) {
            if (PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12.equals(value)) {
                return DIM_ROWS_8_COLS_12;
            }
            if (PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10.equals(value)) {
                return DIM_ROWS_10_COLS_10;
            }
            return null;
        }
    };

    @SuppressWarnings("nls")
    private static final String NOT_ENABLED_STATUS_MSG = i18n.tr("Plate is not enabled");

    @SuppressWarnings("nls")
    private static final String ALIGN_STATUS_MSG = i18n.tr("Align grid with barcodes");

    @SuppressWarnings("nls")
    private static final String SCAN_REQ_STATUS_MSG = i18n.tr("A scan is required");

    protected ListenerList changeListeners = new ListenerList();

    protected int plateId;
    private boolean isEnabled;

    private Map<Settings, StringFieldEditor> plateFieldEditors;
    private Map<Settings, Text> plateTextControls;
    private PlateGridWidget plateGridWidget = null;

    private BooleanFieldEditor enabledFieldEditor;
    private AdvancedRadioGroupFieldEditor orientationFieldEditor;
    private AdvancedRadioGroupFieldEditor gridDimensionsFieldEditor;
    private Button scanBtn;
    private Button refreshBtn;

    private Label statusLabel;

    private boolean internalUpdate;

    private final FlatbedImageScan flatbedImageScan;

    private PlateGrid plateGrid = null;

    public PlateSettings(int plateId) {
        super(GRID);
        this.plateId = plateId;
        internalUpdate = false;

        IPreferenceStore prefs = ScannerConfigPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(prefs);

        // initialize these settings if never assigned
        String orientation = prefs.getString(
            PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateId - 1]);
        if ((orientation == null) || orientation.isEmpty()) {
            prefs.setDefault(PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateId - 1],
                PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE);
        }

        String gridDimensions = prefs.getString(
            PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS[plateId - 1]);
        if ((gridDimensions == null) || gridDimensions.isEmpty()) {
            prefs.setDefault(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS[plateId - 1],
                PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12);
        }

        flatbedImageScan = new FlatbedImageScan();
        flatbedImageScan.addScannedImageChangeListener(this);
    }

    @Override
    public void dispose() {
        flatbedImageScan.removeScannedImageChangeListener(this);
        if (plateGridWidget != null) {
            plateGridWidget.dispose();
        }
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
        setPreferenceStore(ScannerConfigPlugin.getDefault().getPreferenceStore());
    }

    @SuppressWarnings("nls")
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

        plateGridWidget = new PlateGridWidget(right, this);
        plateGridWidget.addPlateWidgetChangeListener(this);
        updateGridSettingsPerPreferences();

        statusLabel = new Label(right, SWT.BORDER);
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite buttonComposite = new Composite(right, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));

        scanBtn = new Button(buttonComposite, SWT.NONE);
        scanBtn.setText(i18n.tr("Scan"));
        scanBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                flatbedImageScan.scan();
            }
        });
        refreshBtn = new Button(buttonComposite, SWT.NONE);
        refreshBtn.setText(i18n.tr("Refresh"));
        refreshBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                PlateSettings.this.notifyChangeListener(
                    IPlateSettingsListener.REFRESH, 0);
            }
        });

        setEnabled(ScannerConfigPlugin.getDefault().getPlateEnabled(plateId));
        return top;
    }

    @SuppressWarnings("nls")
    @Override
    protected void createFieldEditors() {
        StringFieldEditor fe;

        enabledFieldEditor = new BooleanFieldEditor(
            PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1],
            i18n.tr("Enable"),
            getFieldEditorParent());
        addField(enabledFieldEditor);

        String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateId - 1];

        plateFieldEditors = new HashMap<Settings, StringFieldEditor>();
        plateTextControls = new HashMap<Settings, Text>();
        Composite parent = getFieldEditorParent();

        orientationFieldEditor = new AdvancedRadioGroupFieldEditor(
            PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateId - 1],
            i18n.tr("Orientation"),
            2,
            new String[][] {
                { PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE,
                    PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE },
                { PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT,
                    PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT } },
            parent, true);
        addField(orientationFieldEditor);

        gridDimensionsFieldEditor = new AdvancedRadioGroupFieldEditor(
            PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS[plateId - 1],
            i18n.tr("Grid dimensions"),
            2,
            new String[][] {
                { PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12,
                    PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12 },
                { PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10,
                    PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10 } },
            parent, true);

        addField(gridDimensionsFieldEditor);

        int count = 0;
        for (Settings setting : Settings.values()) {
            if (setting.equals(Settings.BARCODE)) {
                fe = new StringFieldEditor(
                    PreferenceConstants.SCANNER_PLATE_BARCODES[plateId - 1],
                    setting + ":", getFieldEditorParent());
            } else {
                fe = new DoubleFieldEditor(prefsArr[count], setting + ":", parent);
                ((DoubleFieldEditor) fe).setValidRange(0, 20);
                addField(fe);
            }
            Text text = fe.getTextControl(parent);
            plateTextControls.put(setting, text);
            plateFieldEditors.put(setting, fe);

            if (!setting.equals(Settings.BARCODE)) {
                text.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent e) {
                        if (plateGrid != null) {
                            double left = Double.parseDouble(plateFieldEditors.get(Settings.LEFT).getStringValue());
                            double top = Double.parseDouble(plateFieldEditors.get(Settings.TOP).getStringValue());
                            double right = Double.parseDouble(plateFieldEditors.get(Settings.RIGHT).getStringValue());
                            double bottom = Double.parseDouble(plateFieldEditors.get(Settings.BOTTOM).getStringValue());
                            plateGrid.setPlate(left, top, right - left, bottom - top);
                        }

                        notifyChangeListener(IPlateSettingsListener.TEXT_CHANGE, 0);
                    }

                });
            }
            ++count;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        Object source = event.getSource();
        if (source == enabledFieldEditor) {
            boolean enabled = enabledFieldEditor.getBooleanValue();
            if (enabled) {
                // set default size
                internalUpdate(new Rectangle2D.Double(0, 0, 4, 3),
                    PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE,
                    PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12);
                updatePlateGridWidget(PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE,
                    PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12);
            }
            setEnabled(enabled);
        } else if (source == orientationFieldEditor) {
            plateGrid.setOrientation(PlateOrientation.getFromString((String) event.getNewValue()));
            notifyChangeListener(IPlateSettingsListener.ORIENTATION, event.getNewValue());
        } else if (source == gridDimensionsFieldEditor) {
            plateGrid.setGridDimensions(PlateDimensions.getFromString((String) event.getNewValue()));
            notifyChangeListener(IPlateSettingsListener.GRID_DIMENSIONS, event.getNewValue());
        }
    }

    private void internalUpdate(Rectangle2D.Double plate, String orientation, String gridDimensions) {
        internalUpdate = true;

        log.debug("internalUpdate: plate: {}", plate);

        plateTextControls.get(Settings.LEFT).setText(String.valueOf(plate.x));
        plateTextControls.get(Settings.TOP).setText(String.valueOf(plate.y));
        plateTextControls.get(Settings.RIGHT).setText(String.valueOf(plate.x + plate.width));
        plateTextControls.get(Settings.BOTTOM).setText(String.valueOf(plate.y + plate.height));
        plateTextControls.get(Settings.BARCODE).setText(getPreferenceStore().getString(
            PreferenceConstants.SCANNER_PLATE_BARCODES[plateId - 1]));

        if (orientation != null) {
            boolean[] orientationSettings = new boolean[] {
                orientation.equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE),
                orientation.equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT)
            };
            orientationFieldEditor.setSelectionArray(orientationSettings);
        }

        if (gridDimensions != null) {
            boolean[] gridDimensionsSettings = new boolean[] {
                gridDimensions.equals(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12),
                gridDimensions.equals(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10)
            };

            gridDimensionsFieldEditor.setSelectionArray(gridDimensionsSettings);
        }
        internalUpdate = false;
    }

    private void internalUpdate(Rectangle2D.Double plate) {
        internalUpdate(plate, null, null);
    }

    private void updatePlateGridWidget(String orientation, String gridDimensions) {
        if (plateGridWidget == null) return;

        boolean internalUpdateCurrentValue = internalUpdate;
        internalUpdate = false;

        // internalUpdate needs to be false for the notification to be sent to the
        // plateGridWidget
        notifyChangeListener(IPlateSettingsListener.ORIENTATION, orientation);
        notifyChangeListener(IPlateSettingsListener.GRID_DIMENSIONS, gridDimensions);
        internalUpdate = internalUpdateCurrentValue;
    }

    private void updateGridSettingsPerPreferences() {
        if (plateGridWidget == null) return;

        IPreferenceStore prefs = ScannerConfigPlugin.getDefault().getPreferenceStore();

        updatePlateGridWidget(
            prefs.getString(PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateId - 1]),
            prefs.getString(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS[plateId - 1]));
    }

    private void updateGridSettings() {
        if (plateGridWidget == null) return;

        String orientation = PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE;
        if (orientationFieldEditor.getRadioSelected() == 1) {
            orientation = PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT;
        }
        String dimensions = PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12;
        if (gridDimensionsFieldEditor.getRadioSelected() == 1) {
            dimensions = PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10;
        }
        updatePlateGridWidget(orientation, dimensions);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    private void setEnabled(boolean enabled) {
        isEnabled = enabled;

        for (Text text : plateTextControls.values()) {
            if (text != null)
                text.setEnabled(enabled);
        }

        orientationFieldEditor.setEnabled(isEnabled, getFieldEditorParent());
        gridDimensionsFieldEditor.setEnabled(isEnabled, getFieldEditorParent());
        statusLabel.setText(isEnabled ? SCAN_REQ_STATUS_MSG : NOT_ENABLED_STATUS_MSG);
        notifyChangeListener(IPlateSettingsListener.ENABLED, enabled ? 1 : 0);
    }

    public void removePlateSettingsChangeListener(
        IPlateSettingsListener listener) {
        changeListeners.remove(listener);
    }

    public void addPlateBaseChangeListener(IPlateSettingsListener listener) {
        changeListeners.add(listener);
    }

    private void notifyChangeListener(final int message, final Object data) {
        if (internalUpdate) return;

        Object[] listeners = changeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final IPlateSettingsListener l = (IPlateSettingsListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    Event e = new Event();
                    e.type = message;
                    e.data = data;
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
        log.debug("performOK");
        saveSettings();
        return super.performOk();
    }

    @Override
    public void imageAvailable(Image image) {
        Assert.isNotNull(image);
        plateGridWidget.imageUpdated(image);
        updateGridSettings();
        if (statusLabel == null) {
            return;
        }

        statusLabel.setText(ALIGN_STATUS_MSG);
        Rectangle imgBounds = image.getBounds();
        double widthInches = imgBounds.width / (double) FlatbedImageScan.PLATE_IMAGE_DPI;
        double heightInches = imgBounds.height / (double) FlatbedImageScan.PLATE_IMAGE_DPI;
        Rectangle2D.Double flatbed = new Rectangle2D.Double(0, 0, widthInches, heightInches);

        ((DoubleFieldEditor) plateFieldEditors.get(Settings.LEFT)).setValidRange(0, widthInches);
        ((DoubleFieldEditor) plateFieldEditors.get(Settings.TOP)).setValidRange(0, heightInches);
        ((DoubleFieldEditor) plateFieldEditors.get(Settings.RIGHT)).setValidRange(0, widthInches);
        ((DoubleFieldEditor) plateFieldEditors.get(Settings.BOTTOM)).setValidRange(0, heightInches);

        plateGrid = new PlateGrid(flatbed, getOrientation(), getDimensionsFromSettings());
        double left = Double.parseDouble(plateFieldEditors.get(Settings.LEFT).getStringValue());
        double top = Double.parseDouble(plateFieldEditors.get(Settings.TOP).getStringValue());
        double right = Double.parseDouble(plateFieldEditors.get(Settings.RIGHT).getStringValue());
        double bottom = Double.parseDouble(plateFieldEditors.get(Settings.BOTTOM).getStringValue());
        plateGrid.setPlate(left, top, right - left, bottom - top);
    }

    @Override
    public void imageDeleted() {
        plateGridWidget.imageUpdated(null);
        if (statusLabel == null)
            return;
        statusLabel.setText(SCAN_REQ_STATUS_MSG);
    }

    @Override
    public void plateUpdated(Rectangle2D.Double plate) {
        log.debug("sizeChanged");
        statusLabel.setText(ALIGN_STATUS_MSG);

        internalUpdate(plate);
    }

    private PlateDimensions getDimensionsFromSettings() {
        IPreferenceStore prefs = ScannerConfigPlugin.getDefault().getPreferenceStore();
        String plateDimString = prefs.getString(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS[plateId - 1]);

        if (plateDimString.equals(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12)) {
            return PlateDimensions.DIM_ROWS_8_COLS_12;
        } else if (plateDimString.equals(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10)) {
            return PlateDimensions.DIM_ROWS_8_COLS_12;
        }

        throw new IllegalStateException("invalid plate grid dimensions: " + plateDimString);
    }

    /**
     * @return A {@link Rectangle} containing the top left corner of the plate and its width and
     *         height in inches. With (0,0) being the top left corner.
     */
    public Rectangle2D.Double getPlate() {
        if (plateGrid == null) return null;

        return plateGrid.getRectangle();
    }

    public Pair<Integer, Integer> getPlateDimensions() {
        return plateGrid.getDimensions();
    }

    public PlateOrientation getOrientation() {
        return plateGrid.getOrientation();
    }
}
