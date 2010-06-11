package edu.ualberta.med.scannerconfig.preferences;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.PlateBoundsWidget;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.calibration.AutoCalibrate;
import edu.ualberta.med.scannerconfig.calibration.FitnessFunct;
import edu.ualberta.med.scannerconfig.scanlib.ScanLib;
import edu.ualberta.med.scannerconfig.widgets.AdvancedRadioGroupFieldEditor;

public class Scanner extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private Map<String, DoubleFieldEditor> dblFieldMap = new HashMap<String, DoubleFieldEditor>();
	private Map<String, IntegerFieldEditor> intFieldMap = new HashMap<String, IntegerFieldEditor>();

	Button selectScannerBtn, autoCalibrateBtn;

	AdvancedRadioGroupFieldEditor dpiRadio, driverTypeRadio;

	IntegerFieldEditor brightnessInputField, contrastInputField,
			debugLevelInputField, thresholdInputField, squaredevInputField,
			correctionsInputField;

	DoubleFieldEditor scanGapDblInput, celldistDblInput;

	Scanner self = null;

	public Scanner() {
		super(GRID);
		self = this;
		setPreferenceStore(ScannerConfigPlugin.getDefault()
				.getPreferenceStore());
	}

	@Override
	public void createFieldEditors() {

		int scannerCap = ScanLib.getInstance().slGetScannerCapability();

		selectScannerBtn = new Button(getFieldEditorParent(), SWT.NONE);
		selectScannerBtn.setText("Select Scanner");
		selectScannerBtn.setImage(ScannerConfigPlugin.getDefault()
				.getImageRegistry().get(ScannerConfigPlugin.IMG_SCANNER));
		selectScannerBtn.addSelectionListener(scannerSelectionListener);

		driverTypeRadio = new AdvancedRadioGroupFieldEditor(
				PreferenceConstants.SCANNER_DRV_TYPE,
				"Driver Type",
				2,
				new String[][] {
						{ "TWAIN", PreferenceConstants.SCANNER_DRV_TYPE_TWAIN },
						{ "WIA", PreferenceConstants.SCANNER_DRV_TYPE_WIA } },
				getFieldEditorParent(), true);
		addField(driverTypeRadio);

		dpiRadio = new AdvancedRadioGroupFieldEditor(
				PreferenceConstants.SCANNER_DPI, "DPI", 5, new String[][] {
						{ "300", "300" }, { "400", "400" }, { "600", "600" } },
				getFieldEditorParent(), true);

		addField(dpiRadio);

		brightnessInputField = new IntegerFieldEditor(
				PreferenceConstants.SCANNER_BRIGHTNESS, "Brightness:",
				getFieldEditorParent());
		brightnessInputField.setValidRange(-1000, 1000);
		addField(brightnessInputField);
		intFieldMap.put(brightnessInputField.getPreferenceName(),
				brightnessInputField);

		contrastInputField = new IntegerFieldEditor(
				PreferenceConstants.SCANNER_CONTRAST, "Contrast:",
				getFieldEditorParent());
		contrastInputField.setValidRange(-1000, 1000);
		addField(contrastInputField);
		intFieldMap.put(contrastInputField.getPreferenceName(),
				contrastInputField);

		debugLevelInputField = new IntegerFieldEditor(
				PreferenceConstants.DLL_DEBUG_LEVEL,
				"Decode Library Debug Level:", getFieldEditorParent());
		debugLevelInputField.setValidRange(0, 9);
		addField(debugLevelInputField);
		intFieldMap.put(debugLevelInputField.getPreferenceName(),
				debugLevelInputField);

		thresholdInputField = new IntegerFieldEditor(
				PreferenceConstants.LIBDMTX_EDGE_THRESH,
				"Decode Edge Threshold:", getFieldEditorParent());
		thresholdInputField.setValidRange(0, 100);
		addField(thresholdInputField);
		intFieldMap.put(thresholdInputField.getPreferenceName(),
				thresholdInputField);

		squaredevInputField = new IntegerFieldEditor(
				PreferenceConstants.LIBDMTX_SQUARE_DEV,
				"Decode Square Deviation:", getFieldEditorParent());
		squaredevInputField.setValidRange(0, 90);
		addField(squaredevInputField);
		intFieldMap.put(squaredevInputField.getPreferenceName(),
				squaredevInputField);

		correctionsInputField = new IntegerFieldEditor(
				PreferenceConstants.LIBDMTX_CORRECTIONS, "Decode Corrections:",
				getFieldEditorParent());
		correctionsInputField.setValidRange(0, 100);
		addField(correctionsInputField);
		intFieldMap.put(correctionsInputField.getPreferenceName(),
				correctionsInputField);

		scanGapDblInput = new DoubleFieldEditor(
				PreferenceConstants.LIBDMTX_SCAN_GAP, "Decode Scan Gap:",
				getFieldEditorParent());
		scanGapDblInput.setValidRange(0.0, 1.0);
		addField(scanGapDblInput);
		dblFieldMap.put(PreferenceConstants.LIBDMTX_SCAN_GAP, scanGapDblInput);

		celldistDblInput = new DoubleFieldEditor(
				PreferenceConstants.LIBDMTX_CELL_DISTANCE,
				"Decode Cell Distance:", getFieldEditorParent());
		celldistDblInput.setValidRange(0.0, 1.0);
		addField(celldistDblInput);
		dblFieldMap.put(PreferenceConstants.LIBDMTX_CELL_DISTANCE,
				celldistDblInput);

		/* TODO calibration button -- figure out what to do with it */
		autoCalibrateBtn = new Button(getFieldEditorParent(), SWT.NONE);
		autoCalibrateBtn.setText("Automatically Calibrate");
		autoCalibrateBtn.setImage(ScannerConfigPlugin.getDefault()
				.getImageRegistry().get(ScannerConfigPlugin.IMG_SCANNER));
		autoCalibrateBtn.addSelectionListener(calibrationListener);
		autoCalibrateBtn.setVisible(false);
		autoCalibrateBtn.setEnabled(false);

		setEnableAllWidgets((scannerCap & ScanLib.CAP_IS_SCANNER) != 0);
	}

	private void setEnableAllWidgets(boolean enableSettings) {

		if (enableSettings) {

			int scannerCap = ScanLib.getInstance().slGetScannerCapability();
			dpiRadio.setEnabledArray(new boolean[] {
					(scannerCap & ScanLib.CAP_DPI_300) != 0,
					(scannerCap & ScanLib.CAP_DPI_400) != 0,
					(scannerCap & ScanLib.CAP_DPI_600) != 0 }, 0,
					getFieldEditorParent());
		} else {
			dpiRadio.setEnabled(false, getFieldEditorParent());
		}

		selectScannerBtn.setEnabled(true);
		driverTypeRadio.setEnabled(enableSettings, getFieldEditorParent());
		autoCalibrateBtn.setEnabled(enableSettings);
		brightnessInputField.setEnabled(enableSettings, getFieldEditorParent());
		contrastInputField.setEnabled(enableSettings, getFieldEditorParent());
		debugLevelInputField.setEnabled(enableSettings, getFieldEditorParent());
		thresholdInputField.setEnabled(enableSettings, getFieldEditorParent());
		squaredevInputField.setEnabled(enableSettings, getFieldEditorParent());
		correctionsInputField
				.setEnabled(enableSettings, getFieldEditorParent());
		scanGapDblInput.setEnabled(enableSettings, getFieldEditorParent());
		celldistDblInput.setEnabled(enableSettings, getFieldEditorParent());

	}

	private SelectionListener scannerSelectionListener = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			int scanlibReturn = ScanLib.getInstance().slSelectSourceAsDefault();
			int scannerCap = ScanLib.getInstance().slGetScannerCapability();

			if (scanlibReturn != ScanLib.SC_SUCCESS) {

				if ((scannerCap & ScanLib.CAP_IS_SCANNER) != 0) {
					return;

				} else {
					setEnableAllWidgets(false);
					ScannerConfigPlugin
							.openError("Source Selection Error",
									"Please plug in a scanner and select an appropiate source driver.");
				}
			} else {

				IPreferenceStore store = self.getPreferenceStore();

				if ((scannerCap & ScanLib.CAP_IS_WIA) != 0) {
					store.setValue(PreferenceConstants.SCANNER_DRV_TYPE,
							PreferenceConstants.SCANNER_DRV_TYPE_WIA);
					driverTypeRadio.setSelectionArray(new boolean[] { false,
							true });
				} else {
					store.setValue(PreferenceConstants.SCANNER_DRV_TYPE,
							PreferenceConstants.SCANNER_DRV_TYPE_TWAIN);
					driverTypeRadio.setSelectionArray(new boolean[] { true,
							false });
				}
				driverTypeRadio.doLoad();

				if ((scannerCap & ScanLib.CAP_DPI_300) != 0) {

					dpiRadio.setSelectionArray(new boolean[] { true, false,
							false });
					store.setValue(PreferenceConstants.SCANNER_DPI, 300);
					PlateBoundsWidget.PALLET_IMAGE_DPI = 300;

				} else if ((scannerCap & ScanLib.CAP_DPI_400) != 0) {

					dpiRadio.setSelectionArray(new boolean[] { false, true,
							false });
					store.setValue(PreferenceConstants.SCANNER_DPI, 400);
					PlateBoundsWidget.PALLET_IMAGE_DPI = 400;

				} else if ((scannerCap & ScanLib.CAP_DPI_600) != 0) {

					dpiRadio.setSelectionArray(new boolean[] { false, false,
							true });
					store.setValue(PreferenceConstants.SCANNER_DPI, 600);
					PlateBoundsWidget.PALLET_IMAGE_DPI = 600;
				}
				dpiRadio.doLoad();

				setEnableAllWidgets(true);
			}

		}
	};

	private SelectionListener calibrationListener = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {

			if (!ScannerConfigPlugin.getDefault().getPlateEnabled(1)) {
				ScannerConfigPlugin.openError("Auto-Calibration Requirements",
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

			if (self.getPreferenceStore().getString(
					PreferenceConstants.SCANNER_DRV_TYPE) == PreferenceConstants.SCANNER_DRV_TYPE_TWAIN) {
				isTwain = true;
			}

			if (!isTwain) { // WIA

				/*
				 * fitnessFunct uses the calibration.bmp file in wia mode. this
				 * saves having to rescan the same image over and over.
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

			} while (ScannerConfigPlugin.openConfim("Continue Calibrating?",
					"Current settings discovered are able to scan "
							+ FitnessFunct.getTubesScanned(autocalibrate
									.getBestChromosome()) + " test tubes.\n\n"
							+ "Would you like to continue calibrating?"));

			if (isTwain) {
				// Update brightness,contrast text input boxes

				if (ScannerConfigPlugin.openConfim("Apply Settings?", String
						.format("\tSettings Obtained:\n\n"
								+ "\tBrightness: %d\n" + "\tContrast: %d\n"
								+ "\tEdge Threshold: %d\n"
								+ "\tScan Gap: %.3f\n"
								+ "\tSquare Deviation: %d\n"
								+ "\tCorrections: %d\n"
								+ "\tCell Distance: %.3f\n"
								+ "\nWould you like to apply these settings?",
								FitnessFunct.getBrightness(autocalibrate
										.getBestChromosome()), FitnessFunct
										.getContrast(autocalibrate
												.getBestChromosome()),
								FitnessFunct.getThreshold(autocalibrate
										.getBestChromosome()), FitnessFunct
										.getGap(autocalibrate
												.getBestChromosome()),
								FitnessFunct.getSquareDev(autocalibrate
										.getBestChromosome()), FitnessFunct
										.getCorrections(autocalibrate
												.getBestChromosome()),
								FitnessFunct.getCellDist(autocalibrate
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

					intFieldMap.get(PreferenceConstants.LIBDMTX_EDGE_THRESH)
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

					intFieldMap.get(PreferenceConstants.LIBDMTX_CORRECTIONS)
							.setStringValue(
									Integer.toString(FitnessFunct
											.getCorrections(autocalibrate
													.getBestChromosome())));

					dblFieldMap.get(PreferenceConstants.LIBDMTX_CELL_DISTANCE)
							.setStringValue(
									Double.toString(FitnessFunct
											.getCellDist(autocalibrate
													.getBestChromosome())));

					ScannerConfigPlugin.openInformation("Settings Applied",
							"Settings have been successfully applied.");
				}
			} else {

				if (ScannerConfigPlugin.openConfim("Apply Settings?", String
						.format("Settings Obtained:\n\n"
								+ "Edge Threshold: %d\n" + "Scan Gap: %.3f\n"
								+ "Square Deviation: %d\n"
								+ "Corrections: %d\n" + "Cell Distance: %.3f\n"
								+ "\nWould you like to apply these settings?",
								FitnessFunct.getThreshold(autocalibrate
										.getBestChromosome()), FitnessFunct
										.getGap(autocalibrate
												.getBestChromosome()),
								FitnessFunct.getSquareDev(autocalibrate
										.getBestChromosome()), FitnessFunct
										.getCorrections(autocalibrate
												.getBestChromosome()),
								FitnessFunct.getCellDist(autocalibrate
										.getBestChromosome())))) {

					// UPDATE SETTINGS
					intFieldMap.get(PreferenceConstants.LIBDMTX_EDGE_THRESH)
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

					intFieldMap.get(PreferenceConstants.LIBDMTX_CORRECTIONS)
							.setStringValue(
									Integer.toString(FitnessFunct
											.getCorrections(autocalibrate
													.getBestChromosome())));

					dblFieldMap.get(PreferenceConstants.LIBDMTX_CELL_DISTANCE)
							.setStringValue(
									Double.toString(FitnessFunct
											.getCellDist(autocalibrate
													.getBestChromosome())));

					ScannerConfigPlugin.openInformation("Settings Applied",
							"Settings have been successfully applied.");

				}

			}
		}

	};

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
