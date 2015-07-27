package com.chen.library.utils.photopicker;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

/**
 * 图片选择接口
 */
public interface IPhotoPicker {
	
	public static enum ReturnType{
		Bitmap,File
	}
	
	public static final int REQUEST_CODE_CAMERA = 1003;
	public static final int REQUEST_CODE_GALLERY = 1004;
	public static final int REQUEST_CODE_CROP = 1005;
	/**
	 * 拍照
	 */
	public void takePhoto();

	/**
	 * 画廊
	 */
	public void fromGallery();

	/**
	 * 裁剪图片
	 */
	public void cropPhoto(File file);

	/**
	 * 裁剪图片
	 */
	public void cropPhoto(Uri uri);
	
	/**
	 * 是否裁剪
	 * @param isCrop
	 */
	public void enableCrop(boolean isCrop);
	
	/**
	 * 接收系统返回数据
	 */
	public void onActivityResult(int arg0, int arg1, final Intent arg2);

	/**
	 * 设置监听
	 */
	public void setGetImgListener(ImgListener listener);

	/**
	 * 设置返回类型
	 * @param type
	 */
	public void setReturnType(ReturnType type);
	
	/**
	 * 位图监听，通过该接口返回获得的位图
	 */
	public interface ImgListener {
		public void getBitmap(Bitmap bitmap);
		public void getFile(File file);
	}

}
