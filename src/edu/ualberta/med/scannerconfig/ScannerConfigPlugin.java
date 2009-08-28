package edu.ualberta.med.scannerconfig;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.ualberta.med.scanlib.ScanCell;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScannerConfigPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "scannerConfig";

    // The shared instance
    private static ScannerConfigPlugin plugin;

    /**
     * The constructor
     */
    public ScannerConfigPlugin() {
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            System.loadLibrary("scanlib");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
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

    public static ScanCell[][] scan(int plateNum) {
        // TODO Auto-generated method stub
        return null;
    }

}
