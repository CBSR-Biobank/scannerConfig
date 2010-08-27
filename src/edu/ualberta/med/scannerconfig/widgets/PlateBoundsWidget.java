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
	private boolean isHorizontal;
	private boolean isEnabled;

	private Image imageBuffer;
	private GC imageGC;

	/* please note that PALLET_IMAGE_DPI may change value */
	public static double PALLET_IMAGE_DPI = 300.0;
	public static final String PALLET_IMAGE_FILE = "plates.bmp";
	private long platesFileLastModified;

	private enum DragMode {
		NONE, MOVE, RESIZE_HORIZONTAL_LEFT, RESIZE_HORIZONTAL_RIGHT,
		RESIZE_VERTICAL_TOP, RESIZE_VERTICAL_BOTTOM, RESIZE_BOTTOM_RIGHT
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
			return (PlateBoundsWidget.this.scannedImage.getBounds().width / (canvasWidth * PlateBoundsWidget.PALLET_IMAGE_DPI));
		}

		public double regionToPixelHeight(double canvasHeight) {
			return (PlateBoundsWidget.this.scannedImage.getBounds().height / (canvasHeight * PlateBoundsWidget.PALLET_IMAGE_DPI));
		}

		/*
		 * left,top,right,bottom are relative to the canvas size, so any change
		 * to the canvas must call this function.
		 */
		public void scaleGrid(Point newCanvasSize) {

			double horiztonalRatio = this
					.regionToPixelWidth(this.oldCanvasSize.x)
					/ this.regionToPixelWidth(newCanvasSize.x);

			double verticalRatio = this
					.regionToPixelHeight(this.oldCanvasSize.y)
					/ this.regionToPixelHeight(newCanvasSize.y);

			this.left = this.left * horiztonalRatio;
			this.top = this.top * verticalRatio;
			this.width = this.width * horiztonalRatio;
			this.height = this.height * verticalRatio;
			this.gapOffsetX = this.gapOffsetX * horiztonalRatio;
			this.gapOffsetY = this.gapOffsetY * verticalRatio;
			this.oldCanvasSize = newCanvasSize;
		}

		public double getGapOffsetX() {
			return this.gapOffsetX;
		}

		public double getGapOffsetY() {
			return this.gapOffsetY;
		}

		public void setGapOffsetX(double gap) {
			this.gapOffsetX = gap;
			if (this.gapOffsetX < 0)
				this.gapOffsetX = 0;
			double w = (PlateBoundsWidget.this.gridRegion.getRectangle().width) / 12.0;
			if (w - this.gapOffsetX < 0.1) {
				this.gapOffsetX = w - 0.1;
			}
		}

		public void setGapOffsetY(double gap) {

			this.gapOffsetY = gap;

			if (this.gapOffsetY < 0)
				this.gapOffsetY = 0;

			double h = (PlateBoundsWidget.this.gridRegion.getRectangle().height) / 8.0;
			if (h - this.gapOffsetY < 0.1) {
				this.gapOffsetY = h - 0.1;
			}
		}

		public GridRegion(ScannerRegion r) {

			double canvasWidth = PlateBoundsWidget.this.canvas.getBounds().width;
			double canvasHeight = PlateBoundsWidget.this.canvas.getBounds().height;

			this.name = r.name;

			this.gapOffsetX = r.gapX / this.regionToPixelWidth(canvasWidth);
			this.gapOffsetY = r.gapY / this.regionToPixelHeight(canvasHeight);
			this.left = r.left / this.regionToPixelWidth(canvasWidth);
			this.top = r.top / this.regionToPixelHeight(canvasHeight);
			this.width = (r.right - r.left)
					/ this.regionToPixelWidth(canvasWidth);
			this.height = (r.bottom - r.top)
					/ this.regionToPixelHeight(canvasHeight);

			this.adjustBounds();

			this.oldCanvasSize = PlateBoundsWidget.this.canvas.getSize();
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
			if (this.left + this.width > PlateBoundsWidget.this.canvas
					.getSize().x - 1) {
				this.left = PlateBoundsWidget.this.canvas.getSize().x
						- this.width - 1;
			}
			if (this.top + this.height > PlateBoundsWidget.this.canvas
					.getSize().y - 1) {
				this.top = PlateBoundsWidget.this.canvas.getSize().y
						- this.height - 1;
			}
		}

		public ScannerRegion getScannerRegion() {

			ScannerRegion r = new ScannerRegion();

			double canvasWidth = PlateBoundsWidget.this.canvas.getBounds().width;
			double canvasHeight = PlateBoundsWidget.this.canvas.getBounds().height;

			this.adjustBounds();

			r.name = this.name;

			r.left = this.left * this.regionToPixelWidth(canvasWidth);
			r.top = this.top * this.regionToPixelHeight(canvasHeight);
			r.right = (this.width + this.left)
					* this.regionToPixelWidth(canvasWidth);
			r.bottom = (this.height + this.top)
					* this.regionToPixelHeight(canvasHeight);
			r.gapX = this.gapOffsetX * this.regionToPixelWidth(canvasWidth);
			r.gapY = this.gapOffsetY * this.regionToPixelHeight(canvasHeight);

			return r;
		}

		public Rectangle getRectangle() {
			return new Rectangle(
					(int) this.left,
					(int) this.top,
					(int) this.width,
					(int) this.height);
		}
	}

	public PlateBoundsWidget(Canvas c, ScannerRegion r,
			boolean isHorizontalRotation) {

		this.initialScannerRegion = r;

		this.isHorizontal = isHorizontalRotation;

		this.canvas = c;
		this.canvas.getParent().layout();
		this.canvas.pack();
		this.canvas.setFocus();
		this.canvas.redraw();
		this.canvas.update();
		this.applyCanvasBindings();

		this.setEnable(this.loadMostRecentImage());
	}

	private void applyCanvasBindings() {

		this.canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (PlateBoundsWidget.this.scannedImage == null
						|| !PlateBoundsWidget.this.isEnabled)
					return;

				if (PlateBoundsWidget.this.gridRegion.getRectangle().contains(
						e.x,
						e.y))
					PlateBoundsWidget.this.canvas.setFocus();

				if (PlateBoundsWidget.this.drag) {
					switch (PlateBoundsWidget.this.dragMode) {
						case MOVE:
							PlateBoundsWidget.this.gridRegion.left = (e.x - PlateBoundsWidget.this.startDragMousePt.x)
									+ PlateBoundsWidget.this.startGridRect.x;

							PlateBoundsWidget.this.gridRegion.top = (e.y - PlateBoundsWidget.this.startDragMousePt.y)
									+ PlateBoundsWidget.this.startGridRect.y;
							break;
						case RESIZE_HORIZONTAL_RIGHT:
							PlateBoundsWidget.this.gridRegion.width = (e.x - PlateBoundsWidget.this.startDragMousePt.x)
									+ PlateBoundsWidget.this.startGridRect.width;
							break;
						case RESIZE_HORIZONTAL_LEFT:
							PlateBoundsWidget.this.gridRegion.left = (e.x - PlateBoundsWidget.this.startDragMousePt.x)
									+ PlateBoundsWidget.this.startGridRect.x;
							PlateBoundsWidget.this.gridRegion.width = (PlateBoundsWidget.this.startDragMousePt.x - e.x)
									+ PlateBoundsWidget.this.startGridRect.width;
							break;
						case RESIZE_VERTICAL_TOP:
							PlateBoundsWidget.this.gridRegion.top = (e.y - PlateBoundsWidget.this.startDragMousePt.y)
									+ PlateBoundsWidget.this.startGridRect.y;
							PlateBoundsWidget.this.gridRegion.height = (PlateBoundsWidget.this.startDragMousePt.y - e.y)
									+ PlateBoundsWidget.this.startGridRect.height;
							break;
						case RESIZE_VERTICAL_BOTTOM:
							PlateBoundsWidget.this.gridRegion.height = (e.y - PlateBoundsWidget.this.startDragMousePt.y)
									+ PlateBoundsWidget.this.startGridRect.height;
							break;
						case RESIZE_BOTTOM_RIGHT:
							PlateBoundsWidget.this.gridRegion.width = (e.x - PlateBoundsWidget.this.startDragMousePt.x)
									+ PlateBoundsWidget.this.startGridRect.width;
							PlateBoundsWidget.this.gridRegion.height = (e.y - PlateBoundsWidget.this.startDragMousePt.y)
									+ PlateBoundsWidget.this.startGridRect.height;
						default:
							break;
					}

					PlateBoundsWidget.this.canvas.redraw();
					PlateBoundsWidget.this.notifyChangeListener();
				}
				else {

					PlateBoundsWidget.this.canvas.setCursor(new Cursor(
							PlateBoundsWidget.this.canvas.getDisplay(),
							SWT.CURSOR_ARROW));

					/*
					 * Creates rectangles on the perimeter of the gridRegion,
					 * the code then checks for mouse-rectangle intersection to
					 * check for moving and resizing of the widget.
					 */
					if (PlateBoundsWidget.this.gridRegion != null) {
						if (PlateBoundsWidget.this.gridRegion.getRectangle()
								.contains(e.x, e.y)) {
							PlateBoundsWidget.this.canvas.setCursor(new Cursor(
									PlateBoundsWidget.this.canvas.getDisplay(),
									SWT.CURSOR_HAND));
							PlateBoundsWidget.this.dragMode = DragMode.MOVE;
						}
						else
							if (new Rectangle(
									PlateBoundsWidget.this.gridRegion
											.getRectangle().x
											+ PlateBoundsWidget.this.gridRegion
													.getRectangle().width,
									PlateBoundsWidget.this.gridRegion
											.getRectangle().y
											+ PlateBoundsWidget.this.gridRegion
													.getRectangle().height,
									15,
									15).contains(e.x, e.y)) {
								PlateBoundsWidget.this.canvas
										.setCursor(new Cursor(
												PlateBoundsWidget.this.canvas
														.getDisplay(),
												SWT.CURSOR_SIZENWSE));
								PlateBoundsWidget.this.dragMode = DragMode.RESIZE_BOTTOM_RIGHT;
							}
							else
								if (new Rectangle(
										PlateBoundsWidget.this.gridRegion
												.getRectangle().x
												+ PlateBoundsWidget.this.gridRegion
														.getRectangle().width,
										PlateBoundsWidget.this.gridRegion
												.getRectangle().y,
										10,
										PlateBoundsWidget.this.gridRegion
												.getRectangle().height)
										.contains(e.x, e.y)) {
									PlateBoundsWidget.this.canvas
											.setCursor(new Cursor(
													PlateBoundsWidget.this.canvas
															.getDisplay(),
													SWT.CURSOR_SIZEE));
									PlateBoundsWidget.this.dragMode = DragMode.RESIZE_HORIZONTAL_RIGHT;
								}
								else
									if (new Rectangle(
											PlateBoundsWidget.this.gridRegion
													.getRectangle().x - 10,
											PlateBoundsWidget.this.gridRegion
													.getRectangle().y,
											10,
											PlateBoundsWidget.this.gridRegion
													.getRectangle().height)
											.contains(e.x, e.y)) {
										PlateBoundsWidget.this.canvas
												.setCursor(new Cursor(
														PlateBoundsWidget.this.canvas
																.getDisplay(),
														SWT.CURSOR_SIZEW));
										PlateBoundsWidget.this.dragMode = DragMode.RESIZE_HORIZONTAL_LEFT;
									}
									else
										if (new Rectangle(
												PlateBoundsWidget.this.gridRegion
														.getRectangle().x,
												PlateBoundsWidget.this.gridRegion
														.getRectangle().y - 10,
												PlateBoundsWidget.this.gridRegion
														.getRectangle().width,
												10).contains(e.x, e.y)) {
											PlateBoundsWidget.this.canvas
													.setCursor(new Cursor(
															PlateBoundsWidget.this.canvas
																	.getDisplay(),
															SWT.CURSOR_SIZEN));
											PlateBoundsWidget.this.dragMode = DragMode.RESIZE_VERTICAL_TOP;
										}
										else
											if (new Rectangle(
													PlateBoundsWidget.this.gridRegion
															.getRectangle().x,
													PlateBoundsWidget.this.gridRegion
															.getRectangle().y
															+ PlateBoundsWidget.this.gridRegion
																	.getRectangle().height,
													PlateBoundsWidget.this.gridRegion
															.getRectangle().width,
													10).contains(e.x, e.y)) {
												PlateBoundsWidget.this.canvas
														.setCursor(new Cursor(
																PlateBoundsWidget.this.canvas
																		.getDisplay(),
																SWT.CURSOR_SIZES));
												PlateBoundsWidget.this.dragMode = DragMode.RESIZE_VERTICAL_BOTTOM;

											}
											else {
												PlateBoundsWidget.this.dragMode = DragMode.NONE;
											}
					}
				}

			}
		});
		this.canvas.addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (PlateBoundsWidget.this.scannedImage == null
						|| !PlateBoundsWidget.this.isEnabled)
					return;

				switch (event.type) {
					case SWT.MouseWheel:
						PlateBoundsWidget.this.gridRegion
								.setGapOffsetX(PlateBoundsWidget.this.gridRegion
										.getGapOffsetX() + event.count / 10.0);
						PlateBoundsWidget.this.gridRegion
								.setGapOffsetY(PlateBoundsWidget.this.gridRegion
										.getGapOffsetY() + event.count / 10.0);

						PlateBoundsWidget.this.canvas.redraw();
						PlateBoundsWidget.this.notifyChangeListener();

						break;
				}
			}
		});

		this.canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {
				if (PlateBoundsWidget.this.scannedImage == null
						&& PlateBoundsWidget.this.isEnabled)
					return;
				PlateBoundsWidget.this.gridRegion
						.scaleGrid(PlateBoundsWidget.this.canvas.getSize());
			}

			@Override
			public void controlResized(ControlEvent e) {
				if (PlateBoundsWidget.this.scannedImage == null
						|| !PlateBoundsWidget.this.isEnabled)
					return;
				PlateBoundsWidget.this.gridRegion
						.scaleGrid(PlateBoundsWidget.this.canvas.getSize());
			}
		});

		this.canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (PlateBoundsWidget.this.scannedImage == null
						|| !PlateBoundsWidget.this.isEnabled)
					return;

				if (PlateBoundsWidget.this.dragMode != DragMode.NONE) {
					PlateBoundsWidget.this.drag = true;
					PlateBoundsWidget.this.startDragMousePt.y = e.y;
					PlateBoundsWidget.this.startDragMousePt.x = e.x;
					PlateBoundsWidget.this.startGridRect = new Rectangle(
							PlateBoundsWidget.this.gridRegion.getRectangle().x,
							PlateBoundsWidget.this.gridRegion.getRectangle().y,
							PlateBoundsWidget.this.gridRegion.getRectangle().width,
							PlateBoundsWidget.this.gridRegion.getRectangle().height);

				}
				PlateBoundsWidget.this.canvas.redraw();

				PlateBoundsWidget.this.notifyChangeListener();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (PlateBoundsWidget.this.scannedImage == null
						|| !PlateBoundsWidget.this.isEnabled)
					return;

				PlateBoundsWidget.this.drag = false;
				PlateBoundsWidget.this.dragMode = DragMode.NONE;
			}
		});

		this.canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (PlateBoundsWidget.this.scannedImage == null
						|| !PlateBoundsWidget.this.isEnabled)
					return;

				if (!PlateBoundsWidget.this.drag) {
					switch (e.keyCode) {
						case SWT.ARROW_LEFT:
							--PlateBoundsWidget.this.gridRegion.left;
							break;
						case SWT.ARROW_RIGHT:
							++PlateBoundsWidget.this.gridRegion.left;
							break;
						case SWT.ARROW_UP:
							--PlateBoundsWidget.this.gridRegion.top;
							break;
						case SWT.ARROW_DOWN:
							++PlateBoundsWidget.this.gridRegion.top;
							break;
					}
					PlateBoundsWidget.this.canvas.redraw();

					PlateBoundsWidget.this.notifyChangeListener();

				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		this.canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {

				// move me ?
				PlateBoundsWidget.this.loadMostRecentImage();

				if (PlateBoundsWidget.this.scannedImage == null
						|| !PlateBoundsWidget.this.isEnabled) {
					e.gc.setForeground(new Color(PlateBoundsWidget.this.canvas
							.getDisplay(), 255, 255, 255));
					e.gc.fillRectangle(
							0,
							0,
							PlateBoundsWidget.this.canvas.getSize().x,
							PlateBoundsWidget.this.canvas.getSize().y);
					return;
				}

				Rectangle imgBounds = PlateBoundsWidget.this.scannedImage
						.getBounds();
				Point canvasSize = PlateBoundsWidget.this.canvas.getSize();

				double imgAspectRatio = (double) imgBounds.width
						/ (double) imgBounds.height;
				if (imgAspectRatio > 1)
					canvasSize.y = (int) (canvasSize.x / imgAspectRatio);
				else
					canvasSize.x = (int) (canvasSize.y * imgAspectRatio);

				PlateBoundsWidget.this.imageBuffer = new Image(
						PlateBoundsWidget.this.canvas.getDisplay(),
						PlateBoundsWidget.this.canvas.getBounds());
				PlateBoundsWidget.this.imageGC = new GC(
						PlateBoundsWidget.this.imageBuffer);
				PlateBoundsWidget.this.imageGC.drawImage(
						PlateBoundsWidget.this.scannedImage,
						0,
						0,
						PlateBoundsWidget.this.scannedImage.getBounds().width,
						PlateBoundsWidget.this.scannedImage.getBounds().height,
						0,
						0,
						PlateBoundsWidget.this.canvas.getBounds().width,
						PlateBoundsWidget.this.canvas.getBounds().height);

				PlateBoundsWidget.this.imageGC.setForeground(new Color(
						PlateBoundsWidget.this.canvas.getDisplay(),
						255,
						0,
						0));

				PlateBoundsWidget.this.imageGC
						.drawRectangle(PlateBoundsWidget.this.gridRegion
								.getRectangle());

				PlateBoundsWidget.this.drawGrid(
						PlateBoundsWidget.this.imageGC,
						PlateBoundsWidget.this.isHorizontal);

				PlateBoundsWidget.this.imageGC.setForeground(new Color(
						PlateBoundsWidget.this.canvas.getDisplay(),
						0,
						0,
						255));

				PlateBoundsWidget.this.imageGC.drawOval(
						(int) PlateBoundsWidget.this.gridRegion.left - 1,
						(int) PlateBoundsWidget.this.gridRegion.top - 1,
						1,
						1);

				PlateBoundsWidget.this.imageGC
						.drawOval(
								(int) (PlateBoundsWidget.this.gridRegion.left + PlateBoundsWidget.this.gridRegion.width) - 3,
								(int) (PlateBoundsWidget.this.gridRegion.top + PlateBoundsWidget.this.gridRegion.height) - 3,
								6,
								6);

				e.gc.drawImage(PlateBoundsWidget.this.imageBuffer, 0, 0);
				PlateBoundsWidget.this.imageGC.dispose();
				PlateBoundsWidget.this.imageBuffer.dispose();
			}
		});
	}

	private void drawGrid(GC gc, boolean drawHorizontal) {

		double X, Y;

		if (drawHorizontal) {
			X = 12.0;
			Y = 8.0;
		}
		else {
			X = 8.0;
			Y = 12.0;
		}

		double w = (this.gridRegion.getRectangle().width) / X;
		double h = (this.gridRegion.getRectangle().height) / Y;

		double ox = this.gridRegion.getRectangle().x;
		double oy = this.gridRegion.getRectangle().y;

		for (int j = 0; j < Y; j++) {
			for (int i = 0; i < X; i++) {

				double cx = ox + i * w + w / 2.0;
				double cy = oy + j * h + h / 2.0;

				Rectangle gridRect = new Rectangle(
						(int) (cx - w / 2.0 + this.gridRegion.getGapOffsetX() / 2.0),
						(int) (cy - h / 2.0 + this.gridRegion.getGapOffsetY() / 2.0),
						(int) (w - this.gridRegion.getGapOffsetX() / 1.0),
						(int) (h - this.gridRegion.getGapOffsetY() / 1.0));

				gc.setForeground(new Color(this.canvas.getDisplay(), 0, 255, 0));
				gc.drawRectangle(gridRect);

				if (drawHorizontal) {
					if ((i == X - 1) && (j == 0)) {
						gc.setBackground(new Color(
								this.canvas.getDisplay(),
								0,
								255,
								255));
						gc.fillRectangle(gridRect);
					}
				}
				else {
					if ((i == X - 1) && (j == Y - 1)) {
						gc.setBackground(new Color(
								this.canvas.getDisplay(),
								0,
								255,
								255));
						gc.fillRectangle(gridRect);
					}
				}

			}
		}
	}

	public void assignRegions(String name, double left, double top,
			double right, double bottom, double gapX, double gapY) {
		if (this.scannedImage == null)
			return;
		Assert.isNotNull(this.canvas, "canvas is null");
		this.gridRegion = new GridRegion(new ScannerRegion(
				name,
				left,
				top,
				right,
				bottom,
				gapX,
				gapY));
		this.canvas.redraw();
	}

	public ScannerRegion getPlateRegion() {
		return this.gridRegion.getScannerRegion();
	}

	private boolean loadMostRecentImage() {
		File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
		if (platesFile.exists()
				&& (this.platesFileLastModified != platesFile.lastModified())) {
			this.platesFileLastModified = platesFile.lastModified();
			this.scannedImage = new Image(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell().getDisplay(),
					PlateBoundsWidget.PALLET_IMAGE_FILE);

			if (this.gridRegion == null)
				this.gridRegion = new GridRegion(initialScannerRegion);

			return true;
		}
		return false;
	}

	public void setEnable(boolean enabled) {
		this.isEnabled = enabled;
		if (canvas != null) {
			this.canvas.redraw();
			this.canvas.update();
		}
	}

	public void rotateGrid() {
		if (this.scannedImage != null) {
			this.isHorizontal = !this.isHorizontal;
			this.notifyChangeListener();
		}
	}

	public boolean getIsHorizontalRotation() {
		return this.isHorizontal;
	}

	public void loadImage() {
		if (this.loadMostRecentImage()) {
			this.notifyChangeListener();
			this.canvas.redraw();
			this.canvas.update();
		}
	}

	public void resetImage() {
		this.scannedImage = null;
		this.notifyChangeListener();
		this.canvas.redraw();
		this.canvas.update();
	}

	/* updates text fields in plateBase */
	public void addChangeListener(IPlateBoundsListener listener) {
		this.changeListeners.add(listener);
	}

	private void notifyChangeListener() {
		Object[] listeners = this.changeListeners.getListeners();
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
