package edu.ualberta.med.scannerconfig.widgets;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import edu.ualberta.med.scanlib.ScanLib;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

public class PalletImageWidget extends Composite {

    private Canvas canvas;

    public static final String palletImageFile = "pallets.bmp";
    public static final double palletImageDpi = 100.0;

    private boolean pointTopLeft;

    private ScannerRegion region;

    private boolean isTwain;

    private Image img;

    private double imgWidth;

    private double imgHeight;

    private double width;

    private double height;

    private Text[] textControls;

    // pointTopLeft: Used to determine which point the user is currently
    // adjusting.The point is either top-left or bottom-right.

    public PalletImageWidget(Composite parent, int style, Canvas c,
        ScannerRegion r, final Color mycolor, Text[] controls) {
        super(parent, style);
        region = r;
        this.textControls = controls;

        isTwain = ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getString(PreferenceConstants.SCANNER_DRV_TYPE).equals(
                PreferenceConstants.SCANNER_DRV_TYPE_TWAIN);

        File palletsFile = new File(palletImageFile);
        if (!palletsFile.exists()) {
            ScanLib.getInstance().slScanImage((int) palletImageDpi, 0, 0, 0, 0,
                palletImageFile);
        }
        img = new Image(getDisplay(), palletImageFile);
        Rectangle bounds = img.getBounds();
        imgWidth = bounds.width;
        imgHeight = bounds.height;

        canvas = c;
        bounds = canvas.getBounds();
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
                    double x1 = (e.x / palletImageDpi / (width / imgWidth));
                    double y1 = (e.y / palletImageDpi / (height / imgHeight));
                    region.left = x1;
                    region.top = y1;
                } else {
                    double x2 = (e.x / palletImageDpi / (width / imgWidth));
                    double y2 = (e.y / palletImageDpi / (height / imgHeight));
                    if (isTwain) {
                        if ((x2 > region.left) && (y2 > region.left)) {
                            region.right = x2;
                            region.bottom = y2;
                            pointTopLeft = true;
                        } else {
                            pointTopLeft = false;
                            double x1 = (e.x / palletImageDpi / (width / imgWidth));
                            double y1 = (e.y / palletImageDpi / (height / imgHeight));
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

                            double x1 = (e.x / palletImageDpi / (width / imgWidth));
                            double y1 = (e.y / palletImageDpi / (height / imgHeight));
                            region.left = x1;
                            region.top = y1;
                        }
                    }

                }
                textControls[0].setText("" + region.left);
                textControls[1].setText("" + region.top);
                textControls[2].setText("" + region.right);
                textControls[3].setText("" + region.bottom);
                canvas.redraw();
                canvas.update();
            }

            @Override
            public void mouseUp(MouseEvent e) {
            }
        });

        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                GC gc = new GC(canvas);
                gc.drawImage(img, 0, 0, (int) imgWidth, (int) imgHeight, 0, 0,
                    (int) width, (int) height);
                gc.setForeground(mycolor);
                double x1 = region.left * palletImageDpi * (width / imgWidth);
                double y1 = region.top * palletImageDpi * (height / imgHeight);
                double x2 = region.right * palletImageDpi * (width / imgWidth);
                double y2 = region.bottom * palletImageDpi
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

    public void assignRegion(double left, double top, double right,
        double bottom) {
        region.set(left, top, right, bottom);
        canvas.redraw();
        canvas.update();
    }
}
