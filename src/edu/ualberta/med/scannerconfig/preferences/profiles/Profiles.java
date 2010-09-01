package edu.ualberta.med.scannerconfig.preferences.profiles;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.dialogs.InputDialog;

public class Profiles extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	Button profileBtns[] = new Button[96];
	Button resetBtn, allBtn, deleteBtn;
	Label profileNameLbl;
	List profileList;

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ScannerConfigPlugin.getDefault()
				.getPreferenceStore());

	}

	@Override
	protected Control createContents(final Composite parent) {

		ProfileManager.instance().reloadProfiles();

		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, true));

		Composite left = new Composite(top, SWT.NONE);
		left.setLayout(new GridLayout(1, false));

		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		gridData.grabExcessVerticalSpace = false;

		Composite right = new Composite(top, SWT.NONE);
		right.setLayout(new GridLayout(1, false));
		right.setLayoutData(gridData);

		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.widthHint = 100;
		gridData.heightHint = 400;

		profileList = new List(left, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		profileList.setLayoutData(gridData);
		profileList.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (profileList.getSelectionIndex() >= 0) {
					setEnabledProfile(true);
					loadActiveProfile();
				}
				else
					setEnabledProfile(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Composite buttons = new Composite(left, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, true));

		Button addButton = new Button(buttons, SWT.SIMPLE);
		addButton.setText("Add...");
		addButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog id = new InputDialog(
						getShell(),
						SWT.NONE,
						"Profile Name",
						"Please enter a profile name: ");

				String newProfileName = id.open(); // TODO strip

				if (newProfileName != null && newProfileName.length() > 1) {
					addNewProfile(newProfileName);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		deleteBtn = new Button(buttons, SWT.SIMPLE);
		deleteBtn.setText("Delete Profile");
		deleteBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeProfile();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		profileNameLbl = new Label(right, SWT.NONE);

		GridLayout gl = new GridLayout(12, true);
		gl.marginHeight = 0;
		gl.horizontalSpacing = 2;
		gl.verticalSpacing = 2;
		Composite profile = new Composite(right, SWT.BORDER);
		profile.setLayout(gl);
		profile.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, true));
		for (int i = 0; i < 96; i++) {
			final int c = i;

			profileBtns[i] = new Button(profile, SWT.CHECK);
			profileBtns[i].setText("" + ((char) (c / 12 + 'A')) + (c % 12 + 1));
			profileBtns[i].setEnabled(false);
			profileBtns[i].addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {

					if (profileBtns[c].getSelection())
						getActiveProfileData().setBit(c);
					else
						getActiveProfileData().resetBit(c);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		Composite buttons2 = new Composite(right, SWT.NONE);
		buttons2.setLayout(new GridLayout(2, false));
		buttons2.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, true));

		allBtn = new Button(buttons2, SWT.SIMPLE);
		allBtn.setText("Select All");
		allBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (int i = 0; i < 96; i++) {
					profileBtns[i].setSelection(true);
					getActiveProfileData().setBit(i);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		resetBtn = new Button(buttons2, SWT.SIMPLE);
		resetBtn.setText("Reset");
		resetBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (int i = 0; i < 96; i++) {
					profileBtns[i].setSelection(false);
					getActiveProfileData().resetBit(i);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		setEnabledProfile(false);

		loadProfilesToSelf(ProfileManager.instance().getProfiles());
		return parent;
	}

	private void setEnabledProfile(boolean enabled) {
		allBtn.setEnabled(enabled);
		resetBtn.setEnabled(enabled);
		deleteBtn.setEnabled(enabled);
		for (int i = 0; i < 96; i++) {
			profileBtns[i].setEnabled(enabled);
			if (!enabled)
				profileBtns[i].setSelection(false);
		}
	}

	private void loadProfilesToSelf(HashMap<String, TriIntC> profilesMap) {
		Set<?> entries = profilesMap.entrySet();
		Iterator<?> it = entries.iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) it.next();
			addProfile((String) entry.getKey());
		}
	}

	private void addNewProfile(String name) {
		ProfileManager.instance().addNewProfile(name);
		addProfile(name);
	}

	private void addProfile(String name) {
		if (profileList.indexOf(name) != -1) {
			return;
		}
		profileList.add(name);
		setEnabledProfile(true);
		profileList.deselectAll();
		profileList.select(profileList.getItemCount() - 1);
		profileList.notifyListeners(SWT.Selection, new Event());

	}

	private void removeProfile() {
		int nextSelection = profileList.getSelectionIndex() - 1;
		if (nextSelection < 0)
			nextSelection = 0;

		ProfileManager.instance().removeProfile(getActiveName());
		profileList.remove(getActiveName());

		profileList.deselectAll();
		if (nextSelection >= 0 && profileList.getItemCount() > 0) {
			profileList.select(nextSelection);
			setEnabledProfile(true);
		}
		else {
			setEnabledProfile(false);
		}
		profileList.notifyListeners(SWT.Selection, new Event());
	}

	private void loadActiveProfile() {
		for (int i = 0; i < 96; i++) {
			profileBtns[i].setSelection(getActiveProfileData().isSetBit(i));
		}
	}

	private TriIntC getActiveProfileData() {
		return ProfileManager.instance().getProfile(getActiveName());
	}

	private String getActiveName() {
		return profileList.getSelection()[0];
	}

	@Override
	protected void performApply() {
		ProfileManager.instance().saveProfiles();
		super.performApply();
	}

	@Override
	public boolean performOk() {
		ProfileManager.instance().saveProfiles();
		return super.performOk();
	}

	@Override
	protected void createFieldEditors() {
	}

}
