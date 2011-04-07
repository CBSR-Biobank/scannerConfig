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

import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.IPlateImageListener;
import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.IPlateSettingsListener;
import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateGrid;
import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateGrid.Orientation;
import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateImageMgr;
import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateSettings;

public class PlateGridWidget implements IPlateImageListener,
    IPlateSettingsListener, MouseMoveListener, Listener, ControlListener,
    MouseListener, KeyListener, PaintListener {

    private enum DragMode {
        NONE, MOVE, RESIZE_HORIZONTAL_LEFT, RESIZE_HORIZONTAL_RIGHT, RESIZE_VERTICAL_TOP, RESIZE_VERTICAL_BOTTOM, RESIZE_BOTTOM_RIGHT, RESIZE_TOP_LEFT
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

    private PlateImageMgr plateImageMgr;

    public PlateGridWidget(PlateSettings plateSettings, Canvas c) {
        Assert.isNotNull(plateSettings);

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
        plateImageMgr = PlateImageMgr.instance();
        plateImageMgr.addScannedImageChangeListener(this);

        if (plateImageMgr.hasImage()) {
            plateImageNew();
        }
    }

    @Override
    public void plateImageNew() {
        haveImage = true;
        if (plateGrid == null) {
            plateGrid = new PlateGrid<Integer>();
            plateGrid.setOrientation(plateSettings.getOrientation());
            resizePlateGrid();
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
        case IPlateSettingsListener.ORIENTATION:
            setPlateOrientation(e.detail);
            break;

        case IPlateSettingsListener.TEXT_CHANGE:
            resizePlateGrid();
            canvas.redraw();
            break;

        case IPlateSettingsListener.ENABLED:
            setEnabled();
            resizePlateGrid();
            canvas.redraw();
            break;

        case IPlateSettingsListener.REFRESH:
            resizePlateGrid();
            canvas.redraw();
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

        Rectangle plateRect = new Rectangle(plateGrid.getLeft(),
            plateGrid.getTop(), plateGrid.getWidth(), plateGrid.getHeight());

        if (plateRect.contains(e.x, e.y))
            canvas.setFocus();

        if (drag) {
            int pos;
            Point canvasSize = canvas.getSize();

            switch (dragMode) {
            case MOVE:
                pos = e.x - startDragMousePt.x + startGridRect.x;
                if ((pos >= 0)
                    && (pos + startGridRect.width + 4 <= canvasSize.x)) {
                    plateGrid.setLeft(pos);
                }

                pos = e.y - startDragMousePt.y + startGridRect.y;
                if ((pos >= 0)
                    && (pos + startGridRect.height + 4 <= canvasSize.y)) {
                    plateGrid.setTop(pos);
                }
                break;
            case RESIZE_HORIZONTAL_RIGHT:
                if ((e.x <= canvasSize.x) && (e.x > startGridRect.x)) {
                    plateGrid.setWidth(e.x - startDragMousePt.x
                        + startGridRect.width);
                }
                break;
            case RESIZE_HORIZONTAL_LEFT:
                if (e.x <= startGridRect.x + startGridRect.width) {
                    plateGrid.setLeft(e.x - startDragMousePt.x
                        + startGridRect.x);
                    plateGrid.setWidth(startDragMousePt.x - e.x
                        + startGridRect.width);
                }
                break;
            case RESIZE_VERTICAL_TOP:
                if (e.y <= startGridRect.y + startGridRect.height) {
                    plateGrid
                        .setTop(e.y - startDragMousePt.y + startGridRect.y);
                    plateGrid.setHeight((startDragMousePt.y - e.y)
                        + startGridRect.height);
                }
                break;
            case RESIZE_VERTICAL_BOTTOM:
                if ((e.y <= canvasSize.y) && (e.y > plateGrid.getTop())) {
                    plateGrid.setHeight((e.y - startDragMousePt.y)
                        + startGridRect.height);
                }
                break;

            case RESIZE_BOTTOM_RIGHT:
                if ((e.x <= canvasSize.x) && (e.x > startGridRect.x)
                    && (e.y <= canvasSize.y) && (e.y > plateGrid.getTop())) {
                    plateGrid.setWidth((e.x - startDragMousePt.x)
                        + startGridRect.width);
                    plateGrid.setHeight((e.y - startDragMousePt.y)
                        + startGridRect.height);
                }
                break;

            case RESIZE_TOP_LEFT:
                if ((e.x >= 0)
                    && (e.x <= startGridRect.x + startGridRect.width)
                    && (e.y >= 0)
                    && (e.y <= startGridRect.y + startGridRect.height)) {
                    plateGrid.setLeft((e.x - startDragMousePt.x)
                        + startGridRect.x);
                    plateGrid.setTop((e.y - startDragMousePt.y)
                        + startGridRect.y);
                    plateGrid.setHeight((startDragMousePt.y - e.y)
                        + startGridRect.height);
                    plateGrid.setWidth((startDragMousePt.x - e.x)
                        + startGridRect.width);
                }
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

        if (event.type == SWT.MouseWheel) {
            int delta = 0;
            if (event.count < 0) {
                delta = 1;
            } else if (event.count > 0) {
                delta = -1;
            }

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
            startGridRect = new Rectangle(plateGrid.getLeft(),
                plateGrid.getTop(), plateGrid.getWidth(), plateGrid.getHeight());

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

        int left, top, width, height;
        Point canvasSize;

        if (!drag) {
            // FIXME - range checking required here
            switch (e.keyCode) {
            case SWT.ARROW_LEFT:
                left = plateGrid.getLeft();
                if (left > 0) {
                    plateGrid.setLeft(left - 1);
                }
                break;
            case SWT.ARROW_RIGHT:
                canvasSize = canvas.getSize();
                left = plateGrid.getLeft();
                width = plateGrid.getWidth();
                if (left + width < canvasSize.x) {
                    plateGrid.setLeft(left + 1);
                }
                break;
            case SWT.ARROW_UP:
                top = plateGrid.getTop();
                if (top > 0) {
                    plateGrid.setTop(top - 1);
                }
                break;
            case SWT.ARROW_DOWN:
                canvasSize = canvas.getSize();
                top = plateGrid.getTop();
                height = plateGrid.getHeight();
                if (top + height < canvasSize.y) {
                    plateGrid.setTop(top + 1);
                }
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

        Image image = plateImageMgr.getScannedImage();
        Assert.isNotNull(image);

        Image plateImage = plateImageMgr.getScannedImage();
        Rectangle imageRect = plateImage.getBounds();
        imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
        imageGC = new GC(imageBuffer);
        imageGC.drawImage(plateImage, 0, 0, imageRect.width, imageRect.height,
            0, 0, canvas.getBounds().width, canvas.getBounds().height);

        drawGrid(imageGC);

        // draw plate rect minus 1 pixel on each side so that it acts as a
        // border for the grid
        Rectangle plateRect = new Rectangle(plateGrid.getLeft() - 1,
            plateGrid.getTop() - 1, plateGrid.getWidth() + 2,
            plateGrid.getHeight() + 2);
        imageGC.setForeground(new Color(canvas.getDisplay(), 255, 0, 0));
        imageGC.drawRectangle(plateRect);

        // create drag circles
        int left = plateRect.x;
        int top = plateRect.y;
        int right = plateRect.x + plateRect.width - 3;
        int bottom = plateRect.y + plateRect.height - 3;

        imageGC.setForeground(new Color(canvas.getDisplay(), 0, 0, 255));
        imageGC.drawOval(left, top, 6, 6);
        imageGC.drawOval(right, bottom, 6, 6);

        e.gc.drawImage(imageBuffer, 0, 0);
        imageGC.dispose();
        imageBuffer.dispose();
    }

    private void drawGrid(GC gc) {
        Assert.isNotNull(plateGrid);
        Rectangle gridRect = new Rectangle(plateGrid.getLeft(),
            plateGrid.getTop(), plateGrid.getWidth(), plateGrid.getHeight());

        int rows = plateGrid.getMaxRows();
        int cols = plateGrid.getMaxCols();
        Orientation orientation = plateGrid.getOrientation();
        double cellWidth = gridRect.width / (double) cols;
        double cellHeight = gridRect.height / (double) rows;
        double gapX = plateGrid.getGapX();
        double gapY = plateGrid.getGapY();
        double doubleGapX = 2.0 * gapX;
        double doubleGapY = 2.0 * gapY;

        Rectangle cellRect;
        Color foregroundColor = new Color(canvas.getDisplay(), 0, 255, 0);
        Color a1BackgroundColor = new Color(canvas.getDisplay(), 0, 255, 255);
        gc.setForeground(foregroundColor);

        double cx, cy = gridRect.y;
        for (int row = 0; row < rows; row++, cy += cellHeight) {
            cx = gridRect.x;

            for (int col = 0; col < cols; col++, cx += cellWidth) {
                cellRect = new Rectangle((int) (cx + gapX), (int) (cy + gapY),
                    (int) (cellWidth - doubleGapX),
                    (int) (cellHeight - doubleGapY));

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
        Rectangle imgBounds = plateImageMgr.getScannedImage().getBounds();
        Point canvasSize = canvas.getSize();

        double widthFactor = (double) PlateImageMgr.PLATE_IMAGE_DPI
            * canvasSize.x / imgBounds.width;
        double heightFactor = (double) PlateImageMgr.PLATE_IMAGE_DPI
            * canvasSize.y / imgBounds.height;

        // ----------------------------------

        int left = (int) (plateSettings.getLeft() * widthFactor);
        int top = (int) (plateSettings.getTop() * heightFactor);
        int right = (int) (plateSettings.getRight() * widthFactor);
        int bottom = (int) (plateSettings.getBottom() * heightFactor);
        int gapX = (int) (plateSettings.getGapX() * widthFactor);
        int gapY = (int) (plateSettings.getGapY() * widthFactor);

        if ((left < 0) || (right < 0) || (top < 0) || (bottom < 0)
            || (left > canvasSize.x) || (right > canvasSize.x)
            || (left > right) || (top > canvasSize.y)
            || (bottom > canvasSize.y) || (top > bottom)) {
            // outside image boundaries, no need to update grid image
            return;
        }

        plateGrid.setLeft(left);
        plateGrid.setTop(top);
        plateGrid.setWidth(right - left);
        plateGrid.setHeight(bottom - top);
        plateGrid.setGapX(gapX);
        plateGrid.setGapY(gapY);
    }

    /**
     * Converts the plate grid parameters to inches.
     * 
     * @return
     */
    public PlateGrid<Double> getConvertedPlateRegion() {
        PlateGrid<Double> result = new PlateGrid<Double>();
        Rectangle imgBounds = plateImageMgr.getScannedImage().getBounds();
        Point canvasSize = canvas.getSize();

        double widthFactor = imgBounds.width
            / (double) PlateImageMgr.PLATE_IMAGE_DPI / canvasSize.x;
        double heightFactor = imgBounds.height
            / (double) PlateImageMgr.PLATE_IMAGE_DPI / canvasSize.y;

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
        plateImageMgr.removeScannedImageChangeListener(this);
        plateSettings.removePlateSettingsChangeListener(this);
    }

    /* updates text fields in plateBase */
    public void addPlateWidgetChangeListener(IPlateGridWidgetListener listener) {
        changeListeners.add(listener);
    }

    private void notifyChangeListener() {
        Object[] listeners = changeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final IPlateGridWidgetListener l = (IPlateGridWidgetListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    l.sizeChanged();
                }
            });
        }
    }

}
