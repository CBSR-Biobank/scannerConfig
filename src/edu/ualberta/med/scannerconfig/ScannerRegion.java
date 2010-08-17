package edu.ualberta.med.scannerconfig;

public class ScannerRegion {

	public String name;

	public double left;

	public double top;

	public double right;

	public double bottom;

	public double gapX;
	public double gapY;

	public ScannerRegion() {
		left = top = right = bottom = gapX = gapY = 0;
	}

	public ScannerRegion(ScannerRegion region) {
		this.set(region);
	}

	public ScannerRegion(String name, double left, double top, double right,
			double bottom, double gapX, double gapY) {
		this.name = name;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.gapX = gapX;
		this.gapY = gapY;
	}

	public void set(ScannerRegion region) {
		name = region.name;
		left = region.left;
		top = region.top;
		right = region.right;
		bottom = region.bottom;
		gapX = region.gapX;
		gapY = region.gapY;
	}

	public void set(double left, double top, double right, double bottom,
			double gapX, double gapY) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.gapX = gapX;
		this.gapY = gapY;
	}
}
