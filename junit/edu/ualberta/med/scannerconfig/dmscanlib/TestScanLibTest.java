package edu.ualberta.med.scannerconfig.dmscanlib;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            new DecodeOptions(0, 0, 0, 0, 0), new WellRectangle[] {});
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());
    }

    /*
     * Uses files from Dropbox shared folder.
     */
    @Test
    public void testDecodeImage() throws Exception {
        ScanLib scanLib = ScanLib.getInstance();

        String fname = System.getenv("HOME")
            + "/Dropbox/CBSR/scanlib/testImages/96tubes_cropped.bmp";

        DecodeOptions decodeOptions =
            new DecodeOptions(0.085, 15, 5, 10, 0.345);

        final WellRectangle[] wells =
            new WellRectangle[] {
                new WellRectangle("A12", new BoundingBox(10.0 / 400.0,
                    20.0 / 400.0, 130.0 / 400.0, 130.0 / 400.0)),
                new WellRectangle("A11", new BoundingBox(150.0 / 400.0,
                    24.0 / 400.0, 250.0 / 400.0, 120.0 / 400.0))
            };

        log.debug("well rectangle: {}", wells[0]);

        DecodeResult r = scanLib.decodeImage(0, fname, decodeOptions, wells);

        Assert.assertNotNull(r);

        log.debug("wells decoded: {}", r.getDecodedWells().size());
        Assert.assertTrue(r.getDecodedWells().size() > 0);

        for (DecodedWell decodedWell : r.getDecodedWells()) {
            log.debug("decoded well: {}", decodedWell);
        }
    }
}
