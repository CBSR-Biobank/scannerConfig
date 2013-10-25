package edu.ualberta.med.scannerconfig;

import org.eclipse.swt.graphics.Image;

public interface IScanImageListener {

    /**
     * Called when a new image is present.
     */
    void imageAvailable(Image image);

    /**
     * Called when the image is no longer available.
     */
    void imageDeleted();
}
