package com.chen.library.utils.imageloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.chen.library.utils.BitmapUtils;
import com.chen.library.utils.Md;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;

public class Utils {

	/**
	 * 根据url下载图片在指定的文件
	 * 
	 * @param urlStr
	 * @param file
	 * @return
	 */
	public static boolean downloadImgByUrlIntoFile(String urlStr, File file) {
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			is = conn.getInputStream();
			fos = new FileOutputStream(file);
			byte[] buf = new byte[512];
			int len = 0;
			while ((len = is.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			fos.flush();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}

			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
			}
		}

		return false;

	}

	public static Bitmap getBitmapFromFile(Context context, String url, int imgWidth, int imgHeight) {
		String mdCode = Md.MD5(url);
		File file = new File(getCachePath(context), mdCode);
		return getBitmapFromFile(file, imgWidth, imgHeight);
	}

	public static Bitmap getBitmapFromFile(File file, int imgWidth, int imgHeight) {
		if (!file.exists())
			return null;
		Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), op);
		op.inSampleSize = BitmapUtils.computeSampleSize(op, -1, imgWidth * imgHeight);
		op.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(file.getAbsolutePath(), op);
	}

	public static String getCachePath(Context context) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return context.getExternalCacheDir().getPath();
		} else {
			return context.getCacheDir().getPath();
		}
	}

	/**
	 * 获取不同形状的bitmap
	 * 
	 * @param request
	 * @return
	 */
	public static Bitmap getDiffSizeBitmap(ImageType type, Bitmap bitmap) {
		switch (type) {
		case Rectangle:
			return bitmap;
		default:
			break;
		}
		return null;

	}

}
