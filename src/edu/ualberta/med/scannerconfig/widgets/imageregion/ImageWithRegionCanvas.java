package edu.ualberta.med.scannerconfig.widgets.imageregion;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.scannerconfig.BarcodeImage;
import edu.ualberta.med.scannerconfig.imageregion.ImageRegion;
import edu.ualberta.med.scannerconfig.imageregion.PointToRegion;
import edu.ualberta.med.scannerconfig.imageregion.Swt2DUtil;
import edu.ualberta.med.scannerconfig.widgets.ImageCanvas;

/**
 * A widget that allows the user to manipulate a region, represented by an axis aligned rectangle,
 * representing a region of interested, which is displayed on top of an image.
 * 
 * @author loyola
 */
public class ImageWithRegionCanvas extends ImageCanvas {

    private static Logger log = LoggerFactory.getLogger(ImageWithRegionCanvas.class.getName());

    protected BarcodeImage barcodeImage;

    private PointToRegion dragRegion = PointToRegion.OUTSIDE_REGION;

    // user region stored in inches to allow for correct resizing of the widget
    //
    // this region is not translated, its starts at (0, 0)
    protected ImageRegion userRegionInInches;

    private AffineTransform regionToImageTransform = new AffineTransform();

    public Point2D.Double lastMousePosInRegion;

    private final Color colorRed;

    private final Color colorBlue;

    private final Color handleBackgroundColor;

    public ImageWithRegionCanvas(Composite parent) {
        super(parent, SWT.DOUBLE_BUFFERED);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setBackground(new Color(Display.getCurrent(), 255, 255, 255));

        Display display = getDisplay();
        colorRed = new Color(display, 255, 0, 0);
        colorBlue = new Color(display, 0, 0, 255);
        handleBackgroundColor = new Color(display, 246, 20, 20);

        addListeners();
    }

    @Override
    public void dispose() {
        colorRed.dispose();
        colorBlue.dispose();
        handleBackgroundColor.dispose();
    }

