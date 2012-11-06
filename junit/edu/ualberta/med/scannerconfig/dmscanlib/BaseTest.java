package edu.ualberta.med.scannerconfig.dmscanlib;

import org.junit.Before;

public class BaseTest {

    @Before
    public void setUp() throws Exception {
        LibraryLoader.getInstance();
    }

}
