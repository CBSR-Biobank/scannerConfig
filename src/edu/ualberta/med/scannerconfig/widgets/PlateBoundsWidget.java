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

import edu.ualberta.med.scannerconfig.ChangeListener;
import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.ScannerRegion.Orientation;
import edu.ualberta.med.scannerconfig.preferences.scanner.GridRegion;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateBase;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateScannedImage;

public class PlateBoundsWidget {

    private enum DragMode {
        NONE, MOVE, RESIZE_HORIZONTAL_LEFT, RESIZE_HORIZONTAL_RIGHT, RESIZE_VERTICAL_TOP, RESIZE_VERTICAL_BOTTOM, RESIZE_BOTTOM_RIGHT, RESIZE_TOP_LEFT
    };

    private boolean isEnabled;

    private PlateBase parentPlateBase;

    private Canvas canvas;

    private GridRegion gridRegion;

    protected ListenerList changeListeners = new ListenerList();

    private ChangeListener scannedImageListner, plateBaseChangeListner;

    private Image imageBuffer;

    private GC imageGC;

    private boolean drag = false;

    private Point startDragMousePt = new Point(0, 0);

    private Rectangle startGridRect = new Rectangle(0, 0, 0, 0);

    private DragMode dragMode = DragMode.NONE;

    public PlateBoundsWidget(final PlateBase plateBase, Canvas c) {

        parentPlateBase = plateBase;

        applyPlateBaseBindings();

        canvas = c;
        canvas.getParent().layout();
        canvas.pack();
        canvas.setFocus();
        canvas.redraw();
        canvas.update();
        applyCanvasBindings();

        setEnable(false);

        if (PlateScannedImage.instance().exists()) {
            gridRegion = new GridRegion(parentPlateBase.getScannerRegionText(),
                canvas);
        }
    }

    private void applyPlateBaseBindings() {

        plateBaseChangeListner = new ChangeListener() {
            @Override
            public void change(Event e) {

                switch (e.type) {
                case ChangeListener.PLATE_BASE_ORIENTATION:
                    setPlateOrientation(e.detail);
                    break;

                case ChangeListener.PLATE_BASE_TEXT_CHANGE:
                    assignRegions(parentPlateBase.getScannerRegionText());
                    break;

                case ChangeListener.PLATE_BASE_ENABLED:
                    setEnable(e.detail == 1);
                    break;

                case ChangeListener.PLATE_BASE_REFRESH:
                    if (PlateScannedImage.instance().exists()) {
                        gridRegion = new GridRegion(
                            parentPlateBase.getScannerRegionText(), canvas);
                    }
                    setEnable(parentPlateBase.isEnabled());
                    break;

                default:
                    break;
                }
            }
        };

        scannedImageListner = new ChangeListener() {
            @Override
            public void change(Event e) {
                if (e.type == ChangeListener.IMAGE_SCANNED) {

                    /* new image scanned */
                    if (e.detail == 1) {
                        gridRegion = new GridRegion(
                            parentPlateBase.getScannerRegionText(), canvas);
                        setEnable(parentPlateBase.isEnabled());
                    }
                    /* image scanned unsuccessfully */
                    else {
                        setEnable(false);
                    }
                }
            }
        };

        parentPlateBase.addPlateBaseChangeListener(plateBaseChangeListner);
        PlateScannedImage.instance().addScannedImageChangeListener(
            scannedImageListner);

    }

