package edu.ualberta.med.scannerconfig;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.ISourceProviderService;
import org.osgi.framework.BundleContext;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.scannerconfig.dmscanlib.BoundingBox;
import edu.ualberta.med.scannerconfig.dmscanlib.DecodeOptions;
import edu.ualberta.med.scannerconfig.dmscanlib.DecodeResult;
import edu.ualberta.med.scannerconfig.dmscanlib.DecodedWell;
import edu.ualberta.med.scannerconfig.dmscanlib.ImageInfo;
import edu.ualberta.med.scannerconfig.dmscanlib.Point;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLibResult;
import edu.ualberta.med.scannerconfig.dmscanlib.WellRectangle;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.sourceproviders.PlateEnabledState;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScannerConfigPlugin extends AbstractUIPlugin {

    private static final I18n i18n = I18nFactory.getI18n(ScannerConfigPlugin.class);

    public static final String IMG_SCANNER = "scanner"; //$NON-NLS-1$

    // The plug-in ID
    public static final String PLUGIN_ID = "scannerConfig"; //$NON-NLS-1$

    private final boolean IS_MS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
    private final boolean IS_LINUX = System.getProperty("os.name").startsWith("Linux");
    private final boolean IS_ARCH_64_BIT = System.getProperty("os.arch").equals("amd64");

    // The shared instance
    private static ScannerConfigPlugin plugin;

    /**
     * The constructor
     */

    @SuppressWarnings("nls")
    public ScannerConfigPlugin() {
        if (IS_MS_WINDOWS) {
            System.loadLibrary("OpenThreadsWin32");
            System.loadLibrary("dmscanlib");
        } else if (IS_LINUX && IS_ARCH_64_BIT){
            System.loadLibrary("dmscanlib64");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    @SuppressWarnings("nls")
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().startsWith("scanner.plate.coords.enabled.")) {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    ISourceProviderService service =
                        (ISourceProviderService) window.getService(ISourceProviderService.class);

                    PlateEnabledState plateEnabledSourceProvider =
                        (PlateEnabledState) service
                            .getSourceProvider(PlateEnabledState.PLATES_ENABLED);
                    Assert.isNotNull(plateEnabledSourceProvider);
                    plateEnabledSourceProvider.setPlateEnabled();
                }
            }
        });
    }

    @SuppressWarnings("nls")
    @Override
    protected void initializeImageRegistry(ImageRegistry registry) {
        registerImage(registry, IMG_SCANNER, "selectScanner.png");
    }

    @SuppressWarnings("nls")
    private void registerImage(ImageRegistry registry, String key, String fileName) {
        try {
            IPath path = new Path("icons/" + fileName);
            URL url = FileLocator.find(getBundle(), path, null);
            if (url != null) {
                ImageDescriptor desc = ImageDescriptor.createFromURL(url);
                registry.put(key, desc);
            }
        } catch (Exception e) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static ScannerConfigPlugin getDefault() {
        return plugin;
    }

    public void initialize() {
    }

    @SuppressWarnings("nls")
    public static void scanImage(BoundingBox region, String filename) throws Exception {
        IPreferenceStore prefs = getDefault().getPreferenceStore();

        int dpi = prefs.getInt(PreferenceConstants.SCANNER_DPI);
        int brightness = prefs.getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
        int contrast = prefs.getInt(PreferenceConstants.SCANNER_CONTRAST);
        int debugLevel = prefs.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);

        ScanLibResult res =
            ScanLib.getInstance()
                .scanImage(debugLevel, dpi, brightness, contrast, region, filename);

        if (res.getResultCode() != ScanLib.SC_SUCCESS) {
            throw new Exception(i18n.tr("Could not scan image:\n") + res.getMessage());
        }
    }

    @SuppressWarnings("nls")
    public static void scanFlatbed(String filename) throws Exception {
        IPreferenceStore prefs = getDefault().getPreferenceStore();

        int dpi = prefs.getInt(PreferenceConstants.SCANNER_DPI);
        int brightness = prefs.getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
        int contrast = prefs.getInt(PreferenceConstants.SCANNER_CONTRAST);
        int debugLevel = prefs.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);

        ScanLibResult res =
            ScanLib.getInstance().scanFlatbed(debugLevel, dpi, brightness, contrast, filename);

        if (res.getResultCode() != ScanLib.SC_SUCCESS) {
            throw new Exception(i18n.tr("Could not scan flatbed:\n") + res.getMessage());
        }
    }

    public static void scanPlate(int plateNumber, String filename) throws Exception {

        if ((plateNumber < 0) || (plateNumber > PreferenceConstants.SCANNER_PALLET_CONFIG.length)) {
            throw new IllegalArgumentException("plate number is invalid: " + plateNumber);
        }

        String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateNumber - 1];

        IPreferenceStore prefs = getDefault().getPreferenceStore();

        BoundingBox region =
            new BoundingBox(new Point(prefs.getDouble(prefsArr[0]), prefs.getDouble(prefsArr[1])),
                new Point(prefs.getDouble(prefsArr[2]), prefs.getDouble(prefsArr[3])));

        region = regionModifyIfScannerWia(region);

        scanImage(region, filename);
    }

    // bbox here has to start at (0,0)
    public static BoundingBox getWellsBoundingBox(final BoundingBox scanBbox) {
        final Point originPt = new Point(0, 0);
        final Point scanBoxPt1Neg = scanBbox.getCorner(0).scale(-1);
        return new BoundingBox(originPt, scanBbox.getCorner(1).translate(scanBoxPt1Neg));
    }

    public static BoundingBox getWiaBoundingBox(final BoundingBox scanBbox) {
        final Point scanBoxPt1Neg = scanBbox.getCorner(0).scale(-1);
        return new BoundingBox(scanBbox.getCorner(0), scanBbox.getCorner(1)
            .translate(scanBoxPt1Neg));
    }

    private static BoundingBox regionModifyIfScannerWia(BoundingBox region) {
        if (!ScannerConfigPlugin.getDefault().getPreferenceStore()
            .getString(PreferenceConstants.SCANNER_DRV_TYPE)
            .equals(PreferenceConstants.SCANNER_DRV_TYPE_WIA)) {
            return region;
        }

        return getWiaBoundingBox(region);
    }

    @SuppressWarnings("nls")
    public static Set<DecodedWell> decodePlate(int plateNumber) throws Exception {
        IPreferenceStore prefs = getDefault().getPreferenceStore();

        int dpi = prefs.getInt(PreferenceConstants.SCANNER_DPI);
        int brightness = prefs.getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
        int contrast = prefs.getInt(PreferenceConstants.SCANNER_CONTRAST);
        int debugLevel = prefs.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);
        int edgeThresh = prefs.getInt(PreferenceConstants.LIBDMTX_EDGE_THRESH);
        double scanGap = prefs.getDouble(PreferenceConstants.LIBDMTX_SCAN_GAP);
        int squareDev = prefs.getInt(PreferenceConstants.LIBDMTX_SQUARE_DEV);
        int corrections = prefs.getInt(PreferenceConstants.LIBDMTX_CORRECTIONS);

        String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateNumber - 1];

        final BoundingBox scanRegion =
            new BoundingBox(new Point(prefs.getDouble(prefsArr[0]), prefs.getDouble(prefsArr[1])),
                new Point(prefs.getDouble(prefsArr[2]), prefs.getDouble(prefsArr[3])));

        final BoundingBox scanBbox = regionModifyIfScannerWia(scanRegion);
        final BoundingBox wellsBbox = getWellsBoundingBox(scanRegion);

        Set<WellRectangle> wells =
            WellRectangle.getWellRectanglesForBoundingBox(wellsBbox, 8, 12, dpi);

        DecodeResult res =
            ScanLib.getInstance().scanAndDecode(debugLevel, dpi, brightness, contrast, scanBbox,
                new DecodeOptions(scanGap, squareDev, edgeThresh, corrections, 1),
                wells.toArray(new WellRectangle[] {}));

        if (res.getResultCode() != ScanLib.SC_SUCCESS) {
            throw new Exception(i18n.tr("Could not decode plate:\n") + res.getMessage());
        }
        return res.getDecodedWells();
    }

    @SuppressWarnings("nls")
    public static Set<DecodedWell> decodeImage(String filename) throws Exception {
        IPreferenceStore prefs = getDefault().getPreferenceStore();

        int debugLevel = prefs.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);
        int edgeThresh = prefs.getInt(PreferenceConstants.LIBDMTX_EDGE_THRESH);
        double scanGap = prefs.getDouble(PreferenceConstants.LIBDMTX_SCAN_GAP);
        int squareDev = prefs.getInt(PreferenceConstants.LIBDMTX_SQUARE_DEV);
        int corrections = prefs.getInt(PreferenceConstants.LIBDMTX_CORRECTIONS);

        File imageFile = new File(filename);
        BufferedImage image = ImageIO.read(imageFile);
        final int dpi = ImageInfo.getImageDpi(imageFile);
        final double dotWidth = 1 / new Double(dpi).doubleValue();
        BoundingBox imageBbox =
            new BoundingBox(new Point(0, 0),
                new Point(image.getWidth(), image.getHeight()).scale(dotWidth));

        Set<WellRectangle> wells =
            WellRectangle.getWellRectanglesForBoundingBox(imageBbox, 8, 12, dpi);

        DecodeResult res =
            ScanLib.getInstance().decodeImage(debugLevel, filename,
                new DecodeOptions(scanGap, squareDev, edgeThresh, corrections, 1),
                wells.toArray(new WellRectangle[] {}));

        if (res.getResultCode() != ScanLib.SC_SUCCESS) {
            throw new Exception(i18n.tr("Could not decode image: ") + res.getMessage());
        }
        return res.getDecodedWells();
    }

    @SuppressWarnings("nls")
    public boolean getPlateEnabled(int plateId) {
        Assert.isTrue((plateId > 0)
            && (plateId <= PreferenceConstants.SCANNER_PALLET_ENABLED.length),
            i18n.tr("plate id is invalid: ") + plateId);
        return getPreferenceStore().getBoolean(
            PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1]);
    }

    public int getPlateCount() {
        int result = 0;
        for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_ENABLED.length; ++i) {
            if (getPreferenceStore().getBoolean(PreferenceConstants.SCANNER_PALLET_ENABLED[i]))
                ++result;
        }
        return result;
    }

    public static int getPlatesMax() {
        return PreferenceConstants.SCANNER_PALLET_ENABLED.length;
    }

    public int getDpi() {
        return getPreferenceStore().getInt(PreferenceConstants.SCANNER_DPI);
    }

    public int getBrightness() {
        return getPreferenceStore().getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
    }

    public int getContrast() {
        return getPreferenceStore().getInt(PreferenceConstants.SCANNER_CONTRAST);
    }

    /**
     * Display an error message
     */
    public static void openError(String title, String message) {
        MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            title, message);
    }

    public static void openInformation(String title, String message) {
        MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getShell(), title, message);
    }

    public static boolean openConfim(String title, String message) {
        return MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getShell(), title, message);
    }

    /**
     * Display an error message asynchronously
     */
    public static void openAsyncError(final String title, final String message) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell(), title, message);
            }
        });
    }

    @SuppressWarnings("nls")
    public int getPlateNumber(String barcode, boolean realscan) {
        for (int i = 0; i < PreferenceConstants.SCANNER_PLATE_BARCODES.length; i++) {
            if (realscan && !ScannerConfigPlugin.getDefault().getPlateEnabled(i + 1)) continue;

            String pref =
                getPreferenceStore().getString(PreferenceConstants.SCANNER_PLATE_BARCODES[i]);
            Assert.isTrue(!pref.isEmpty(), i18n.tr("preference not assigned"));
            if (pref.equals(barcode)) {
                return i + 1;
            }
        }
        return -1;
    }

    @SuppressWarnings("nls")
    public List<String> getPossibleBarcodes(boolean realscan) {
        List<String> barcodes = new ArrayList<String>();
        for (int i = 0; i < PreferenceConstants.SCANNER_PLATE_BARCODES.length; i++) {
            if (realscan && !ScannerConfigPlugin.getDefault().getPlateEnabled(i + 1)) continue;

            String pref =
                getPreferenceStore().getString(PreferenceConstants.SCANNER_PLATE_BARCODES[i]);
            Assert.isTrue(!pref.isEmpty(), i18n.tr("preference not assigned"));
            barcodes.add(pref);
        }
        return barcodes;
    }

    public static int getPlatesEnabledCount(boolean realscan) {
        int count = 0;
        for (int i = 0; i < PreferenceConstants.SCANNER_PLATE_BARCODES.length; i++) {
            if (!realscan || ScannerConfigPlugin.getDefault().getPlateEnabled(i + 1)) count++;
        }
        return count;
    }
}
