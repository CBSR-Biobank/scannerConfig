package edu.ualberta.med.scannerconfig.calibration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.scanlib.ScanCell;
import edu.ualberta.med.scannerconfig.scanlib.ScanLib;

public class FitnessFunct extends FitnessFunction {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	// (0.33 is recommended)
	// Accuracy: constant between range: [0.032,0.036)
	// Speed: very small impact (inversely proportional to speed)
	public final static double CELLDIST_LIST[] = { 0.32, 0.33, 0.34, 0.345,
			0.35, 0.36 };

	// (0.085 is recommended)
	// Accuracy: constant between range: [0.065,0.096) , outside that range,
	// accuracy drops fast.
	// Speed: small impact when in-between the suggested [0.065,0.096) range. (
	// gap setting is directly proportional to speed)
	public final static double GAPS_LIST[] = { 0.065, 0.075, 0.085, 0.090,
			0.093, 0.096 };

	// (5 is recommended)
	// Accuracy: high level of impact, optimal threshold range: (0,10).
	// Speed: no noticeable impact
	public final static int THRESHOLD_LIST[] = { 2, 3, 5, 7, 8 }; // 0-100

	// (15 is recommended)
	// Accuracy: constant when set to 10+ .
	// Speed: small impact, inversely proportional to speed.
	public final static int SQUAREDEV_LIST[] = { 10, 12, 15, 17, 18 };

	// (10 is recommended)
	// Accuracy: constant when set to 5+
	// Speed: no noticeable impact
	public final static int CORRECTION_LIST[] = { 5, 6, 8, 10, 12 };

	public final static int BRIGHTNESS_LIST[] = { -750, -500, -250, -100, -50,
			-20, -10, 0, 10, 20, 50, 100, 250, 500, 750 };

	public final static int CONTRAST_LIST[] = { -750, -500, -250, -100, -50,
			-20, -10, 0, 10, 20, 50, 100, 250, 500, 750 };

	public static int getSquareDev(IChromosome chroma) {
		return SQUAREDEV_LIST[(Integer) chroma.getGene(0).getAllele()];
	}

	public static int getThreshold(IChromosome chroma) {
		return THRESHOLD_LIST[(Integer) chroma.getGene(1).getAllele()];
	}

	public static double getGap(IChromosome chroma) {
		return GAPS_LIST[(Integer) chroma.getGene(2).getAllele()];
	}

	public static int getCorrections(IChromosome chroma) {
		return CORRECTION_LIST[(Integer) chroma.getGene(3).getAllele()];
	}

	public static double getCellDist(IChromosome chroma) {
		return CELLDIST_LIST[(Integer) chroma.getGene(4).getAllele()];
	}

	public static int getBrightness(IChromosome chroma) {
		return BRIGHTNESS_LIST[(Integer) chroma.getGene(5).getAllele()];
	}

	public static int getContrast(IChromosome chroma) {
		return CONTRAST_LIST[(Integer) chroma.getGene(6).getAllele()];
	}

	/* Our fitness value is our test-tubes scanned */
	public static int getTubesScanned(IChromosome chroma) {
		return (int) chroma.getFitnessValue();
	}

	@Override
	/* TODO incorporate scanning time into the fitness value */
	protected double evaluate(IChromosome chroma) {
		return scanlibCount(chroma);
	}

	// Returns the amount of tubes successfully scanned
	private static int scanlibCount(IChromosome chroma) {

		// Delete scanlib.txt if it exists
		File scanlibFile = new File("scanlib.txt");
		if (scanlibFile.exists()) {
			scanlibFile.delete();
		}

		double gap = getGap(chroma);
		double celldist = getCellDist(chroma);

		int squareDev = getSquareDev(chroma);
		int threshold = getThreshold(chroma);
		int corrections = getCorrections(chroma);

		if (chroma.getGenes().length == 7) { // TWAIN

			int brightness = getBrightness(chroma);
			int contrast = getContrast(chroma);
			
			int dpi = ScannerConfigPlugin.getDefault().getPreferenceStore().getInt(
		            PreferenceConstants.SCANNER_DPI);

			//plate = 1
			int tubesscanned = ScannerConfigPlugin.getTestTubesScanned(1, dpi,
					brightness, contrast, 0, threshold, gap, squareDev,
					corrections, celldist);

			// XXX Debug Code
			System.out
					.printf(
							"TWAIN Tubes Scanned: %02d, Return: %01d, Set: "
									+ "bright: %03d, contra: %03d, gap: %.3f, squdev: %02d, thres: %03d, corr: %02d, celld: %.3f\n",
							tubesscanned, 0, brightness, contrast, gap,
							squareDev, threshold, corrections, celldist);

			return tubesscanned;
		} else { // WIA

			long before = System.currentTimeMillis();

			int retcode = ScanLib.getInstance().slDecodeImage(0, 1,
					"calibration.bmp", gap, squareDev, threshold, corrections,
					celldist);

			long scanTime = (System.currentTimeMillis() - before) / 1000; // sec

			if (retcode != ScanLib.SC_SUCCESS) {
				// XXX Debug Code
				System.out.printf("Scanlib: Could not decode image\n");
				System.out
						.printf(
								"Evil Settings: "
										+ "gap: %.3f, squdev: %02d, thres: %03d, corr: %02d, celld: %.3f\n",
								gap, squareDev, threshold, corrections,
								celldist);
				return 0;
			}

			int tubesscanned = countTubesScanned();
			// XXX Debug Code
			System.out
					.printf(
							"WIA Tubes Scanned: %02d, Time: %04d Return: %01d, Set: "
									+ "gap: %.3f, squdev: %02d, thres: %03d, corr: %02d, celld: %.3f\n",
							tubesscanned, scanTime, retcode, gap, squareDev,
							threshold, corrections, celldist);
			return tubesscanned;
		}

	}

	public static int countTubesScanned() {
		File scanlibFile = new File("scanlib.txt");
		Scanner fileInput = null;
		int tubesScanned = 0;

		if (scanlibFile.exists()) {
			try {
				fileInput = new Scanner(new BufferedReader(new FileReader(
						"scanlib.txt")));
			} catch (IOException e) {
			}
			while (fileInput.hasNextLine()) {
				tubesScanned++;
				fileInput.nextLine();
			}
		}
		if (tubesScanned > 0) // CSV line
			--tubesScanned;

		return tubesScanned;
	}

}