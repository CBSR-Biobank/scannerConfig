package edu.ualberta.med.scannerconfig.imageprocessing.imagelab;

import org.eclipse.swt.graphics.ImageData;


public interface ImageFilter {
	public ImageData filter(ImageData id);
}

