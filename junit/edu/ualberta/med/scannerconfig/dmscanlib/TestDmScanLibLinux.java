package edu.ualberta.med.scannerconfig.dmscanlib;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class TestDmScanLibLinux extends RequiresJniLibraryTest {

     //private static Logger log = LoggerFactory.getLogger(TestDmScanLibLinux.class);
     
     @Before
     public void beforeMethod() {
         // this test is valid only when not running on windows
         Assume.assumeTrue(!LibraryLoader.getInstance().runningMsWindows());        
     }

    @Test
    public void linuxEmptyImplementationJNI() throws Exception {
        ScanLib scanLib = ScanLib.getInstance();
        ScanLibResult r = scanLib.isTwainAvailable();
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.getScannerCapability();
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.scanImage(0, 0, 0, 0, null, "tmp.txt");
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.scanFlatbed(0, 0, 0, 0, "tmp.txt");
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());

        r = scanLib.scanAndDecode(0, 0, 0, 0, null, null,
            new WellRectangle[] {});
        Assert.assertEquals(ScanLib.SC_FAIL, r.getResultCode());
        Assert.assertEquals(ScanLib.SC_FAIL, r.getValue());
    }
}
