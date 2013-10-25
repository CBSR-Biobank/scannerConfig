package edu.ualberta.med.scannerconfig.dmscanlib;

import org.junit.Assert;
import org.junit.Test;

public class TestPoint {

    @Test
    public void point() {
        Point pt = new Point(2,3);
        Assert.assertEquals(2.0, pt.getX(), 0.0);
        Assert.assertEquals(3.0, pt.getY(), 0.0);        
    }

    @Test
    public void translate() {
        Point pt = new Point(2,3);
        Point trPt = pt.translate(new Point(4,8));   
        Assert.assertEquals(6.0, trPt.getX(), 0.0);
        Assert.assertEquals(11.0, trPt.getY(), 0.0);
    }

    @Test
    public void scale() {
        Point pt = new Point(2,3);
        Point scaledPt = pt.scale(5);   
        Assert.assertEquals(10.0, scaledPt.getX(), 0.0);
        Assert.assertEquals(15.0, scaledPt.getY(), 0.0);
    }
}
