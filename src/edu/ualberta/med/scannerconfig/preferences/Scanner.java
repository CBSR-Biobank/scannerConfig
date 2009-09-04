package edu.ualberta.med.scannerconfig.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;

public class Scanner extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    public Scanner() {
        super(GRID);
        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
    }

    @Override
    public void createFieldEditors() {
        RadioGroupFieldEditor rgFe = new RadioGroupFieldEditor(
            PreferenceConstants.SCANNER_DRV_TYPE, "Driver Type", 2,
            new String[][] {
                { "TWAIN", PreferenceConstants.SCANNER_DRV_TYPE_TWAIN },
                { "WIA", PreferenceConstants.SCANNER_DRV_TYPE_WIA } },
            getFieldEditorParent(), true);
        addField(rgFe);

        rgFe = new RadioGroupFieldEditor(PreferenceConstants.SCANNER_DPI,
            "DPI", 3, new String[][] { { "300", "300" }, { "400", "400" },
                { "600", "600" } }, getFieldEditorParent(), true);
        addField(rgFe);

        IntegerFieldEditor intFe = new IntegerFieldEditor(
            PreferenceConstants.SCANNER_BRIGHTNESS, "Brigtness",
            getFieldEditorParent());
        intFe.setValidRange(-1000, 1000);
        addField(intFe);

        intFe = new IntegerFieldEditor(PreferenceConstants.SCANNER_CONTRAST,
            "Contrast", getFieldEditorParent());
        intFe.setValidRange(-1000, 1000);
        addField(intFe);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

}
