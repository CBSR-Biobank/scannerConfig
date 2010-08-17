package edu.ualberta.med.scannerconfig.widgets;

import java.awt.geom.Point2D;
import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
<<<<<<< HEAD
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
=======
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scannerconfig.IPlateBoundsListener;
import edu.ualberta.med.scannerconfig.ScannerRegion;

public class PlateBoundsWidget {

	private Canvas canvas;

<<<<<<< HEAD
	private Image imageBuffer;
	private GC imageGC;

	public GridRegion getGridRegion() {
		if (gridRegion == null)
			this.gridRegion = new GridRegion(initialScannerRegion);
		return gridRegion;
	}

	/* please note that this can change value */
	public static double PALLET_IMAGE_DPI = 300.0;
	public static final String PALLET_IMAGE_FILE = "plates.bmp";
	private long platesFileLastModified;

	private enum DragMode {
		NONE, MOVE, RESIZE_HORIZONTAL_LEFT, RESIZE_HORIZONTAL_RIGHT, RESIZE_VERTICAL_TOP, RESIZE_VERTICAL_BOTTOM, RESIZE_VERTICAL_BOTTOM_RIGHT
	};

	private boolean drag = false;
	private Point startDragMousePt = new Point(0, 0);
	private Rectangle startGridRect = new Rectangle(0, 0, 0, 0);
	private DragMode dragMode = DragMode.NONE;

	protected ListenerList changeListeners = new ListenerList();

	private class GridRegion {
		private double left, top, width, height;
		private double gapOffsetX, gapOffsetY;

		public String name;

		private Point oldCanvasSize;

		public double regionToPixelWidth(double canvasWidth) {// canvas.getBounds().width
			return (scannedImage.getBounds().width / (canvasWidth * PALLET_IMAGE_DPI));
		}

		public double regionToPixelHeight(double canvasHeight) {
			return (scannedImage.getBounds().height / (canvasHeight * PALLET_IMAGE_DPI));
		}

		public void scaleGrid(Point newCanvasSize) {

			double horiztonalRatio = regionToPixelWidth(oldCanvasSize.x)
					/ regionToPixelWidth(newCanvasSize.x);

			double verticalRatio = regionToPixelHeight(oldCanvasSize.y)
					/ regionToPixelHeight(newCanvasSize.y);

			this.left = this.left * horiztonalRatio;
			this.top = this.top * verticalRatio;
			this.width = this.width * horiztonalRatio;
			this.height = this.height * verticalRatio;
			gapOffsetX = gapOffsetX * horiztonalRatio;
			gapOffsetY = gapOffsetY * verticalRatio;
			this.oldCanvasSize = newCanvasSize;
		}

		public double getGapOffsetX() {
			return gapOffsetX;
		}

		public double getGapOffsetY() {
			return gapOffsetY;
		}

		public void setGapOffsetX(double gap) {
			gapOffsetX = gap;
			if (gapOffsetX < 0)
				gapOffsetX = 0;
			double w = (getGridRegion().getRectangle().width) / 12.0;
			if (w - gapOffsetX < 0.1) {
				gapOffsetX = w - 0.1;
			}
		}

		public void setGapOffsetY(double gap) {
			gapOffsetY = gap;
			if (gapOffsetY < 0)
				gapOffsetY = 0;

			double h = (getGridRegion().getRectangle().height) / 8.0;
			if (h - gapOffsetY < 0.1) {
				gapOffsetY = h - 0.1;
			}
		}

		public GridRegion(ScannerRegion r) {

			double canvasWidth = canvas.getBounds().width;
			double canvasHeight = canvas.getBounds().height;

			this.name = r.name;

			this.gapOffsetX = r.gapX / regionToPixelWidth(canvasWidth);
			this.gapOffsetY = r.gapY / regionToPixelHeight(canvasHeight);
			this.left = r.left / regionToPixelWidth(canvasWidth);
			this.top = r.top / regionToPixelHeight(canvasHeight);
			this.width = (r.right - r.left) / regionToPixelWidth(canvasWidth);
			this.height = (r.bottom - r.top)
					/ regionToPixelHeight(canvasHeight);

			this.oldCanvasSize = canvas.getSize();

		}
=======
	public static final String PALLET_IMAGE_FILE = "plates.bmp";

