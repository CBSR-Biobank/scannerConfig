package edu.ualberta.med.scannerconfig.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;
import edu.ualberta.med.scannerconfig.widgets.AdvancedRadioGroupFieldEditor;

public class Scanner extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage, SelectionListener {

    private Map<String, DoubleFieldEditor> dblFieldMap =
        new HashMap<String, DoubleFieldEditor>();
    private Map<String, IntegerFieldEditor> intFieldMap =
        new HashMap<String, IntegerFieldEditor>();

    Button selectScannerBtn;
    AdvancedRadioGroupFieldEditor dpiRadio, driverTypeRadio;

    IntegerFieldEditor brightnessInputField, contrastInputField,
        debugLevelInputField, thresholdInputField, squaredevInputField,
        correctionsInputField;

    DoubleFieldEditor scanGapDblInput, celldistDblInput;

    public Scanner() {
        super(GRID);
        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
    }

    @Override
    public void createFieldEditors() {

        int scannerCap = ScanLib.getInstance().slGetScannerCapability();

        selectScannerBtn = new Button(getFieldEditorParent(), SWT.NONE);
        selectScannerBtn.setText("Select Scanner");
        selectScannerBtn.setImage(ScannerConfigPlugin.getDefault()
            .getImageRegistry().get(ScannerConfigPlugin.IMG_SCANNER));
        selectScannerBtn.addSelectionListener(this);

        // TODO warn about driver-type changing
        driverTypeRadio =
            new AdvancedRadioGroupFieldEditor(
                PreferenceConstants.SCANNER_DRV_TYPE, "Driver Type", 2,
                new String[][] {
                    { "TWAIN", PreferenceConstants.SCANNER_DRV_TYPE_TWAIN },
                    { "WIA", PreferenceConstants.SCANNER_DRV_TYPE_WIA } },
                getFieldEditorParent(), true);
        addField(driverTypeRadio);

        dpiRadio =
            new AdvancedRadioGroupFieldEditor(PreferenceConstants.SCANNER_DPI,
                "DPI", 5, new String[][] {
                    { PreferenceConstants.SCANNER_300_DPI,
                        PreferenceConstants.SCANNER_300_DPI },
                    { PreferenceConstants.SCANNER_400_DPI,
                        PreferenceConstants.SCANNER_400_DPI },
                    { PreferenceConstants.SCANNER_600_DPI,
                        PreferenceConstants.SCANNER_600_DPI } },
                getFieldEditorParent(), true);

        addField(dpiRadio);

        brightnessInputField =
            new IntegerFieldEditor(PreferenceConstants.SCANNER_BRIGHTNESS,
                "Brightness:", getFieldEditorParent());
        brightnessInputField.setValidRange(-1000, 1000);
        addField(brightnessInputField);
        intFieldMap.put(brightnessInputField.getPreferenceName(),
            brightnessInputField);

        contrastInputField =
            new IntegerFieldEditor(PreferenceConstants.SCANNER_CONTRAST,
                "Contrast:", getFieldEditorParent());
        contrastInputField.setValidRange(-1000, 1000);
        addField(contrastInputField);
        intFieldMap.put(contrastInputField.getPreferenceName(),
            contrastInputField);

        debugLevelInputField =
            new IntegerFieldEditor(PreferenceConstants.DLL_DEBUG_LEVEL,
                "Decode Library Debug Level:", getFieldEditorParent());
        debugLevelInputField.setValidRange(0, 9);
        addField(debugLevelInputField);
        intFieldMap.put(debugLevelInputField.getPreferenceName(),
            debugLevelInputField);

        thresholdInputField =
            new IntegerFieldEditor(PreferenceConstants.LIBDMTX_EDGE_THRESH,
                "Decode Edge Threshold:", getFieldEditorParent());
        thresholdInputField.setValidRange(0, 100);
        addField(thresholdInputField);
        intFieldMap.put(thresholdInputField.getPreferenceName(),
            thresholdInputField);

        squaredevInputField =
            new IntegerFieldEditor(PreferenceConstants.LIBDMTX_SQUARE_DEV,
                "Decode Square Deviation:", getFieldEditorParent());
        squaredevInputField.setValidRange(0, 90);
        addField(squaredevInputField);
        intFieldMap.put(squaredevInputField.getPreferenceName(),
            squaredevInputField);

        correctionsInputField =
            new IntegerFieldEditor(PreferenceConstants.LIBDMTX_CORRECTIONS,
                "Decode Corrections:", getFieldEditorParent());
        correctionsInputField.setValidRange(0, 100);
        addField(correctionsInputField);
        intFieldMap.put(correctionsInputField.getPreferenceName(),
            correctionsInputField);

        scanGapDblInput =
            new DoubleFieldEditor(PreferenceConstants.LIBDMTX_SCAN_GAP,
                "Decode Scan Gap:", getFieldEditorParent());
        scanGapDblInput.setValidRange(0.0, 1.0);
        addField(scanGapDblInput);
        dblFieldMap.put(PreferenceConstants.LIBDMTX_SCAN_GAP, scanGapDblInput);

        celldistDblInput =
            new DoubleFieldEditor(PreferenceConstants.LIBDMTX_CELL_DISTANCE,
                "Decode Cell Distance:", getFieldEditorParent());
        celldistDblInput.setValidRange(0.0, 1.0);
        addField(celldistDblInput);
        dblFieldMap.put(PreferenceConstants.LIBDMTX_CELL_DISTANCE,
            celldistDblInput);
        setEnableAllWidgets((scannerCap & ScanLib.CAP_IS_SCANNER) != 0);
    }

