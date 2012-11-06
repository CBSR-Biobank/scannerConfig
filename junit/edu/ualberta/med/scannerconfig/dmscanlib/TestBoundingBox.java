package edu.ualberta.med.scannerconfig.dmscanlib;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBoundingBox extends BaseTest {

    private static Logger log = LoggerFactory
        .getLogger(TestBoundingBox.class);

    @Test
    public void boundingBoxGetCorner() throws Exception {

    }

    @Test
    public void boundingBoxScale() throws Exception {

    }

    @Test
    public void invalidBoundingBox() throws Exception {
        try {
            new BoundingBox(new Point(0, 0), new Point(0, 0));
            Assert
                .fail("should not be allowed to create a bounding box with zero area");
        } catch (IllegalArgumentException e) {
            // do nothing here
        }

        try {
            new BoundingBox(new Point(10, 10), new Point(10, 10));
            Assert
                .fail("should not be allowed to create a bounding box with zero area");
        } catch (IllegalArgumentException e) {
            // do nothing here
        }
    }
}
