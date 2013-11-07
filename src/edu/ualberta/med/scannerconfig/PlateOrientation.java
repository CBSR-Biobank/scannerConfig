package edu.ualberta.med.scannerconfig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public enum PlateOrientation {
    LANDSCAPE("Landscape", Constants.i18n.tr("Landscape")),
    PORTRAIT("Portrait", Constants.i18n.tr("Portrait"));

    private static class Constants {
        private static final I18n i18n = I18nFactory.getI18n(PlateOrientation.class);
    }

    public static final int size = PlateOrientation.values().length;

    private final String id;
    private final String displayLabel;

    private static final Map<String, PlateOrientation> ID_MAP;

    static {
        Map<String, PlateOrientation> map = new LinkedHashMap<String, PlateOrientation>();

        for (PlateOrientation orientationEnum : values()) {
            PlateOrientation check = map.get(orientationEnum.getId());
            if (check != null) {
                throw new IllegalStateException("plate orientation value "
                    + orientationEnum.getId() + " used multiple times");
            }

            map.put(orientationEnum.getId(), orientationEnum);
        }

        ID_MAP = Collections.unmodifiableMap(map);
    }

    private PlateOrientation(String id, String displayLabel) {
        this.id = id;
        this.displayLabel = displayLabel;
    }

    public String getId() {
        return id;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public static Map<String, PlateOrientation> valuesMap() {
        return ID_MAP;
    }

    public static PlateOrientation getFromIdString(String id) {
        PlateOrientation result = valuesMap().get(id);
        if (result == null) {
            throw new IllegalStateException("invalid plate orientation: " + id);
        }
        return result;
    }
}
