package edu.ualberta.med.scannerconfig.preferences;

import java.io.File;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.calibration.*;
import edu.ualberta.med.scannerconfig.scanlib.ScanLib;

public class Scanner extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

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

		rgFe = new RadioGroupFieldEditor(PreferenceConstants.SCANNER_DPI,
				"DPI", 5, new String[][] { { "300", "300" }, { "400", "400" },
						{ "600", "600" }, { "720", "720" }, { "800", "800" } },
				getFieldEditorParent(), true);
		addField(rgFe);

		IntegerFieldEditor intFe = new IntegerFieldEditor(
				PreferenceConstants.SCANNER_BRIGHTNESS, "Brightness:",
				getFieldEditorParent());
		intFe.setValidRange(-1000, 1000);
		addField(intFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.SCANNER_CONTRAST,
				"Contrast:", getFieldEditorParent());
		intFe.setValidRange(-1000, 1000);
		addField(intFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.DLL_DEBUG_LEVEL,
				"Decode Library Debug Level:", getFieldEditorParent());
		intFe.setValidRange(0, 9);
		addField(intFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_EDGE_THRESH,
				"Decode Edge Threshold:", getFieldEditorParent());
		intFe.setValidRange(0, 100);
		addField(intFe);

		DoubleFieldEditor dblFe = new DoubleFieldEditor(
				PreferenceConstants.LIBDMTX_SCAN_GAP, "Decode Scan Gap:",
				getFieldEditorParent());
		dblFe.setValidRange(0.0, 1.0);
		addField(dblFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_SQUARE_DEV,
				"Decode Square Deviation:", getFieldEditorParent());
		intFe.setValidRange(0, 90);
		addField(intFe);

		intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_CORRECTIONS,
				"Decode Corrections:", getFieldEditorParent());
		intFe.setValidRange(0, 100);
		addField(intFe);

		dblFe = new DoubleFieldEditor(
				PreferenceConstants.LIBDMTX_CELL_DISTANCE,
				"Decode Cell Distance:", getFieldEditorParent());
		dblFe.setValidRange(0.0, 1.0);
		addField(dblFe);

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
								"Please place the pallet on Plate 1.\n"
										+ "Note: It is recommended that you use the most difficult "
										+ "test tubes for this process.\n.")) {
					return;
				}
				boolean isTwain = false; // TESTING WITH WIA FIRST

				if (!isTwain) { // WIA

					/*
					 * fitnessFunct uses the calibration.bmp file in wia mode.
					 * this saves having to rescan the same image over and over.
					 */
					
					/*
					File calibrationImage = new File("calibration.bmp");
					if (calibrationImage.exists()) {
						calibrationImage.delete();
					}
					try {
						ScannerConfigPlugin.scanPlate(1, "calibration.bmp");
					} catch (Exception e1) {
						ScannerConfigPlugin.openAsyncError("Error Scanning",
								"Failed to scan Plate 1 to file: calibration.bmp.\n"+
								"Please make sure Plate 1 is properly configured.");
						return;
					}*/
				}

				AutoCalibrate autocalibrate = new AutoCalibrate(isTwain, 10);

				if (!autocalibrate.isInitialized()) {
					ScannerConfigPlugin
							.openAsyncError("Auto Calibration Failed",
									"An unexpected error occured, auto-calibration could not be started.");
					return;
				}
				
				do {
					for (int i = 0; i < 10; i++) {
						autocalibrate.iterateEvolution();
					}

					if (FitnessFunct.getAccuracy(autocalibrate
							.getBestChromosome()) > 99) {
						break;
					}

				} while (ScannerConfigPlugin.openConfim(
						"Continue Calibrating?",
						"With the current settings obtained, scanning will be "
								+ FitnessFunct.getAccuracy(autocalibrate
										.getBestChromosome())
								+ " % accurate. (on simular tubes)\n"
								+ "Would you like to continue calibrating?"));

				
				// Update all input boxes but brightness,contrast

				if (isTwain) {
					// Update brightness,contrast text input boxes
				}

			}

		});

	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
