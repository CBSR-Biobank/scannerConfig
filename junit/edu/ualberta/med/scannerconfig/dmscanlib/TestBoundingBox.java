package edu.ualberta.med.scannerconfig.dmscanlib;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class TestBoundingBox {

    //private static Logger log = LoggerFactory
    //    .getLogger(TestBoundingBox.class);

    @Test
    public void boundingBox() {
        BoundingBox bbox = new BoundingBox(new Point(0,0), new Point(10,10));
        Assert.assertEquals(0.0, bbox.getCornerX(0), 0.0);
        Assert.assertEquals(0.0, bbox.getCornerY(0), 0.0);
        Assert.assertEquals(10.0, bbox.getCornerX(1), 0.0);
        Assert.assertEquals(10.0, bbox.getCornerY(1), 0.0);
        
        try {
            bbox.getCornerX(2);
            Assert.fail("bounding boxes only have 2 corners");
        } catch (IllegalArgumentException e) {
            // do nothing here
        }
        
        try {
            bbox.getCornerY(2);
            Assert.fail("bounding boxes only have 2 corners");
        } catch (IllegalArgumentException e) {
            // do nothing here
        }
    }

    @Test
    public void invalid() {
        try {
            new BoundingBox(new Point(0, 0), new Point(0, 0));
            Assert.fail("should not be allowed to create a bounding box with zero area");
        } catch (IllegalArgumentException e) {
            // do nothing here
        }

        try {
            new BoundingBox(new Point(10, 10), new Point(10, 10));
            Assert.fail("should not be allowed to create a bounding box with zero area");
        } catch (IllegalArgumentException e) {
            // do nothing here
        }
    }

    @Test
    public void getCorner() {
        BoundingBox bbox = new BoundingBox(new Point(0,0), new Point(10,10));
        try {
            bbox.getCorner(2);
            Assert.fail("bounding boxes only have 2 corners");
        } catch (IllegalArgumentException e) {
            // do nothing here
        }

    }
    
    @Test
    public void widthAndHeight() {
        BoundingBox bbox = new BoundingBox(new Point(0,0), new Point(10,10));
        Point whPt = bbox.getWidthAndHeightAsPoint();
        Assert.assertEquals(10.0, whPt.getX(), 0.0);
        Assert.assertEquals(10.0, whPt.getY(), 0.0);
        
        bbox = new BoundingBox(new Point(0,0), new Point(5,15));
        Assert.assertEquals(5.0, bbox.getWidth(), 0.0);
        Assert.assertEquals(15.0, bbox.getHeight(), 0.0);
    }

    @Test
    public void translate() {
        BoundingBox bbox = new BoundingBox(new Point(0,0), new Point(10,15));
        Point tr = new Point(5.5, 7.5);
        
        BoundingBox trBbox = bbox.translate(tr);
        Assert.assertEquals(5.5, trBbox.getCornerX(0), 0.0);
        Assert.assertEquals(7.5, trBbox.getCornerY(0), 0.0);
        Assert.assertEquals(15.5, trBbox.getCornerX(1), 0.0);
        Assert.assertEquals(22.5, trBbox.getCornerY(1), 0.0);

    }
}
