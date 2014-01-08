package edu.ualberta.med.scannerconfig.dmscanlib;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.scannerconfig.BarcodePosition;
import edu.ualberta.med.scannerconfig.ImageInfo;
import edu.ualberta.med.scannerconfig.PalletDimensions;
import edu.ualberta.med.scannerconfig.PalletOrientation;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;

@SuppressWarnings("nls")
public class TestDmScanLibWindows extends RequiresJniLibraryTest {

	private static Logger log = LoggerFactory
			.getLogger(TestDmScanLibWindows.class);

	@Before
	public void beforeMethod() {
		// these tests are valid only when not running on windows
		Assume.assumeTrue(LibraryLoader.getInstance().runningMsWindows());
	}

	@Test
	public void scanImage() throws Exception {
		ScanLib scanLib = ScanLib.getInstance();
		ScanLibResult r = scanLib.isTwainAvailable();
		Assert.assertEquals(ScanLibResult.Result.SUCCESS, r.getResultCode());

		BoundingBox region = new BoundingBox(new Point(0, 0), new Point(4, 4));

		final int dpi = 300;
		String filename = "tempscan.bmp";
		File file = new File(filename);
		file.delete(); // dont care if file doesn't exist

		r = scanLib.scanImage(3, dpi, 0, 0, region, filename);

		Assert.assertNotNull(r);
		Assert.assertEquals(ScanLibResult.Result.SUCCESS, r.getResultCode());

		File imageFile = new File(filename);
		Assert.assertTrue(Math.abs(dpi - ImageInfo.getImageDpi(imageFile)) <= 1);

		BufferedImage image = ImageIO.read(imageFile);
		Assert.assertEquals(new Double(region.getWidth() * dpi).intValue(),
				image.getWidth());
		Assert.assertEquals(new Double(region.getHeight() * dpi).intValue(),
				image.getHeight());
	}

	@Test
	public void scanImageBadParams() throws Exception {
		ScanLib scanLib = ScanLib.getInstance();
		ScanLibResult r = scanLib.isTwainAvailable();
		Assert.assertEquals(ScanLibResult.Result.SUCCESS, r.getResultCode());

		BoundingBox scanBox = new BoundingBox(new Point(0, 0), new Point(4, 4));

		r = scanLib.scanImage(0, 300, 0, 0, scanBox, null);
		Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());

		r = scanLib.scanImage(0, 300, 0, 0, null, "tempscan.bmp");
		Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());

		r = scanLib.scanImage(0, 0, 0, 0, scanBox, "tempscan.bmp");
		Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());
	}

	@Test
	public void scanFlatbed() throws Exception {
		ScanLib scanLib = ScanLib.getInstance();
		ScanLibResult r = scanLib.isTwainAvailable();
		Assert.assertEquals(ScanLibResult.Result.SUCCESS, r.getResultCode());

		final int dpi = 300;
		String filename = "flatbed.bmp";
		File file = new File(filename);
		file.delete(); // dont care if file doesn't exist

		r = scanLib.scanFlatbed(0, dpi, 0, 0, filename);

		Assert.assertNotNull(r);
		Assert.assertEquals(ScanLibResult.Result.SUCCESS, r.getResultCode());

		File imageFile = new File(filename);
		Assert.assertTrue(Math.abs(dpi - ImageInfo.getImageDpi(imageFile)) <= 1);
	}

	@Test
	public void scanFlatbedBadParams() throws Exception {
		ScanLib scanLib = ScanLib.getInstance();
		ScanLibResult r = scanLib.isTwainAvailable();
		Assert.assertEquals(ScanLibResult.Result.SUCCESS, r.getResultCode());

		r = scanLib.scanFlatbed(0, 300, 0, 0, null);
		Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());

		r = scanLib.scanFlatbed(0, 0, 0, 0, "tempscan.bmp");
		Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());
	}

	@Test
	public void scanAndDecode() throws Exception {
		ScanLib scanLib = ScanLib.getInstance();
		ScanLibResult r = scanLib.isTwainAvailable();
		Assert.assertEquals(ScanLibResult.Result.SUCCESS, r.getResultCode());

		BoundingBox scanRegion = new BoundingBox(new Point(0.400, 0.265),
				new Point(4.566, 3.020));

		BoundingBox scanBbox = ScannerConfigPlugin
				.getWiaBoundingBox(scanRegion);

		final int dpi = 300;

		BoundingBox wellsBbox = new BoundingBox(0, 0, Math.floor(dpi
				* scanRegion.getWidth()), Math.floor(dpi
				* scanRegion.getHeight()));

		Set<CellRectangle> wells = CellRectangle.getCellsForBoundingBox(
				wellsBbox, PalletOrientation.LANDSCAPE,
				PalletDimensions.DIM_ROWS_8_COLS_12, BarcodePosition.BOTTOM);

		DecodeResult dr = scanLib.scanAndDecode(3, dpi, 0, 0, scanBbox,
				DecodeOptions.getDefaultDecodeOptions(),
				wells.toArray(new CellRectangle[] {}));

		Assert.assertNotNull(dr);
		Assert.assertFalse(dr.getDecodedWells().isEmpty());

		for (DecodedWell decodedWell : dr.getDecodedWells()) {
			log.debug("decoded well: {}", decodedWell);
		}

		log.debug("wells decoded: {}", dr.getDecodedWells().size());
	}
}
