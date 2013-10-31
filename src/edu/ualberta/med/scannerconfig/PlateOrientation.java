package edu.ualberta.med.scannerconfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PlateOrientation {
    LANDSCAPE("Landscape"),
    PORTRAIT("Portrait");

    private final String preferenceStoreString;

    private static final Map<String, PlateOrientation> LABELS_MAP;

    static {
        Map<String, PlateOrientation> map = new HashMap<String, PlateOrientation>();

        for (PlateOrientation orientationEnum : values()) {
            PlateOrientation check = map.get(orientationEnum.toString());
            if (check != null) {
                throw new IllegalStateException("permission enum value "
                    + orientationEnum.toString() + " used multiple times");
            }

            map.put(orientationEnum.toString(), orientationEnum);
        }

        LABELS_MAP = Collections.unmodifiableMap(map);
    }

    private PlateOrientation(String label) {
        this.preferenceStoreString = label;
    }

    @Override
    public String toString() {
        return preferenceStoreString;
    }

    public static Map<String, PlateOrientation> valuesMap() {
        return LABELS_MAP;
    }

    public static PlateOrientation getFromString(String label) {
        PlateOrientation result = valuesMap().get(label);
        if (result == null) {
            throw new IllegalStateException("invalid plate orientation: " + label);
        }
        return result;
    }
}