	/* please note that this can change value */
	public static double PALLET_IMAGE_DPI = 300.0;

	private boolean pointTopLeft;
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b

	private Rectangle plateRect;

	private ScannerRegion scannerRegion;

<<<<<<< HEAD
			double canvasWidth = canvas.getBounds().width;
			double canvasHeight = canvas.getBounds().height;

			r.name = this.name;

			r.left = this.left * regionToPixelWidth(canvasWidth);
			r.top = this.top * regionToPixelHeight(canvasHeight);
			r.right = (this.width + this.left)
					* regionToPixelWidth(canvasWidth);
			r.bottom = (this.height + this.top)
					* regionToPixelHeight(canvasHeight);
			r.gapX = this.gapOffsetX * regionToPixelWidth(canvasWidth);
			r.gapY = this.gapOffsetY * regionToPixelHeight(canvasHeight);

			return r;

		}
=======
	private Image img;

	private long platesFileLastModified;
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b

	protected ListenerList changeListeners = new ListenerList();

	// pointTopLeft: Used to determine which point the user is currently
	// adjusting.The point is either top-left or bottom-right.

<<<<<<< HEAD
	public PlateBoundsWidget(Canvas c, ScannerRegion r) {
=======
	public PlateBoundsWidget(Canvas c, ScannerRegion r, final Color mycolor) {
		canvas = c;
		scannerRegion = r;
		plateRect = new Rectangle(-1, -1, -1, -1);
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b

		File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
		if (platesFile.exists()) {
			platesFileLastModified = platesFile.lastModified();
			img = new Image(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell().getDisplay(),
					PALLET_IMAGE_FILE);
		}

<<<<<<< HEAD
		canvas.getParent().layout();
		canvas.pack();
		canvas.setFocus();
		canvas.redraw();
		canvas.update();
=======
		pointTopLeft = true;
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b

		canvas.getParent().addControlListener(new ControlListener() {

			@Override
<<<<<<< HEAD
			public void mouseMove(MouseEvent e) {

				if (getGridRegion().getRectangle().contains(e.x, e.y))
					canvas.setFocus();

				if (drag) {
					switch (dragMode) {
					case MOVE:
						getGridRegion().left = (e.x - startDragMousePt.x)
								+ startGridRect.x;

						getGridRegion().top = (e.y - startDragMousePt.y)
								+ startGridRect.y;
						break;
					case RESIZE_HORIZONTAL_RIGHT:
						getGridRegion().width = (e.x - startDragMousePt.x)
								+ startGridRect.width;
						break;
					case RESIZE_HORIZONTAL_LEFT:
						getGridRegion().left = (e.x - startDragMousePt.x)
								+ startGridRect.x;
						getGridRegion().width = (startDragMousePt.x - e.x)
								+ startGridRect.width;
						break;
					case RESIZE_VERTICAL_TOP:
						getGridRegion().top = (e.y - startDragMousePt.y)
								+ startGridRect.y;
						getGridRegion().height = (startDragMousePt.y - e.y)
								+ startGridRect.height;
						break;
					case RESIZE_VERTICAL_BOTTOM:
						getGridRegion().height = (e.y - startDragMousePt.y)
								+ startGridRect.height;
						break;
					case RESIZE_VERTICAL_BOTTOM_RIGHT:
						getGridRegion().width = (e.x - startDragMousePt.x)
								+ startGridRect.width;
						getGridRegion().height = (e.y - startDragMousePt.y)
								+ startGridRect.height;
					default:
						break;
					}

					canvas.redraw();
					notifyChangeListener();
				} else {

					canvas.setCursor(new Cursor(canvas.getDisplay(),
							SWT.CURSOR_ARROW));
					if (gridRegion != null) {
						if (getGridRegion().getRectangle().contains(e.x, e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_HAND));
							dragMode = DragMode.MOVE;
						} else if (new Rectangle(
								getGridRegion().getRectangle().x
										+ getGridRegion().getRectangle().width,
								gridRegion.getRectangle().y
										+ getGridRegion().getRectangle().height,
								15, 15).contains(e.x, e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZENWSE));
							dragMode = DragMode.RESIZE_VERTICAL_BOTTOM_RIGHT;
						} else if (new Rectangle(
								getGridRegion().getRectangle().x
										+ getGridRegion().getRectangle().width,
								gridRegion.getRectangle().y, 10, gridRegion
										.getRectangle().height).contains(e.x,
								e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZEE));
							dragMode = DragMode.RESIZE_HORIZONTAL_RIGHT;
						} else if (new Rectangle(
								getGridRegion().getRectangle().x - 10,
								gridRegion.getRectangle().y, 10, gridRegion
										.getRectangle().height).contains(e.x,
								e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZEW));
							dragMode = DragMode.RESIZE_HORIZONTAL_LEFT;
						} else if (new Rectangle(
								getGridRegion().getRectangle().x,
								getGridRegion().getRectangle().y - 10,
								gridRegion.getRectangle().width, 10).contains(
								e.x, e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZEN));
							dragMode = DragMode.RESIZE_VERTICAL_TOP;
						} else if (new Rectangle(
								getGridRegion().getRectangle().x,
								getGridRegion().getRectangle().y
										+ getGridRegion().getRectangle().height,
								getGridRegion().getRectangle().width, 10)
								.contains(e.x, e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZES));
							dragMode = DragMode.RESIZE_VERTICAL_BOTTOM;

						} else {
							dragMode = DragMode.NONE;
						}
					}
				}
