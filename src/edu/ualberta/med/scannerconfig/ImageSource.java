package edu.ualberta.med.scannerconfig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public enum ImageSource {
    FLATBED_SCANNER("IMAGE_SOURCE_FLATBED_SCANNER", Constants.i18n.tr("Flatbed scanner")),
    FILE("IMAGE_SOURCE_FILE", Constants.i18n.tr("File"));

    private static class Constants {
        private static final I18n i18n = I18nFactory.getI18n(ImageSource.class);
    }

    private final String id;
    private final String displayLabel;

    private static final Map<String, ImageSource> ID_MAP;

    static {
        Map<String, ImageSource> map = new LinkedHashMap<String, ImageSource>();

        for (ImageSource enumValue : values()) {
            ImageSource check = map.get(enumValue.getId());
            if (check != null) {
                throw new IllegalStateException("plate dimensions value "
                    + enumValue.getId() + " used multiple times");
            }

            map.put(enumValue.getId(), enumValue);
        }

        ID_MAP = Collections.unmodifiableMap(map);
    }

    private ImageSource(String id, String displayString) {
        this.id = id;
        this.displayLabel = displayString;
    }

    public String getId() {
        return id;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public static Map<String, ImageSource> valuesMap() {
        return ID_MAP;
    }

    public static ImageSource getFromIdString(String id) {
        ImageSource result = valuesMap().get(id);
        if (result == null) {
            throw new IllegalStateException("invalid image source: " + id);
        }
        return result;
    }

}
