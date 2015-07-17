package com.chen.library.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;

public class BitmapUtils {

	/**
	 * 图片缩放
	 * 
	 * @param bitmap
	 *            位图
	 * @param scale
	 *            缩放比例
	 */
	public static final Bitmap scaleBitmap(Bitmap bitmap, float scale) {
		int bitmapWidth = bitmap.getWidth() ;
		int bitmapHeight = bitmap.getHeight() ;
		return redrawBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, 0, scale) ;
	}

	/**
	 * 图片旋转
	 * 
	 * @param bitmap
	 *            位图
	 * @param degrees
	 *            旋转角度
	 */
	public static final Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
		int bitmapWidth = bitmap.getWidth() ;
		int bitmapHeight = bitmap.getHeight() ;
		return redrawBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, degrees, 1) ;
	}

	/**
	 * 图片重绘
	 * 
	 * @param bitmap
	 *            位图
	 * @param cropX
	 *            切图X位置
	 * @param cropY
	 *            切图Y位置
	 * @param cropWidth
	 *            切图宽度
	 * @param cropHeight
	 *            切图高度
	 */
	public static final Bitmap cropBitmap(Bitmap bitmap, int cropX, int cropY, int cropWidth, int cropHeight) {
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight) ;
		bitmap.recycle() ;
		return resizedBitmap ;
	}

	/**
	 * 图片重绘
	 * 
	 * @param bitmap
	 *            位图
	 * @param cropX
	 *            切图X位置
	 * @param cropY
	 *            切图Y位置
	 * @param cropWidth
	 *            切图宽度
	 * @param cropHeight
	 *            切图高度
	 * @param degrees
	 *            旋转角度
	 * @param scale
	 *            缩放比例
	 */
	public static final Bitmap redrawBitmap(Bitmap bitmap, int cropX, int cropY, int cropWidth, int cropHeight, int degrees, float scale) {
		Matrix matrix = new Matrix() ;
		matrix.postRotate(degrees) ;
		matrix.postScale(scale, scale) ;
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight, matrix, false) ;
		bitmap.recycle() ;
		return resizedBitmap ;
	}

	/**
	 * 用来计算BitmapFactory.Options.inSampleSize的值
	 * 用法：
	 * opts.inSampleSize = computeSampleSize(opts, -1, 480*800);
	 * @param options
	 * @param minSideLength
	 * 			最小边长
	 * @param maxNumOfPixels
	 * @return
	 */
	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels) ;

		int roundedSize ;
		if (initialSize <= 8) {
			roundedSize = 1 ;
			while (roundedSize < initialSize) {
				roundedSize <<= 1 ;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8 ;
		}

		return roundedSize ;
	}

	public static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth ;
		double h = options.outHeight ;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels)) ;
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength)) ;

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound ;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1 ;
		} else if (minSideLength == -1) {
			return lowerBound ;
		} else {
			return upperBound ;
		}
	}
	
	/**
	 * @param 图片文件
	 * @return 宽、高组成的数组
	 */
	public static int[] getSize(File bitmapFile) {
		int[] hw = new int[]{0,0};
		BitmapFactory.Options opts = new BitmapFactory.Options() ;
		opts.inJustDecodeBounds = true ;
		BitmapFactory.decodeFile(bitmapFile.getPath(), opts);
		hw[0] = opts.outWidth;
		hw[1] = opts.outHeight;
		return hw;
	}
	
	
	public static final Drawable getAlphDrawable(Context context, Bitmap bitmap) {
		TransitionDrawable td = new TransitionDrawable(new Drawable[] { new ColorDrawable(android.R.color.transparent), new BitmapDrawable(context.getResources(), bitmap) });
		return td;
	}

	/**
	 * 
	 * @param bitmap
	 */
	private static void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	/**
	 * 将bitmap转化为数组
	 * 
	 * @param bmp
	 * @return
	 */
	public byte[] bmpToByteArray(Bitmap bmp) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		recycleBitmap(bmp);
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 创建缩放后的图片
	 * 
	 * @param bitmap
	 *            源位图
	 * @param width
	 *            缩放后位图的宽px
	 * @param height
	 *            缩放后位图的高px
	 * @return
	 */
	public static final Bitmap scaleBitmap(Bitmap bitmap, float width, float height) {
		float sourceWidth = bitmap.getWidth();
		float sourceHeight = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = (width / sourceWidth);
		float scaleHeight = (height / sourceHeight);
		matrix.postScale(scaleWidth, scaleHeight);
		// 当进行的不只是平移操作的时候，最后的参数为true，可以进行滤波处理，有助于改善新图像质量
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, (int) sourceWidth, (int) sourceHeight, matrix, true);
		recycleBitmap(bitmap);
		return newbmp;
	}

	/**
	 * 创建缩放后的图片
	 * 
	 * @param bitmap
	 *            源位图
	 * @param wScale
	 *            宽缩放的比例f
	 * @param hScale
	 *            高缩放的比例f
	 * @return
	 */
	public static final Bitmap scaleBitmap2(Bitmap bitmap, float wScale, float hScale) {
		Matrix matrix = new Matrix();
		matrix.postScale(wScale, hScale);
		// 当进行的不只是平移操作的时候，最后的参数为true，可以进行滤波处理，有助于改善新图像质量
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		recycleBitmap(bitmap);
		return newbmp;
	}

	/**
	 * 按宽度放大缩小图片
	 * 
	 * @param bitmap
	 *            源位图
	 * @param wScale
	 *            宽缩放的比例f
	 * @param hScale
	 *            高缩放的比例f
	 * @return
	 */
	public static final Bitmap scaleBitmap3(Bitmap bitmap, float width) {
		float sourceWidth = bitmap.getWidth();
		float sourceHeight = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = (width / sourceWidth);
		matrix.postScale(scaleWidth, scaleWidth);
		// 当进行的不只是平移操作的时候，最后的参数为true，可以进行滤波处理，有助于改善新图像质量
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, (int) sourceWidth, (int) sourceHeight, matrix, true);
		recycleBitmap(bitmap);
		return newbmp;
	}

	/**
	 * 创建旋转后的图片
	 * 
	 * @param bitmap
	 * @param degrees
	 * @return
	 */
	public static final Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
		int sourceWidth = bitmap.getWidth();
		int sourceHeight = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		// 当进行的不只是平移操作的时候，最后的参数为true，可以进行滤波处理，有助于改善新图像质量
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, sourceWidth, sourceHeight, matrix, true);
		recycleBitmap(bitmap);
		return newbmp;
	}

	/**
	 * 创建带圆角的bitmap图片
	 * 
	 * @param bitmap
	 *            源位图
	 * @param round
	 *            圆角的弧度
	 * @return 带有圆角的图片(Bitmap 类型)
	 */
	public static Bitmap roundCorner(Bitmap bitmap, int round) {
		// 创建画笔
		Paint paint = new Paint();
		// 消除锯齿
		paint.setAntiAlias(true);
		// 创建和位图宽高一样的矩形对象
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		// 由rect对象创建一个rectF对象，该对象的精度为float
		RectF rectF = new RectF(rect);
		// 创建和原位图一样大小的位图
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		// 根据创建的位图新建画布，上面这样创建的位图是可改变的
		Canvas canvas = new Canvas(output);
		// 将画布填充为无色
		canvas.drawARGB(0, 0, 0, 0);
		// 画一个透明的圆角矩形
		canvas.drawRoundRect(rectF, round, round, paint);
		// 设置和画笔画上去后显示画笔的颜色
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		// 将源bitmap填充到上面圆角矩形的画布中，就呈现出一个新的圆角的bitmap
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * 创建圆形图片
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap getRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;

			left = 0;
			top = 0;
			right = width;
			bottom = width;

			height = width;

			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;

			float clip = (width - height) / 2;

			top = 0;
			left = clip;
			right = width - clip;
			bottom = height;

			width = height;

			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		final int color = 0xff424242;
		final Paint paint = new Paint();
		paint.setColor(color);
		paint.setAntiAlias(true);

		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);
		// 创建一个计算好后的长宽相等的bitmap
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		// 创建一个画布，将绘制到该bitmap上
		Canvas canvas = new Canvas(output);
		// 设置画布颜色，全透明
		canvas.drawARGB(0, 0, 0, 0);
		//
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		//
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		//
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	/**
	 * 将drawable转化成bitmap
	 * 
	 * @param drawable
	 *            源drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		// 取 drawable 的长宽,colorbitmap返回-1的
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		// 建立对应 大小的bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立一个canvas，将会绘制到bitmap上
		Canvas canvas = new Canvas(bitmap);
		// 指定一个矩形区域，放在drawable得Rect中
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中，将会调用上面的方法
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 解析图片文件，获取宽高
	 * 
	 * @param bitmapFile
	 *            图片文件
	 * @return 宽、高组成的数组
	 */
	public static int[] getBitmapFileSize(File bitmapFile) {
		int[] hw = new int[] { 0, 0 };
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(bitmapFile.getPath(), opts);
		hw[0] = opts.outWidth;
		hw[1] = opts.outHeight;
		return hw;
	}

	/**
	 * 从文件中解析bitmap,如果没有指定解析出Bitmap的大小，则直接decodeFile，但是有些像素太大的解析不出，一定要设置bitmap大小，由于拍照后照片返回会颠倒 这个方法会帮你矫正照片
	 * 
	 * @param dst
	 *            bitmap文件
	 * @param width
	 *            生成bitmap宽度
	 * @param height
	 *            生成bitmap高度
	 * @return 是bitmap文件
	 */
	public static Bitmap getBitmapFromFileAdjustDegree(File dst, int width, int height) {
		if (null != dst && dst.exists()) {
			BitmapFactory.Options opts = null;
			if (width > 0 && height > 0) {
				opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(dst.getPath(), opts);
				final int minSideLength = Math.min(width, height);
				opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
			}
			int result = ExifInterface.ORIENTATION_UNDEFINED;
			try {
				ExifInterface exifInterface = new ExifInterface(dst.getPath());
				result = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int rotate = 0;
			switch (result) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			default:
				break;
			}
			Bitmap bitmap = BitmapFactory.decodeFile(dst.getPath(), opts);
			if (rotate > 0) {
				bitmap = rotateBitmap(bitmap, rotate);
			}
			return bitmap;
		}
		return null;
	}

	/**
	 * 从文件中解析bitmap,如果没有指定解析出Bitmap的大小，则直接decodeFile，但是有些像素大的解析不出，最好设置bitmap大小
	 * 
	 * @param dst
	 *            bitmap文件
	 * @param width
	 *            生成bitmap宽度
	 * @param height
	 *            生成bitmap高度
	 * @return 是bitmap文件
	 */
	public static Bitmap getBitmapFromFile(File dst, int width, int height) {
		if (null != dst && dst.exists()) {
			BitmapFactory.Options opts = null;
			if (width > 0 && height > 0) {
				opts = new BitmapFactory.Options();
				// 不去真是解析图片，只是获取宽高
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(dst.getPath(), opts);
				final int minSideLength = Math.min(width, height);
				opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
			}
			// 获取图片文件的角度
			int degree = getBitmapDegree(dst);
			if (degree != 0)
				return rotateBitmap(BitmapFactory.decodeFile(dst.getPath(), opts), degree);
			else
				return BitmapFactory.decodeFile(dst.getPath(), opts);
		}
		return null;
	}

	/**
	 * 返回图片文件的角度
	 * 
	 * @param file
	 *            图片文件
	 * @return 需要偏转的角度
	 */
	public static int getBitmapDegree(File file) {
		int rotate = 0;
		int result = 0;
		try {
			// 文件信息
			ExifInterface exifInterface = new ExifInterface(file.getPath());
			// 获取图片的角度
			result = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
		} catch (IOException e) {
			e.printStackTrace();
		}
		switch (result) {
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotate = 90;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotate = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotate = 270;
			break;
		}
		return rotate;
	}

	/**
	 * 获取缩略图图片
	 * 
	 * @param imagePath
	 *            图片的路径
	 * @param width
	 *            图片的宽度
	 * @param height
	 *            图片的高度
	 * @return 缩略图图片
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 获得带倒影的图片方法
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, width, height / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

		return bitmapWithReflection;
	}

	/**
	 * 水印
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createBitmapForWatermark(Bitmap src, Bitmap watermark) {
		if (src == null) {
			return null;
		}
		int w = src.getWidth();
		int h = src.getHeight();
		int ww = watermark.getWidth();
		int wh = watermark.getHeight();
		// create the new blank bitmap
		Bitmap newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newb);
		// draw src into
		cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
		// draw watermark into
		cv.drawBitmap(watermark, w - ww + 5, h - wh + 5, null);// 在src的右下角画入水印
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
		return newb;
	}

	/**
	 * 保存bitmap至文件
	 * @param bitmap
	 * @param file
	 * @return
	 */
	public static boolean saveBitmapToFile(Bitmap bitmap , File file)
	{
		try {
			//create a file to write bitmap data  
			file.createNewFile(); 
			 
			//Convert bitmap to byte array  
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos); 
			byte[] bitmapdata = bos.toByteArray(); 
			 
			//write the bytes in file  
			FileOutputStream fos;
			fos = new FileOutputStream(file);
			fos.write(bitmapdata); 
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		} 
	}
	
}
