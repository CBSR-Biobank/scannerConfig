package edu.ualberta.med.scannerconfig.dmscanlib;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ScanLibSimulate extends ScanLib {

	private static Random r = new Random();

	@Override
	public int slIsTwainAvailable() {
		return -1;
	}

	@Override
	public int slSelectSourceAsDefault() {
		return -1;
	}

	@Override
	public int slScanImage(long verbose, long dpi, int brightness,
			int contrast, double left, double top, double right, double bottom,
			String filename) {
		return -1;
	}

	@Override
	public int slDecodePlate(long verbose, long dpi, int brightness,
			int contrast, long plateNum, double left, double top, double right,
			double bottom, double scanGap, long squareDev, long edgeThresh,
			long corrections, double cellDistance, double gapX, double gapY,
			long profileA, long profileB, long profileC) {
		try {
			// simulate decode
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"dmscanlib.txt"));

			List<Integer> sampleIds = new ArrayList<Integer>();
			int samples = r.nextInt(96) + 1;
			for (int i = 0; i < samples; ++i) {
				sampleIds.add(r.nextInt(samples));
			}
			Collections.sort(sampleIds);

			out.write("#Plate,Row,Col,Barcode");
			out.newLine();
			for (Integer id : sampleIds) {
				out.write(String.format("%d,%c,%d,%s", plateNum,
						(id / 12) + 'A', id % 12 + 1, getRandomString(10)));
				out.newLine();
			}
			out.flush();
			return ScanLib.SC_SUCCESS;
		} catch (IOException e) {
			return ScanLib.SC_INVALID_IMAGE;
		}
	}

	private static String getRandomString(int maxlen) {
		String str = new String();
		for (int j = 0; j < maxlen; ++j) {
			str += (char) ('A' + r.nextInt(26));
		}
		return str;
	}

	@Override
	public int slDecodeImage(long verbose, long plateNum, String filename,
			double scanGap, long squareDev, long edgeThresh, long corrections,
			double cellDistance, double gapX, double gapY, long profileA,
			long profileB, long profileC) {
		return -1;
	}

	@Override
	public int slGetScannerCapability() {
		return 255;
	}

}