    private void setEnableAllWidgets(boolean enableSettings) {

        if (enableSettings) {

            int scannerCap = ScanLib.getInstance().slGetScannerCapability();
            dpiRadio.setEnabledArray(new boolean[] {
                (scannerCap & ScanLib.CAP_DPI_300) != 0,
                (scannerCap & ScanLib.CAP_DPI_400) != 0,
                (scannerCap & ScanLib.CAP_DPI_600) != 0 }, 0,
                getFieldEditorParent());
        } else {
            dpiRadio.setEnabled(false, getFieldEditorParent());
        }

        selectScannerBtn.setEnabled(true);
        driverTypeRadio.setEnabled(enableSettings, getFieldEditorParent());
        brightnessInputField.setEnabled(enableSettings, getFieldEditorParent());
        contrastInputField.setEnabled(enableSettings, getFieldEditorParent());
        debugLevelInputField.setEnabled(enableSettings, getFieldEditorParent());
        thresholdInputField.setEnabled(enableSettings, getFieldEditorParent());
        squaredevInputField.setEnabled(enableSettings, getFieldEditorParent());
        correctionsInputField
            .setEnabled(enableSettings, getFieldEditorParent());
        scanGapDblInput.setEnabled(enableSettings, getFieldEditorParent());
        celldistDblInput.setEnabled(enableSettings, getFieldEditorParent());

    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        int scanlibReturn = ScanLib.getInstance().slSelectSourceAsDefault();
        int scannerCap = ScanLib.getInstance().slGetScannerCapability();

        if (scanlibReturn != ScanLib.SC_SUCCESS) {
            // just stay with the last selected source
            if ((scannerCap & ScanLib.CAP_IS_SCANNER) != 0) {
                return;
            }
            setEnableAllWidgets(false);
            ScannerConfigPlugin
                .openError("Source Selection Error",
                    "Please plug in a scanner and select an appropiate source driver.");
            return;
        }

        IPreferenceStore prefs =
            ScannerConfigPlugin.getDefault().getPreferenceStore();

        String drvSetting = null;
        boolean[] drvRadioSettings = new boolean[] { false, false };

        if ((scannerCap & ScanLib.CAP_IS_WIA) != 0) {
            drvSetting = PreferenceConstants.SCANNER_DRV_TYPE_WIA;
            drvRadioSettings[1] = true;
        } else {
            drvSetting = PreferenceConstants.SCANNER_DRV_TYPE_TWAIN;
            drvRadioSettings[0] = true;
        }

        prefs.setValue(PreferenceConstants.SCANNER_DRV_TYPE, drvSetting);
        driverTypeRadio.setSelectionArray(drvRadioSettings);
        driverTypeRadio.doLoad();

        String dpiSetting = null;
        boolean[] dpiRadioSettings = new boolean[] { false, false };

        if ((scannerCap & ScanLib.CAP_DPI_300) != 0) {
            dpiRadioSettings[0] = true;
            dpiSetting = PreferenceConstants.SCANNER_300_DPI;

        } else if ((scannerCap & ScanLib.CAP_DPI_400) != 0) {
            dpiRadioSettings[1] = true;
            dpiSetting = PreferenceConstants.SCANNER_400_DPI;

        } else if ((scannerCap & ScanLib.CAP_DPI_600) != 0) {
            dpiRadioSettings[2] = true;
            dpiSetting = PreferenceConstants.SCANNER_600_DPI;
        } else {
            ScannerConfigPlugin.openAsyncError("Scanner Error",
                "DPI is not supported");
            return;
        }

        prefs.setValue(PreferenceConstants.SCANNER_DPI, dpiSetting);
        dpiRadio.setSelectionArray(dpiRadioSettings);
        dpiRadio.doLoad();

        setEnableAllWidgets(true);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // do nothing
    }

}