=======
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b

				if (canvas.getSize() != null
						&& !canvas.getSize().equals(new Point(0, 0))) {
					pointTopLeft = true;
				}
			}
		});
		canvas.addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseWheel:
					getGridRegion().setGapOffsetX(
							getGridRegion().getGapOffsetX() + event.count
									/ 10.0);
					getGridRegion().setGapOffsetY(
							getGridRegion().getGapOffsetY() + event.count
									/ 10.0);

					canvas.redraw();
					notifyChangeListener();

					break;
				}
			}
		});

		canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {

			}

			@Override
			public void controlResized(ControlEvent e) {
				getGridRegion().scaleGrid(canvas.getSize());
			}
		});

		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {

				if (img == null)
					return;

<<<<<<< HEAD
				if (dragMode != DragMode.NONE) {
					drag = true;
					startDragMousePt.y = e.y;
					startDragMousePt.x = e.x;
					startGridRect = new Rectangle(getGridRegion()
							.getRectangle().x,
							getGridRegion().getRectangle().y, gridRegion
									.getRectangle().width, gridRegion
									.getRectangle().height);
=======
				getPlateRect();

				if ((plateRect.x != 0 || plateRect.y != 0)
						&& Point2D.distance(e.x, e.y, plateRect.x, plateRect.y) < 30) {

					plateRect.width = plateRect.width + (plateRect.x - e.x);
					plateRect.height = plateRect.height + (plateRect.y - e.y);
					plateRect.x = e.x;
					plateRect.y = e.y;
				} else if ((plateRect.width != 0 || plateRect.height != 0)
						&& Point2D.distance(e.x, e.y, plateRect.x
								+ plateRect.width, plateRect.y
								+ plateRect.height) < 30) {
					plateRect.width = e.x - plateRect.x;
					plateRect.height = e.y - plateRect.y;
				} else {
					if (pointTopLeft) {
						plateRect.x = e.x;
						plateRect.y = e.y;
						plateRect.width = 0;
						plateRect.height = 0;
					} else {
						if (e.x > plateRect.x) {
							plateRect.width = e.x - plateRect.x;
						} else {
							plateRect.width = plateRect.x - e.x;
							plateRect.x = e.x;
						}
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b

						if (e.y > plateRect.y) {
							plateRect.height = e.y - plateRect.y;
						} else {
							plateRect.height = plateRect.y - e.y;
							plateRect.y = e.y;
						}
					}
					pointTopLeft = !pointTopLeft;
				}

				canvas.redraw();

				notifyChangeListener();
			}

			@Override
			public void mouseUp(MouseEvent e) {
<<<<<<< HEAD
				drag = false;
				dragMode = DragMode.NONE;
			}
		});

		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (!drag) {
					switch (e.keyCode) {
					case SWT.ARROW_LEFT:
						--getGridRegion().left;
						break;
					case SWT.ARROW_RIGHT:
						++getGridRegion().left;
						break;
					case SWT.ARROW_UP:
						--getGridRegion().top;
						break;
					case SWT.ARROW_DOWN:
						++getGridRegion().top;
						break;
					}
					canvas.redraw();

					notifyChangeListener();

				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
=======
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b
			}
		});

		canvas.getParent().layout();
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
				if (platesFile.exists()
						&& (platesFileLastModified != platesFile.lastModified())) {
					platesFileLastModified = platesFile.lastModified();
					img = new Image(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell()
									.getDisplay(), PALLET_IMAGE_FILE);
				}

				if (img == null)
					return;

