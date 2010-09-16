package edu.ualberta.med.scannerconfig;

public class ScannerRegion {

	public String name;

	public double left;

	public double top;

	public double right;

	public double bottom;

	public double gapX;

	public double gapY;

	public boolean verticalRotation;

	public ScannerRegion() {
		left = top = right = bottom = gapX = gapY = 0;
		verticalRotation = false;
	}

	public ScannerRegion(ScannerRegion region) {
		this.set(region);
	}

	public ScannerRegion(String name, double left, double top, double right,
			double bottom, double gapX, double gapY, boolean verticalRotation) {
		this.name = name;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.gapX = gapX;
		this.gapY = gapY;
		this.verticalRotation = verticalRotation;
	}

	public void set(ScannerRegion region) {
		name = region.name;
		left = region.left;
		top = region.top;
		right = region.right;
		bottom = region.bottom;
		gapX = region.gapX;
		gapY = region.gapY;
		verticalRotation = region.verticalRotation;
	}

	public void set(double left, double top, double right, double bottom,
			double gapX, double gapY, boolean verticalRotation) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.gapX = gapX;
		this.gapY = gapY;
		this.verticalRotation = verticalRotation;
	}
}
