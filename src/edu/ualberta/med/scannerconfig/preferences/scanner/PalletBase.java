package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class PalletBase extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    protected int palletId;

    public PalletBase(int palletId) {
        super(GRID);
        this.palletId = palletId;
        setPreferenceStore(ScannerConfigPlugin.getDefault()
            .getPreferenceStore());
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

    @Override
    public void init(IWorkbench workbench) {

    }

}
