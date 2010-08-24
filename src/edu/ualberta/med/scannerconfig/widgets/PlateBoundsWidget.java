package edu.ualberta.med.scannerconfig.widgets;

import java.io.File;

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
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scannerconfig.IPlateBoundsListener;
import edu.ualberta.med.scannerconfig.ScannerRegion;

public class PlateBoundsWidget {

	private Canvas canvas;
	private Image scannedImage;
	private GridRegion gridRegion;
	private ScannerRegion initialScannerRegion;

	private Image imageBuffer;
	private GC imageGC;

	public GridRegion getGridRegion() {
		if (gridRegion == null)
			this.gridRegion = new GridRegion(initialScannerRegion);
		return gridRegion;
	}

	/* please note that PALLET_IMAGE_DPI may change value */
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
		// pixel coordinates
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

		/*
		 * left,top,right,bottom are relative to the canvas size, so any change
		 * to the canvas must call this function.
		 */
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

			adjustBounds();

			this.oldCanvasSize = canvas.getSize();
		}

		private void adjustBounds() {
			if (this.width < 50) {
				this.width = 50;
			}
			if (this.height < 50) {
				this.height = 50;
			}
			if (this.left < 0) {
				this.left = 0;
			}
			if (this.top < 0) {
				this.top = 0;
			}
			if (this.left + this.width > canvas.getSize().x - 1) {
				this.left = canvas.getSize().x - this.width - 1;
			}
			if (this.top + this.height > canvas.getSize().y - 1) {
				this.top = canvas.getSize().y - this.height - 1;
			}
		}

		public ScannerRegion getScannerRegion() {

			ScannerRegion r = new ScannerRegion();

			double canvasWidth = canvas.getBounds().width;
			double canvasHeight = canvas.getBounds().height;

			adjustBounds();

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

		public Rectangle getRectangle() {
			return new Rectangle((int) left, (int) top, (int) width,
					(int) height);
		}
	}

	public PlateBoundsWidget(Canvas c, ScannerRegion r) {

		loadMostRecentImage();

		initialScannerRegion = r;

		canvas = c;
		canvas.getParent().layout();
		canvas.pack();
		canvas.setFocus();
		canvas.redraw();
		canvas.update();
		applyCanvasBindings();
	}

	private void applyCanvasBindings() {

		canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (scannedImage == null)
					return;

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

					/*
					 * Creates rectangles on the perimeter of the gridRegion,
					 * the code then checks for mouse-rectangle intersection to
					 * check for moving and resizing of the widget.
					 */
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

			}
		});
		canvas.addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (scannedImage == null)
					return;

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
				if (scannedImage == null)
					return;
				getGridRegion().scaleGrid(canvas.getSize());
			}

			@Override
			public void controlResized(ControlEvent e) {
				if (scannedImage == null)
					return;
				getGridRegion().scaleGrid(canvas.getSize());
			}
		});

		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (scannedImage == null)
					return;

				if (dragMode != DragMode.NONE) {
					drag = true;
					startDragMousePt.y = e.y;
					startDragMousePt.x = e.x;
					startGridRect = new Rectangle(getGridRegion()
							.getRectangle().x,
							getGridRegion().getRectangle().y, gridRegion
									.getRectangle().width, gridRegion
									.getRectangle().height);

				}
				canvas.redraw();

				notifyChangeListener();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (scannedImage == null)
					return;

				drag = false;
				dragMode = DragMode.NONE;
			}
		});

		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (scannedImage == null)
					return;

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
			}
		});

		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {

				// move me ?
				loadMostRecentImage();

				if (scannedImage == null) {
					e.gc.setForeground(new Color(canvas.getDisplay(), 255, 255,
							255));
					e.gc.fillRectangle(0, 0, canvas.getSize().x,
							canvas.getSize().y);
					return;
				}

				Rectangle imgBounds = scannedImage.getBounds();
				Point canvasSize = canvas.getSize();

				double imgAspectRatio = (double) imgBounds.width
						/ (double) imgBounds.height;
				if (imgAspectRatio > 1)
					canvasSize.y = (int) (canvasSize.x / imgAspectRatio);
				else
					canvasSize.x = (int) (canvasSize.y * imgAspectRatio);

				imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
				imageGC = new GC(imageBuffer);
				imageGC.drawImage(scannedImage, 0, 0,
						scannedImage.getBounds().width,
						scannedImage.getBounds().height, 0, 0,
						canvas.getBounds().width, canvas.getBounds().height);

				imageGC.setForeground(new Color(canvas.getDisplay(), 255, 0, 0));

				imageGC.drawRectangle(getGridRegion().getRectangle());

				drawGrid(imageGC, false);

				imageGC.setForeground(new Color(canvas.getDisplay(), 0, 0, 255));

				imageGC.drawOval((int) getGridRegion().left - 1,
						(int) getGridRegion().top - 1, 1, 1);

				imageGC.drawOval(
						(int) (getGridRegion().left + getGridRegion().width) - 3,
						(int) (getGridRegion().top + getGridRegion().height) - 3,
						6, 6);

				e.gc.drawImage(imageBuffer, 0, 0);
				imageGC.dispose();
				imageBuffer.dispose();
			}
		});
	}

	private void drawGrid(GC gc, boolean drawHorizontal) {

		double X, Y;

		if (drawHorizontal) {
			X = 12.0;
			Y = 8.0;
		} else {
			X = 8.0;
			Y = 12.0;
		}

		double w = (getGridRegion().getRectangle().width) / X;
		double h = (getGridRegion().getRectangle().height) / Y;

		double ox = getGridRegion().getRectangle().x;
		double oy = getGridRegion().getRectangle().y;

		for (int j = 0; j < Y; j++) {
			for (int i = 0; i < X; i++) {

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
	}

	public void assignRegions(String name, double left, double top,
			double right, double bottom, double gapX, double gapY) {
		if (scannedImage == null)
			return;
		Assert.isNotNull(canvas, "canvas is null");
		gridRegion = new GridRegion(new ScannerRegion(name, left, top, right,
				bottom, gapX, gapY));
		canvas.redraw();
	}

	public ScannerRegion getPlateRegion() {
		return this.getGridRegion().getScannerRegion();
	}

	private boolean loadMostRecentImage() {
		File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
		if (platesFile.exists()
				&& (platesFileLastModified != platesFile.lastModified())) {
			platesFileLastModified = platesFile.lastModified();
			scannedImage = new Image(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell().getDisplay(),
					PALLET_IMAGE_FILE);
			return true;
		}
		return false;
	}

	public void loadImage() {
		if (loadMostRecentImage()) {
			notifyChangeListener();
			canvas.redraw();
			canvas.update();
		}
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

}
