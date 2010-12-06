package edu.ualberta.med.scannerconfig.preferences.scanner;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class Decoding extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    private Map<String, DoubleFieldEditor> dblFieldMap = new HashMap<String, DoubleFieldEditor>();
    private Map<String, IntegerFieldEditor> intFieldMap = new HashMap<String, IntegerFieldEditor>();

    IntegerFieldEditor debugLevelInputField, thresholdInputField,
        squaredevInputField, correctionsInputField;

    DoubleFieldEditor scanGapDblInput, celldistDblInput;

    public Decoding() {
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
    protected void createFieldEditors() {
        debugLevelInputField = new IntegerFieldEditor(
            PreferenceConstants.DLL_DEBUG_LEVEL, "Decode Library Debug Level:",
            getFieldEditorParent());
        debugLevelInputField.setValidRange(0, 9);
        addField(debugLevelInputField);
        intFieldMap.put(debugLevelInputField.getPreferenceName(),
            debugLevelInputField);

        thresholdInputField = new IntegerFieldEditor(
            PreferenceConstants.LIBDMTX_EDGE_THRESH, "Decode Edge Threshold:",
            getFieldEditorParent());
        thresholdInputField.setValidRange(0, 100);
        addField(thresholdInputField);
        intFieldMap.put(thresholdInputField.getPreferenceName(),
            thresholdInputField);

        squaredevInputField = new IntegerFieldEditor(
            PreferenceConstants.LIBDMTX_SQUARE_DEV, "Decode Square Deviation:",
            getFieldEditorParent());
        squaredevInputField.setValidRange(0, 90);
        addField(squaredevInputField);
        intFieldMap.put(squaredevInputField.getPreferenceName(),
            squaredevInputField);

        correctionsInputField = new IntegerFieldEditor(
            PreferenceConstants.LIBDMTX_CORRECTIONS, "Decode Corrections:",
            getFieldEditorParent());
        correctionsInputField.setValidRange(0, 100);
        addField(correctionsInputField);
        intFieldMap.put(correctionsInputField.getPreferenceName(),
            correctionsInputField);

        scanGapDblInput = new DoubleFieldEditor(
            PreferenceConstants.LIBDMTX_SCAN_GAP, "Decode Scan Gap:",
            getFieldEditorParent());
        scanGapDblInput.setValidRange(0.0, 1.0);
        addField(scanGapDblInput);
        dblFieldMap.put(PreferenceConstants.LIBDMTX_SCAN_GAP, scanGapDblInput);

        celldistDblInput = new DoubleFieldEditor(
            PreferenceConstants.LIBDMTX_CELL_DISTANCE, "Decode Cell Distance:",
            getFieldEditorParent());
        celldistDblInput.setValidRange(0.0, 1.0);
        addField(celldistDblInput);
        dblFieldMap.put(PreferenceConstants.LIBDMTX_CELL_DISTANCE,
            celldistDblInput);
    }

}
