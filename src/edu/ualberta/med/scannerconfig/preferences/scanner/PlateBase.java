package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ChangeListener;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.widgets.PlateBoundsWidget;

public class PlateBase extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	protected ListenerList changeListeners = new ListenerList();

	protected int plateId;
	private boolean isEnabled;

	private Text[] textControls;
	private Canvas canvas;
	private PlateBoundsWidget plateBoundsWidget;

	private BooleanFieldEditor booleanFieldEditor;
	private Button rotateBtn;
	private Button scanBtn;

	private boolean isHorizontalRotation;

	private Label statusLabel;

	public PlateBase(int plateId) {
		super(GRID);
		this.plateId = plateId;

		setPreferenceStore(ScannerConfigPlugin.getDefault()
				.getPreferenceStore());
	}

	@Override
	protected Control createContents(final Composite parent) {

		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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

		scanBtn = new Button(right, SWT.NONE);
		scanBtn.setText("Scan");
		scanBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				PlateScannedImage.instance().scanPlateImage();
				updateStatus();
			}
		});
		if (System.getProperty("os.name").startsWith("Windows")
				&& ScannerConfigPlugin.getDefault().getPlateEnabled(plateId)) {
			this.setEnabled(true);
		}
		else {
			this.setEnabled(false);
		}

		return top;
	}

	private void updateStatus() {
		if (!isEnabled) {
			statusLabel.setText(" plate is not enabled");
			return;
		}

		if (!PlateScannedImage.instance().exists()) {
			statusLabel.setText(" a scan is required to configure this pallet");
			return;
		}

		statusLabel.setText(" outline the barcodes using the green grid");
		return;
	}

	@Override
	protected void createFieldEditors() {
		DoubleFieldEditor fe;
		String[] labels = { "Left", "Top", "Right", "Bottom", "GapX", "GapY" };

		booleanFieldEditor = new BooleanFieldEditor(
				PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1],
				"Enable",
				getFieldEditorParent());
		addField(booleanFieldEditor);

		String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateId - 1];

		textControls = new Text[6];

		for (int i = 0; i < 6; ++i) {
			fe = new DoubleFieldEditor(
					prefsArr[i],
					labels[i] + ":",
					getFieldEditorParent());
			fe.setValidRange(0, 20);
			addField(fe);
			textControls[i] = fe.getTextControl(getFieldEditorParent());
			textControls[i].addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					PlateBase.this.notifyChangeListener(
							ChangeListener.PLATE_BASE_TEXT_CHANGE,
							0);
				}

			});
		}

		rotateBtn = new Button(getFieldEditorParent(), SWT.BORDER);
		rotateBtn.setText("Rotate");
		rotateBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isHorizontalRotation = !isHorizontalRotation;
				PlateBase.this.notifyChangeListener(
						ChangeListener.PLATE_BASE_ROTATE,
						0);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	/* create canvas and plate widget */
	private void createCanvasComp(Composite parent) {

		canvas = new Canvas(parent, SWT.BORDER | SWT.NO_BACKGROUND);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		canvas.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

		IPreferenceStore prefs = ScannerConfigPlugin.getDefault()
				.getPreferenceStore();

		String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateId - 1];

		this.isHorizontalRotation = prefs.getBoolean(prefsArr[6]);

		plateBoundsWidget = new PlateBoundsWidget(this, canvas);

		plateBoundsWidget.addPlateWidgetChangeListener(new ChangeListener() {
			@Override
			public void change(Event e) {
				ScannerRegion r = plateBoundsWidget.getPlateRegion();
				textControls[0].setText(String.valueOf(r.left));
				textControls[1].setText(String.valueOf(r.top));
				textControls[2].setText(String.valueOf(r.right));
				textControls[3].setText(String.valueOf(r.bottom));
				textControls[4].setText(String.valueOf(r.gapX));
				textControls[5].setText(String.valueOf(r.gapY));
				PlateBase.this.isHorizontalRotation = r.horizontalRotation;
			}
		});
	}

	private String formatInput(String s) {
		try {
			Double.parseDouble(s);
			return s;
		}
		catch (NumberFormatException e) {
			return "0";
		}
	}

	public ScannerRegion getScannerRegionText() {
		return new ScannerRegion(
				"" + plateId,
				Double.parseDouble(formatInput(textControls[0].getText())),
				Double.parseDouble(formatInput(textControls[1].getText())),
				Double.parseDouble(formatInput(textControls[2].getText())),
				Double.parseDouble(formatInput(textControls[3].getText())),
				Double.parseDouble(formatInput(textControls[4].getText())),
				Double.parseDouble(formatInput(textControls[5].getText())),
				this.isHorizontalRotation);

	}

	@Override
	public void dispose() {
		plateBoundsWidget.dispose();
		super.dispose();
	}

	private void setEnabled(boolean enabled) {

		this.isEnabled = enabled;

		for (int i = 0; i < 6; ++i) {
			if (textControls[i] != null)
				textControls[i].setEnabled(enabled);
		}

		if (rotateBtn != null)
			rotateBtn.setEnabled(enabled);

		if (scanBtn != null)
			scanBtn.setEnabled(enabled);

		notifyChangeListener(ChangeListener.PLATE_BASE_ENABLED, enabled ? 1 : 0);

		updateStatus();
	}

	public boolean isEnabled() {
		return this.isEnabled;
	}

	@Override
	public void setValid(boolean enable) {
		super.setValid(enable);

		getContainer().updateButtons();
		Button applyButton = getApplyButton();
		if (applyButton != null)
			applyButton.setEnabled(true);
	}

	public void removePlateBaseChangeListener(ChangeListener listener) {
		this.changeListeners.remove(listener);
	}

	public void addPlateBaseChangeListener(ChangeListener listener) {
		this.changeListeners.add(listener);
	}

	private void notifyChangeListener(final int message, final int detail) {
		Object[] listeners = this.changeListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			final ChangeListener l = (ChangeListener) listeners[i];
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {

					Event e = new Event();
					e.type = message;
					e.detail = detail;
					l.change(e);
				}
			});
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ScannerConfigPlugin.getDefault()
				.getPreferenceStore());
	}

	private void saveSettings() {
		ScannerConfigPlugin
				.getDefault()
				.getPreferenceStore()
				.setValue(
						PreferenceConstants.SCANNER_PALLET_CONFIG[plateId - 1][6],
						this.isHorizontalRotation);

		this.setEnabled(booleanFieldEditor.getBooleanValue());
	}

	@Override
	public boolean performOk() {
		saveSettings();
		return super.performOk();
	}

	@Override
	protected void performApply() {
		saveSettings();
		super.performApply();
	}
}
