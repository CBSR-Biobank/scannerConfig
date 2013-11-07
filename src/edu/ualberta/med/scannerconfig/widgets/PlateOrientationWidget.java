package edu.ualberta.med.scannerconfig.widgets;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.gui.common.widgets.GroupedRadioSelectionWidget;
import edu.ualberta.med.scannerconfig.PlateOrientation;

public class PlateOrientationWidget extends GroupedRadioSelectionWidget<PlateOrientation> {

    private static final I18n i18n = I18nFactory.getI18n(PlateOrientationWidget.class);

    private static final Map<PlateOrientation, String> SELECTIONS_MAP;

    static {
        Map<PlateOrientation, String> map = new LinkedHashMap<PlateOrientation, String>();

        for (PlateOrientation orientation : PlateOrientation.values()) {
            map.put(orientation, orientation.getDisplayLabel());
        }

        SELECTIONS_MAP = Collections.unmodifiableMap(map);
    }

    public PlateOrientationWidget(Composite parent, PlateOrientation initialValue) {
        super(parent, i18n.tr("Plate orientation"), SELECTIONS_MAP, initialValue);
    }
}
