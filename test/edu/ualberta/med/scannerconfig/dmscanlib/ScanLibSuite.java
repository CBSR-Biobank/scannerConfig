package edu.ualberta.med.scannerconfig.dmscanlib;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestDmScanLibLinux.class,
    TestDmScanLibWindows.class,
    TestPoint.class,
})
public class ScanLibSuite {
}
