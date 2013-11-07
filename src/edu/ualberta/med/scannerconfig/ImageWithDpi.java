package edu.ualberta.med.scannerconfig;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

public class ImageWithDpi {

    private final Image image;

    private final int dpi;

    public ImageWithDpi(String filename, int dpi) {
        image = new Image(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay(),
            filename);
        this.dpi = dpi;
    }

    public int getDpi() {
        return dpi;
    }

    public Rectangle getBounds() {
        return image.getBounds();
    }

    public Image getImage() {
        return image;
    }

    /**
     * Returns the dimensions of the image in inches.
     * 
     * @return a Pair, the left contains the width and the right the height in inches.
     */
    public Pair<Double, Double> getDimensionInInches() {
        Rectangle imgBounds = image.getBounds();
        double width = (double) imgBounds.width / dpi;
        double height = (double) imgBounds.height / dpi;
        return new ImmutablePair<Double, Double>(width, height);
    }

    public void dispose() {
        image.dispose();
    }

}
