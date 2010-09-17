package edu.ualberta.med.scannerconfig;

import org.eclipse.swt.widgets.Event;

public interface ChangeListener {
	public final static int IMAGE_SCANNED = 1;
	public final static int PALLET_WIDGET_CGHANGED = 2;
	public final static int PLATE_BASE_ORIENTATION = 3;
	public final static int PLATE_BASE_TEXT_CHANGE = 4;
	public final static int PLATE_BASE_ENABLED = 5;
	public final static int PLATE_BASE_REFRESH = 6;

	void change(Event e);
}
