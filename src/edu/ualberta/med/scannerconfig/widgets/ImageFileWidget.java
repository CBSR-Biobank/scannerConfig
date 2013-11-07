package edu.ualberta.med.scannerconfig.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.scannerconfig.CameraPosition;

public class ImageFileWidget extends Composite {

    // private static final I18n i18n = I18nFactory.getI18n(ImageFileWidget.class);

    // private static Logger log = LoggerFactory.getLogger(ImageFileWidget.class.getName());

    private final GridData gridData;

    private final CameraPositionWidget cameraPositionWidget;

    public ImageFileWidget(Composite parent, CameraPosition cameraPosition) {
        super(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 1;
        setLayout(layout);

        gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        setLayoutData(gridData);

        cameraPositionWidget = new CameraPositionWidget(this, cameraPosition);
    }

    @Override
    public void setVisible(boolean visible) {
        gridData.exclude = !visible;
        super.setVisible(visible);
    }

    public CameraPosition getSelection() {
        return cameraPositionWidget.getSelection();
    }

}