<<<<<<< HEAD
				Rectangle imgBounds = scannedImage.getBounds();
				Point canvasSize = canvas.getSize();

				double imgAspectRatio = (double) imgBounds.width
						/ (double) imgBounds.height;
				if (imgAspectRatio > 1)
=======
				getPlateRect();

				Rectangle imgBounds = img.getBounds();

				double imgAspectRatio = (double) imgBounds.width
						/ (double) imgBounds.height;

				Point canvasSize = canvas.getSize();
				// wide
				if (imgAspectRatio > 1) {
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b
					canvasSize.y = (int) (canvasSize.x / imgAspectRatio);
				else
					canvasSize.x = (int) (canvasSize.y * imgAspectRatio);

				imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
				imageGC = new GC(imageBuffer);
				imageGC.drawImage(scannedImage, 0, 0,
						scannedImage.getBounds().width,
						scannedImage.getBounds().height, 0, 0,
						canvas.getBounds().width, canvas.getBounds().height);

<<<<<<< HEAD
				imageGC.setForeground(new Color(canvas.getDisplay(), 255, 0, 0));

				imageGC.drawRectangle(getGridRegion().getRectangle());

				drawGrid(imageGC);

				imageGC.setForeground(new Color(canvas.getDisplay(), 0, 0, 255));

				imageGC.drawOval((int) getGridRegion().left - 1,
						(int) getGridRegion().top - 1, 1, 1);

				imageGC.drawOval(
						(int) (getGridRegion().left + getGridRegion().width) - 3,
						(int) (getGridRegion().top + getGridRegion().height) - 3,
						6, 6);

				e.gc.drawImage(imageBuffer, 0, 0);
				imageBuffer.dispose();
			}
		});

	}

	private void drawGrid(GC gc) {

		double w = (getGridRegion().getRectangle().width) / 12.0;
		double h = (getGridRegion().getRectangle().height) / 8.0;
		double ox = getGridRegion().getRectangle().x;
		double oy = getGridRegion().getRectangle().y;

		for (int j = 0; j < 8; j++) {
			for (int i = 0; i < 12; i++) {

				double cx = ox + i * w + w / 2.0;
				double cy = oy + j * h + h / 2.0;

				gc.setForeground(new Color(canvas.getDisplay(), 0, 255, 0));
				gc.drawRectangle(new Rectangle(
						(int) (cx - w / 2.0 + getGridRegion().getGapOffsetX() / 2.0),
						(int) (cy - h / 2.0 + getGridRegion().getGapOffsetY() / 2.0),
						(int) (w - getGridRegion().getGapOffsetX() / 1.0),
						(int) (h - getGridRegion().getGapOffsetY() / 1.0)));

				gc.setForeground(new Color(canvas.getDisplay(), 255, 255, 0));
				gc.drawPoint((int) cx, (int) cy);
			}
		}
=======
				GC gc = new GC(canvas);

				// width/height <= 1
				gc.drawImage(img, 0, 0, imgBounds.width, imgBounds.height, 0,
						0, canvasBounds.width, canvasBounds.height);

				gc.setForeground(mycolor);
				gc.drawRectangle(plateRect);
				gc.drawOval(plateRect.x - 3, plateRect.y - 3, 6, 6);
				gc.drawOval(plateRect.x + plateRect.width - 3, plateRect.y
						+ plateRect.height - 3, 6, 6);
				gc.dispose();
			}
		});

		canvas.update();
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b
	}

	public void addChangeListener(IPlateBoundsListener listener) {
		changeListeners.add(listener);
	}

	private void notifyChangeListener() {
		Object[] listeners = changeListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			final IPlateBoundsListener l = (IPlateBoundsListener) listeners[i];
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					l.change();
				}
			});
		}

	}

