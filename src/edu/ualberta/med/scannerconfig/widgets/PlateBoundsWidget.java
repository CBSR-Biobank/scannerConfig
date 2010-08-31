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
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateBase;
import edu.ualberta.med.scannerconfig.preferences.scanner.PlateScannedImage;

public class PlateBoundsWidget {

	private boolean isEnabled;
	private PlateBase parentPlateBase;

	private Canvas canvas;
	private GridRegion gridRegion;

	protected ListenerList changeListeners = new ListenerList();
	private ChangeListener scannedImageListner, plateBaseChangeListner;

	private Image imageBuffer;
	private GC imageGC;

	private enum DragMode {
		NONE, MOVE, RESIZE_HORIZONTAL_LEFT, RESIZE_HORIZONTAL_RIGHT,
		RESIZE_VERTICAL_TOP, RESIZE_VERTICAL_BOTTOM, RESIZE_BOTTOM_RIGHT
	};

	private boolean drag = false;
	private Point startDragMousePt = new Point(0, 0);
	private Rectangle startGridRect = new Rectangle(0, 0, 0, 0);
	private DragMode dragMode = DragMode.NONE;

	private class GridRegion {
		// pixel coordinates
		private double left, top, width, height;
		private double gapOffsetX, gapOffsetY;
		private boolean horizontalRotation;

		public String name;

		private Point oldCanvasSize;

		public double regionToPixelWidth(double canvasWidth) {// canvas.getBounds().width
			if (PlateScannedImage.instance().exists()) {
				return (PlateScannedImage.instance().getScannedImage()
						.getBounds().width / (canvasWidth * PlateScannedImage.PALLET_IMAGE_DPI));
			}
			else {
				System.err.println("regionToPixelWidth: Warning bad state");
				return 1.0;
			}
		}

		public double regionToPixelHeight(double canvasHeight) {
			if (PlateScannedImage.instance().exists()) {
				return (PlateScannedImage.instance().getScannedImage()
						.getBounds().height / (canvasHeight * PlateScannedImage.PALLET_IMAGE_DPI));
			}
			else {
				System.err.println("regionToPixelHeight: Warning bad state");
				return 1.0;
			}
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
			double w = (PlateBoundsWidget.this.getGridRegion().getRectangle().width) / 12.0;
			if (w - this.gapOffsetX < 0.1) {
				this.gapOffsetX = w - 0.1;
			}
		}

		public void setGapOffsetY(double gap) {

			this.gapOffsetY = gap;

			if (this.gapOffsetY < 0)
				this.gapOffsetY = 0;

			double h = (PlateBoundsWidget.this.getGridRegion().getRectangle().height) / 8.0;
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
			this.horizontalRotation = r.horizontalRotation;

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
			r.horizontalRotation = this.horizontalRotation;

			return r;
		}

		public Rectangle getRectangle() {
			return new Rectangle(
					(int) this.left,
					(int) this.top,
					(int) this.width,
					(int) this.height);
		}

		public boolean isHorizontalRotation() {
			return this.horizontalRotation;
		}

		public void rotate() {
			this.horizontalRotation = !this.horizontalRotation;
			double t = this.height;
			this.height = this.width;
			this.width = t;
			this.adjustBounds();
		}
	}

	private GridRegion getGridRegion() {
		if (gridRegion == null && PlateScannedImage.instance().exists())
			gridRegion = new GridRegion(parentPlateBase.getScannerRegionText());
		return gridRegion;
	}

	public PlateBoundsWidget(final PlateBase plateBase, Canvas c) {

		this.parentPlateBase = plateBase;

		this.applyPlateBaseBindings();

		this.canvas = c;
		this.canvas.getParent().layout();
		this.canvas.pack();
		this.canvas.setFocus();
		this.canvas.redraw();
		this.canvas.update();
		this.applyCanvasBindings();

		this.setEnable(false);

	}

