package edu.ualberta.med.scannerconfig.widgets;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.gui.common.widgets.GroupedRadioSelectionWidget;
import edu.ualberta.med.scannerconfig.CameraPosition;

public class CameraPositionWidget extends GroupedRadioSelectionWidget<CameraPosition> {

    private static final I18n i18n = I18nFactory.getI18n(CameraPositionWidget.class);

    private static final Map<CameraPosition, String> SELECTIONS_MAP;

    static {
        Map<CameraPosition, String> map = new LinkedHashMap<CameraPosition, String>();

        for (CameraPosition orientation : CameraPosition.values()) {
            map.put(orientation, orientation.getDisplayLabel());
        }

        SELECTIONS_MAP = Collections.unmodifiableMap(map);
    }

    public CameraPositionWidget(Composite parent, CameraPosition initialValue) {
        super(parent, i18n.tr("Camera position"), SELECTIONS_MAP, initialValue);
    }
}
