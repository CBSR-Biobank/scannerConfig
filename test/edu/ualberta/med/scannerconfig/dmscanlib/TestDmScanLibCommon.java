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

import edu.ualberta.med.scannerconfig.BarcodePosition;
import edu.ualberta.med.scannerconfig.ImageInfo;
import edu.ualberta.med.scannerconfig.PlateDimensions;
import edu.ualberta.med.scannerconfig.PlateOrientation;

@SuppressWarnings("nls")
public class TestDmScanLibCommon extends RequiresJniLibraryTest {

    private static Logger log = LoggerFactory
        .getLogger(TestDmScanLibCommon.class);

    /*
     * Uses files from Dropbox shared folder.
     */
    @Test
    public void decodeImage() throws Exception {
        ScanLib scanLib = ScanLib.getInstance();

        final String fname =
            System.getProperty("user.dir") + "/testImages/ohs_pallet.bmp";
        File imageFile = new File(fname);

        BufferedImage image = ImageIO.read(imageFile);
        final int dpi = ImageInfo.getImageDpi(imageFile);
        final double dotWidth = 1 / new Double(dpi).doubleValue();
        BoundingBox imageBbox = new BoundingBox(new Point(0, 0),
            new Point(image.getWidth(), image.getHeight()).scale(dotWidth));

        log.debug("image dimensions: {}", imageBbox);

        Set<CellRectangle> wells = CellRectangle.getCellsForBoundingBox(
            imageBbox, PlateOrientation.LANDSCAPE, PlateDimensions.DIM_ROWS_8_COLS_12,
            BarcodePosition.BOTTOM);

        DecodeResult r = scanLib.decodeImage(3, fname,
            DecodeOptions.getDefaultDecodeOptions(),
            wells.toArray(new CellRectangle[] {}));

        Assert.assertNotNull(r);
        log.debug("result is: {}", r.getResultCode());
        Assert.assertTrue(r.getDecodedWells().size() > 0);

        for (DecodedWell decodedWell : r.getDecodedWells()) {
            log.debug("decoded well: {}", decodedWell);
        }

        log.debug("wells decoded: {}", r.getDecodedWells().size());
    }

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
        CellRectangle[] wells = new CellRectangle[8 * 12];

        r = scanLib.decodeImage(3, fname, decodeOptions, wells);

        Assert.assertNotNull(r);
        Assert.assertEquals(ScanLib.SC_INVALID_NOTHING_TO_DECODE,
            r.getResultCode());
        Assert.assertEquals(0, r.getDecodedWells().size());

        // try and invalid filename
        wells =
            new CellRectangle[] {
                new CellRectangle("A12", new BoundingBox(new Point(10, 20),
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
