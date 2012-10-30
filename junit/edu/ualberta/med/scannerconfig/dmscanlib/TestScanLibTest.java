package edu.ualberta.med.scannerconfig.dmscanlib;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.biobank.util.SbsLabeling;

public class TestScanLibTest {

    private static Logger log = LoggerFactory.getLogger(TestScanLibTest.class);

    private boolean isMsWindows = false;

    @Before
    public void setUp() throws Exception {
        String osname = System.getProperty("os.name");
        isMsWindows = osname.startsWith("Windows");

        if (isMsWindows) {
            System.loadLibrary("OpenThreadsWin32");
            System.loadLibrary("cxcore210");
            System.loadLibrary("cv210");
            System.loadLibrary("msvcr100");
            System.loadLibrary("msvcp100");
            System.loadLibrary("libglog");
            System.loadLibrary("dmscanlib");
        } else {
            System.loadLibrary("dmscanlib64");
        }
    }

    @Test
    public void testLinuxEmptyImplementationJNI() throws Exception {
        if (isMsWindows) return;

        ScanLib scanLib = ScanLib.getInstance();
        ScanLibResult r = scanLib.isTwainAvailable();
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.getScannerCapability();
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.scanImage(
            0, 0, 0, 0, new BoundingBox(0, 0, 0, 0), "tmp.txt");
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.scanFlatbed(0, 0, 0, 0, "tmp.txt");
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.scanAndDecode(0, 0, 0, 0, new BoundingBox(0, 0, 0, 0),
            new DecodeOptions(0, 0, 0, 0, 0, 0), new WellRectangle[] {});
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());
    }

    /*
     * Uses files from Dropbox shared folder.
     */
    @Test
    public void testDecodeImage() throws Exception {
        ScanLib scanLib = ScanLib.getInstance();

        DecodeOptions decodeOptions =
            // new DecodeOptions(0.085, 10, 5, 10, 1, 0.345);
            new DecodeOptions(0.05, 10, 5, 10, 1, 0.345);

        String fname = System.getenv("HOME")
            + "/Dropbox/CBSR/scanlib/testImages/96tubes_cropped.bmp";
        // + "/Dropbox/CBSR/scanlib/testImages/ohs_pallet.bmp";

        Set<WellRectangle> wells = new HashSet<WellRectangle>();

        BufferedImage image = ImageIO.read(new File(fname));
        double width = image.getWidth();
        double height = image.getHeight();
        double wellWidth = width / 12.0;
        double wellHeight = height / 8.0;
        Point horTranslation = new Point(new Double(wellWidth).intValue(), 0);
        Point verTranslation = new Point(0, new Double(wellHeight).intValue());

        for (int row = 0; row < 8; ++row) {
            BoundingBox bbox =
                new BoundingBox(0, 0,
                    new Double(wellWidth).intValue(),
                    new Double(wellHeight).intValue()).translate(verTranslation
                    .scale(row));

            for (int col = 0; col < 12; ++col) {
                WellRectangle well = new WellRectangle(
                    SbsLabeling.fromRowCol(row, 11 - col), bbox);
                log.debug("{}", well);
                wells.add(well);
                bbox = bbox.translate(horTranslation);
            }
        }

        // log.debug("well rectangle: {}", wells[0]);

        DecodeResult r = scanLib.decodeImage(3, fname, decodeOptions,
            wells.toArray(new WellRectangle[] {}));

        Assert.assertNotNull(r);
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
    public void testDecodeBadParams() throws Exception {
        ScanLib scanLib = ScanLib.getInstance();

        String fname = System.getenv("HOME")
            + "/Dropbox/CBSR/scanlib/testImages/96tubes_cropped.bmp";

        DecodeOptions decodeOptions =
            new DecodeOptions(0.085, 15, 5, 10, 1, 0.345);

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
        wells = new WellRectangle[] {
            new WellRectangle("A12", new BoundingBox(10, 20, 130, 130)),
        };

        r = scanLib.decodeImage(5, new UUID(128, 256).toString(),
            decodeOptions, wells);

        Assert.assertNotNull(r);
        Assert.assertEquals(ScanLib.SC_INVALID_IMAGE, r.getResultCode());
        Assert.assertEquals(0, r.getDecodedWells().size());
    }
}
