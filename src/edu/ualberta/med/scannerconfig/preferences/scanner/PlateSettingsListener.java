package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.swt.widgets.Event;

public interface PlateSettingsListener {
    public final static int PLATE_BASE_ORIENTATION = 3;
    public final static int PLATE_BASE_TEXT_CHANGE = 4;
    public final static int PLATE_BASE_ENABLED = 5;
    public final static int PLATE_BASE_REFRESH = 6;

    void plateGridChange(Event e);

}
