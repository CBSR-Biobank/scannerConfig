package edu.ualberta.med.scannerconfig.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.calibration.AutoCalibrate;
import edu.ualberta.med.scannerconfig.calibration.FitnessFunct;
import edu.ualberta.med.scannerconfig.scanlib.ScanLib;

public class Scanner extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private Map<String, DoubleFieldEditor> dblFieldMap = new HashMap<String, DoubleFieldEditor>();
	private Map<String, IntegerFieldEditor> intFieldMap = new HashMap<String, IntegerFieldEditor>();

	private List<Integer> possibleDpis = Arrays.asList(300, 400, 600);

	private List<Integer> allowedDpis = null;

	public Scanner() {
		super(GRID);
		setPreferenceStore(ScannerConfigPlugin.getDefault()
				.getPreferenceStore());
	}

	@Override
	public void createFieldEditors() {
		Button b = new Button(getFieldEditorParent(), SWT.NONE);
		b.setText("Select Scanner");
		b.setImage(ScannerConfigPlugin.getDefault().getImageRegistry().get(
				ScannerConfigPlugin.IMG_SCANNER));
		b.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				int scanlibReturn = ScanLib.getInstance()
						.slSelectSourceAsDefault();

				if (scanlibReturn != ScanLib.SC_SUCCESS) {
					ScannerConfigPlugin.openError("Source Selection Error",
							ScanLib.getErrMsg(scanlibReturn));
				}

			}
		});

		RadioGroupFieldEditor rgFe = new RadioGroupFieldEditor(
				PreferenceConstants.SCANNER_DRV_TYPE,
				"Driver Type",
				2,
				new String[][] {
						{ "TWAIN", PreferenceConstants.SCANNER_DRV_TYPE_TWAIN },
						{ "WIA", PreferenceConstants.SCANNER_DRV_TYPE_WIA } },
				getFieldEditorParent(), true);
		addField(rgFe);

		// check with scanner for valid dpi's
		if (allowedDpis == null) {
			allowedDpis = new ArrayList<Integer>();

			BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
				public void run() {
					for (Integer dpi : possibleDpis) {
						allowedDpis.add(dpi);
					}
				}
			});
		}

		String[][] options = new String[allowedDpis.size()][2];
		for (int i = 0; i < allowedDpis.size(); ++i) {
			options[i][0] = options[i][1] = allowedDpis.get(i).toString();
		}

		rgFe = new RadioGroupFieldEditor(PreferenceConstants.SCANNER_DPI,
				"DPI", 5, options, getFieldEditorParent(), true);
		addField(rgFe);

		IntegerFieldEditor intFe = new IntegerFieldEditor(
				PreferenceConstants.SCANNER_BRIGHTNESS, "Brightness:",
				getFieldEditorParent());
		intFe.setValidRange(-1000, 1000);
		addField(intFe);
		intFieldMap.put(intFe.getPreferenceName(), intFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.SCANNER_CONTRAST,
				"Contrast:", getFieldEditorParent());
		intFe.setValidRange(-1000, 1000);
		addField(intFe);
		intFieldMap.put(intFe.getPreferenceName(), intFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.DLL_DEBUG_LEVEL,
				"Decode Library Debug Level:", getFieldEditorParent());
		intFe.setValidRange(0, 9);
		addField(intFe);
		intFieldMap.put(intFe.getPreferenceName(), intFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_EDGE_THRESH,
				"Decode Edge Threshold:", getFieldEditorParent());
		intFe.setValidRange(0, 100);
		addField(intFe);
		intFieldMap.put(intFe.getPreferenceName(), intFe);

		DoubleFieldEditor dblFe = new DoubleFieldEditor(
				PreferenceConstants.LIBDMTX_SCAN_GAP, "Decode Scan Gap:",
				getFieldEditorParent());
		dblFe.setValidRange(0.0, 1.0);
		addField(dblFe);
		dblFieldMap.put(PreferenceConstants.LIBDMTX_SCAN_GAP, dblFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_SQUARE_DEV,
				"Decode Square Deviation:", getFieldEditorParent());
		intFe.setValidRange(0, 90);
		addField(intFe);
		intFieldMap.put(intFe.getPreferenceName(), intFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_CORRECTIONS,
				"Decode Corrections:", getFieldEditorParent());
		intFe.setValidRange(0, 100);
		addField(intFe);
		intFieldMap.put(intFe.getPreferenceName(), intFe);

		dblFe = new DoubleFieldEditor(
				PreferenceConstants.LIBDMTX_CELL_DISTANCE,
				"Decode Cell Distance:", getFieldEditorParent());
		dblFe.setValidRange(0.0, 1.0);
		addField(dblFe);
		dblFieldMap.put(PreferenceConstants.LIBDMTX_CELL_DISTANCE, dblFe);

		Button autoCalibrateBtn = new Button(getFieldEditorParent(), SWT.NONE);
		autoCalibrateBtn.setText("Automatically Calibrate");
		autoCalibrateBtn.setImage(ScannerConfigPlugin.getDefault()
				.getImageRegistry().get(ScannerConfigPlugin.IMG_SCANNER));
		autoCalibrateBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (!ScannerConfigPlugin.getDefault().getPlateEnabled(1)) {
					ScannerConfigPlugin.openError(
							"Auto-Calibration Requirements",
							"Auto-Calibration is performed on Plate 1.\n"
									+ "Please enable and configure Plate 1.\n");
					return;
				}
				if (!ScannerConfigPlugin
						.openConfim(
								"Place Pallet on Plate 1",
								"Please place a full pallet on Plate 1.\n"
										+ "Note: It is recommended that you use the most difficult "
										+ "test tubes for this process.\n")) {
					return;
				}
				boolean isTwain = false;

				if (ScannerConfigPlugin.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.SCANNER_DRV_TYPE) == PreferenceConstants.SCANNER_DRV_TYPE_TWAIN) {
					isTwain = true;
				}

				if (!isTwain) { // WIA

					/*
					 * fitnessFunct uses the calibration.bmp file in wia mode.
					 * this saves having to rescan the same image over and over.
					 */

					File calibrationImage = new File("calibration.bmp");
					if (calibrationImage.exists()) {
						calibrationImage.delete();
					}
					try {
						ScannerConfigPlugin.scanPlate(1, "calibration.bmp");
					} catch (Exception e1) {
						ScannerConfigPlugin
								.openAsyncError(
										"Error Scanning",
										"Failed to scan Plate 1 to file: calibration.bmp.\n"
												+ "Please make sure Plate 1 is properly configured.");
						return;
					}
				}

				AutoCalibrate autocalibrate = new AutoCalibrate(isTwain, 15);// 10

				if (!autocalibrate.isInitialized()) {
					ScannerConfigPlugin
							.openAsyncError("Auto Calibration Failed",
									"An unexpected error occured, auto-calibration could not be started.");
					return;
				}

				do {
					for (int i = 0; i < 1; i++) {
						autocalibrate.iterateEvolution();
					}

				} while (ScannerConfigPlugin.openConfim(
						"Continue Calibrating?",
						"Current settings discovered are able to scan "
								+ FitnessFunct.getTubesScanned(autocalibrate
										.getBestChromosome())
								+ " test tubes.\n\n"
								+ "Would you like to continue calibrating?"));

				if (isTwain) {
					// Update brightness,contrast text input boxes

					if (ScannerConfigPlugin
							.openConfim(
									"Apply Settings?",
									String
											.format(
													"\tSettings Obtained:\n\n"
															+ "\tBrightness: %d\n"
															+ "\tContrast: %d\n"
															+ "\tEdge Threshold: %d\n"
															+ "\tScan Gap: %.3f\n"
															+ "\tSquare Deviation: %d\n"
															+ "\tCorrections: %d\n"
															+ "\tCell Distance: %.3f\n"
															+ "\nWould you like to apply these settings?",
													FitnessFunct
															.getBrightness(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getContrast(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getThreshold(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getGap(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getSquareDev(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getCorrections(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getCellDist(autocalibrate
																	.getBestChromosome())))) {

						// UPDATE SETTINGS

						intFieldMap.get(PreferenceConstants.SCANNER_BRIGHTNESS)
								.setStringValue(
										Integer.toString(FitnessFunct
												.getBrightness(autocalibrate
														.getBestChromosome())));
						intFieldMap.get(PreferenceConstants.SCANNER_CONTRAST)
								.setStringValue(
										Integer.toString(FitnessFunct
												.getContrast(autocalibrate
														.getBestChromosome())));

						intFieldMap
								.get(PreferenceConstants.LIBDMTX_EDGE_THRESH)
								.setStringValue(
										Integer.toString(FitnessFunct
												.getThreshold(autocalibrate
														.getBestChromosome())));

						dblFieldMap.get(PreferenceConstants.LIBDMTX_SCAN_GAP)
								.setStringValue(
										Double.toString(FitnessFunct
												.getGap(autocalibrate
														.getBestChromosome())));

						intFieldMap.get(PreferenceConstants.LIBDMTX_SQUARE_DEV)
								.setStringValue(
										Integer.toString(FitnessFunct
												.getSquareDev(autocalibrate
														.getBestChromosome())));

						intFieldMap
								.get(PreferenceConstants.LIBDMTX_CORRECTIONS)
								.setStringValue(
										Integer.toString(FitnessFunct
												.getCorrections(autocalibrate
														.getBestChromosome())));

						dblFieldMap.get(
								PreferenceConstants.LIBDMTX_CELL_DISTANCE)
								.setStringValue(
										Double.toString(FitnessFunct
												.getCellDist(autocalibrate
														.getBestChromosome())));

						ScannerConfigPlugin.openInformation("Settings Applied",
								"Settings have been successfully applied.");
					}
				} else {

					if (ScannerConfigPlugin
							.openConfim(
									"Apply Settings?",
									String
											.format(
													"Settings Obtained:\n\n"
															+ "Edge Threshold: %d\n"
															+ "Scan Gap: %.3f\n"
															+ "Square Deviation: %d\n"
															+ "Corrections: %d\n"
															+ "Cell Distance: %.3f\n"
															+ "\nWould you like to apply these settings?",
													FitnessFunct
															.getThreshold(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getGap(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getSquareDev(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getCorrections(autocalibrate
																	.getBestChromosome()),
													FitnessFunct
															.getCellDist(autocalibrate
																	.getBestChromosome())))) {

						// UPDATE SETTINGS
						intFieldMap
								.get(PreferenceConstants.LIBDMTX_EDGE_THRESH)
								.setStringValue(
										Integer.toString(FitnessFunct
												.getThreshold(autocalibrate
														.getBestChromosome())));

						dblFieldMap.get(PreferenceConstants.LIBDMTX_SCAN_GAP)
								.setStringValue(
										Double.toString(FitnessFunct
												.getGap(autocalibrate
														.getBestChromosome())));

						intFieldMap.get(PreferenceConstants.LIBDMTX_SQUARE_DEV)
								.setStringValue(
										Integer.toString(FitnessFunct
												.getSquareDev(autocalibrate
														.getBestChromosome())));

						intFieldMap
								.get(PreferenceConstants.LIBDMTX_CORRECTIONS)
								.setStringValue(
										Integer.toString(FitnessFunct
												.getCorrections(autocalibrate
														.getBestChromosome())));

						dblFieldMap.get(
								PreferenceConstants.LIBDMTX_CELL_DISTANCE)
								.setStringValue(
										Double.toString(FitnessFunct
												.getCellDist(autocalibrate
														.getBestChromosome())));

						ScannerConfigPlugin.openInformation("Settings Applied",
								"Settings have been successfully applied.");

					}

				}
			}

		});

	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
