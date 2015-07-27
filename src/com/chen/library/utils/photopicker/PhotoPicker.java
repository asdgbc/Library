package com.chen.library.utils.photopicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.UUID;

import com.chen.library.common.Setting;
import com.chen.library.utils.BitmapUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * 需要权限 READ_EXTERNAL_STORAGE
 * 
 * @author chenxuex
 * 
 */
public class PhotoPicker implements IPhotoPicker {

	public static final String MIME_TYPE_IMAGE_JPEG = "image/*";

	protected File imageFile = null;
	private WeakReference<Activity> activity = null;

	private boolean isCrop = false;
	private ImgListener mListener = null;

	private CropParams mCropParams = null;
	private Thread mHandler;

	private ReturnType mReturnType;

	/**
	 * @param tempFile
	 *            图片临时文件，拍照返回的图片保存在这个文件里面
	 */
	public PhotoPicker(Activity activity, File tempFile) {
		this.activity = new WeakReference<Activity>(activity);
		this.imageFile = tempFile;
		mReturnType = ReturnType.Bitmap;
	}

	public PhotoPicker(Activity activity) {
		// 如果imageFile只有文件名没有路径，那么拍完照点击完成是不会返回到之前的activity的
		this(activity, new File(Setting.TEMP_PATH, UUID.randomUUID() + ".jpg"));
	}

