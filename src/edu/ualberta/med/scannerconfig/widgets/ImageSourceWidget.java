package edu.ualberta.med.scannerconfig.widgets;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.gui.common.widgets.Event;
import edu.ualberta.med.biobank.gui.common.widgets.GroupedRadioSelectionWidget;
import edu.ualberta.med.scannerconfig.CameraPosition;
import edu.ualberta.med.scannerconfig.ImageSource;
import edu.ualberta.med.scannerconfig.ScanPlate;

/**
 * A widget that allows the user to select the source for where to aquire an image containinig 2D
 * barcodes. The user of the widget also registers a listener callback to be notified when the user
 * has made the selection and is ready to aquire the image.
 * 
 * @author nelson
 * 
 */
public class ImageSourceWidget extends Composite {

    private static final I18n i18n = I18nFactory.getI18n(ImageSourceWidget.class);

    // private static Logger log = LoggerFactory.getLogger(ImageSourceWidget.class.getName());

    private static final Map<ImageSource, String> SELECTIONS_MAP;

    static {
        Map<ImageSource, String> map = new LinkedHashMap<ImageSource, String>();

        for (ImageSource enumValue : ImageSource.values()) {
            map.put(enumValue, enumValue.getDisplayLabel());
        }

        SELECTIONS_MAP = Collections.unmodifiableMap(map);
    }

    private final GroupedRadioSelectionWidget<ImageSource> imageSourceSelection;

    private final ScanningControlsWidget scanningControlsWidget;

    private final ImageFileWidget imageFileWidget;

    private edu.ualberta.med.biobank.gui.common.events.SelectionListener selectionListener;

    /**
     * A widget that allows the user to select where aquire an image containinig 2D barcodes. The
     * possible choices are either from a flatbed scanner or an image from the file system.
     * 
     * @param parent The parent composite.
     * @param minWidth The minimum with allowed for the grid the widget is displayed in.
     * @param imageSourceInitVal The initial value for the image source.
     * @param scanPlateInitVal The initial value for the plate to be scanned. This value can be
     *            {@link null}.
     * @param cameraPositionInitVal The initial value for the camera position. See
     *            {@link CameraPosition} for possible values.
     */
    public ImageSourceWidget(
        final Composite parent,
        int minWidth,
        ImageSource imageSourceInitVal,
        ScanPlate scanPlateInitVal,
        CameraPosition cameraPositionInitVal) {
        super(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        setLayout(layout);

        GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        setLayoutData(gridData);

        imageSourceSelection = new GroupedRadioSelectionWidget<ImageSource>(
            this, i18n.tr("Image Source"), SELECTIONS_MAP, imageSourceInitVal);
        imageSourceSelection.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ImageSource selection = imageSourceSelection.getSelection();
                scanningControlsWidget.setVisible(selection == ImageSource.FLATBED_SCANNER);
                imageFileWidget.setVisible(selection == ImageSource.FILE);

                // be careful here, re-laying out is tricky
                getParent().getParent().layout(true, true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });

        scanningControlsWidget = new ScanningControlsWidget(this, minWidth, scanPlateInitVal);
        imageFileWidget = new ImageFileWidget(this, cameraPositionInitVal);

        scanningControlsWidget.addSelectionListener(
            new edu.ualberta.med.biobank.gui.common.events.SelectionListener() {

                @Override
                public void widgetSelected(Event e) {
                    if (ImageSourceWidget.this.selectionListener != null) {
                        ImageSourceWidget.this.selectionListener.widgetSelected(e);
                    }
                }
            });

        scanningControlsWidget.setVisible(imageSourceInitVal == ImageSource.FLATBED_SCANNER);
        imageFileWidget.setVisible(imageSourceInitVal == ImageSource.FILE);
    }

    public void addSelectionListener(
        edu.ualberta.med.biobank.gui.common.events.SelectionListener listener) {
        this.selectionListener = listener;
    }
}
