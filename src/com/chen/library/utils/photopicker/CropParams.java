package com.chen.library.utils.photopicker;

import android.graphics.Bitmap;

/**
 * 裁剪参数配置类
 * @author chenxuex
 *
 */
public class CropParams {

	public static final String CROP_TYPE = "image/*";
	public static final String OUTPUT_FORMAT = Bitmap.CompressFormat.JPEG.toString();

    public static final int DEFAULT_ASPECT = 1;
    public static final int DEFAULT_OUTPUT = 100;
	
	public int aspectX = 0;
	public int aspectY = 0;
	public int outputX = 0;
	public int outputY = 0;
	public boolean scale;
	public boolean scaleUpIfNeeded;
	public boolean returnData;
	public boolean noFaceDetection;
	public String outputFormat;
	public String crop;
	public String type;
	
	public CropParams() {
        type = CROP_TYPE;
        outputFormat = OUTPUT_FORMAT;
        crop = "true";
        scale = true;
        returnData = true;
        noFaceDetection = true;
        scaleUpIfNeeded = true;
        aspectX = DEFAULT_ASPECT;
        aspectY = DEFAULT_ASPECT;
        outputX = DEFAULT_OUTPUT;
        outputY = DEFAULT_OUTPUT;
    }
	
}