<<<<<<< HEAD
	@Deprecated
	public void assignRegions(String name, double left, double top,
			double right, double bottom, double gapX, double gapY) {
		Assert.isNotNull(canvas, "canvas is null");
		gridRegion = new GridRegion(new ScannerRegion(name, left, top, right,
				bottom, gapX, gapY));
		canvas.redraw();

=======
	private void getPlateRect() {
		Assert.isNotNull(canvas, "canvas is null");

		if (plateRect.width != -1)
			return;

		Rectangle imgBounds = img.getBounds();
		Rectangle canvasBounds = canvas.getBounds();

		plateRect.x = (int) (scannerRegion.left * canvasBounds.width
				* PALLET_IMAGE_DPI / imgBounds.width);
		plateRect.y = (int) (scannerRegion.top * canvasBounds.height
				* PALLET_IMAGE_DPI / imgBounds.height);
		plateRect.width = (int) (scannerRegion.right * canvasBounds.width
				* PALLET_IMAGE_DPI / imgBounds.width)
				- plateRect.x;
		plateRect.height = (int) (scannerRegion.bottom * canvasBounds.height
				* PALLET_IMAGE_DPI / imgBounds.height)
				- plateRect.y;
>>>>>>> b93d850637fb5f899c40c8557566b313f861ec9b
	}

	public void assignRegionLeft(double left) {
		Assert.isNotNull(canvas, "canvas is null");

		Rectangle imgBounds = img.getBounds();
		Rectangle canvasBounds = canvas.getBounds();
		plateRect.x = (int) (left * canvasBounds.width * PALLET_IMAGE_DPI / imgBounds.width);
		canvas.redraw();
		canvas.update();
	}

	public void assignRegionTop(double top) {
		Assert.isNotNull(canvas, "canvas is null");
		Rectangle imgBounds = img.getBounds();
		Rectangle canvasBounds = canvas.getBounds();
		plateRect.y = (int) (top * canvasBounds.height * PALLET_IMAGE_DPI / imgBounds.height);
		canvas.redraw();
		canvas.update();
	}

	public void assignRegionRight(double right) {
		Assert.isNotNull(canvas, "canvas is null");
		Rectangle imgBounds = img.getBounds();
		Rectangle canvasBounds = canvas.getBounds();
		plateRect.width = (int) (right * canvasBounds.width * PALLET_IMAGE_DPI / imgBounds.width)
				- plateRect.x;
		canvas.redraw();
		canvas.update();
	}

	public void assignRegionBottom(double bottom) {
		Assert.isNotNull(canvas, "canvas is null");
		Rectangle imgBounds = img.getBounds();
		Rectangle canvasBounds = canvas.getBounds();
		plateRect.height = (int) (bottom * canvasBounds.height
				* PALLET_IMAGE_DPI / imgBounds.height)
				- plateRect.y;
		canvas.redraw();
		canvas.update();
	}

	public ScannerRegion getPlateRetion() {
		ScannerRegion region = new ScannerRegion();

		Rectangle imgBounds = img.getBounds();
		Rectangle canvasBounds = canvas.getBounds();
		double widthRatio = imgBounds.width / PALLET_IMAGE_DPI
				/ canvasBounds.width;
		double heightRatio = imgBounds.height / PALLET_IMAGE_DPI
				/ canvasBounds.height;

		region.set(plateRect.x * widthRatio, plateRect.y * heightRatio,
				(plateRect.width + plateRect.x) * widthRatio,
				(plateRect.height + plateRect.y) * heightRatio);

		return region;
	}
}
