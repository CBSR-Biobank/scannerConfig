package edu.ualberta.med.scannerconfig.widgets;

import java.awt.geom.Rectangle2D;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.scannerconfig.ImageWithDpi;

/**
 * A widget that allows the user to manipulate a rectangle, representing a scanning region,
 * projected on to of an image of the entire flatbed scanning region.
 * 
 * @author loyola
 */
public class ImageWithRegionWidget implements MouseMoveListener,
    Listener, ControlListener, MouseListener, KeyListener, PaintListener {

    private static Logger log = LoggerFactory.getLogger(ImageWithRegionWidget.class.getName());

    private enum DragMode {
        NONE,
        MOVE,
        RESIZE_HORIZONTAL_LEFT,
        RESIZE_HORIZONTAL_RIGHT,
        RESIZE_VERTICAL_TOP,
        RESIZE_VERTICAL_BOTTOM,
        RESIZE_BOTTOM_RIGHT,
        RESIZE_TOP_LEFT
    };

    protected ImageWithDpi image;

    protected final Canvas canvas;

    protected GC imageGC;

    private boolean drag = false;

    private final Point startDragMousePt = new Point(0, 0);

    private Rectangle2D.Double startGridRect = new Rectangle2D.Double(0, 0, 0, 0);

    protected Rectangle2D.Double userRegion;

    private DragMode dragMode = DragMode.NONE;

    public ImageWithRegionWidget(Composite parent) {
        canvas = new Canvas(parent, SWT.BORDER | SWT.NO_BACKGROUND);
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        canvas.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
        canvas.getParent().layout();
        canvas.pack();
        canvas.setFocus();
        canvas.redraw();
        canvas.update();
        canvas.addMouseMoveListener(this);
        canvas.addControlListener(this);
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
        canvas.addPaintListener(this);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void mouseDown(MouseEvent e) {
        if (image == null) return;

        if (dragMode != DragMode.NONE) {
            drag = true;
            startDragMousePt.y = e.y;
            startDragMousePt.x = e.x;
            startGridRect = userRegion;

        }
        canvas.redraw();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (image == null) return;

        drag = false;
        dragMode = DragMode.NONE;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void mouseMove(MouseEvent e) {
        if (image == null) return;

        canvas.setFocus();

        if (drag) {
            mouseDrag(e);
            return;
        }

        canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_ARROW));

        /*
         * Creates rectangles on the perimeter of the gridRegion, the code then checks for
         * mouse-rectangle intersection to check for moving and resizing of the widget.
         */
        if (userRegion != null) {
            if (userRegion.contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_HAND));
                dragMode = DragMode.MOVE;
            } else if (new Rectangle(
                (int) (userRegion.x + userRegion.width),
                (int) (userRegion.y + userRegion.height),
                15,
                15).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZENWSE));
                dragMode = DragMode.RESIZE_BOTTOM_RIGHT;
            } else if (new Rectangle(
                (int) (userRegion.x - 10),
                (int) (userRegion.y - 10),
                15, 15).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZENWSE));
                dragMode = DragMode.RESIZE_TOP_LEFT;
            } else if (new Rectangle(
                (int) (userRegion.x + userRegion.width),
                (int) (userRegion.y),
                10,
                (int) userRegion.height)
                .contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEE));
                dragMode = DragMode.RESIZE_HORIZONTAL_RIGHT;
            } else if (new Rectangle(
                (int) (userRegion.x - 10),
                (int) (userRegion.y),
                10,
                (int) userRegion.height).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEW));
                dragMode = DragMode.RESIZE_HORIZONTAL_LEFT;
            } else if (new Rectangle(
                (int) userRegion.x,
                (int) (userRegion.y - 10),
                (int) userRegion.width,
                10).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEN));
                dragMode = DragMode.RESIZE_VERTICAL_TOP;
            } else if (new Rectangle(
                (int) userRegion.x,
                (int) (userRegion.y + userRegion.height),
                (int) userRegion.width,
                10).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZES));
                dragMode = DragMode.RESIZE_VERTICAL_BOTTOM;
            } else {
                dragMode = DragMode.NONE;
            }
        }
    }

    /*
     * Called when the user is dragging the mouse over the canvas.
     */
    protected void mouseDrag(MouseEvent e) {
        Point canvasSize = canvas.getSize();
        int delta;
        userRegion = (Rectangle2D.Double) startGridRect.clone();

        switch (dragMode) {
        case MOVE:
            delta = e.x - startDragMousePt.x;
            if (delta < 0) {
                userRegion.x = Math.max(0, startGridRect.x + delta);
            } else {
                userRegion.x = Math.min(canvasSize.x - startGridRect.width - 4,
                    startGridRect.x + delta);
            }

            delta = e.y - startDragMousePt.y;
            if (delta < 0) {
                userRegion.y = Math.max(0, startGridRect.y + delta);
            } else {
                userRegion.y = Math.min(canvasSize.y - startGridRect.height - 4,
                    startGridRect.y + delta);
            }
            break;
        case RESIZE_HORIZONTAL_LEFT:
            userRegion = resizeLeftEdge(userRegion, e.x);
            break;
        case RESIZE_HORIZONTAL_RIGHT:
            userRegion = resizeRightEdge(userRegion, e.x, canvasSize.x);
            break;
        case RESIZE_VERTICAL_TOP:
            userRegion = resizeTopEdge(userRegion, e.y);
            break;
        case RESIZE_VERTICAL_BOTTOM:
            userRegion = resizeBottomEdge(userRegion, e.y, canvasSize.y);
            break;
        case RESIZE_BOTTOM_RIGHT:
            userRegion = resizeRightEdge(userRegion, e.x, canvasSize.x);
            userRegion = resizeBottomEdge(userRegion, e.y, canvasSize.y);
            break;

        case RESIZE_TOP_LEFT:
            userRegion = resizeLeftEdge(userRegion, e.x);
            userRegion = resizeTopEdge(userRegion, e.y);
        default:
            // do nothing
        }

        log.trace("mousemove: scanRegionForCanvas: {}", userRegion);
        canvas.redraw();
    }

    /*
     * Calculates the new scan region when the user is resizing the scan region by either left
     * clicking on the region's left edge or the top left corner.
     */
    private Rectangle2D.Double resizeLeftEdge(Rectangle2D.Double gridRect, int mousePosX) {
        double right = startGridRect.x + startGridRect.width;
        double posX = Math.max(0, Math.min(right, mousePosX));
        Rectangle2D.Double result = new Rectangle2D.Double();
        result.x = posX;
        result.y = gridRect.y;
        result.width = right - posX;
        result.height = gridRect.height;

        log.debug("resizeHorizontalLeft: right: {}, width: {}", right, result.width);
        return result;
    }

    /*
     * Calculates the new scan region when the user is resizing the scan region by either left
     * clicking on the region's right edge or the bottom right corner.
     */
    private Rectangle2D.Double resizeRightEdge(Rectangle2D.Double gridRect, int mousePosX,
        int maxSizeX) {
        double posX = Math.max(startGridRect.x, Math.min(maxSizeX, mousePosX));
        Rectangle2D.Double result = new Rectangle2D.Double();
        result.x = gridRect.x;
        result.y = gridRect.y;
        result.width = posX - startGridRect.x;
        result.height = gridRect.height;
        return result;
    }

    /*
     * Calculates the new scan region when the user is resizing the scan region by either left
     * clicking on the region's top edge or the top left corner.
     */
    private Rectangle2D.Double resizeTopEdge(Rectangle2D.Double gridRect, int mousePosY) {
        double bottom = startGridRect.y + startGridRect.height;
        double posY = Math.max(0, Math.min(bottom, mousePosY));
        Rectangle2D.Double result = new Rectangle2D.Double();
        result.x = gridRect.x;
        result.y = posY;
        result.width = gridRect.width;
        result.height = bottom - posY;
        return result;
    }

    /*
     * Calculates the new scan region when the user is resizing the scan region by either left
     * clicking on the region's bottom edge or the bottom right corner.
     */
    private Rectangle2D.Double resizeBottomEdge(Rectangle2D.Double gridRect, int mousePosY,
        int maxSizeY) {
        double posY = Math.max(startGridRect.y, Math.min(maxSizeY, mousePosY));
        Rectangle2D.Double result = new Rectangle2D.Double();
        result.x = gridRect.x;
        result.y = gridRect.y;
        result.width = gridRect.width;
        result.height = posY - startGridRect.y;
        return result;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleEvent(Event event) {
        // do nothing
    }

    /**
     * @inheritDoc
     */
    @Override
    public void controlMoved(ControlEvent e) {
        if (image == null) return;
        canvas.redraw();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void controlResized(ControlEvent e) {
        if (image == null) return;
        canvas.redraw();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (image == null) return;

        Point canvasSize;
        // log.debug("keyPressed: event: {}, plate: {}", e, newRegionAfterModify);

        switch (e.keyCode) {
        case SWT.ARROW_LEFT:
            if (userRegion.x - 1 > 0) {
                userRegion.x += -1;
            }
            break;
        case SWT.ARROW_RIGHT:
            canvasSize = canvas.getSize();
            if (userRegion.x + userRegion.width + 1 < canvasSize.x) {
                userRegion.x += 1;
            }
            break;
        case SWT.ARROW_UP:
            if (userRegion.y - 1 > 0) {
                userRegion.y += -1;
            }
            break;
        case SWT.ARROW_DOWN:
            canvasSize = canvas.getSize();
            if (userRegion.y + userRegion.height + 1 < canvasSize.y) {
                userRegion.y += 1;
            }
            break;
        }
        canvas.redraw();
        // log.debug("keyPressed: after change: plate: {}", scanRegionForCanvas);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * @inheritDoc
     */
    @Override
    public void paintControl(PaintEvent e) {
        if (image == null) {
            e.gc.setForeground(new Color(canvas.getDisplay(), 255, 255, 255));
            e.gc.fillRectangle(0, 0, canvas.getSize().x, canvas.getSize().y);
            return;
        }

        paintCanvas(e);
    }

    /**
     * If the flatbed image is available, the image is drawn on the canvas and the scan region
     * rectangle projected on top.
     */
    protected void paintCanvas(PaintEvent e) {
        log.trace("paintControl: scanRegionForCanvas: {}", userRegion);

        if (userRegion == null) return;

        Rectangle imageRect = image.getBounds();
        Image imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
        imageGC = new GC(imageBuffer);
        imageGC.drawImage(image.getImage(), 0, 0, imageRect.width, imageRect.height,
            0, 0, canvas.getBounds().width, canvas.getBounds().height);

        Display display = canvas.getDisplay();
        Color a1BackgroundColor = new Color(display, 0, 255, 255);
        imageGC.setBackground(a1BackgroundColor);

        Color red = new Color(display, 255, 0, 0);
        Color blue = new Color(display, 0, 0, 255);
        Rectangle plateRect = new Rectangle(
            (int) (userRegion.x),
            (int) (userRegion.y),
            (int) (userRegion.width),
            (int) (userRegion.height));
        imageGC.setForeground(red);
        imageGC.drawRectangle(plateRect);

        // create drag circles
        int left = plateRect.x;
        int top = plateRect.y;
        int right = plateRect.x + plateRect.width - 3;
        int bottom = plateRect.y + plateRect.height - 3;

        imageGC.setForeground(blue);
        imageGC.drawOval(left, top, 6, 6);
        imageGC.drawOval(right, bottom, 6, 6);

        e.gc.drawImage(imageBuffer, 0, 0);

        red.dispose();
        blue.dispose();

        imageGC.dispose();
        imageBuffer.dispose();
    }

    /**
     * Called by parent widget when a new flatbed image is available.
     */
    protected void imageUpdated(ImageWithDpi image) {
        this.image = image;
    }

    /**
     * Called by parent widget when to refresh the flatbed image and the region superimposed on it.
     */
    public void refresh() {
        canvas.redraw();
    }

    /**
     * Returns the user region rectangle in dimensions that apply to the size of the canvas.
     * 
     * @return
     */
    protected Rectangle2D.Double getUserRegion() {
        return this.userRegion;
    }

    /**
     * Assigns the region in dimensions that apply to the size of the canvas.
     * 
     * @param region a rectangle containing the top, left, widht and height of the region.
     */
    protected void setUserRegion(Rectangle2D.Double region) {
        this.userRegion = region;
    }

    /**
     * Returns the user region rectangle in inches. This depends on the image being displayed and
     * its DPI.
     * 
     * @return
     */
    protected Rectangle2D.Double getUserRegionInInches() {
        Pair<Double, Double> dimensionsInInches = image.getDimensionInInches();
        double imageWidthInInches = dimensionsInInches.getLeft();
        double imageHeightInInches = dimensionsInInches.getRight();
        Point canvasSize = canvas.getSize();

        Rectangle2D.Double plateInInches = new Rectangle2D.Double(
            imageWidthInInches * userRegion.x / canvasSize.x,
            imageHeightInInches * userRegion.y / canvasSize.y,
            imageWidthInInches * userRegion.width / canvasSize.x,
            imageHeightInInches * userRegion.height / canvasSize.y);
        return plateInInches;
    }

    /**
     * Assigns the region in uints of inches. The region will then be converted to one that applies
     * to the size of the canvas the image is being displayed in.
     * 
     * @param region a rectangle containing the top, left, widht and height of the region.
     */
    protected void setUserRegionInInches(Rectangle2D.Double region) {
        Pair<Double, Double> dimensionsInInches = image.getDimensionInInches();
        double imageWidthInInches = dimensionsInInches.getLeft();
        double imageHeightInInches = dimensionsInInches.getRight();
        Point canvasSize = canvas.getSize();

        userRegion = new Rectangle2D.Double(
            region.x * canvasSize.x / imageWidthInInches,
            region.y * canvasSize.y / imageHeightInInches,
            region.width * canvasSize.x / imageWidthInInches,
            region.height * canvasSize.y / imageHeightInInches);

        log.debug("setUserRegionInInches: region: {}", userRegion);
    }

}