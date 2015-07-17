package com.chen.library.utils.photopicker;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

/**
 * 图片选择接口
 */
public interface IPhotoPicker {
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
	public void setOnBitmapListener(BitmapListener listener);

	/**
	 * 位图监听，通过该接口返回获得的位图
	 */
	public interface BitmapListener {
		public void getBitmap(Bitmap bitmap);
	}

}
