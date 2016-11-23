package com.softa.imageenhancer;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class TestEnhancer implements ImageEnhancer {

	private static final int ACTION_3 = 3;
	private static final int ACTION_2 = 2;
	private static final int ACTION_1 = 1;
	private static final int ACTION_0 = 0;
	private int progress;

	public TestEnhancer() {

	}

	public Bitmap enhanceImageHSV(Bitmap theImage, int action) {

		// Get the image pixels
		int height = theImage.getHeight();
		int width = theImage.getWidth();
		Log.d("DEBUG", "Image size is " + width + "px by " + height + "px." );
		int[] pixels = new int[height * width];
		theImage.getPixels(pixels, 0, width,0,0, width, height);
		//int picEqualized [] = new int[height*width];


		progress = 5;
		Bitmap modifiedImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Bitmap modifiedImage1 = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		Log.d("DEBUG", "pixels length = " + pixels.length);

		//Convert pixels to brightness values;
		float[][] hsvPixels = convertToHSV(pixels);


		progress = 40;

		//Log.d("DEBUG", "hsvPixels length = " + hsvPixels.length);

		// Here below some manipulations of the image is made as examples.
		// This should be changed to your image enhancement algorithms.
		if (action == ACTION_0) {
			progress = 60;
			//call CalculateHist method to get the histogram
			int[] h = CalculateHist(theImage);
			int[] h1 = CalculateHist1(theImage);
			int[] h2 = CalculateHist2(theImage);
			//calculate total number of pixel
			int mass = theImage.getWidth() * theImage.getHeight();
			int k;int k1;int k3;
			float scale = (float) 255.0 / mass;
			//CDF calculation
			int[] C1=cdf(h,scale);
			int[]C2=cdf(h1,scale);
			int[]C3=cdf(h2,scale);

			//mapping new pixels values
			for (int i = 0; i < theImage.getWidth(); i++) {
				for (int j = 0; j < theImage.getHeight(); j++) {
					int pixel = theImage.getPixel(i, j);
					//set the new value
					k = C1[Color.red(pixel)];
					k1 = C2[Color.green(pixel)];
					k3 = C3[Color.blue(pixel)];
					int rgb = Color.rgb(k, k1, k3);
					modifiedImage.setPixel(i, j, rgb);
					//modifiedImage1.getPixels(pixels, 0, width,0,0, width, height);
				}
			}
		}else {
			for (int i = 0; i < hsvPixels.length; i++) {
				hsvPixels[i][1] = 0; // Set color saturation to zero
				pixels[i] = Color.HSVToColor(hsvPixels[i]);

			}
			modifiedImage.setPixels(pixels, 0, width, 0, 0, width, height);
		}
		progress = 80;
		Log.d("DEBUG","creating BITMAP,width x height "+width+" "+height);
		//Bitmap modifiedImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		Log.d("DEBUG", "saturation zeroed");
		progress = 100;
		return modifiedImage;
	}

	private float[][] convertToHSV(int[] pixels) {
		float[][] hsvPixels = new float[pixels.length][3];
		for (int i = 0; i < pixels.length; i++) {
			Color.RGBToHSV(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]), hsvPixels[i]);

		}
		return hsvPixels;
	}
	public int[] CalculateHist(Bitmap bi) {
		int k;
		int levels[] = new int[256];
		for (int i = 0; i < bi.getWidth(); i++) {
			for (int j = 0; j < bi.getHeight(); j++) {
				int pixel = bi.getPixel(i, j);
				levels[Color.red(pixel)]++;
			}
		}
		return levels;
	}
	public int[] CalculateHist1(Bitmap bi) {
		int k;
		int levels[] = new int[256];
		for (int i = 0; i < bi.getWidth(); i++) {
			for (int j = 0; j < bi.getHeight(); j++) {
				int pixel = bi.getPixel(i, j);
				levels[Color.green(pixel)]++;
			}
		}
		return levels;
	}
	public int[] CalculateHist2(Bitmap bi) {
		int k;
		int levels[] = new int[256];
		for (int i = 0; i < bi.getWidth(); i++) {
			for (int j = 0; j < bi.getHeight(); j++) {
				int pixel = bi.getPixel(i, j);
				levels[Color.blue(pixel)]++;
			}
		}
		return levels;
	}
	public int[]cdf(int[] h,float scale){
		int sum=0;
		for (int x = 0; x < h.length; x++) {
			sum += h[x];
			int value1 = (int) (scale * sum);
			h[x] = value1;
		}
		return h;
	}



	public int getProgress() {
		// Log.d("DEBUG", "Progress: "+progress);
		return progress;
	}

	@Override
	public Bitmap enhanceImage(Bitmap bitmap, int configuration) {
		switch (configuration) {
			case ACTION_0:
				return enhanceImageHSV(bitmap, 0);
			case ACTION_1:
				return enhanceImageHSV(bitmap, 1);
		}
		return enhanceImageHSV(bitmap, 0);
	}

	@Override
	public String[] getConfigurationOptions() {
		return new String[]{ "Histogram Eq RGB",  "Convert to grayscale"};

	}


}

