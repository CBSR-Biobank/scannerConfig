package edu.ualberta.med.scannerconfig.widgets;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scannerconfig.IPlateBoundsListener;
import edu.ualberta.med.scannerconfig.ScannerRegion;

public class PlateBoundsWidget {

	private Canvas canvas;
	private Image scannedImage;
	private GridRegion gridRegion;
	private ScannerRegion initialScannerRegion;

	private Image[] imageBuffer = new Image[2];
	private int currentBuffer;

	public GridRegion getGridRegion() {
		if (gridRegion == null)
			this.gridRegion = new GridRegion(initialScannerRegion);
		return gridRegion;
	}

	/* please note that this can change value */
	public static double PALLET_IMAGE_DPI = 300.0;
	public static final String PALLET_IMAGE_FILE = "plates.bmp";
	private long platesFileLastModified;

	private enum Mode {
		NONE, MOVE, RESIZE_HORIZONTAL_LEFT, RESIZE_HORIZONTAL_RIGHT, RESIZE_VERTICAL_TOP, RESIZE_VERTICAL_BOTTOM
	};

	private boolean drag = false;
	private Point startDragMousePt = new Point(0, 0);
	private Rectangle startGridRect = new Rectangle(0, 0, 0, 0);
	private Mode dragMode = Mode.NONE;

	protected ListenerList changeListeners = new ListenerList();

	private class GridRegion {
		public double left, top, width, height;
		public String name;

		public double regionToPixelWidth() {
			return (scannedImage.getBounds().width / (canvas.getBounds().width * PALLET_IMAGE_DPI));
		}

		public double regionToPixelHeight() {
			return (scannedImage.getBounds().height / (canvas.getBounds().height * PALLET_IMAGE_DPI));
		}

		public GridRegion(ScannerRegion r) {

			this.name = r.name;

			this.left = r.left / regionToPixelWidth();
			this.top = r.top / regionToPixelHeight();
			this.width = (r.right - r.left) / regionToPixelWidth();
			this.height = (r.bottom - r.top) / regionToPixelHeight();

		}

		public ScannerRegion getScannerRegion() {

			ScannerRegion r = new ScannerRegion();

			r.name = this.name;

			r.left = this.left * regionToPixelWidth();
			r.top = this.top * regionToPixelHeight();
			r.right = (this.width + this.left) * regionToPixelWidth();
			r.bottom = (this.height + this.top) * regionToPixelHeight();

			return r;

		}

		public Rectangle getRectangle() {
			return new Rectangle((int) left, (int) top, (int) width,
					(int) height);
		}

	}

