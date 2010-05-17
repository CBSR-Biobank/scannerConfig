package edu.ualberta.med.scannerconfig.imageprocessing.filters;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

import edu.ualberta.med.scannerconfig.imageprocessing.imagelab.ImageFilter;

public class Contrast implements ImageFilter{

	@Override
	public ImageData filter(ImageData id) {

		ImageData imgDataOut = id;
		//RGB[] pixels = id.getRGBs();
		
		for(int y = 0; y < 10; y++ ){
			for(int x = 0; x < 10; x++ ){
				imgDataOut.setPixel(x, y, 1 << 24);
			}
		}
		return imgDataOut;
	}

}
