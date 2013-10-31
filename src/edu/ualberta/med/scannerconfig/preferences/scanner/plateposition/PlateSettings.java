package edu.ualberta.med.scannerconfig.preferences.scanner.plateposition;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import edu.ualberta.med.scannerconfig.ScanRegion;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.widgets.IScanRegionWidget;
import edu.ualberta.med.scannerconfig.widgets.ScanRegionWidget;

/**
 * A preference page that manages the size of the scanning region for an individual plate.
 * 
 * @author loyola
 * 
 */
public class PlateSettings extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage, IScanImageListener, IScanRegionWidget {

    private static Logger log = LoggerFactory.getLogger(PlateSettings.class.getName());

    private static final I18n i18n = I18nFactory.getI18n(PlateSettings.class);

    @SuppressWarnings("nls")
    private enum Settings {
        LEFT(i18n.tr("Left")),
        TOP(i18n.tr("Top")),
        RIGHT(i18n.tr("Right")),
        BOTTOM(i18n.tr("Bottom")),
        BARCODE(i18n.tr("Barcode"));

        private final String label;

        private Settings(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

    };

    @SuppressWarnings("nls")
    private static final String NOT_ENABLED_STATUS_MSG = i18n.tr("Plate is not enabled");

    @SuppressWarnings("nls")
    private static final String ALIGN_STATUS_MSG = i18n.tr("Align rectangle with the edge tubes");

    @SuppressWarnings("nls")
    private static final String SCAN_REQ_STATUS_MSG = i18n.tr("A scan is required");

    protected ListenerList changeListeners = new ListenerList();

    protected int plateId;
    private boolean isEnabled;

    private Map<Settings, StringFieldEditor> plateFieldEditors;
    private Map<Settings, Text> plateTextControls;
    private ScanRegionWidget scanRegionWidget = null;

    private BooleanFieldEditor enabledFieldEditor;
    private Button scanBtn;
    private Button refreshBtn;

    private Label statusLabel;

    private final FlatbedImageScan flatbedImageScan;

    private Rectangle2D.Double flatbedRectangle;

    private ScanRegion scanRegion = null;

    private boolean internalUpdate = false;

    public PlateSettings(int plateId) {
        super(GRID);
        this.plateId = plateId;

        IPreferenceStore prefs = ScannerConfigPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(prefs);

        flatbedImageScan = new FlatbedImageScan();
        flatbedImageScan.addScannedImageChangeListener(this);
    }

    @Override
    public void dispose() {
        flatbedImageScan.removeScannedImageChangeListener(this);
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
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        top.setLayoutData(gd);

        final Control s = super.createContents(top);
        gd = (GridData) s.getLayoutData();
        if (gd == null) {
            gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        }
        gd.grabExcessHorizontalSpace = false;
        gd.widthHint = 160;
        s.setLayoutData(gd);

        Composite right = new Composite(top, SWT.NONE);
        right.setLayout(new GridLayout(1, false));
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.grabExcessHorizontalSpace = true;
        gd.widthHint = 200;
        right.setLayoutData(gd);

        scanRegionWidget = new ScanRegionWidget(right, this);

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
                scanWidgetRefresh();
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
                        if (scanRegion != null) {
                            double left = Double.parseDouble(plateFieldEditors.get(Settings.LEFT).getStringValue());
                            double top = Double.parseDouble(plateFieldEditors.get(Settings.TOP).getStringValue());
                            double right = Double.parseDouble(plateFieldEditors.get(Settings.RIGHT).getStringValue());
                            double bottom = Double.parseDouble(plateFieldEditors.get(Settings.BOTTOM).getStringValue());
                            scanRegion = new ScanRegion(flatbedRectangle,
                                new Rectangle2D.Double(left, top, right - left, bottom - top));

                            if (!internalUpdate) {
                                scanRegionDimensionsUpdated();
                            }
                        }
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
                internalUpdate(new Rectangle2D.Double(0, 0, 4, 3));
            }
            setEnabled(enabled);
        }
    }

    private void internalUpdate(Rectangle2D.Double plate) {
        log.trace("internalUpdate: plate: {}", plate);

        internalUpdate = true;
        plateTextControls.get(Settings.LEFT).setText(String.valueOf(plate.x));
        plateTextControls.get(Settings.TOP).setText(String.valueOf(plate.y));
        plateTextControls.get(Settings.RIGHT).setText(String.valueOf(plate.x + plate.width));
        plateTextControls.get(Settings.BOTTOM).setText(String.valueOf(plate.y + plate.height));
        plateTextControls.get(Settings.BARCODE).setText(getPreferenceStore().getString(
            PreferenceConstants.SCANNER_PLATE_BARCODES[plateId - 1]));
        internalUpdate = false;
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

        statusLabel.setText(isEnabled ? SCAN_REQ_STATUS_MSG : NOT_ENABLED_STATUS_MSG);
        scanWidgetSetEnabled(enabled);
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
        if (statusLabel == null) {
            return;
        }

        statusLabel.setText(ALIGN_STATUS_MSG);
        Rectangle imgBounds = image.getBounds();
        double widthInches = imgBounds.width / (double) FlatbedImageScan.PLATE_IMAGE_DPI;
        double heightInches = imgBounds.height / (double) FlatbedImageScan.PLATE_IMAGE_DPI;
        flatbedRectangle = new Rectangle2D.Double(0, 0, widthInches, heightInches);

        ((DoubleFieldEditor) plateFieldEditors.get(Settings.LEFT)).setValidRange(0, widthInches);
        ((DoubleFieldEditor) plateFieldEditors.get(Settings.TOP)).setValidRange(0, heightInches);
        ((DoubleFieldEditor) plateFieldEditors.get(Settings.RIGHT)).setValidRange(0, widthInches);
        ((DoubleFieldEditor) plateFieldEditors.get(Settings.BOTTOM)).setValidRange(0, heightInches);

        double left = Double.parseDouble(plateFieldEditors.get(Settings.LEFT).getStringValue());
        double top = Double.parseDouble(plateFieldEditors.get(Settings.TOP).getStringValue());
        double right = Double.parseDouble(plateFieldEditors.get(Settings.RIGHT).getStringValue());
        double bottom = Double.parseDouble(plateFieldEditors.get(Settings.BOTTOM).getStringValue());

        scanRegion = new ScanRegion(flatbedRectangle,
            new Rectangle2D.Double(left, top, right - left, bottom - top));
        scanRegionWidget.imageUpdated(image);
    }

    @Override
    public void imageDeleted() {
        scanRegionWidget.imageUpdated(null);
        if (statusLabel == null)
            return;
        statusLabel.setText(SCAN_REQ_STATUS_MSG);
    }

    @Override
    public void scanRegionChanged(Rectangle2D.Double plate) {
        statusLabel.setText(ALIGN_STATUS_MSG);
        internalUpdate(plate);
    }

    private void scanRegionDimensionsUpdated() {
        if (scanRegionWidget != null) {
            scanRegionWidget.scanRegionDimensionsUpdated();
        }
    }

    private void scanWidgetSetEnabled(boolean setting) {
        if (scanRegionWidget != null) {
            scanRegionWidget.setEnabled(setting);
        }
    }

    private void scanWidgetRefresh() {
        if (scanRegionWidget != null) {
            scanRegionWidget.refresh();
        }
    }

    @Override
    public Rectangle2D.Double scanRegionInInches() {
        if (scanRegion != null) {
            return scanRegion.getRectangle();
        }
        return new Rectangle2D.Double();
    }
}
