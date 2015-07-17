package com.chen.library.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * 
 * @author liulj
 * 
 */
public class PhotoUtilLj {
	/**
	 * 通过内容提供者获取保存在系统数据库中的所有存在的图片文件
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public static List<ExternalImageInfo> getMediaStoreImages(Context context) {
		List<ExternalImageInfo> mImageInfos = new ArrayList<ExternalImageInfo>();
		Cursor imagecursor = null;
		try {
			final String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_MODIFIED };
			final String sortOrder = MediaStore.Images.Media._ID;
			// 下面两个方法都行
			// imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
			imagecursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
			if (imagecursor != null && imagecursor.getCount() > 0) {
				while (imagecursor.moveToNext()) {
					ExternalImageInfo imageInfo = new ExternalImageInfo();
					// 返回data在第几列，并插入地址
					int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
					String path = imagecursor.getString(dataColumnIndex);
					File file = new File(path);
					// 该路径下的文件存在则添加到集合中
					if (file.exists()) {
						imageInfo.setPath(path);
						// 插入修改时间
						int modifiedColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
						String modifiedDate = imagecursor.getString(modifiedColumnIndex);
						imageInfo.setDate(modifiedDate);
						// 添加名字
						mImageInfos.add(imageInfo);
					}
				}
			}
		} catch (Exception e) {
			imagecursor.close();
		} finally {
			imagecursor.close();
		}
		// 按降序排
		Collections.sort(mImageInfos);
		return mImageInfos;
	}

	/**
	 * 根据所有图片信息，把图片按日期分组
	 * 
	 * @param imageInfos
	 *            所有图片的信息集合
	 * @return 按日期分好组后的map
	 */
	public static Map<String, List<ExternalImageInfo>> getAllDirByImages(List<ExternalImageInfo> imageInfos) {
		HashMap<String, List<ExternalImageInfo>> map = new HashMap<String, List<ExternalImageInfo>>();
		map.clear();
		List<ExternalImageInfo> imageInfoAll = new ArrayList<ExternalImageInfo>();
		imageInfoAll.addAll(imageInfos);
		map.put("所有图片", imageInfoAll);
		for (ExternalImageInfo imageInfo : imageInfos) {
			if (imageInfo != null && imageInfo.getPath() != null) {
				File file = new File(imageInfo.getPath());
				if (file.exists()) {
					if (file.getParentFile() != null) {
						if (map.containsKey(file.getParentFile().getName())) {
							// 如果key已经存在
							map.get(file.getParentFile().getName()).add(imageInfo);
						} else {
							// 如果key不存在
							List<ExternalImageInfo> imageInfos2 = new ArrayList<ExternalImageInfo>();
							imageInfos2.add(imageInfo);
							map.put(file.getParentFile().getName(), imageInfos2);
						}
					}
				}
			}
		}
		return map;
	}

	/**
	 * 发出查找文件广播,此广播会异步扫文件，并插入到media database中
	 * 
	 * @param context
	 *            上下文
	 * @param picStr
	 *            需要扫描的图片文件全路径
	 */
	public static void ScanFile(Context context, String picStr) {
		File mFile = new File(picStr);
		if (mFile.exists()) {
			// String url = MediaStore.Images.Media.insertImage(context.getContentResolver(), picStr, name, "");
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mFile)));
			Toast.makeText(context, "已经保存到路径：" + picStr, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 扫描某个文件夹
	 * 
	 * @param context
	 * @param dir
	 */
	public static void ScanDirectory(Context context, String dir) {
		String action = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";
		Intent scanIntent = new Intent(action);
		scanIntent.setData(Uri.fromFile(new File(dir)));
		context.sendBroadcast(scanIntent);
	}

	public static class ExternalImageInfo implements Comparable<ExternalImageInfo> {
		// 设置id为自增长的组件
		private Integer id;
		private String path;
		private int status;// 0未选中,1选中未插入数据库,||(这边是已经插入数据库的可能状态)2选中插入数据库,3已经上传照片,4完全发布
		private String name;
		private String date;// 秒数
		private int fileid;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public int getFileid() {
			return fileid;
		}

		public void setFileid(int fileid) {
			this.fileid = fileid;
		}

		@Override
		public int compareTo(ExternalImageInfo another) {
			long a = Long.parseLong(date);
			long b = Long.parseLong(another.getDate());
			if (b > a) {
				return 1;
			} else if (b < a) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