    private void addListeners() {
        addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                ImageWithRegionCanvas.this.mouseMove(e);
            }
        });

        addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                // do nothing
            }

            @Override
            public void keyPressed(KeyEvent e) {
                ImageWithRegionCanvas.this.keyPressed(e);
            }
        });

        addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseScrolled(MouseEvent e) {
                ImageWithRegionCanvas.this.mouseScrolled(e);
            }

        });
        setFocus();
    }

    @Override
    protected void paint(GC gc) {
        if (barcodeImage == null) {
            super.paint(gc);
            return;
        }

        if (userRegionInInches == null) {
            throw new IllegalStateException("user region is null");
        }

        // draw the region on the source image
        Image clippedImage = clippedSourceImage();
        Rectangle2D.Double regionRect = userRegionInInches.getRectangle();

        // ensure the region fits on the image
        if (barcodeImage.getRectangleInInches().contains(regionRect)) {
            // transform the region rectangle to the canvas
            Rectangle clientRect = getClientArea();
            Rectangle2D.Double regionRectOnImage =
                Swt2DUtil.transformRect(regionToImageTransform, regionRect);
            Rectangle2D.Double regionRectOnCanvas =
                Swt2DUtil.transformRect(sourceImageToCanvasTransform, regionRectOnImage);

            GC newGC = new GC(clippedImage);
            newGC.setClipping(clientRect);
            newGC.setForeground(colorRed);
            newGC.drawRectangle(
                (int) regionRectOnCanvas.x,
                (int) regionRectOnCanvas.y,
                (int) regionRectOnCanvas.width,
                (int) regionRectOnCanvas.height);

            // create resize handles
            newGC.setForeground(colorBlue);
            newGC.setBackground(handleBackgroundColor);
            for (Rectangle2D.Double rect : userRegionInInches.getResizeHandleRects().values()) {

                Rectangle2D.Double handleOnImage =
                    Swt2DUtil.transformRect(regionToImageTransform, rect);
                Rectangle2D.Double handleCanvas =
                    Swt2DUtil.transformRect(sourceImageToCanvasTransform, handleOnImage);

                newGC.fillRectangle(
                    (int) handleCanvas.x,
                    (int) handleCanvas.y,
                    (int) handleCanvas.width,
                    (int) handleCanvas.height);
            }

            newGC.dispose();
        }

        gc.drawImage(clippedImage, 0, 0);
        clippedImage.dispose();
    }

    /**
     * Called by parent widget when a new flatbed image is available.
     */
    protected void updateImage(BarcodeImage barcodeImage) {
        if (barcodeImage == null) {
            throw new IllegalArgumentException("barcode image is null");
        }
        removeImage();

        setSourceImage(barcodeImage.getImage());
        this.barcodeImage = barcodeImage;
        double scaleFactor = barcodeImage.getScaleFactor();

        // update the tranform to convert from inches to the image dimensions in pixels
        AffineTransform t = new AffineTransform();
        t.scale(scaleFactor, scaleFactor);
        regionToImageTransform = t;
        fitCanvas();
    }

    public void removeImage() {
        if (this.barcodeImage != null) {
            setSourceImage(null);
            this.barcodeImage.dispose();
            this.barcodeImage = null;
        }
    }

    /*
     * Zoom in or out when the user presses the Control key and scrolls up or down. Scroll up zooms
     * in.
     */
    protected void mouseScrolled(MouseEvent e) {
        if ((e.stateMask & SWT.MODIFIER_MASK & SWT.CTRL) == SWT.CTRL) {
            if (e.count >= 0) {
                // centerZoomIn(e.x, e.y);
                zoomIn();
            } else {
                // centerZoomOut(e.x, e.y);
                zoomOut();
            }
        }
    }

    @Override
    protected void mouseDown(MouseEvent e) {
        super.mouseDown(e);

        if (getSourceImage() == null) return;

        lastMousePosInRegion = canvasPointToRegion(e.x, e.y);
    }

    @Override
    protected void mouseUp(MouseEvent e) {
        super.mouseUp(e);

        if (getSourceImage() == null) return;

        dragRegion = PointToRegion.OUTSIDE_REGION;

    }

    private Point2D.Double canvasPointToRegion(double x, double y) {
        Point2D.Double canvasPoint = new Point2D.Double(x, y);
        Point2D.Double pointOnImage = Swt2DUtil.inverseTransformPoint(sourceImageToCanvasTransform, canvasPoint);
        Point2D.Double pointInInches = Swt2DUtil.inverseTransformPoint(regionToImageTransform, pointOnImage);
        return pointInInches;
    }

    /*
     * Called when the user is dragging the mouse over the canvas.
     */
    @Override
    protected void mouseDrag(MouseEvent e) {
        Point2D.Double mouseInRegion = canvasPointToRegion(e.x, e.y);
        double dx = mouseInRegion.x - lastMousePosInRegion.x;
        double dy = mouseInRegion.y - lastMousePosInRegion.y;

        log.trace("mousemove: dragMode: {}", dragRegion);

        switch (dragRegion) {
        case OUTSIDE_REGION:
            super.mouseDrag(e);
            break;
        case IN_REGION:
            userRegionInInches.translate(dx, dy);
            break;
        case IN_HANDLE_NORTH_WEST:
            userRegionInInches.resizeLeftEdge(dx);
            userRegionInInches.resizeTopEdge(dy);
            break;
        case IN_HANDLE_NORTH:
            userRegionInInches.resizeTopEdge(dy);
            break;
        case IN_HANDLE_NORTH_EAST:
            userRegionInInches.resizeRightEdge(dx);
            userRegionInInches.resizeTopEdge(dy);
            break;
        case IN_HANDLE_EAST:
            userRegionInInches.resizeRightEdge(dx);
            break;
        case IN_HANDLE_SOUTH_EAST:
            userRegionInInches.resizeRightEdge(dx);
            userRegionInInches.resizeBottomEdge(dy);
            break;
        case IN_HANDLE_SOUTH:
            userRegionInInches.resizeBottomEdge(dy);
            break;
        case IN_HANDLE_SOUTH_WEST:
            userRegionInInches.resizeLeftEdge(dx);
            userRegionInInches.resizeBottomEdge(dy);
            break;
        case IN_HANDLE_WEST:
            userRegionInInches.resizeLeftEdge(dx);
            break;

        default:
            throw new IllegalStateException("invalid value for drag region");
        }
        lastMousePosInRegion = mouseInRegion;
        redraw();
    }

    /*
     * Called when the user moves the mouse.
     */
    private void mouseMove(MouseEvent e) {
        if (getSourceImage() == null) return;

        if (userRegionInInches == null) {
            throw new IllegalStateException("user region is null");
        }

        setFocus();

        if (mouseDragInfo.mouseDrag) {
            mouseDrag(e);
            return;
        }

        setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
        Point2D.Double mousePointInInches = canvasPointToRegion(e.x, e.y);

        dragRegion = userRegionInInches.pointToRegion(mousePointInInches);
        switch (dragRegion) {
        case OUTSIDE_REGION:
            break;
        case IN_REGION:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
            break;
        case IN_HANDLE_NORTH_WEST:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_SIZENWSE));
            break;
        case IN_HANDLE_NORTH:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_SIZEN));
            break;
        case IN_HANDLE_NORTH_EAST:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_SIZENESW));
            break;
        case IN_HANDLE_EAST:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_SIZEE));
            break;
        case IN_HANDLE_SOUTH_EAST:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_SIZENWSE));
            break;
        case IN_HANDLE_SOUTH:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_SIZES));
            break;
        case IN_HANDLE_SOUTH_WEST:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_SIZENESW));
            break;
        case IN_HANDLE_WEST:
            setCursor(new Cursor(getDisplay(), SWT.CURSOR_SIZEW));
            break;

        default:
            throw new IllegalStateException("invalid value for drag region");
        }

        // log.debug("mouseMove: drag region: {}", dragRegion);
    }

    protected void keyPressed(KeyEvent e) {
        if (getSourceImage() == null) return;

        double dx = 0;
        double dy = 0;

        switch (e.keyCode) {
        case SWT.ARROW_LEFT:
            dx = -0.05;
            break;
        case SWT.ARROW_RIGHT:
            dx = 0.05;
            break;
        case SWT.ARROW_UP:
            dy = -0.05;
            break;
        case SWT.ARROW_DOWN:
            dy = 0.05;
            break;
        }
        userRegionInInches.translate(dx, dy);
        log.trace("keyPressed: after change: {}", e.keyCode);
        redraw();
    }

    /**
     * Returns the user region rectangle in inches.
     * 
     * @return
     */
    public Rectangle2D.Double getUserRegionInInches() {
        if (barcodeImage == null) {
            throw new IllegalStateException("image is null");
        }
        Rectangle2D.Double rect = userRegionInInches.getRectangle();
        return rect;
    }

    /**
     * Assigns the region in uints of inches. The region will then be converted to one that applies
     * to the size of the canvas the image is being displayed in.
     * 
     * @param barcodeImage The image the region is being defined in.
     * @param region a rectangle containing the top, left, widht and height of the region in units
     *            of inches.
     * @return Returns true if the region lies within the image.
     */
    public boolean setUserRegionInInches(BarcodeImage barcodeImage, Rectangle2D.Double region) {
        Rectangle2D.Double imageRect = barcodeImage.getRectangleInInches();

        if (imageRect.contains(region)) {
            userRegionInInches = new ImageRegion(imageRect, region);
            return true;
        }
        return false;
    }

    /**
     * Assign the region based on the previously loaded barcode image.
     * 
     * @param region a rectangle containing the top, left, widht and height of the region in units
     *            of inches.
     * @return Returns true if the region lies within the image.
     */
    public boolean setUserRegionInInches(Rectangle2D.Double region) {
        if (barcodeImage == null) {
            throw new IllegalStateException("barcodeImage is null");
        }
        return setUserRegionInInches(barcodeImage, region);
    }
}
