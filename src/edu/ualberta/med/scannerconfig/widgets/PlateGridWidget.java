package edu.ualberta.med.scannerconfig.widgets;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import edu.ualberta.med.scannerconfig.preferences.scanner.PlateGrid;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateGrid.Orientation;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateImage;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateImageListener;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateSettings;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateSettingsListener;

public class PlateGridWidget implements PlateImageListener,
    PlateSettingsListener, MouseMoveListener, Listener, ControlListener,
    MouseListener, KeyListener, PaintListener {

    private enum DragMode {
        NONE, MOVE, RESIZE_HORIZONTAL_LEFT, RESIZE_HORIZONTAL_RIGHT,
        RESIZE_VERTICAL_TOP, RESIZE_VERTICAL_BOTTOM, RESIZE_BOTTOM_RIGHT,
        RESIZE_TOP_LEFT
    };

    private PlateSettings plateSettings;

    private Canvas canvas;

    protected ListenerList changeListeners = new ListenerList();

    private Image imageBuffer;

    private GC imageGC;

    private boolean drag = false;

    private Point startDragMousePt = new Point(0, 0);

    private Rectangle startGridRect = new Rectangle(0, 0, 0, 0);

    private DragMode dragMode = DragMode.NONE;

    private boolean haveImage;

    private PlateGrid<Integer> plateGrid = null;

    public PlateGridWidget(PlateSettings plateSettings, Canvas c) {

        this.plateSettings = plateSettings;

        canvas = c;
        canvas.getParent().layout();
        canvas.pack();
        canvas.setFocus();
        canvas.redraw();
        canvas.update();
        canvas.addMouseMoveListener(this);
        canvas.addListener(SWT.MouseWheel, this);
        canvas.addControlListener(this);
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
        canvas.addPaintListener(this);

        setEnabled();

        haveImage = false;

        plateSettings.addPlateBaseChangeListener(this);
        PlateImage.instance().addScannedImageChangeListener(this);
    }

    @Override
    public void plateImageNew() {
        haveImage = true;
        if (plateGrid == null) {
            plateGrid = new PlateGrid<Integer>();
        }
        canvas.redraw();
        setEnabled();
    }

    @Override
    public void plateImageDeleted() {
        haveImage = false;
        setEnabled();
    }

    @Override
    public void plateGridChange(Event e) {
        switch (e.type) {
        case PlateSettingsListener.ORIENTATION:
            setPlateOrientation(e.detail);
            break;

        case PlateSettingsListener.TEXT_CHANGE:
            canvas.redraw();
            break;

        case PlateSettingsListener.ENABLED:
            setEnabled();
            break;

        case PlateSettingsListener.REFRESH:
            setEnabled();
            break;

        default:
            Assert.isTrue(false, "invalid event received: " + e);
        }
    }

    @Override
    public void mouseMove(MouseEvent e) {
        if (!haveImage)
            return;

        Assert.isNotNull(plateGrid);

        Rectangle plateRect =
            new Rectangle(plateGrid.getLeft(), plateGrid.getTop(),
                plateGrid.getWidth(), plateGrid.getHeight());

        if (plateRect.contains(e.x, e.y))
            canvas.setFocus();

        if (drag) {
            switch (dragMode) {
            case MOVE:
                plateGrid.setLeft((e.x - startDragMousePt.x) + startGridRect.x);
                plateGrid.setTop((e.y - startDragMousePt.y) + startGridRect.y);
                break;
            case RESIZE_HORIZONTAL_RIGHT:
                plateGrid.setWidth((e.x - startDragMousePt.x)
                    + startGridRect.width);
                break;
            case RESIZE_HORIZONTAL_LEFT:
                plateGrid.setLeft((e.x - startDragMousePt.x) + startGridRect.x);
                plateGrid.setWidth((startDragMousePt.x - e.x)
                    + startGridRect.width);
                break;
            case RESIZE_VERTICAL_TOP:
                plateGrid.setTop((e.y - startDragMousePt.y) + startGridRect.y);
                plateGrid.setHeight((startDragMousePt.y - e.y)
                    + startGridRect.height);
                break;
            case RESIZE_VERTICAL_BOTTOM:
                plateGrid.setHeight((e.y - startDragMousePt.y)
                    + startGridRect.height);
                break;

            case RESIZE_BOTTOM_RIGHT:
                plateGrid.setWidth((e.x - startDragMousePt.x)
                    + startGridRect.width);
                plateGrid.setHeight((e.y - startDragMousePt.y)
                    + startGridRect.height);
                break;

            case RESIZE_TOP_LEFT:
                plateGrid.setLeft((e.x - startDragMousePt.x) + startGridRect.x);
                plateGrid.setTop((e.y - startDragMousePt.y) + startGridRect.y);
                plateGrid.setHeight((startDragMousePt.y - e.y)
                    + startGridRect.height);
                plateGrid.setWidth((startDragMousePt.x - e.x)
                    + startGridRect.width);
            default:
                // do nothing
            }

            canvas.redraw();
            notifyChangeListener();
            return;
        }

        canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_ARROW));

        /*
         * Creates rectangles on the perimeter of the gridRegion, the code then
         * checks for mouse-rectangle intersection to check for moving and
         * resizing of the widget.
         */

        if (plateRect.contains(e.x, e.y)) {
            canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_HAND));
            dragMode = DragMode.MOVE;
        } else if (new Rectangle(plateRect.x + plateRect.width, plateRect.y
            + plateRect.height, 15, 15).contains(e.x, e.y)) {
            canvas.setCursor(new Cursor(canvas.getDisplay(),
                SWT.CURSOR_SIZENWSE));
            dragMode = DragMode.RESIZE_BOTTOM_RIGHT;
        } else if (new Rectangle(plateRect.x - 10, plateRect.y - 10, 15, 15)
            .contains(e.x, e.y)) {
            canvas.setCursor(new Cursor(canvas.getDisplay(),
                SWT.CURSOR_SIZENWSE));
            dragMode = DragMode.RESIZE_TOP_LEFT;
        } else if (new Rectangle(plateRect.x + plateRect.width, plateRect.y,
            10, plateRect.height).contains(e.x, e.y)) {
            canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEE));
            dragMode = DragMode.RESIZE_HORIZONTAL_RIGHT;
        } else if (new Rectangle(plateRect.x - 10, plateRect.y, 10,
            plateRect.height).contains(e.x, e.y)) {
            canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEW));
            dragMode = DragMode.RESIZE_HORIZONTAL_LEFT;
        } else if (new Rectangle(plateRect.x, plateRect.y - 10,
            plateRect.width, 10).contains(e.x, e.y)) {
            canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEN));
            dragMode = DragMode.RESIZE_VERTICAL_TOP;
        } else if (new Rectangle(plateRect.x, plateRect.y + plateRect.height,
            plateRect.width, 10).contains(e.x, e.y)) {
            canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZES));
            dragMode = DragMode.RESIZE_VERTICAL_BOTTOM;
        } else {
            dragMode = DragMode.NONE;
        }

    }

    @Override
    public void handleEvent(Event event) {
        if (!haveImage)
            return;

        int delta = 1;
        if (event.count < 0) {
            delta = -1;
        }

        if (event.type == SWT.MouseWheel) {
            plateGrid.setGapX(plateGrid.getGapX() + delta);
            plateGrid.setGapY(plateGrid.getGapY() + delta);
            canvas.redraw();
            notifyChangeListener();
        }
    }

    @Override
    public void controlMoved(ControlEvent e) {
        if (!haveImage)
            return;
        canvas.redraw();
    }

    @Override
    public void controlResized(ControlEvent e) {
        if (!haveImage)
            return;
        resizePlateGrid();
        canvas.redraw();
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if (!haveImage)
            return;

        if (dragMode != DragMode.NONE) {
            drag = true;
            startDragMousePt.y = e.y;
            startDragMousePt.x = e.x;
            startGridRect =
                new Rectangle(plateGrid.getLeft(), plateGrid.getTop(),
                    plateGrid.getWidth(), plateGrid.getHeight());

        }
        canvas.redraw();
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (!haveImage)
            return;

        drag = false;
        dragMode = DragMode.NONE;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!haveImage)
            return;

        if (!drag) {
            switch (e.keyCode) {
            case SWT.ARROW_LEFT:
                plateGrid.setLeft(plateGrid.getLeft() - 1);
                break;
            case SWT.ARROW_RIGHT:
                plateGrid.setLeft(plateGrid.getLeft() + 1);
                break;
            case SWT.ARROW_UP:
                plateGrid.setTop(plateGrid.getTop() - 1);
                break;
            case SWT.ARROW_DOWN:
                plateGrid.setTop(plateGrid.getTop() + 1);
                break;
            }
            canvas.redraw();
            notifyChangeListener();

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void paintControl(PaintEvent e) {
        if (!haveImage || (plateGrid == null)) {
            e.gc.setForeground(new Color(canvas.getDisplay(), 255, 255, 255));
            e.gc.fillRectangle(0, 0, canvas.getSize().x, canvas.getSize().y);
            return;
        }

        Image image = PlateImage.instance().getScannedImage();
        Assert.isNotNull(image);

        Image plateImage = PlateImage.instance().getScannedImage();
        Rectangle imageRect = plateImage.getBounds();
        imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
        imageGC = new GC(imageBuffer);
        imageGC.drawImage(plateImage, 0, 0, imageRect.width, imageRect.height,
            0, 0, canvas.getBounds().width, canvas.getBounds().height);

        imageGC.setForeground(new Color(canvas.getDisplay(), 255, 0, 0));

        Rectangle plateRect =
            new Rectangle(plateGrid.getLeft(), plateGrid.getTop(),
                plateGrid.getWidth(), plateGrid.getHeight());
        imageGC.drawRectangle(plateRect);

        drawGrid(imageGC);
        imageGC.setForeground(new Color(canvas.getDisplay(), 0, 0, 255));

        // create drag circles
        int left = plateGrid.getLeft() - 1;
        int top = plateGrid.getTop() - 1;
        int right = plateGrid.getLeft() + plateGrid.getWidth() - 3;
        int bottom = plateGrid.getTop() + plateGrid.getHeight() - 3;

        imageGC.drawOval(left, top, 1, 1);
        imageGC.drawOval(right, bottom, 6, 6);

        e.gc.drawImage(imageBuffer, 0, 0);
        imageGC.dispose();
        imageBuffer.dispose();
    }

    private void drawGrid(GC gc) {
        int rows, cols;
        Orientation orientation = plateGrid.getOrientation();

        if (orientation == Orientation.LANDSCAPE) {
            rows = PlateGrid.MAX_ROWS;
            cols = PlateGrid.MAX_COLS;
        } else {
            rows = PlateGrid.MAX_COLS;
            cols = PlateGrid.MAX_ROWS;
        }

        Rectangle gridRect =
            new Rectangle(plateGrid.getLeft(), plateGrid.getTop(),
                plateGrid.getWidth(), plateGrid.getHeight());

        double gapX = plateGrid.getGapX();
        double gapY = plateGrid.getGapY();
        double cellWidth = gridRect.width / (double) cols;
        double cellHeight = gridRect.height / (double) rows;

        Rectangle cellRect;
        Color foregroundColor = new Color(canvas.getDisplay(), 0, 255, 0);
        Color a1BackgroundColor = new Color(canvas.getDisplay(), 0, 255, 255);
        gc.setForeground(foregroundColor);

        double cx, cy = gridRect.y;
        for (int row = 0; row < rows; row++, cy += cellHeight) {
            cx = gridRect.x;

            for (int col = 0; col < cols; col++, cx += cellWidth) {
                cellRect =
                    new Rectangle((int) (cx + gapX), (int) (cy + gapY),
                        (int) (cellWidth - 2 * gapX),
                        (int) (cellHeight - 2 * gapY));

                gc.drawRectangle(cellRect);

                if (orientation == Orientation.LANDSCAPE) {
                    if ((col == cols - 1) && (row == 0)) {
                        gc.setBackground(a1BackgroundColor);
                        gc.fillRectangle(cellRect);
                    }
                } else {
                    if ((col == 0) && (row == 0)) {
                        gc.setBackground(a1BackgroundColor);
                        gc.fillRectangle(cellRect);
                    }
                }

            }
        }
    }

    private void resizePlateGrid() {
        if (!haveImage)
            return;

        Assert.isNotNull(canvas, "canvas is null");
        Rectangle imgBounds =
            PlateImage.instance().getScannedImage().getBounds();
        Point canvasSize = canvas.getSize();

        double widthFactor =
            PlateImage.PLATE_IMAGE_DPI * canvasSize.x / imgBounds.width;
        double heightFactor =
            PlateImage.PLATE_IMAGE_DPI * canvasSize.y / imgBounds.height;

        plateGrid.setLeft((int) (plateSettings.getLeft() * widthFactor));
        plateGrid.setTop((int) (plateSettings.getTop() * heightFactor));
        plateGrid.setWidth((int) (plateSettings.getRight() * widthFactor)
            - plateGrid.getLeft());
        plateGrid.setHeight((int) (plateSettings.getBottom() * heightFactor)
            - plateGrid.getTop());
        plateGrid.setGapX((int) (plateSettings.getGapX() * widthFactor));
        plateGrid.setGapY((int) (plateSettings.getGapY() * widthFactor));
    }

    /**
     * Converts the plate grid parameters to inches.
     * 
     * @return
     */
    public PlateGrid<Double> getConvertedPlateRegion() {
        PlateGrid<Double> result = new PlateGrid<Double>();
        Rectangle imgBounds =
            PlateImage.instance().getScannedImage().getBounds();
        Point canvasSize = canvas.getSize();

        double widthFactor =
            imgBounds.width / PlateImage.PLATE_IMAGE_DPI / canvasSize.x;
        double heightFactor =
            imgBounds.height / PlateImage.PLATE_IMAGE_DPI / canvasSize.y;

        result.setLeft(plateGrid.getLeft() * widthFactor);
        result.setTop(plateGrid.getTop() * heightFactor);
        result.setWidth(plateGrid.getWidth() * widthFactor);
        result.setHeight(plateGrid.getHeight() * heightFactor);
        result.setGapX(plateGrid.getGapX() * widthFactor);
        result.setGapY(plateGrid.getGapY() * heightFactor);
        result.setOrientation(plateGrid.getOrientation());
        return result;
    }

    private void setEnabled() {
        if ((canvas == null) || canvas.isDisposed())
            return;

        canvas.redraw();
        canvas.update();
    }

    private void setPlateOrientation(int orientation) {
        if (!haveImage)
            return;

        plateGrid.setOrientation(orientation == 1 ? Orientation.PORTRAIT
            : Orientation.LANDSCAPE);
        canvas.redraw();
    }

    public void dispose() {
        PlateImage.instance().removeScannedImageChangeListener(this);
        plateSettings.removePlateSettingsChangeListener(this);
    }

    /* updates text fields in plateBase */
    public void addPlateWidgetChangeListener(PlateGridWidgetListener listener) {
        changeListeners.add(listener);
    }

    private void notifyChangeListener() {
        Object[] listeners = changeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final PlateGridWidgetListener l =
                (PlateGridWidgetListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    l.sizeChanged();
                }
            });
        }
    }

}