	private void applyPlateBaseBindings() {

		plateBaseChangeListner = new ChangeListener() {
			@Override
			public void change(Event e) {

				switch (e.type) {
					case ChangeListener.PLATE_BASE_ROTATE:
						PlateBoundsWidget.this.rotateGrid();
						break;

					case ChangeListener.PLATE_BASE_TEXT_CHANGE:
						PlateBoundsWidget.this.assignRegions(parentPlateBase
								.getScannerRegionText());
						break;

					case ChangeListener.PLATE_BASE_ENABLED:
						PlateBoundsWidget.this.setEnable(e.detail == 1);
						break;
					case ChangeListener.PALLET_BASE_REFRESH:
						getGridRegion();
						PlateBoundsWidget.this
								.setEnable(PlateBoundsWidget.this.parentPlateBase
										.isEnabled());
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
								parentPlateBase.getScannerRegionText());
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

		this.canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (PlateBoundsWidget.this.notValid())
					return;

				if (PlateBoundsWidget.this.getGridRegion().getRectangle()
						.contains(e.x, e.y))
					PlateBoundsWidget.this.canvas.setFocus();

				if (PlateBoundsWidget.this.drag) {
					switch (PlateBoundsWidget.this.dragMode) {
						case MOVE:
							PlateBoundsWidget.this.getGridRegion().left = (e.x - PlateBoundsWidget.this.startDragMousePt.x)
									+ PlateBoundsWidget.this.startGridRect.x;

							PlateBoundsWidget.this.getGridRegion().top = (e.y - PlateBoundsWidget.this.startDragMousePt.y)
									+ PlateBoundsWidget.this.startGridRect.y;
							break;
						case RESIZE_HORIZONTAL_RIGHT:
							PlateBoundsWidget.this.getGridRegion().width = (e.x - PlateBoundsWidget.this.startDragMousePt.x)
									+ PlateBoundsWidget.this.startGridRect.width;
							break;
						case RESIZE_HORIZONTAL_LEFT:
							PlateBoundsWidget.this.getGridRegion().left = (e.x - PlateBoundsWidget.this.startDragMousePt.x)
									+ PlateBoundsWidget.this.startGridRect.x;
							PlateBoundsWidget.this.getGridRegion().width = (PlateBoundsWidget.this.startDragMousePt.x - e.x)
									+ PlateBoundsWidget.this.startGridRect.width;
							break;
						case RESIZE_VERTICAL_TOP:
							PlateBoundsWidget.this.getGridRegion().top = (e.y - PlateBoundsWidget.this.startDragMousePt.y)
									+ PlateBoundsWidget.this.startGridRect.y;
							PlateBoundsWidget.this.getGridRegion().height = (PlateBoundsWidget.this.startDragMousePt.y - e.y)
									+ PlateBoundsWidget.this.startGridRect.height;
							break;
						case RESIZE_VERTICAL_BOTTOM:
							PlateBoundsWidget.this.getGridRegion().height = (e.y - PlateBoundsWidget.this.startDragMousePt.y)
									+ PlateBoundsWidget.this.startGridRect.height;
							break;
						case RESIZE_BOTTOM_RIGHT:
							PlateBoundsWidget.this.getGridRegion().width = (e.x - PlateBoundsWidget.this.startDragMousePt.x)
									+ PlateBoundsWidget.this.startGridRect.width;
							PlateBoundsWidget.this.getGridRegion().height = (e.y - PlateBoundsWidget.this.startDragMousePt.y)
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
						if (PlateBoundsWidget.this.getGridRegion()
								.getRectangle().contains(e.x, e.y)) {
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
				if (PlateBoundsWidget.this.notValid())
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
				if (PlateBoundsWidget.this.notValid())
					return;
				PlateBoundsWidget.this.gridRegion
						.scaleGrid(PlateBoundsWidget.this.canvas.getSize());
			}

			@Override
			public void controlResized(ControlEvent e) {
				if (PlateBoundsWidget.this.notValid())
					return;
				PlateBoundsWidget.this.gridRegion
						.scaleGrid(PlateBoundsWidget.this.canvas.getSize());
			}
		});

		this.canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (PlateBoundsWidget.this.notValid())
					return;

				if (PlateBoundsWidget.this.dragMode != DragMode.NONE) {
					PlateBoundsWidget.this.drag = true;
					PlateBoundsWidget.this.startDragMousePt.y = e.y;
					PlateBoundsWidget.this.startDragMousePt.x = e.x;
					PlateBoundsWidget.this.startGridRect = new Rectangle(
							PlateBoundsWidget.this.getGridRegion()
									.getRectangle().x,
							PlateBoundsWidget.this.getGridRegion()
									.getRectangle().y,
							PlateBoundsWidget.this.getGridRegion()
									.getRectangle().width,
							PlateBoundsWidget.this.getGridRegion()
									.getRectangle().height);

				}
				PlateBoundsWidget.this.canvas.redraw();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (PlateBoundsWidget.this.notValid())
					return;

				PlateBoundsWidget.this.drag = false;
				PlateBoundsWidget.this.dragMode = DragMode.NONE;
			}
		});