    private void applyCanvasBindings() {

        canvas.addMouseMoveListener(new MouseMoveListener() {

            @Override
            public void mouseMove(MouseEvent e) {
                if (notValid())
                    return;

                if (gridRegion.getRectangle().contains(e.x, e.y))
                    canvas.setFocus();

                if (drag) {
                    switch (dragMode) {
                    case MOVE:
                        gridRegion.setLeft((e.x - startDragMousePt.x)
                            + startGridRect.x);
                        gridRegion.setTop((e.y - startDragMousePt.y)
                            + startGridRect.y);
                        break;
                    case RESIZE_HORIZONTAL_RIGHT:
                        gridRegion.setWidth((e.x - startDragMousePt.x)
                            + startGridRect.width);
                        break;
                    case RESIZE_HORIZONTAL_LEFT:
                        gridRegion.setLeft((e.x - startDragMousePt.x)
                            + startGridRect.x);
                        gridRegion.setWidth((startDragMousePt.x - e.x)
                            + startGridRect.width);
                        break;
                    case RESIZE_VERTICAL_TOP:
                        gridRegion.setTop((e.y - startDragMousePt.y)
                            + startGridRect.y);
                        gridRegion.setHeight((startDragMousePt.y - e.y)
                            + startGridRect.height);
                        break;
                    case RESIZE_VERTICAL_BOTTOM:
                        gridRegion.setHeight((e.y - startDragMousePt.y)
                            + startGridRect.height);
                        break;

                    case RESIZE_BOTTOM_RIGHT:
                        gridRegion.setWidth((e.x - startDragMousePt.x)
                            + startGridRect.width);
                        gridRegion.setHeight((e.y - startDragMousePt.y)
                            + startGridRect.height);
                        break;

                    case RESIZE_TOP_LEFT:
                        gridRegion.setLeft((e.x - startDragMousePt.x)
                            + startGridRect.x);
                        gridRegion.setTop((e.y - startDragMousePt.y)
                            + startGridRect.y);
                        gridRegion.setHeight((startDragMousePt.y - e.y)
                            + startGridRect.height);
                        gridRegion.setWidth((startDragMousePt.x - e.x)
                            + startGridRect.width);
                    default:

                        break;
                    }

                    canvas.redraw();
                    notifyChangeListener();
                    return;
                }

                canvas.setCursor(new Cursor(canvas.getDisplay(),
                    SWT.CURSOR_ARROW));

                /*
                 * Creates rectangles on the perimeter of the gridRegion, the
                 * code then checks for mouse-rectangle intersection to check
                 * for moving and resizing of the widget.
                 */
                if (gridRegion == null)
                    return;

                if (gridRegion.getRectangle().contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_HAND));
                    dragMode = DragMode.MOVE;
                } else if (new Rectangle(gridRegion.getRectangle().x
                    + gridRegion.getRectangle().width, gridRegion
                    .getRectangle().y + gridRegion.getRectangle().height, 15,
                    15).contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZENWSE));
                    dragMode = DragMode.RESIZE_BOTTOM_RIGHT;
                } else if (new Rectangle(gridRegion.getRectangle().x - 10,
                    gridRegion.getRectangle().y - 10, 15, 15)
                    .contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZENWSE));
                    dragMode = DragMode.RESIZE_TOP_LEFT;

                }

                else if (new Rectangle(gridRegion.getRectangle().x
                    + gridRegion.getRectangle().width, gridRegion
                    .getRectangle().y, 10, gridRegion.getRectangle().height)
                    .contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZEE));
                    dragMode = DragMode.RESIZE_HORIZONTAL_RIGHT;
                } else if (new Rectangle(gridRegion.getRectangle().x - 10,
                    gridRegion.getRectangle().y, 10,
                    gridRegion.getRectangle().height).contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZEW));
                    dragMode = DragMode.RESIZE_HORIZONTAL_LEFT;
                } else if (new Rectangle(gridRegion.getRectangle().x,
                    gridRegion.getRectangle().y - 10,
                    gridRegion.getRectangle().width, 10).contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZEN));
                    dragMode = DragMode.RESIZE_VERTICAL_TOP;
                } else if (new Rectangle(gridRegion.getRectangle().x,
                    gridRegion.getRectangle().y
                        + gridRegion.getRectangle().height, gridRegion
                        .getRectangle().width, 10).contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZES));
                    dragMode = DragMode.RESIZE_VERTICAL_BOTTOM;

                } else {
                    dragMode = DragMode.NONE;
                }

            }
        });

        canvas.addListener(SWT.MouseWheel, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (notValid())
                    return;

                switch (event.type) {
                case SWT.MouseWheel:
                    gridRegion.setGapOffsetX(gridRegion.getGapOffsetX()
                        + event.count / 10.0);
                    gridRegion.setGapOffsetY(gridRegion.getGapOffsetY()
                        + event.count / 10.0);

                    canvas.redraw();
                    notifyChangeListener();

                    break;
                }
            }
        });

        canvas.addControlListener(new ControlListener() {

            @Override
            public void controlMoved(ControlEvent e) {
                if (notValid())
                    return;
                gridRegion.scaleGrid(canvas.getSize());
            }

            @Override
            public void controlResized(ControlEvent e) {
                if (notValid())
                    return;
                gridRegion.scaleGrid(canvas.getSize());
            }
        });

        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (notValid())
                    return;

                if (dragMode != DragMode.NONE) {
                    drag = true;
                    startDragMousePt.y = e.y;
                    startDragMousePt.x = e.x;
                    startGridRect = new Rectangle(gridRegion.getRectangle().x,
                        gridRegion.getRectangle().y,
                        gridRegion.getRectangle().width, gridRegion
                            .getRectangle().height);

                }
                canvas.redraw();
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (notValid())
                    return;

                drag = false;
                dragMode = DragMode.NONE;
            }
        });

        canvas.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (notValid())
                    return;

                if (!drag) {
                    switch (e.keyCode) {
                    case SWT.ARROW_LEFT:
                        gridRegion.setLeft(gridRegion.getLeft() - 1);
                        break;
                    case SWT.ARROW_RIGHT:
                        gridRegion.setLeft(gridRegion.getLeft() + 1);
                        break;
                    case SWT.ARROW_UP:
                        gridRegion.setTop(gridRegion.getTop() - 1);
                        break;
                    case SWT.ARROW_DOWN:
                        gridRegion.setTop(gridRegion.getTop() + 1);
                        break;
                    }
                    canvas.redraw();
                    notifyChangeListener();

                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        canvas.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {

                if (notValid()) {
                    e.gc.setForeground(new Color(canvas.getDisplay(), 255, 255,
                        255));
                    e.gc.fillRectangle(0, 0, canvas.getSize().x,
                        canvas.getSize().y);
                    return;
                }

                Rectangle imgBounds = PlateScannedImage.instance()
                    .getScannedImage().getBounds();
                Point canvasSize = canvas.getSize();

                double imgAspectRatio = (double) imgBounds.width
                    / (double) imgBounds.height;
                if (imgAspectRatio > 1)
                    canvasSize.y = (int) (canvasSize.x / imgAspectRatio);
                else
                    canvasSize.x = (int) (canvasSize.y * imgAspectRatio);

                imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
                imageGC = new GC(imageBuffer);
                imageGC.drawImage(PlateScannedImage.instance()
                    .getScannedImage(), 0, 0, PlateScannedImage.instance()
                    .getScannedImage().getBounds().width, PlateScannedImage
                    .instance().getScannedImage().getBounds().height, 0, 0,
                    canvas.getBounds().width, canvas.getBounds().height);

                imageGC.setForeground(new Color(canvas.getDisplay(), 255, 0, 0));

                imageGC.drawRectangle(gridRegion.getRectangle());

                drawGrid(imageGC, gridRegion.getOrientation());

                imageGC.setForeground(new Color(canvas.getDisplay(), 0, 0, 255));

                int left = (int) gridRegion.getLeft() - 1;
                int top = (int) gridRegion.getTop() - 1;
                int right = (int) gridRegion.getLeft()
                    + (int) gridRegion.getWidth() - 3;
                int bottom = (int) gridRegion.getTop()
                    + (int) gridRegion.getHeight() - 3;

                imageGC.drawOval(left, top, 1, 1);
                imageGC.drawOval(right, bottom, 6, 6);

                e.gc.drawImage(imageBuffer, 0, 0);
                imageGC.dispose();
                imageBuffer.dispose();
            }
        });
    }

    private void drawGrid(GC gc, Orientation orientation) {

        double X, Y;

        if (orientation == Orientation.HORIZONTAL) {
            X = 12.0;
            Y = 8.0;
        } else {
            X = 8.0;
            Y = 12.0;
        }

        double w = (gridRegion.getRectangle().width) / X;
        double h = (gridRegion.getRectangle().height) / Y;

        double ox = gridRegion.getRectangle().x;
        double oy = gridRegion.getRectangle().y;

        for (int j = 0; j < Y; j++) {
            for (int i = 0; i < X; i++) {

                double cx = ox + i * w + w / 2.0;
                double cy = oy + j * h + h / 2.0;

                Rectangle gridRect = new Rectangle(
                    (int) (cx - w / 2.0 + this.gridRegion.getGapOffsetX() / 2.0),
                    (int) (cy - h / 2.0 + gridRegion.getGapOffsetY() / 2.0),
                    (int) (w - gridRegion.getGapOffsetX() / 1.0),
                    (int) (h - gridRegion.getGapOffsetY() / 1.0));

                gc.setForeground(new Color(canvas.getDisplay(), 0, 255, 0));
                gc.drawRectangle(gridRect);

                if (orientation == Orientation.HORIZONTAL) {
                    if ((i == X - 1) && (j == 0)) {
                        gc.setBackground(new Color(canvas.getDisplay(), 0, 255,
                            255));
                        gc.fillRectangle(gridRect);
                    }
                } else {
                    if ((i == X - 1) && (j == Y - 1)) {
                        gc.setBackground(new Color(canvas.getDisplay(), 0, 255,
                            255));
                        gc.fillRectangle(gridRect);
                    }
                }

            }
        }
    }

    public void assignRegions(ScannerRegion sr) {
        Assert.isNotNull(canvas, "canvas is null");
        gridRegion = new GridRegion(sr, canvas);
        canvas.redraw();
    }

    public ScannerRegion getPlateRegion() {
        return gridRegion.getScannerRegion();
    }

    private boolean notValid() {
        return (!PlateScannedImage.instance().exists() || !isEnabled || gridRegion == null);
    }

    private void setEnable(boolean enabled) {
        isEnabled = enabled;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
            canvas.update();
        }
    }

    private void setPlateOrientation(int orientation) {
        if (notValid())
            return;

        gridRegion.setOrientation(orientation == 1 ? Orientation.VERTICAL
            : Orientation.HORIZONTAL);
        canvas.redraw();
    }

    public void dispose() {
        PlateScannedImage.instance().removeScannedImageChangeListener(
            scannedImageListner);
        parentPlateBase.removePlateBaseChangeListener(plateBaseChangeListner);
    }

    /* updates text fields in plateBase */
    public void addPlateWidgetChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    private void notifyChangeListener() {
        Object[] listeners = changeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ChangeListener l = (ChangeListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    Event e = new Event();
                    e.type = ChangeListener.PALLET_WIDGET_CGHANGED;
                    l.change(e);
                }
            });
        }
    }

}
