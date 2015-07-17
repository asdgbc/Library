package com.chen.library.utils.imageloader;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 加载图片的请求
 * 
 * @author chenxuex
 * 
 */
public class Request {
	private String url;
	private ImageView targetView;
	private Bitmap bitmap;
	/**
	 *  类型，控制图片形状,默认方形
	 *  {@link Utils#getDiffSizeBitmap(ImageType type, Bitmap bitmap)}
	 *  {@link #ImageType}
	 */
	private ImageType type;

	public Request(String url, ImageView targetView) {
		this(url, targetView, null);
	}

	public Request(String url, ImageView targetView, Bitmap bitmap) {
		this(url, targetView, bitmap, ImageType.Rectangle);
	}

	public Request(String url, ImageView targetView, Bitmap bitmap, ImageType type) {
		this.url = url;
		this.targetView = targetView;
		this.bitmap = bitmap;
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ImageView getTargetView() {
		return targetView;
	}

	public void setTargetView(ImageView targetView) {
		this.targetView = targetView;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public ImageType getType() {
		return type;
	}

	public void setType(ImageType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Request))
			return false;

		// if (super.equals(o))
		// return true;

		Request tmp = (Request) o;

		if (this.url.equals(tmp.getUrl()) && this.targetView.equals(tmp.getTargetView())) {
			return true;
		}

		return false;
	}
}
