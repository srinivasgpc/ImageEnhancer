package com.softa.imageenhancer;

import android.graphics.Bitmap;

public interface ImageEnhancer {
	
	public Bitmap enhanceImage(Bitmap bitmap, int configuration);
	public int getProgress();
	public String[] getConfigurationOptions();

}
