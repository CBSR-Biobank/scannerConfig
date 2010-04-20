package edu.ualberta.med.scannerconfig.sourceproviders;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;

public class PlateEnabledState extends AbstractSourceProvider {

    public final static String PLATE_1_ENABLED = "edu.ualberta.med.scannerconfig.sourceprovider.plate1Enabled";
    public final static String PLATE_2_ENABLED = "edu.ualberta.med.scannerconfig.sourceprovider.plate2Enabled";
    public final static String PLATE_3_ENABLED = "edu.ualberta.med.scannerconfig.sourceprovider.plate3Enabled";
    public final static String PLATE_4_ENABLED = "edu.ualberta.med.scannerconfig.sourceprovider.plate4Enabled";
    public final static String PLATE_5_ENABLED = "edu.ualberta.med.scannerconfig.sourceprovider.plate5Enabled";

    @Override
    public void dispose() {
    }

    @Override
    public Map<String, Object> getCurrentState() {
        Map<String, Object> currentStateMap = new HashMap<String, Object>(1);
        currentStateMap.put(PLATE_1_ENABLED, new Boolean(ScannerConfigPlugin
            .getDefault().getPlateEnabled(1)));
        currentStateMap.put(PLATE_2_ENABLED, new Boolean(ScannerConfigPlugin
            .getDefault().getPlateEnabled(2)));
        currentStateMap.put(PLATE_3_ENABLED, new Boolean(ScannerConfigPlugin
            .getDefault().getPlateEnabled(3)));
        currentStateMap.put(PLATE_4_ENABLED, new Boolean(ScannerConfigPlugin
            .getDefault().getPlateEnabled(4)));
        currentStateMap.put(PLATE_5_ENABLED, new Boolean(ScannerConfigPlugin
            .getDefault().getPlateEnabled(5)));
        return currentStateMap;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { PLATE_1_ENABLED, PLATE_2_ENABLED,
            PLATE_3_ENABLED, PLATE_4_ENABLED, PLATE_5_ENABLED };
    }

    public void setPlateEnabled(int plate) {
        String[] sourceNames = getProvidedSourceNames();
        Assert.isTrue((plate > 0) && (plate <= sourceNames.length));
        fireSourceChanged(ISources.WORKBENCH, sourceNames[plate - 1],
            ScannerConfigPlugin.getDefault().getPlateEnabled(plate));
    }

}
