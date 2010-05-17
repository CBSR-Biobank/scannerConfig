package edu.ualberta.med.scannerconfig.imageprocessing.imagelab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class ImageProvider {

	private ImageData imgData;

	public ImageProvider(String filename) {
		Image img = new Image(null, filename);
		if (img != null) {
			imgData = img.getImageData();
		} else {
			imgData = null;
		}
	}

	public ImageData getImageData() {
		return imgData;
	}

	public boolean applyFilter(ImageFilter imgFilter) {

		if (imgData == null) {
			return false;
		}

		imgData = imgFilter.filter(imgData);
		return true;
	}

	public boolean saveImage(String filename) {
		ImageLoader loader = new ImageLoader();
		if (imgData != null) {
			loader.data = new ImageData[] { imgData };
			loader.save(filename, SWT.IMAGE_PNG);
			return true;
		}
		else{
			return false;
		}	
		
	}

}
