package edu.ualberta.med.scannerconfig.widgets;

import java.awt.geom.Rectangle2D;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.scannerconfig.FlatbedImageScan;
import edu.ualberta.med.scannerconfig.ImageWithDpi;

/**
 * A widget that allows the user to manipulate a rectangle, representing a scanning region,
 * projected on to of an image of the entire flatbed scanning region.
 * 
 * The image should have a DPI of {@link FlatbedImageScan.PLATE_IMAGE_DPI}
 * 
 * @author loyola
 */
public class ScanRegionWidget extends ImageWithRegionWidget {

    private static Logger log = LoggerFactory.getLogger(ScanRegionWidget.class.getName());

    private final IScanRegionWidget parentWidget;

    protected boolean enabled = false;

    public ScanRegionWidget(Composite parent, IScanRegionWidget parentWidget) {
        super(parent);
        if (parentWidget == null) {
            throw new NullPointerException("parent widget is null");
        }
        this.parentWidget = parentWidget;
        setEnabled(enabled);
    }

    @Override
    protected void mouseDrag(MouseEvent e) {
        super.mouseDrag(e);
        notifyListener();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        notifyListener();
    }

    /*
     * Used to inform the parent widget that the user has used the mouse or the keyboard to change
     * the dimensions of the scan region.
     */
    private void notifyListener() {
        final Rectangle2D.Double plateInInches = getUserRegionInInches();

        SafeRunnable.run(new SafeRunnable() {
            @Override
            public void run() {
                parentWidget.scanRegionChanged(plateInInches);
            }
        });
    }

    /**
     * Called by parent widget when the dimensions of the scan region have been updated by the user.
     * 
     * Converts the scan region from inches to units used by the canvas that displays the flatbed
     * image.
     * 
     * @param scanRegionInInches
     */
    public void scanRegionDimensionsUpdated(Rectangle2D.Double scanRegionInInches) {
        setUserRegionInInches(scanRegionInInches);
        canvas.redraw();
        canvas.update();
    }

    /**
     * Called by parent widget when to enable or disable this scan region.
     * 
     * @param setting When {@link true} then the scan region is enabled. It is disabled otherwise.
     */
    public void setEnabled(boolean setting) {
        if ((canvas == null) || canvas.isDisposed()) return;
        log.trace("setEnabled: {}", setting);
        enabled = setting;

        if (!enabled) {
            super.imageUpdated(null);
        }

        canvas.redraw();
        canvas.update();
    }

    /**
     * Called by parent widget when a new flatbed image is available.
     */
    @Override
    public void imageUpdated(ImageWithDpi image) {
        super.imageUpdated(image);
        setEnabled(enabled);
    }
}
