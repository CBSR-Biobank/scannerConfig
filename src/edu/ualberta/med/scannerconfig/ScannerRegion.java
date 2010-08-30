package edu.ualberta.med.scannerconfig;

public class ScannerRegion {

	public String name;

	public double left;

	public double top;

	public double right;

	public double bottom;

	public double gapX;

	public double gapY;

	public boolean horizontalRotation;

	public ScannerRegion() {
		left = top = right = bottom = gapX = gapY = 0;
		horizontalRotation = true;
	}

	public ScannerRegion(ScannerRegion region) {
		this.set(region);
	}

	public ScannerRegion(String name, double left, double top, double right,
			double bottom, double gapX, double gapY, boolean horizontalRotation) {
		this.name = name;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.gapX = gapX;
		this.gapY = gapY;
		this.horizontalRotation = horizontalRotation;
	}

	public void set(ScannerRegion region) {
		name = region.name;
		left = region.left;
		top = region.top;
		right = region.right;
		bottom = region.bottom;
		gapX = region.gapX;
		gapY = region.gapY;
		horizontalRotation = region.horizontalRotation;
	}

	public void set(double left, double top, double right, double bottom,
			double gapX, double gapY, boolean horizontalRotation) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.gapX = gapX;
		this.gapY = gapY;
		this.horizontalRotation = horizontalRotation;
	}
}
