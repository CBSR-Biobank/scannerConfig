package edu.ualberta.med.scannerconfig.widgets;

import java.awt.geom.Rectangle2D;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.util.SafeRunnable;
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

import edu.ualberta.med.scannerconfig.FlatbedImageScan;

/**
 * A widget that allows the user to manipulate a rectangle, representing a scanning region,
 * projected on to of an image of the entire flatbed scanning region.
 * 
 * @author loyola
 */
public class ScanRegionWidget implements MouseMoveListener,
    Listener, ControlListener, MouseListener, KeyListener, PaintListener {

    private static Logger log = LoggerFactory.getLogger(ScanRegionWidget.class.getName());

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

    private final IScanRegionWidget parentWidget;

    protected final Canvas canvas;

    private GC imageGC;

    private boolean drag = false;

    private final Point startDragMousePt = new Point(0, 0);

    private Rectangle2D.Double startGridRect = new Rectangle2D.Double(0, 0, 0, 0);

    private DragMode dragMode = DragMode.NONE;

    private Image image;

    protected boolean enabled = false;

    public ScanRegionWidget(Composite parent, IScanRegionWidget parentWidget) {
        if (parentWidget == null) {
            throw new NullPointerException("parent widget is null");
        }
        this.parentWidget = parentWidget;

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
        setEnabled(enabled);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void mouseDown(MouseEvent e) {
        if (!enabled || (image == null)) return;

        if (dragMode != DragMode.NONE) {
            drag = true;
            startDragMousePt.y = e.y;
            startDragMousePt.x = e.x;
            startGridRect = getScanRegionForCanvas();

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
        if (!enabled || (image == null)) return;

        drag = false;
        dragMode = DragMode.NONE;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void mouseMove(MouseEvent e) {
        if (!enabled || (image == null)) return;

        canvas.setFocus();

        if (drag) {
            mouseDrag(e);
            return;
        }

        canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_ARROW));

        Rectangle2D.Double scanRegionForCanvas = getScanRegionForCanvas();

        /*
         * Creates rectangles on the perimeter of the gridRegion, the code then checks for
         * mouse-rectangle intersection to check for moving and resizing of the widget.
         */
        if (scanRegionForCanvas != null) {
            if (scanRegionForCanvas.contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_HAND));
                dragMode = DragMode.MOVE;
            } else if (new Rectangle(
                (int) (scanRegionForCanvas.x + scanRegionForCanvas.width),
                (int) (scanRegionForCanvas.y + scanRegionForCanvas.height),
                15,
                15).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZENWSE));
                dragMode = DragMode.RESIZE_BOTTOM_RIGHT;
            } else if (new Rectangle(
                (int) (scanRegionForCanvas.x - 10),
                (int) (scanRegionForCanvas.y - 10),
                15, 15).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZENWSE));
                dragMode = DragMode.RESIZE_TOP_LEFT;
            } else if (new Rectangle(
                (int) (scanRegionForCanvas.x + scanRegionForCanvas.width),
                (int) (scanRegionForCanvas.y),
                10,
                (int) scanRegionForCanvas.height)
                .contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEE));
                dragMode = DragMode.RESIZE_HORIZONTAL_RIGHT;
            } else if (new Rectangle(
                (int) (scanRegionForCanvas.x - 10),
                (int) (scanRegionForCanvas.y),
                10,
                (int) scanRegionForCanvas.height).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEW));
                dragMode = DragMode.RESIZE_HORIZONTAL_LEFT;
            } else if (new Rectangle(
                (int) scanRegionForCanvas.x,
                (int) (scanRegionForCanvas.y - 10),
                (int) scanRegionForCanvas.width,
                10).contains(e.x, e.y)) {
                canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEN));
                dragMode = DragMode.RESIZE_VERTICAL_TOP;
            } else if (new Rectangle(
                (int) scanRegionForCanvas.x,
                (int) (scanRegionForCanvas.y + scanRegionForCanvas.height),
                (int) scanRegionForCanvas.width,
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
    private void mouseDrag(MouseEvent e) {
        Point canvasSize = canvas.getSize();
        int delta;
        Rectangle2D.Double newScanRegion = (Rectangle2D.Double) startGridRect.clone();

        switch (dragMode) {
        case MOVE:
            delta = e.x - startDragMousePt.x;
            if (delta < 0) {
                newScanRegion.x = Math.max(0, startGridRect.x + delta);
            } else {
                newScanRegion.x = Math.min(canvasSize.x - startGridRect.width - 4,
                    startGridRect.x + delta);
            }

            delta = e.y - startDragMousePt.y;
            if (delta < 0) {
                newScanRegion.y = Math.max(0, startGridRect.y + delta);
            } else {
                newScanRegion.y = Math.min(canvasSize.y - startGridRect.height - 4,
                    startGridRect.y + delta);
            }
            break;
        case RESIZE_HORIZONTAL_LEFT:
            newScanRegion = resizeLeftEdge(newScanRegion, e.x);
            break;
        case RESIZE_HORIZONTAL_RIGHT:
            newScanRegion = resizeRightEdge(newScanRegion, e.x, canvasSize.x);
            break;
        case RESIZE_VERTICAL_TOP:
            newScanRegion = resizeTopEdge(newScanRegion, e.y);
            break;
        case RESIZE_VERTICAL_BOTTOM:
            newScanRegion = resizeBottomEdge(newScanRegion, e.y, canvasSize.y);
            break;
        case RESIZE_BOTTOM_RIGHT:
            newScanRegion = resizeRightEdge(newScanRegion, e.x, canvasSize.x);
            newScanRegion = resizeBottomEdge(newScanRegion, e.y, canvasSize.y);
            break;

        case RESIZE_TOP_LEFT:
            newScanRegion = resizeLeftEdge(newScanRegion, e.x);
            newScanRegion = resizeTopEdge(newScanRegion, e.y);
        default:
            // do nothing
        }

        log.debug("mousemove: scanRegionForCanvas: {}", newScanRegion);
        canvas.redraw();
        notifyListener(newScanRegion);
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
        if (!enabled || (image == null)) return;
        canvas.redraw();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void controlResized(ControlEvent e) {
        if (!enabled || (image == null)) return;
        canvas.redraw();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (!enabled || (image == null) || drag) return;

        Point canvasSize;
        // log.debug("keyPressed: event: {}, plate: {}", e, scanRegionForCanvas);
        Rectangle2D.Double scanRegionForCanvas = getScanRegionForCanvas();

        switch (e.keyCode) {
        case SWT.ARROW_LEFT:
            if (scanRegionForCanvas.x - 1 > 0) {
                scanRegionForCanvas.x += -1;
            }
            break;
        case SWT.ARROW_RIGHT:
            canvasSize = canvas.getSize();
            if (scanRegionForCanvas.x + scanRegionForCanvas.width + 1 < canvasSize.x) {
                scanRegionForCanvas.x += 1;
            }
            break;
        case SWT.ARROW_UP:
            if (scanRegionForCanvas.y - 1 > 0) {
                scanRegionForCanvas.y += -1;
            }
            break;
        case SWT.ARROW_DOWN:
            canvasSize = canvas.getSize();
            if (scanRegionForCanvas.y + scanRegionForCanvas.height + 1 < canvasSize.y) {
                scanRegionForCanvas.y += 1;
            }
            break;
        }
        canvas.redraw();
        // log.debug("keyPressed: after change: plate: {}", scanRegionForCanvas);
        notifyListener(scanRegionForCanvas);
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
        Rectangle2D.Double scanRegionForCanvas = getScanRegionForCanvas();
        log.trace("paintControl: scanRegionForCanvas: {}", scanRegionForCanvas);

        Rectangle imageRect = image.getBounds();
        Image imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
        imageGC = new GC(imageBuffer);
        imageGC.drawImage(image, 0, 0, imageRect.width, imageRect.height,
            0, 0, canvas.getBounds().width, canvas.getBounds().height);

        Display display = canvas.getDisplay();
        Color a1BackgroundColor = new Color(display, 0, 255, 255);
        imageGC.setBackground(a1BackgroundColor);

        Color red = new Color(display, 255, 0, 0);
        Color blue = new Color(display, 0, 0, 255);
        Rectangle plateRect = new Rectangle(
            (int) (scanRegionForCanvas.x),
            (int) (scanRegionForCanvas.y),
            (int) (scanRegionForCanvas.width),
            (int) (scanRegionForCanvas.height));
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

    /*
     * These factors depend on the size and DPI of the image being displayed, and the size of the
     * canvas it is being displayed in.
     * 
     * @return a Pair, the left contains the widht factor and the right the height factor.
     */
    private Pair<Double, Double> getConversionFactors() {
        if (image == null) return null;

        Rectangle imgBounds = image.getBounds();
        Point canvasSize = canvas.getSize();

        double widthFactor = (double)
            FlatbedImageScan.PLATE_IMAGE_DPI * canvasSize.x / imgBounds.width;
        double heightFactor = (double)
            FlatbedImageScan.PLATE_IMAGE_DPI * canvasSize.y / imgBounds.height;
        return new ImmutablePair<Double, Double>(widthFactor, heightFactor);
    }

    /*
     * Converts the scan region from inches to units used by the canvas that displays the flatbed
     * image.
     */
    protected Rectangle2D.Double getScanRegionForCanvas() {
        Rectangle2D.Double scanRegionInInches = parentWidget.scanRegionInInches();
        Pair<Double, Double> factors = getConversionFactors();
        if (factors == null) return new Rectangle2D.Double();
        double widthFactor = factors.getLeft();
        double heightFactor = factors.getRight();

        Rectangle2D.Double scanRegionForCanvas = new Rectangle2D.Double(
            scanRegionInInches.x * widthFactor,
            scanRegionInInches.y * heightFactor,
            scanRegionInInches.width * widthFactor,
            scanRegionInInches.height * heightFactor);
        return scanRegionForCanvas;
    }

    /*
     * Used to inform the parent widget that the user has used the mouse or the keyboard to change
     * the dimensions of the scan region.
     */
    private void notifyListener(Rectangle2D.Double scanRegionFromCanvas) {
        Pair<Double, Double> factors = getConversionFactors();
        if (factors == null) {
            throw new IllegalStateException("cannot get conversion factors");
        }
        double widthFactor = factors.getLeft();
        double heightFactor = factors.getRight();

        final Rectangle2D.Double plateInInches = new Rectangle2D.Double(
            scanRegionFromCanvas.x / widthFactor,
            scanRegionFromCanvas.y / heightFactor,
            scanRegionFromCanvas.width / widthFactor,
            scanRegionFromCanvas.height / heightFactor);

        SafeRunnable.run(new SafeRunnable() {
            @Override
            public void run() {
                parentWidget.scanRegionChanged(plateInInches);
            }
        });
    }

    /**
     * Called by parent widget when a new flatbed image is available.
     */
    public void imageUpdated(Image image) {
        this.image = image;
        setEnabled(enabled);
    }

    /**
     * Called by parent widget when the dimensions of the scan region have been updated by the user.
     */
    public void scanRegionDimensionsUpdated() {
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
            image = null;
        }

        canvas.redraw();
        canvas.update();
    }

    /**
     * Called by parent widget when to refresh the flatbed image and the region superimposed on it.
     */
    public void refresh() {
        canvas.redraw();
    }
}
