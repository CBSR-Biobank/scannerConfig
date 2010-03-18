package edu.ualberta.med.scannerconfig;

import java.io.File;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class PlateImageManager {

    private Canvas canvas;

    public static final String PALLET_IMAGE_FILE = "plates.bmp";

    public static final double PALLET_IMAGE_DPI = 100.0;

    private boolean pointTopLeft;

    private ScannerRegion region;

    private boolean isTwain;

    private Image img;

    private double imgWidth;

    private double imgHeight;

    private double width;

    private double height;

    private Text[] textControls;

    private long platesFileLastModified;

    // pointTopLeft: Used to determine which point the user is currently
    // adjusting.The point is either top-left or bottom-right.

    public PlateImageManager(Canvas c, ScannerRegion r, final Color mycolor,
        Text[] controls) {
        region = r;
        this.textControls = controls;

        isTwain = ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getString(PreferenceConstants.SCANNER_DRV_TYPE).equals(
                PreferenceConstants.SCANNER_DRV_TYPE_TWAIN);

        File platesFile = new File(PlateImageManager.PALLET_IMAGE_FILE);
        if (platesFile.exists()) {
            platesFileLastModified = platesFile.lastModified();
            img = new Image(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell().getDisplay(),
                PALLET_IMAGE_FILE);
            Rectangle bounds = img.getBounds();
            imgWidth = bounds.width;
            imgHeight = bounds.height;

        }

        canvas = c;
        Rectangle bounds = canvas.getBounds();
        width = bounds.width;
        height = bounds.height;

        pointTopLeft = true;

        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }

            @Override
            public void mouseDown(MouseEvent e) {

                if (pointTopLeft) {
                    pointTopLeft = false;
                    double x1 = (e.x / PALLET_IMAGE_DPI / (width / imgWidth));
                    double y1 = (e.y / PALLET_IMAGE_DPI / (height / imgHeight));
                    region.left = x1;
                    region.top = y1;
                } else {
                    double x2 = (e.x / PALLET_IMAGE_DPI / (width / imgWidth));
                    double y2 = (e.y / PALLET_IMAGE_DPI / (height / imgHeight));
                    if (isTwain) {
                        if ((x2 > region.left) && (y2 > region.left)) {
                            region.right = x2;
                            region.bottom = y2;
                            pointTopLeft = true;
                        } else {
                            pointTopLeft = false;
                            double x1 = (e.x / PALLET_IMAGE_DPI / (width / imgWidth));
                            double y1 = (e.y / PALLET_IMAGE_DPI / (height / imgHeight));
                            region.left = x1;
                            region.top = y1;
                        }
                    } else {
                        // WIA
                        if ((x2 - region.left > 0) && (y2 - region.top > 0)) {
                            region.right = x2 - region.left;
                            region.bottom = y2 - region.top;
                            pointTopLeft = true;
                        } else {
                            pointTopLeft = false;

                            double x1 = (e.x / PALLET_IMAGE_DPI / (width / imgWidth));
                            double y1 = (e.y / PALLET_IMAGE_DPI / (height / imgHeight));
                            region.left = x1;
                            region.top = y1;
                        }
                    }

                }

                textControls[0].setText("" + region.left);
                textControls[1].setText("" + region.top);
                textControls[2].setText("" + region.right);
                textControls[3].setText("" + region.bottom);
            }

            @Override
            public void mouseUp(MouseEvent e) {
            }
        });

        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                File platesFile = new File(
                    PlateImageManager.PALLET_IMAGE_FILE);
                if (platesFile.exists()
                    && (platesFileLastModified != platesFile.lastModified())) {
                    platesFileLastModified = platesFile.lastModified();
                    img = new Image(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell().getDisplay(),
                        PALLET_IMAGE_FILE);
                    Rectangle bounds = img.getBounds();
                    imgWidth = bounds.width;
                    imgHeight = bounds.height;
                }

                GC gc = new GC(canvas);
                gc.drawImage(img, 0, 0, (int) imgWidth, (int) imgHeight, 0, 0,
                    (int) width, (int) height);
                gc.setForeground(mycolor);
                double x1 = region.left * PALLET_IMAGE_DPI * (width / imgWidth);
                double y1 = region.top * PALLET_IMAGE_DPI
                    * (height / imgHeight);
                double x2 = region.right * PALLET_IMAGE_DPI
                    * (width / imgWidth);
                double y2 = region.bottom * PALLET_IMAGE_DPI
                    * (height / imgHeight);

                if (isTwain) {
                    gc.drawRectangle((int) x1, (int) y1, (int) (x2 - x1),
                        (int) (y2 - y1));
                } else {
                    // WIA x,y,w,h
                    gc.drawRectangle((int) x1, (int) y1, (int) x2, (int) y2);
                }

                gc.dispose();
            }
        });

        canvas.addControlListener(new ControlListener() {

            @Override
            public void controlMoved(ControlEvent e) {
            }

            @Override
            public void controlResized(ControlEvent e) {
                Rectangle bounds = canvas.getBounds();
                width = bounds.width;
                height = bounds.height;
            }

        });

        canvas.update();
    }

    public void assignRegionLeft(double left) {
        region.left = left;
        canvas.redraw();
        canvas.update();
    }

    public void assignRegionTop(double top) {
        region.top = top;
        canvas.redraw();
        canvas.update();
    }

    public void assignRegionRight(double right) {
        region.right = right;
        canvas.redraw();
        canvas.update();
    }

    public void assignRegionBottom(double bottom) {
        region.bottom = bottom;
        canvas.redraw();
        canvas.update();
    }
}