	public PlateBoundsWidget(Canvas c, ScannerRegion r, final Color mycolor) {

		File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
		if (platesFile.exists()) {
			platesFileLastModified = platesFile.lastModified();
			scannedImage = new Image(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell().getDisplay(),
					PALLET_IMAGE_FILE);
		}
		canvas = c;
		initialScannerRegion = r;
		Assert.isNotNull(scannedImage, "scannedImage is null");

		canvas.getParent().layout();
		canvas.pack();

		canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
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
					case RESIZE_VERTICAL_BOTTOM:
						getGridRegion().height = (e.y - startDragMousePt.y)
								+ startGridRect.height;
						break;
					default:
						break;
					}
					canvas.redraw();
					notifyChangeListener();
				} else {

					canvas.setCursor(new Cursor(canvas.getDisplay(),
							SWT.CURSOR_ARROW));
					if (gridRegion != null) {
						if (gridRegion.getRectangle().contains(e.x, e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_HAND));
							dragMode = Mode.MOVE;
						} else if (new Rectangle(gridRegion.getRectangle().x
								+ gridRegion.getRectangle().width, gridRegion
								.getRectangle().y, 10, gridRegion
								.getRectangle().height).contains(e.x, e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZEE));
							dragMode = Mode.RESIZE_HORIZONTAL_RIGHT;
						} else if (new Rectangle(
								gridRegion.getRectangle().x - 10, gridRegion
										.getRectangle().y, 10, gridRegion
										.getRectangle().height).contains(e.x,
								e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZEW));
							dragMode = Mode.RESIZE_HORIZONTAL_LEFT;
						} else if (new Rectangle(gridRegion.getRectangle().x,
								gridRegion.getRectangle().y - 10, gridRegion
										.getRectangle().width, 10).contains(
								e.x, e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZEN));
							dragMode = Mode.RESIZE_VERTICAL;
						} else if (new Rectangle(gridRegion.getRectangle().x,
								gridRegion.getRectangle().y
										+ gridRegion.getRectangle().height,
								gridRegion.getRectangle().width, 10).contains(
								e.x, e.y)) {
							canvas.setCursor(new Cursor(canvas.getDisplay(),
									SWT.CURSOR_SIZES));
							dragMode = Mode.RESIZE_VERTICAL;
						} else {
							dragMode = Mode.NONE;
						}
					}
				}

			}
		});

		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(MouseEvent e) {

				if (scannedImage == null)
					return;

				if (dragMode != Mode.NONE) {
					drag = true;
					startDragMousePt.y = e.y;
					startDragMousePt.x = e.x;
					startGridRect = new Rectangle(gridRegion.getRectangle().x,
							gridRegion.getRectangle().y, gridRegion
									.getRectangle().width, gridRegion
									.getRectangle().height);

				}
				canvas.redraw();
				canvas.update();
				notifyChangeListener();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
				drag = false;
				dragMode = Mode.NONE;
			}
		});

		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {

				File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
				if (platesFile.exists()
						&& (platesFileLastModified != platesFile.lastModified())) {
					platesFileLastModified = platesFile.lastModified();
					scannedImage = new Image(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell()
									.getDisplay(), PALLET_IMAGE_FILE);
				}
				if (scannedImage == null)
					return;

				Rectangle imgBounds = scannedImage.getBounds();

				Point canvasSize = canvas.getSize();

				double imgAspectRatio = (double) imgBounds.width
						/ (double) imgBounds.height;
				// wide
				if (imgAspectRatio > 1) {
					canvasSize.y = (int) (canvasSize.x / imgAspectRatio);
				} else {
					canvasSize.x = (int) (canvasSize.y * imgAspectRatio);
				}
				if (!canvas.getSize().equals(canvasSize)
						&& !canvas.getSize().equals(canvas.getClientArea()))
					canvas.setSize(canvasSize);

				Rectangle canvasBounds = canvas.getBounds();

				imageBuffer[currentBuffer] = new Image(canvas.getDisplay(),
						canvas.getBounds());

				if (imageBuffer[0] == null) {
					imageBuffer[0] = new Image(canvas.getDisplay(), canvas
							.getBounds());
				}
				if (imageBuffer[1] == null) {
					imageBuffer[1] = new Image(canvas.getDisplay(), canvas
							.getBounds());
				}

				GC gc = new GC(imageBuffer[currentBuffer]);

				// width/height <= 1
				gc.drawImage(scannedImage, 0, 0, imgBounds.width,
						imgBounds.height, 0, 0, canvasBounds.width,
						canvasBounds.height);

				gc.setForeground(mycolor);
				// gc.drawRectangle(getGridRegion().getRectangle());

				double gx = (3.0 / 32.0) / getGridRegion().regionToPixelWidth();
				double gy = (3.0 / 32.0)
						/ getGridRegion().regionToPixelHeight();
				double w = (getGridRegion().getRectangle().width - gx * 12) / 12;
				double h = (getGridRegion().getRectangle().height - gy * 8) / 8;
				double ox = getGridRegion().getRectangle().x;
				double oy = getGridRegion().getRectangle().y;

				for (int j = 0; j < 8; j++) {
					for (int i = 0; i < 12; i++) {
						gc.drawRectangle(new Rectangle(
								(int) (ox + i * (gx + w)), (int) (oy + j
										* (gy + h)), (int) w, (int) h));
					}
				}

				gc.drawOval((int) getGridRegion().left - 3,
						(int) getGridRegion().top - 3, 6, 6);
				gc.drawOval(
						(int) (getGridRegion().left + getGridRegion().width) - 3,
						(int) (getGridRegion().top + getGridRegion().height) - 3,
						6, 6);

				// if (currentBuffer == 1) {
				e.gc.drawImage(imageBuffer[1], 0, 0);
				currentBuffer = 1;
				// } else {
				// e.gc.drawImage(imageBuffer[0], 0, 0);
				// currentBuffer = 1;
				// }

				gc.dispose();
			}
		});
		canvas.redraw();
		canvas.update();
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

	public void assignRegions(String name, double left, double top,
			double right, double bottom) {
		Assert.isNotNull(canvas, "canvas is null");
		this.gridRegion = new GridRegion(new ScannerRegion(name, left, top,
				right, bottom));
	}

	public ScannerRegion getPlateRegion() {
		return this.getGridRegion().getScannerRegion();
	}
}
// canvas.getParent().addControlListener(new ControlListener() {
//
// @Override
// public void controlMoved(ControlEvent e) {
// }
//
// @Override
// public void controlResized(ControlEvent e) {
//
// if (canvas.getSize() != null
// && !canvas.getSize().equals(new Point(0, 0))) {
// }
// }
// });

//
// public GridRegion() {
// left = top = width = height = 0.0;
// }
//
// public GridRegion(GridRegion region) {
// left = region.left;
// top = region.top;
// width = region.width;
// height = region.height;
// }
//
// public GridRegion(double left, double top, double width, double
// height) {
// this.left = left;
// this.top = top;
// this.width = width;
// this.height = height;
// }
//
// public void RectRegion(GridRegion region) {
// this.RectRegion(region);
// }
//
// public void set(double left, double top, double width, double height)
// {
// this.left = left;
// this.top = top;
// this.width = width;
// this.height = height;
// }
//
// public boolean equal(GridRegion region, double epsilon) {
// return ((Math.abs(left - region.left) <= epsilon)
// && (Math.abs(top - region.top) <= epsilon)
// && (Math.abs(width - region.width) < epsilon) && (Math
// .abs(height - region.height) < epsilon));
// }