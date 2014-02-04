package edu.ualberta.med.scannerconfig.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.scannerconfig.ImageSource;

/**
 * Manages the settings for the {@link DecodeImageDialog}.
 * 
 * @author loyola
 * 
 */
public class ImageSourceDialogSettings {

    private static Logger log = LoggerFactory.getLogger(ImageSourceDialogSettings.class);

    @SuppressWarnings("nls")
    private static final String LAST_USED_IMAGE_SOURCE_KEY = "DecodePlateDialogSettings.last.image.source.";

    @SuppressWarnings("nls")
    private static final String DECODE_PLATE_SETTINGS_KEY = "DecodePlateDialogSettings.plate.settings.";

    private final IDialogSettings dialogSettings;

    private ImageSource imageSource;

    private final Map<String, ImageSourceSettings> imageSourceSettings;

    /**
     * This object is responsible for storage and retrieval of the settings used by
     * {@link DecodeImageDialog}.
     * 
     * @param settings The settings object for the dialog box.
     */
    public ImageSourceDialogSettings(IDialogSettings settings) {
        this.dialogSettings = settings;
        this.imageSourceSettings = new HashMap<String, ImageSourceSettings>(ImageSource.size);
        restore();
    }

    /**
     * Restores the settings from the dialog's settings store. If the settings store does not have a
     * value for a setting then the default values are assigned.
     */
    @SuppressWarnings("nls")
    public void restore() {
        imageSource = restoreImageSource();

        String[] values;
        for (ImageSource source : ImageSource.values()) {
            ImageSourceSettings settings = null;
            values = dialogSettings.getArray(DECODE_PLATE_SETTINGS_KEY + source.getId());
            if (values != null) {
                settings = ImageSourceSettings.getFromSettingsStringArray(source, values);
            } else {
                settings = ImageSourceSettings.defaultSettings(source);
                log.trace("restore: default value applied");
            }
            log.trace("restore: source: {}, settings: {}", source, settings.toSettingsStringArray());
            imageSourceSettings.put(source.getId(), settings);
        }
    }

    /**
     * Saves the settings to the dialog's setting store.
     */
    @SuppressWarnings("nls")
    public void save() {
        saveImageSource(getImageSource());
        for (ImageSourceSettings settings : imageSourceSettings.values()) {
            if (settings != null) {
                String key = DECODE_PLATE_SETTINGS_KEY + settings.getImageSource().getId();
                dialogSettings.put(key, settings.toSettingsStringArray());
                log.trace("save: source: {}, settings: {}", settings.getImageSource(), settings.toSettingsStringArray());
            }
        }
    }

    private ImageSource restoreImageSource() {
        String value = dialogSettings.get(LAST_USED_IMAGE_SOURCE_KEY);
        if (value != null) {
            return ImageSource.getFromIdString(value);
        }
        return ImageSource.FILE;
    }

    private void saveImageSource(ImageSource key) {
        dialogSettings.put(LAST_USED_IMAGE_SOURCE_KEY, key.getId());
    }

    public ImageSource getImageSource() {
        return imageSource;
    }

    public void setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
    }

    public ImageSourceSettings getImageSourceSettings(ImageSource source) {
        return imageSourceSettings.get(source.getId());
    }

}
