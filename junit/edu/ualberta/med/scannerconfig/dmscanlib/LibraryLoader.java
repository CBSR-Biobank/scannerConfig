package edu.ualberta.med.scannerconfig.dmscanlib;

public class LibraryLoader {

    private static LibraryLoader instance = null;

    private boolean isMsWindows = false;

    private LibraryLoader() {
        String osname = System.getProperty("os.name");
        isMsWindows = osname.startsWith("Windows");

        if (isMsWindows) {
            System.loadLibrary("OpenThreadsWin32");
            System.loadLibrary("dmscanlib");
        } else {
            System.loadLibrary("dmscanlib64");
        }
    }

    public static LibraryLoader getInstance() {
        if (instance != null) return instance;

        instance = new LibraryLoader();
        return instance;
    }

    public boolean runningMsWindows() {
        return isMsWindows;
    }

}
