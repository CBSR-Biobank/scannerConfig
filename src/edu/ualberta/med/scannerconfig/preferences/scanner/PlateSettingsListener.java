package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.swt.widgets.Event;

public interface PlateSettingsListener {
    public final static int ORIENTATION = 1;
    public final static int TEXT_CHANGE = 2;
    public final static int ENABLED = 3;
    public final static int REFRESH = 4;

    void plateGridChange(Event e);

}
