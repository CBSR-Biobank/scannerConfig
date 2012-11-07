package edu.ualberta.med.scannerconfig.dmscanlib;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDmScanLibLinux extends BaseTest {

    private static Logger log = LoggerFactory
        .getLogger(TestDmScanLibLinux.class);

    @Test
    public void linuxEmptyImplementationJNI() throws Exception {
        // this test is valid only when not running on windows
        Assert.assertEquals(false, LibraryLoader.getInstance()
            .runningMsWindows());

        ScanLib scanLib = ScanLib.getInstance();
        ScanLibResult r = scanLib.isTwainAvailable();
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.getScannerCapability();
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.scanImage(0, 0, 0, 0, null, "tmp.txt");
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.scanFlatbed(0, 0, 0, 0, "tmp.txt");
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r =
            scanLib.scanAndDecode(0, 0, 0, 0, null, null,
                new WellRectangle[] {});
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());
    }

    /*
     * Uses files from Dropbox shared folder.
     */
    @Test
    public void decodeImage() throws Exception {
        ScanLib scanLib = ScanLib.getInstance();

        final String fname =
            System.getProperty("user.dir") + "/testImages/96tubes.bmp";
        File imageFile = new File(fname);

        BufferedImage image = ImageIO.read(imageFile);
        final int dpi = ImageInfo.getImageDpi(imageFile);
        final double dotWidth = 1 / new Double(dpi).doubleValue();
        BoundingBox imageBbox = new BoundingBox(new Point(0, 0),
            new Point(image.getWidth(), image.getHeight()).scale(dotWidth));

        log.debug("image dimensions: {}", imageBbox);

        Set<WellRectangle> wells =
            WellRectangle.getWellRectanglesForBoundingBox(
                imageBbox, 8, 12, dpi);

        // log.debug("well rectangle: {}", wells[0]);

        DecodeResult r = scanLib.decodeImage(0, fname,
                DecodeOptions.getDefaultDecodeOptions(),
                wells.toArray(new WellRectangle[] {}));

        Assert.assertNotNull(r);
        log.debug("result is: {}", r.getResultCode());
        Assert.assertTrue(r.getDecodedWells().size() > 0);

        for (DecodedWell decodedWell : r.getDecodedWells()) {
            log.debug("decoded well: {}", decodedWell);
        }

        log.debug("wells decoded: {}", r.getDecodedWells().size());
    }

    /*
     * Uses files from Dropbox shared folder.
     */
    @Test
    public void decodeBadParams() throws Exception {
        ScanLib scanLib = ScanLib.getInstance();

        final String fname =
            System.getProperty("user.dir") + "/testImages/96tubes.bmp";

        DecodeOptions decodeOptions = DecodeOptions.getDefaultDecodeOptions();

        DecodeResult r = scanLib.decodeImage(3, fname, decodeOptions, null);

        Assert.assertNotNull(r);
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(0, r.getDecodedWells().size());

        // do not fill in the well information
        WellRectangle[] wells = new WellRectangle[8 * 12];

        r = scanLib.decodeImage(3, fname, decodeOptions, wells);

        Assert.assertNotNull(r);
        Assert.assertEquals(ScanLib.SC_INVALID_NOTHING_TO_DECODE,
            r.getResultCode());
        Assert.assertEquals(0, r.getDecodedWells().size());

        // try and invalid filename
        wells =
            new WellRectangle[] {
                new WellRectangle("A12", new BoundingBox(new Point(10, 20),
                    new Point(130, 130))),
            };

        r =
            scanLib.decodeImage(5, new UUID(128, 256).toString(),
                decodeOptions, wells);

        Assert.assertNotNull(r);
        Assert.assertEquals(ScanLib.SC_INVALID_IMAGE, r.getResultCode());
        Assert.assertEquals(0, r.getDecodedWells().size());
    }
}
