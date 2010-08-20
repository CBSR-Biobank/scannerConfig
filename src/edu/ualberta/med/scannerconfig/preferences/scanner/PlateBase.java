package edu.ualberta.med.scannerconfig.preferences.scanner;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scannerconfig.IPlateBoundsListener;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.widgets.PlateBoundsWidget;

public class PlateBase extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	protected int plateId;

	private Text[] textControls;

	private PlateBoundsWidget plateBoundsWidget;

	private ScannerRegion origScannerRegion;

	private Label statusLabel;

	private Canvas canvas;

	public PlateBase(int plateId) {
		super(GRID);
		this.plateId = plateId;
		setPreferenceStore(ScannerConfigPlugin.getDefault()
				.getPreferenceStore());
		textControls = new Text[6];
	}

	@Override
	protected Control createContents(final Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		File platesFile = new File(PlateBoundsWidget.PALLET_IMAGE_FILE);
		if (platesFile.exists()) {
			// platesFile.delete();
		}

		Control s = super.createContents(top);
		GridData gd = (GridData) s.getLayoutData();
		if (gd == null) {
			gd = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
			s.setLayoutData(gd);
		}
		gd.verticalAlignment = SWT.BEGINNING;

		Composite right = new Composite(top, SWT.NONE);
		right.setLayout(new GridLayout(1, false));
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 200;
		right.setLayoutData(gd);

		createCanvasComp(right);

		statusLabel = new Label(right, SWT.BORDER);
		statusLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		if (!ScannerConfigPlugin.getDefault().getPlateEnabled(plateId)) {
			statusLabel.setText(" plate is not enabled");
		} else if (!platesFile.exists()) {
			statusLabel.setText(" a scan is required to configure this pallet");
		} else {
			statusLabel
					.setText(" click on opposite corners to configure pallet");
		}

		Button b = new Button(right, SWT.NONE);
		b.setText("Scan");
		b.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				statusLabel.setText("scanning...");
				BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
					@Override
					public void run() {
						int brightness = ScannerConfigPlugin.getDefault()
								.getPreferenceStore()
								.getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
						int contrast = ScannerConfigPlugin.getDefault()
								.getPreferenceStore()
								.getInt(PreferenceConstants.SCANNER_CONTRAST);
						int debugLevel = ScannerConfigPlugin.getDefault()
								.getPreferenceStore()
								.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);
						final int result = ScanLib.getInstance().slScanImage(
								debugLevel,
								(int) PlateBoundsWidget.PALLET_IMAGE_DPI,
								brightness, contrast, 0, 0, 20, 20,
								PlateBoundsWidget.PALLET_IMAGE_FILE);

						parent.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (result != ScanLib.SC_SUCCESS) {
									MessageDialog.openError(PlatformUI
											.getWorkbench()
											.getActiveWorkbenchWindow()
											.getShell(), "Scanner error",
											ScanLib.getErrMsg(result));
									return;
								}

								File platesFile = new File(
										PlateBoundsWidget.PALLET_IMAGE_FILE);
								if (platesFile.exists()) {
									plateBoundsWidget.loadImage();
									for (int i = 0; i < 6; ++i) {
										textControls[i].setEnabled(true);
									}
								}
							}
						});
					}
				});
				statusLabel
						.setText(" click on opposite corners to configure pallet");
			}
		});

		if (System.getProperty("os.name").startsWith("Windows")
				&& !platesFile.exists()) {
			for (int i = 0; i < 6; ++i) {
				textControls[i].setEnabled(false);
			}
		}

		return top;
	}

	@Override
	protected void createFieldEditors() {
		DoubleFieldEditor fe;
		String[] labels = { "Left", "Top", "Right", "Bottom", "GapX", "GapY" };

		BooleanFieldEditor bfe = new BooleanFieldEditor(
				PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1],
				"Enable", getFieldEditorParent());
		addField(bfe);

		String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[plateId - 1];

		for (int i = 0; i < 6; ++i) {
			fe = new DoubleFieldEditor(prefsArr[i], labels[i] + ":",
					getFieldEditorParent());
			fe.setValidRange(0, 20);
			addField(fe);
			textControls[i] = fe.getTextControl(getFieldEditorParent());
			textControls[i].addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (plateBoundsWidget == null)
						return;
					try {
						plateBoundsWidget.assignRegions("" + plateId,
								Double.parseDouble(textControls[0].getText()),
								Double.parseDouble(textControls[1].getText()),
								Double.parseDouble(textControls[2].getText()),
								Double.parseDouble(textControls[3].getText()),
								Double.parseDouble(textControls[4].getText()),
								Double.parseDouble(textControls[5].getText()));
						setValid(true);
					} catch (NumberFormatException ex) {
						setValid(false);
					}
				}
			});
		}

	}

	@Override
	public void setValid(boolean enable) {
		super.setValid(enable);

		getContainer().updateButtons();
		Button applyButton = getApplyButton();
		if (applyButton != null)
			applyButton.setEnabled(true);
	}

	private void createCanvasComp(Composite parent) {
		canvas = new Canvas(parent, SWT.BORDER | SWT.NO_BACKGROUND);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		canvas.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

		IPreferenceStore prefs = ScannerConfigPlugin.getDefault()
				.getPreferenceStore();

		String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[plateId - 1];

		origScannerRegion = new ScannerRegion("" + plateId, ScannerConfigPlugin
				.getDefault().getPreferenceStore().getDouble(prefsArr[0]),
				prefs.getDouble(prefsArr[1]), prefs.getDouble(prefsArr[2]),
				prefs.getDouble(prefsArr[3]), prefs.getDouble(prefsArr[4]),
				prefs.getDouble(prefsArr[5]));

		plateBoundsWidget = new PlateBoundsWidget(canvas, new ScannerRegion(
				origScannerRegion));

		plateBoundsWidget.addChangeListener(new IPlateBoundsListener() {

			@Override
			public void change() {
				ScannerRegion r = plateBoundsWidget.getPlateRegion();
				textControls[0].setText(String.valueOf(r.left));
				textControls[1].setText(String.valueOf(r.top));
				textControls[2].setText(String.valueOf(r.right));
				textControls[3].setText(String.valueOf(r.bottom));
				textControls[4].setText(String.valueOf(r.gapX));
				textControls[5].setText(String.valueOf(r.gapY));
			}
		});
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ScannerConfigPlugin.getDefault()
				.getPreferenceStore());
	}
}
