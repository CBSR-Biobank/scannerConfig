package edu.ualberta.med.scannerconfig.widgets;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
import edu.ualberta.med.biobank.gui.common.widgets.Event;
import edu.ualberta.med.biobank.gui.common.widgets.GroupedRadioSelectionWidget;
import edu.ualberta.med.scannerconfig.ScanPlate;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;

/**
 * This widget allows the user to select a scan plate. It allows the user to select one of the
 * currently defined scan plates enabled in the preferences. If no scan plates are enabled then an
 * appropriate message is displayed by this widget instead.
 * 
 * @author loyola
 * 
 */
public class ScanningControlsWidget extends Composite implements SelectionListener {

    private static final I18n i18n = I18nFactory.getI18n(ScanningControlsWidget.class);

    private final int minWidth;

    private final ScanPlate plateDefault;

    private final GroupedRadioSelectionWidget<ScanPlate> plateToScanWidget;

    private final GridData gridData;

    private Button scanButton;

    private Button rescanButton;

    private edu.ualberta.med.biobank.gui.common.events.SelectionListener selectionListener;

    public ScanningControlsWidget(
        Composite parent,
        int minWidth,
        ScanPlate plateDefault) {
        super(parent, SWT.NONE);
        this.minWidth = minWidth;
        this.plateDefault = plateDefault;

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        setLayout(layout);

        gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        setLayoutData(gridData);

        if (ScannerConfigPlugin.getDefault().getPlatesEnabled().isEmpty()) {
            createPreferencesWarningMessage();
            plateToScanWidget = null;
        } else {
            plateToScanWidget = createPlateSelectionWidget();
            createScanningButtons();
        }
    }

    private BgcBaseText createPreferencesWarningMessage() {
        BgcBaseText text = new BgcBaseText(this,
            SWT.WRAP | SWT.READ_ONLY | SWT.SHADOW_ETCHED_IN);
        text.setText(i18n.tr("No scanning plates have been defined in the preferences. "
            + "Please modify your configuration if you wish to use the flatbed scanner."));
        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
        gd.widthHint = minWidth - 6;
        text.setLayoutData(gd);
        return text;
    }

    private GroupedRadioSelectionWidget<ScanPlate> createPlateSelectionWidget() {
        Map<ScanPlate, String> selectionsMap = new LinkedHashMap<ScanPlate, String>();
        for (ScanPlate plate : ScannerConfigPlugin.getDefault().getPlatesEnabled()) {
            selectionsMap.put(plate, plate.getDisplayLabel());
        }

        return new GroupedRadioSelectionWidget<ScanPlate>(
            this, i18n.tr("Plate to scan"), selectionsMap, plateDefault);
    }

    private void createScanningButtons() {
        final Composite composite = new Composite(this, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        composite.setLayoutData(gd);

        scanButton = new Button(composite, SWT.PUSH);
        scanButton.setText(i18n.tr("Scan"));
        scanButton.addSelectionListener(this);

        rescanButton = new Button(composite, SWT.PUSH);
        rescanButton.setText(i18n.tr("Rescan"));
        rescanButton.addSelectionListener(this);
    }

    @Override
    public void setVisible(boolean visible) {
        gridData.exclude = !visible;
        super.setVisible(visible);
    }

    public ScanPlate getSelectedScanPlate() {
        if (ScannerConfigPlugin.getDefault().getPlatesEnabled().isEmpty()) {
            throw new IllegalStateException("no scanning plates have been defined");
        }
        return plateToScanWidget.getSelection();
    }

    public void addSelectionListener(
        edu.ualberta.med.biobank.gui.common.events.SelectionListener listener) {
        this.selectionListener = listener;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        // tell parent
        if (selectionListener != null) {
            Event newEvent = new Event();
            newEvent.widget = this;
            newEvent.type = SWT.Selection;
            newEvent.data = getSelectedScanPlate();

            if (e.getSource().equals(scanButton)) {
                newEvent.detail = ImageSourceAction.SCAN.getId();
            } else if (e.getSource().equals(scanButton)) {
                newEvent.detail = ImageSourceAction.RESCAN.getId();
            } else {
                throw new IllegalStateException("invalid source for event: " + e.getSource());
            }

            selectionListener.widgetSelected(newEvent);
        }

    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }
}
