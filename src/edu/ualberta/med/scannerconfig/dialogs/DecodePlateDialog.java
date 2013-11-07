package edu.ualberta.med.scannerconfig.dialogs;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.gui.common.dialogs.PersistedDialog;
import edu.ualberta.med.biobank.gui.common.events.SelectionListener;
import edu.ualberta.med.biobank.gui.common.widgets.Event;
import edu.ualberta.med.biobank.gui.common.widgets.utils.ComboSelectionUpdate;
import edu.ualberta.med.scannerconfig.CameraPosition;
import edu.ualberta.med.scannerconfig.ImageSource;
import edu.ualberta.med.scannerconfig.ImageWithDpi;
import edu.ualberta.med.scannerconfig.PlateDimensions;
import edu.ualberta.med.scannerconfig.PlateOrientation;
import edu.ualberta.med.scannerconfig.ScanPlate;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLibResult;
import edu.ualberta.med.scannerconfig.widgets.ImageSourceAction;
import edu.ualberta.med.scannerconfig.widgets.ImageSourceWidget;
import edu.ualberta.med.scannerconfig.widgets.PlateGridWidget;
import edu.ualberta.med.scannerconfig.widgets.PlateOrientationWidget;

/**
 * A dialog box that is used to project a grid on top of a scanned plate image. The grid is then
 * used to provide regions where 2D barcodes will be searched for and if found decoded.
 * 
 * Allows the user to aquire an image from a flatbed scanner or to import an image from a file.
 * 
 * @author nelson
 * 
 */
public class DecodePlateDialog extends PersistedDialog implements SelectionListener {

    private static final I18n i18n = I18nFactory.getI18n(DecodePlateDialog.class);

    private static Logger log = LoggerFactory.getLogger(DecodePlateDialog.class);

    private static final String SCANNING_DIALOG_SETTINGS = "SCANNING_DIALOG_SETTINGS";

    private static final String LAST_USED_IMAGE_SOURCE = "DecodePlateDialog.last.used.image.source";

    private static final String LAST_USED_PLATE = "DecodePlateDialog.last.used.plate";

    private static final String LAST_USED_PLATE_ORIENTATION = "DecodePlateDialog.last.used.plate.orientation";

    private static final String LAST_USED_GRID_RECTANGLE = "DecodePlateDialog.last.used.plate.dimensions";

    private static final String LAST_USED_PLATE_DIMENSIONS = "DecodePlateDialog.last.used.plate.dimensions";

    private static final String LAST_USED_CAMERA_POSITION = "DecodePlateDialog.last.used.camera.positions";

    private static final String FAKE_PLATE_IMAGE_FILE_NAME = "fakePlateImage.bmp";

    private static final int CONTROLS_MIN_WIDTH = 160;

    private static final String TITLE_AREA_MESSAGE_SELECT_PLATE =
        i18n.tr("Select the options to match the image you are decodingge");

    @SuppressWarnings("nls")
    private static final String TITLE = i18n.tr("Decode pallet");

    @SuppressWarnings("nls")
    private static final String TITLE_AREA_MESSAGE_DECODING = i18n.tr(
        "Adjust the grid to decode the barcodes contained in the image.");

    private final int plateId;

    private final List<PlateDimensions> validPlateDimensions;

    private PlateOrientationWidget plateOrientationWidget;

    private ComboViewer plateDimensionsWidget;

    private ImageSourceWidget imageSourceWidget;

    private PlateDimensions selectedPlateDimensions;

    private PlateGridWidget plateGridWidget;

    private ImageWithDpi imageToDecode;

    private ScanPlate plateScanned;

    // used for debugging in Linux
    private final boolean haveFakePlateImage;

    /**
     * Use this constructor to limit the valid plate dimensions the user can choose from.
     * 
     * @param parentShell
     * @param validPlateDimensions
     */
    public DecodePlateDialog(Shell parentShell, List<PlateDimensions> validPlateDimensions) {
        super(parentShell);
        this.plateId = 1;
        this.validPlateDimensions = validPlateDimensions;

        // For Linux set debugMode to true if a fake flatbed image exits
        if (!System.getProperty("os.name").startsWith("Windows")) {
            File platesFile = new File(FAKE_PLATE_IMAGE_FILE_NAME);
            haveFakePlateImage = platesFile.exists();
        } else {
            haveFakePlateImage = false;
        }
    }

