package edu.ualberta.med.scannerconfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public enum PlateDimensions {

    DIM_ROWS_8_COLS_12(new ImmutablePair<Integer, Integer>(8, 12), "ROWS_8_COLS_12", "8x12"),
    DIM_ROWS_10_COLS_10(new ImmutablePair<Integer, Integer>(10, 10), "ROWS_10_COLS_10", "10x10");

    private final ImmutablePair<Integer, Integer> dimensions;
    private final String preferenceStoreString;
    private final String displayString;

    private static final Map<String, PlateDimensions> PREF_STORE_STRING_MAP;

    static {
        Map<String, PlateDimensions> map = new HashMap<String, PlateDimensions>();

        for (PlateDimensions dimensionsEnum : values()) {
            PlateDimensions check = map.get(dimensionsEnum.toString());
            if (check != null) {
                throw new IllegalStateException("permission enum value "
                    + dimensionsEnum.toString() + " used multiple times");
            }

            map.put(dimensionsEnum.toString(), dimensionsEnum);
        }

        PREF_STORE_STRING_MAP = Collections.unmodifiableMap(map);
    }

    private PlateDimensions(ImmutablePair<Integer, Integer> dimensions,
        String preferenceStoreString,
        String displayString) {
        this.dimensions = dimensions;
        this.preferenceStoreString = preferenceStoreString;
        this.displayString = displayString;
    }

    @Override
    public String toString() {
        return preferenceStoreString;
    }

    public String getDisplayString() {
        return displayString;
    }

    public Pair<Integer, Integer> getDimensions() {
        return dimensions;
    }

    public Integer getRows() {
        return dimensions.left;
    }

    public Integer getCols() {
        return dimensions.right;
    }

    public static Map<String, PlateDimensions> valuesMap() {
        return PREF_STORE_STRING_MAP;
    }

    public static PlateDimensions getFromString(String prefStoreString) {
        PlateDimensions result = valuesMap().get(prefStoreString);
        if (result == null) {
            throw new IllegalStateException("invalid plate dimensions: " + prefStoreString);
        }
        return result;
    }
}