	public void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (this.imageFile != null) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(this.imageFile)); // set the image file name
		}
		// intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
		this.activity.get().startActivityForResult(intent, REQUEST_CODE_CAMERA);
	}

	public void fromGallery() {
		Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
		getImage.addCategory(Intent.CATEGORY_OPENABLE);
		getImage.setType(MIME_TYPE_IMAGE_JPEG);
		this.activity.get().startActivityForResult(getImage, REQUEST_CODE_GALLERY);
	}

	/**
	 * 
	 * @param file
	 *            需要裁剪的图片文件
	 * @param width
	 *            裁剪后的宽度
	 * @param height
	 *            裁剪后的高度
	 * 
	 */
	public void cropPhoto(File file) {
		cropPhoto(Uri.fromFile(file));
	}

	/**
	 * 
	 * @param uri
	 * 
	 * @param width
	 *            裁剪后的宽度
	 * @param height
	 *            裁剪后的高度
	 * 
	 */
	public void cropPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, MIME_TYPE_IMAGE_JPEG);
		if (this.mCropParams == null) {
			this.mCropParams = new CropParams();
		}
		this.activity.get().startActivityForResult(getCropIntent(intent, this.mCropParams), REQUEST_CODE_CROP);
	}

	private Intent getCropIntent(Intent intent, CropParams cp) {
		// 发送裁剪新号
		intent.putExtra("crop", cp.crop);
		// aspectX aspectY 是裁剪框宽高的比例
		intent.putExtra("aspectX", cp.aspectX);// 裁剪区的宽
		intent.putExtra("aspectY", cp.aspectY);// 裁剪区的高
		// outputX outputY 是裁剪后获得的图片的宽高，单位为像素
		intent.putExtra("outputX", cp.outputX);// 裁剪后获得图片的宽
		intent.putExtra("outputY", cp.outputY);// 裁剪后获得图片的高
		intent.putExtra("scale", cp.scale);// 是否保持比例
		intent.putExtra("scaleUpIfNeeded", cp.scaleUpIfNeeded);// 黑边
		// 图片输出格式
		intent.putExtra("outputFormat", cp.outputFormat);
		// 是否去除面部检测， 如果你需要特定的比例去裁剪图片，那么这个一定要去掉，因为它会破坏掉特定的比例。
		intent.putExtra("noFaceDetection", cp.noFaceDetection);
		// if (isCropBigBitmap(cp)) {// 大图裁剪如果使用return-data返回，会占用太多内存
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(this.imageFile));
		intent.putExtra("return-data", false);
		// } else {
		// 是否将数据保留在Bitmap中返回，如果是，则保存在返回intent.getExtras().getParcelable("data");
		// intent.putExtra("return-data", cp.returnData);
		// }
		return intent;
	}

	/**
	 * 根据CropParams判断是否为大图裁剪
	 * 
	 * @param cp
	 * @return
	 */
	private boolean isCropBigBitmap(CropParams cp) {
		return cp.outputX * cp.outputY >= 177 * 177 ? true : false;
	}

	/**
	 * 设置裁剪参数
	 * 
	 * @param cp
	 */
	public void setCropParam(CropParams cp) {
		this.mCropParams = cp;
	}

	@Override
	public void enableCrop(boolean b) {
		this.isCrop = b;
	}

	@Override
	public void onActivityResult(int arg0, int arg1, final Intent arg2) {
		if (arg0 == REQUEST_CODE_CAMERA && arg1 == Activity.RESULT_OK) {
			mHandler = new Thread() {
				@Override
				public void run() {
					if (imageFile.exists()) {
						if (isCrop) {
							cropPhoto(imageFile);
						} else {
							performReturn(imageFile);
						}
					} else {
						if (arg2 != null && arg2.hasExtra("data")) {
							final Bitmap tmp = arg2.getParcelableExtra("data");
							if (isCrop) {
								BitmapUtils.saveBitmapToFile(tmp, imageFile);
								cropPhoto(imageFile);
							} else {
								performReturn(tmp);
							}
						}
					}
				}
			};
			mHandler.start();
		} else if (arg0 == REQUEST_CODE_GALLERY && arg1 == Activity.RESULT_OK) {
			mHandler = new Thread() {
				@Override
				public void run() {
					Uri uri = arg2.getData();
					if (isCrop) {
						cropPhoto(Uri.fromFile(new File(GetPathFromUri.getPath(activity.get(), uri))));
					} else {
						try {
							final Bitmap mPortrait;
							mPortrait = BitmapFactory.decodeStream(activity.get().getContentResolver().openInputStream(uri));
							performReturn(mPortrait);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			};
			mHandler.start();
		} else if (arg0 == REQUEST_CODE_CROP && arg1 == Activity.RESULT_OK) {
			mHandler = new Thread() {
				@Override
				public void run() {
					performReturn(imageFile);
				}
			};
			mHandler.run();
		}
	}

	/**
	 * 返回获得的bitmap
	 * 
	 * @param bitmap
	 */
	private void returnBitmap(final Bitmap bitmap) {
		if (mListener != null && activity.get() != null) {
			activity.get().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (activity.get()!=null) {
						mListener.getBitmap(bitmap);
					}
				}
			});
		}
	}

	/**
	 * 图片文件
	 * 
	 * @param file
	 */
	private void returnFile(final File file) {
		if (mListener != null && activity.get() != null) {
			activity.get().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (activity.get()!=null) {
						mListener.getFile(file);
					}
				}
			});
		}
	}

	/**
	 * 根据mReturnType返回不同的类型
	 * 
	 * @param ob
	 */
	private void performReturn(final Object ob) {
		if (activity == null || mListener == null) {
			return;
		}
		if (ob instanceof File) {
			if (this.mReturnType == ReturnType.Bitmap) {
				Bitmap tmp = BitmapUtils.getBitmapFromFile((File) ob, 0, 0);
				returnBitmap(tmp);
			} else if (this.mReturnType == ReturnType.File) {
				returnFile((File) ob);
			}
		} else if (ob instanceof Bitmap) {
			if (this.mReturnType == ReturnType.Bitmap) {
				returnBitmap((Bitmap) ob);
			} else if (this.mReturnType == ReturnType.File) {
				BitmapUtils.saveBitmapToFile((Bitmap) ob, imageFile);
				returnFile(imageFile);
			}
		}
	}

	@Override
	public void setGetImgListener(ImgListener listener) {
		this.mListener = listener;
	}

	@Override
	public void setReturnType(ReturnType type) {
		this.mReturnType = type;
	}

}