		this.canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (PlateBoundsWidget.this.notValid())
					return;

				if (!PlateBoundsWidget.this.drag) {
					switch (e.keyCode) {
						case SWT.ARROW_LEFT:
							--PlateBoundsWidget.this.getGridRegion().left;
							break;
						case SWT.ARROW_RIGHT:
							++PlateBoundsWidget.this.getGridRegion().left;
							break;
						case SWT.ARROW_UP:
							--PlateBoundsWidget.this.getGridRegion().top;
							break;
						case SWT.ARROW_DOWN:
							++PlateBoundsWidget.this.getGridRegion().top;
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

				if (PlateBoundsWidget.this.notValid()) {
					e.gc.setForeground(new Color(PlateBoundsWidget.this.canvas
							.getDisplay(), 255, 255, 255));
					e.gc.fillRectangle(
							0,
							0,
							PlateBoundsWidget.this.canvas.getSize().x,
							PlateBoundsWidget.this.canvas.getSize().y);
					return;
				}

				Rectangle imgBounds = PlateScannedImage.instance()
						.getScannedImage().getBounds();
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
						PlateScannedImage.instance().getScannedImage(),
						0,
						0,
						PlateScannedImage.instance().getScannedImage()
								.getBounds().width,
						PlateScannedImage.instance().getScannedImage()
								.getBounds().height,
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
						PlateBoundsWidget.this.gridRegion
								.isHorizontalRotation());

				PlateBoundsWidget.this.imageGC.setForeground(new Color(
						PlateBoundsWidget.this.canvas.getDisplay(),
						0,
						0,
						255));

				PlateBoundsWidget.this.imageGC.drawOval(
						(int) PlateBoundsWidget.this.getGridRegion().left - 1,
						(int) PlateBoundsWidget.this.getGridRegion().top - 1,
						1,
						1);

				PlateBoundsWidget.this.imageGC.drawOval(
						(int) (PlateBoundsWidget.this.getGridRegion().left + PlateBoundsWidget.this
								.getGridRegion().width) - 3,
						(int) (PlateBoundsWidget.this.getGridRegion().top + PlateBoundsWidget.this
								.getGridRegion().height) - 3,
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

		double w = (this.getGridRegion().getRectangle().width) / X;
		double h = (this.getGridRegion().getRectangle().height) / Y;

		double ox = this.getGridRegion().getRectangle().x;
		double oy = this.getGridRegion().getRectangle().y;

		for (int j = 0; j < Y; j++) {
			for (int i = 0; i < X; i++) {

				double cx = ox + i * w + w / 2.0;
				double cy = oy + j * h + h / 2.0;

				Rectangle gridRect = new Rectangle(
						(int) (cx - w / 2.0 + this.getGridRegion()
								.getGapOffsetX() / 2.0),
						(int) (cy - h / 2.0 + this.getGridRegion()
								.getGapOffsetY() / 2.0),
						(int) (w - this.getGridRegion().getGapOffsetX() / 1.0),
						(int) (h - this.getGridRegion().getGapOffsetY() / 1.0));

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

	public void assignRegions(ScannerRegion sr) {
		Assert.isNotNull(this.canvas, "canvas is null");
		this.gridRegion = new GridRegion(sr);
		this.canvas.redraw();
	}

	public ScannerRegion getPlateRegion() {
		return this.getGridRegion().getScannerRegion();
	}

	private boolean notValid() {
		return (!PlateScannedImage.instance().exists()
				|| !PlateBoundsWidget.this.isEnabled || this.gridRegion == null);
	}

	private void setEnable(boolean enabled) {
		this.isEnabled = enabled;
		if (canvas != null && !canvas.isDisposed()) {
			this.canvas.redraw();
			this.canvas.update();
		}
	}

	private void rotateGrid() {
		if (notValid())
			return;

		this.getGridRegion().rotate();
		this.canvas.redraw();
	}

	public void dispose() {
		PlateScannedImage.instance().removeScannedImageChangeListener(
				scannedImageListner);
		parentPlateBase.removePlateBaseChangeListener(plateBaseChangeListner);
	}

	/* updates text fields in plateBase */
	public void addPlateWidgetChangeListener(ChangeListener listener) {
		this.changeListeners.add(listener);
	}

	private void notifyChangeListener() {
		Object[] listeners = this.changeListeners.getListeners();
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