    /**
     * Use this constructor to allow any plate dimension defined in {@link PlateDimensions}.
     * 
     * @param parentShell
     */
    public DecodePlateDialog(Shell parentShell) {
        this(parentShell, Arrays.asList(PlateDimensions.values()));

    }

    @Override
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = super.getDialogSettings();
        IDialogSettings section = settings.getSection(SCANNING_DIALOG_SETTINGS);
        if (section == null) {
            section = settings.addNewSection(SCANNING_DIALOG_SETTINGS);
        }
        return section;
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return getDialogSettings();
    }

    @Override
    protected String getTitleAreaMessage() {
        return TITLE_AREA_MESSAGE_SELECT_PLATE;
    }

    @Override
    protected String getTitleAreaTitle() {
        return TITLE;
    }

    @Override
    protected String getDialogShellTitle() {
        return TITLE;
    }

    @Override
    protected void createDialogAreaInternal(Composite parent) throws Exception {
        final Composite contents = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        contents.setLayout(layout);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.grabExcessHorizontalSpace = true;
        contents.setLayoutData(gd);
        createControls(contents);
        createImageControl(contents);
    }

    private void createControls(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        GridData gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        gd.minimumWidth = CONTROLS_MIN_WIDTH;
        composite.setLayoutData(gd);

        plateOrientationWidget = createPlateOrientationWidget(composite);
        plateDimensionsWidget = createPlateDimensionsWidget(composite);
        createImageSourceControls(composite);
    }

    private PlateOrientationWidget createPlateOrientationWidget(Composite parent) {
        final PlateOrientationWidget widget = new PlateOrientationWidget(
            parent, restorePlateOrientation(getDialogSettings()));
        widget.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                plateGridWidget.setPlateOrientation(widget.getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        return widget;
    }

    private ComboViewer createPlateDimensionsWidget(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        composite.setLayoutData(gd);

        selectedPlateDimensions = restorePlateDimensions(getDialogSettings());

        return widgetCreator.createComboViewer(
            composite,
            i18n.tr("Plate dimensions"),
            validPlateDimensions,
            selectedPlateDimensions,
            i18n.tr("select dimension for the plate"),
            new ComboSelectionUpdate() {
                @Override
                public void doSelection(Object selectedObject) {
                    if (selectedObject instanceof PlateDimensions) {
                        selectedPlateDimensions = (PlateDimensions) selectedObject;
                        plateGridWidget.setPlateDimensions(selectedPlateDimensions);
                    } else {
                        throw new IllegalStateException("invalid selection");
                    }
                }
            },
            new LabelProvider() {
                @Override
                public String getText(Object element) {
                    if (element instanceof PlateDimensions) {
                        return ((PlateDimensions) element).getDisplayLabel();
                    }
                    return super.getText(element);
                }
            });
    }

    private void createImageSourceControls(final Composite parent) {
        ImageSource imageSource = restoreImageSource(getDialogSettings());
        ScanPlate restoreScannedPlateId = restoreScannedPlateId(getDialogSettings());
        CameraPosition restoreCameraPosition = restoreCameraPosition(getDialogSettings());

        imageSourceWidget = new ImageSourceWidget(parent, CONTROLS_MIN_WIDTH,
            imageSource, restoreScannedPlateId, restoreCameraPosition);

        imageSourceWidget.addSelectionListener(this);
    }

    private void createImageControl(Composite parent) {
        plateGridWidget = new PlateGridWidget(parent);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, i18n.tr("Done"), false);
    }

    /*
     * Called when something is selected on one of the sub widgets.
     * 
     * (non-Javadoc)
     * 
     * @see
     * edu.ualberta.med.biobank.gui.common.events.SelectionListener#widgetSelected(edu.ualberta.
     * med.biobank.gui.common.widgets.Event)
     */
    @Override
    public void widgetSelected(Event e) {
        ImageSourceAction imageSourceAction = ImageSourceAction.getFromId(e.detail);
        plateScanned = (ScanPlate) e.data;

        if (imageSourceAction == ImageSourceAction.SCAN) {
            @SuppressWarnings("nls")
            String filename = String.format("plate_scan_%d.bmp", plateId);
            ScanLibResult.Result result;
            result = ScannerConfigPlugin.scanPlate(plateId, filename);

            if ((result == ScanLibResult.Result.SUCCESS)
                || ((result == ScanLibResult.Result.FAIL) && (haveFakePlateImage))) {
                setMessage(TITLE_AREA_MESSAGE_DECODING, IMessageProvider.NONE);

                if ((result == ScanLibResult.Result.FAIL) && (haveFakePlateImage)) {
                    filename = FAKE_PLATE_IMAGE_FILE_NAME;
                }

                // TODO: allow user to select DPI as well in this dialog
                imageToDecode = new ImageWithDpi(filename, ScannerConfigPlugin.getDefault().getDpi());
                plateGridWidget.imageUpdated(
                    imageToDecode,
                    restoreGridRectangle(getDialogSettings(), plateScanned),
                    plateOrientationWidget.getSelection(),
                    selectedPlateDimensions);
            } else {
                setMessage(i18n.tr("Could not scan the plate region"), IMessageProvider.ERROR);
            }
        }
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    private ImageSource restoreImageSource(IDialogSettings settings) {
        String s = settings.get(LAST_USED_IMAGE_SOURCE);
        if (s != null) {
            return ImageSource.getFromIdString(s);
        }
        return ImageSource.FLATBED_SCANNER;
    }

    private ScanPlate restoreScannedPlateId(IDialogSettings settings) {
        Set<ScanPlate> platesEnabled = ScannerConfigPlugin.getDefault().getPlatesEnabled();
        if (platesEnabled.isEmpty()) {
            return null;
        }

        ScanPlate defaultPlate = platesEnabled.iterator().next();

        try {
            ScanPlate lastUsedPlate = ScanPlate.getFromId(settings.getInt(LAST_USED_PLATE));
            return (platesEnabled.contains(lastUsedPlate)) ? lastUsedPlate : defaultPlate;
        } catch (NumberFormatException e) {
            return defaultPlate;
        }
    }

    private PlateOrientation restorePlateOrientation(IDialogSettings settings) {
        String s = settings.get(LAST_USED_PLATE_ORIENTATION);
        if (s != null) {
            return PlateOrientation.getFromIdString(s);
        }
        return PlateOrientation.LANDSCAPE;
    }

    private PlateDimensions restorePlateDimensions(IDialogSettings settings) {
        String s = settings.get(LAST_USED_PLATE_DIMENSIONS);
        if (s != null) {
            return PlateDimensions.getFromIdString(s);
        }
        return PlateDimensions.DIM_ROWS_8_COLS_12;
    }

    private CameraPosition restoreCameraPosition(IDialogSettings settings) {
        String s = settings.get(LAST_USED_CAMERA_POSITION);
        if (s != null) {
            return CameraPosition.getFromIdString(s);
        }
        return CameraPosition.BELOW;
    }

    private Rectangle2D.Double restoreGridRectangle(IDialogSettings settings, ScanPlate plateId) {
        String[] values = settings.getArray(LAST_USED_GRID_RECTANGLE + plateId.getId());
        if (values != null) {
            if (values.length != 4) {
                throw new IllegalStateException("invalid length for grid rectangle settings");
            }
            double left = Double.parseDouble(values[0]);
            double top = Double.parseDouble(values[1]);
            double right = Double.parseDouble(values[2]);
            double bottom = Double.parseDouble(values[3]);
            return new Rectangle2D.Double(left, top, right - left, bottom - top);
        }

        // return a default value
        if (imageToDecode == null) {
            throw new IllegalStateException("no image to get dimensions from");
        }
        Pair<Double, Double> imageDimensions = imageToDecode.getDimensionInInches();
        return new Rectangle2D.Double(0.1, 0.1,
            imageDimensions.getLeft() - 0.2, imageDimensions.getRight() - 0.2);
    }
}
