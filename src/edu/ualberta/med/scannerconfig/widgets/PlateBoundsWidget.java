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

import edu.ualberta.med.scannerconfig.preferences.scanner.ChangeListener;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateBase;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateGrid;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateGrid.Orientation;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateScannedImage;

public class PlateBoundsWidget {

    private enum DragMode {
        NONE, MOVE, RESIZE_HORIZONTAL_LEFT, RESIZE_HORIZONTAL_RIGHT, RESIZE_VERTICAL_TOP, RESIZE_VERTICAL_BOTTOM, RESIZE_BOTTOM_RIGHT, RESIZE_TOP_LEFT
    };

    private boolean isEnabled;

    private PlateBase parentPlateBase;

    private Canvas canvas;

    protected ListenerList changeListeners = new ListenerList();

    private ChangeListener scannedImageListner, plateBaseChangeListner;

    private Image imageBuffer;

    private GC imageGC;

    private boolean drag = false;

    private Point startDragMousePt = new Point(0, 0);

    private Rectangle startGridRect = new Rectangle(0, 0, 0, 0);

    private DragMode dragMode = DragMode.NONE;

    private double imgAspectRatio;

    private boolean haveImage;

    private PlateGrid plateGrid;

    public PlateBoundsWidget(PlateBase plateBase, Canvas c) {

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
        haveImage = false;
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

                    haveImage = (e.detail == 1);

                    if (!haveImage) {
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
                if (!haveImage)
                    return;

                if (plateGrid.getRectangle().contains(e.x, e.y))
                    canvas.setFocus();

                if (drag) {
                    switch (dragMode) {
                    case MOVE:
                        plateGrid.setLeft((e.x - startDragMousePt.x)
                            + startGridRect.x);
                        plateGrid.setTop((e.y - startDragMousePt.y)
                            + startGridRect.y);
                        break;
                    case RESIZE_HORIZONTAL_RIGHT:
                        plateGrid.setWidth((e.x - startDragMousePt.x)
                            + startGridRect.width);
                        break;
                    case RESIZE_HORIZONTAL_LEFT:
                        plateGrid.setLeft((e.x - startDragMousePt.x)
                            + startGridRect.x);
                        plateGrid.setWidth((startDragMousePt.x - e.x)
                            + startGridRect.width);
                        break;
                    case RESIZE_VERTICAL_TOP:
                        plateGrid.setTop((e.y - startDragMousePt.y)
                            + startGridRect.y);
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
                        plateGrid.setLeft((e.x - startDragMousePt.x)
                            + startGridRect.x);
                        plateGrid.setTop((e.y - startDragMousePt.y)
                            + startGridRect.y);
                        plateGrid.setHeight((startDragMousePt.y - e.y)
                            + startGridRect.height);
                        plateGrid.setWidth((startDragMousePt.x - e.x)
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
                if (plateGrid == null)
                    return;

                if (plateGrid.getRectangle().contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_HAND));
                    dragMode = DragMode.MOVE;
                } else if (new Rectangle(plateGrid.getRectangle().x
                    + plateGrid.getRectangle().width,
                    plateGrid.getRectangle().y
                        + plateGrid.getRectangle().height, 15, 15).contains(
                    e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZENWSE));
                    dragMode = DragMode.RESIZE_BOTTOM_RIGHT;
                } else if (new Rectangle(plateGrid.getRectangle().x - 10,
                    plateGrid.getRectangle().y - 10, 15, 15).contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZENWSE));
                    dragMode = DragMode.RESIZE_TOP_LEFT;

                }

