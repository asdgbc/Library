package com.chen.library.utils;

import java.io.File;
import java.text.DecimalFormat;

import android.text.TextPaint;
import android.widget.TextView;

public class CommonUtils {
	/**
	 * 取得文件大小
	 * 
	 * @param file
	 *            文件
	 * @return 文件大小
	 */
	public static long getFileSize(File file) {
		long size = 0;
		for (File subFile : file.listFiles()) {
			if (subFile.isDirectory()) {
				size += getFileSize(subFile);
			} else {
				size += subFile.length();
			}
		}
		return size;
	}
	
	
	public static float getTextWidth(String text, float Size) {
		TextPaint FontPaint = new TextPaint();
		FontPaint.setTextSize(Size);
		return FontPaint.measureText(text);
	}
	
	/**
	 * 计算出该TextView中文字的长度(像素)
	 * 
	 * @param textView
	 * @param text
	 *            文字
	 * @return
	 */
	public static float getTextViewLength(TextView textView, String text) {
		TextPaint paint = textView.getPaint();
		// 得到使用该paint写上text的时候,像素为多少
		float textLength = paint.measureText(text);
		return textLength;
	}
	
	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 *            文件大小
	 * @return
	 */
	public static String FormatFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}

		if (fileSizeString.equals(".00B")) {
			fileSizeString = "0" + fileSizeString;
		}
		return fileSizeString;
	}
	
	/**
	 * 获取文件夹大小
	 * @param path
	 * 			路径
	 * @return
	 */
	public static final long getFolderSize(String path) {
		File cacheFolder = new File(path);
		// 文件夹是否存在
		if (cacheFolder.exists()) {
			return getFileSize(cacheFolder);
		} else {
			return 0;
		}
	}
	
	/**
	 * 清空文件夹
	 * @param path
	 * 			文件夹路径
	 */
	public static final void clearFolder(String path) {
		File tempFolder = new File(path);
		if (tempFolder.exists()) {
			File[] tempFiles = tempFolder.listFiles();
			for (File tempFile : tempFiles) {
				tempFile.delete();
			}
		}
	}
	
}