                else if (new Rectangle(plateGrid.getRectangle().x
                    + plateGrid.getRectangle().width,
                    plateGrid.getRectangle().y, 10,
                    plateGrid.getRectangle().height).contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZEE));
                    dragMode = DragMode.RESIZE_HORIZONTAL_RIGHT;
                } else if (new Rectangle(plateGrid.getRectangle().x - 10,
                    plateGrid.getRectangle().y, 10,
                    plateGrid.getRectangle().height).contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZEW));
                    dragMode = DragMode.RESIZE_HORIZONTAL_LEFT;
                } else if (new Rectangle(plateGrid.getRectangle().x, plateGrid
                    .getRectangle().y - 10, plateGrid.getRectangle().width, 10)
                    .contains(e.x, e.y)) {
                    canvas.setCursor(new Cursor(canvas.getDisplay(),
                        SWT.CURSOR_SIZEN));
                    dragMode = DragMode.RESIZE_VERTICAL_TOP;
                } else if (new Rectangle(plateGrid.getRectangle().x, plateGrid
                    .getRectangle().y + plateGrid.getRectangle().height,
                    plateGrid.getRectangle().width, 10).contains(e.x, e.y)) {
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
                if (!haveImage)
                    return;

                switch (event.type) {
                case SWT.MouseWheel:
                    plateGrid.setGapX(plateGrid.getGapX() + event.count / 10);
                    plateGrid.setGapY(plateGrid.getGapY() + event.count / 10);

                    canvas.redraw();
                    notifyChangeListener();

                    break;
                }
            }
        });

        canvas.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {
                if (!haveImage)
                    return;
                plateGrid.scaleGrid(canvas.getSize());
            }

            @Override
            public void controlResized(ControlEvent e) {
                if (!haveImage)
                    return;
                plateGrid.scaleGrid(canvas.getSize());
            }
        });

        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (!haveImage)
                    return;

                if (dragMode != DragMode.NONE) {
                    drag = true;
                    startDragMousePt.y = e.y;
                    startDragMousePt.x = e.x;
                    startGridRect = new Rectangle(plateGrid.getRectangle().x,
                        plateGrid.getRectangle().y,
                        plateGrid.getRectangle().width, plateGrid
                            .getRectangle().height);

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
        });

        canvas.addKeyListener(new KeyListener() {

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
        });

        canvas.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {

                if (!haveImage)
                    e.gc.setForeground(new Color(canvas.getDisplay(), 255, 255,
                        255));
                    e.gc.fillRectangle(0, 0, canvas.getSize().x,
                        canvas.getSize().y);
                    return;
                }

                Rectangle imgBounds = PlateScannedImage.instance()
                    .getScannedImage().getBounds();
                Point canvasSize = canvas.getSize();

                imgAspectRatio = (double) imgBounds.width
                    / (double) imgBounds.height;
                if (imgAspectRatio > 1)
                    canvasSize.y = (int) (canvasSize.x / imgAspectRatio);
                else
                    canvasSize.x = (int) (canvasSize.y * imgAspectRatio);

                Image plateImage = PlateScannedImage.instance()
                    .getScannedImage();
                Rectangle plateRect = plateImage.getBounds();
                imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
                imageGC = new GC(imageBuffer);
                imageGC.drawImage(plateImage, 0, 0, plateRect.width,
                    plateRect.height, 0, 0, canvas.getBounds().width,
                    canvas.getBounds().height);

                imageGC.setForeground(new Color(canvas.getDisplay(), 255, 0, 0));

                imageGC.drawRectangle(plateGrid.getRectangle());

                drawGrid(imageGC);

                imageGC.setForeground(new Color(canvas.getDisplay(), 0, 0, 255));

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
        });
    }

    private void drawGrid(GC gc) {
        int rows, cols;
        Orientation orientation = plateGrid.getOrientation();

        if (orientation == Orientation.HORIZONTAL) {
            cols = 12;
            rows = 8;
        } else {
            cols = 8;
            rows = 12;
        }

        Rectangle canvasRect = canvas.getBounds();
        plateGrid.scaleGrid(new Point(canvasRect.width, canvasRect.height));
        Rectangle gridRect = plateGrid.getRectangle();

        double w = gridRect.width / cols / canvasRect.width;
        double h = gridRect.height / rows / canvasRect.height;

        double ox = gridRect.x;
        double oy = gridRect.y;

        double gapX = plateGrid.getGapX();
        double gapY = plateGrid.getGapY();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                double cx = ox + col * w + w / 2.0;
                double cy = oy + row * h + h / 2.0;

                Rectangle cellRect = new Rectangle(
                    (int) (cx - w / 2.0 + gapX / 2.0),
                    (int) (cy - h / 2.0 + gapY / 2.0), (int) (w - gapX / 1.0),
                    (int) (h - gapY / 1.0));

                gc.setForeground(new Color(canvas.getDisplay(), 0, 255, 0));
                gc.drawRectangle(cellRect);

                if (orientation == Orientation.HORIZONTAL) {
                    if ((col == cols - 1) && (row == 0)) {
                        gc.setBackground(new Color(canvas.getDisplay(), 0, 255,
                            255));
                        gc.fillRectangle(cellRect);
                    }
                } else {
                    if ((col == 0) && (row == 0)) {
                        gc.setBackground(new Color(canvas.getDisplay(), 0, 255,
                            255));
                        gc.fillRectangle(cellRect);
                    }
                }

            }
        }
    }

    public void assignRegions(PlateGrid sr) {
        Assert.isNotNull(canvas, "canvas is null");
        plateGrid = new PlateGrid(sr, canvas);
        canvas.redraw();
    }

    public PlateGrid getPlateRegion() {
        return plateGrid.getScannerRegion();
    }

    private void setEnable(boolean enabled) {
        isEnabled = enabled;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
            canvas.update();
        }
    }

    private void setPlateOrientation(int orientation) {
        if (!haveImage)
            return;

        plateGrid.setOrientation(orientation == 1 ? Orientation.VERTICAL
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
