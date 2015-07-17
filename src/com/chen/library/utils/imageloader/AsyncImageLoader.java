package com.chen.library.utils.imageloader;

import java.io.File;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.chen.library.utils.Md;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

public class AsyncImageLoader {

	// Double checked Locking实现单例模式
	// private static volatile AsyncImageLoader instance ;
	// private AsyncImageLoader() {}
	//
	// public AsyncImageLoader getInstance(){
	// if (instance==null) {
	// synchronized(AsyncImageLoader.class){
	// if (instance==null) {
	// instance = new AsyncImageLoader();
	// }
	// }
	// }
	// return instance;
	// }

	private static class LIFOLoader {
		private static final AsyncImageLoader instance = new AsyncImageLoader(Type.LIFO);
	}

	private static class FIFOLoader {
		private static final AsyncImageLoader instance = new AsyncImageLoader(Type.FIFO);
	}

	public static AsyncImageLoader getInstance() {
		return getInstance(Type.LIFO);
	}

	public static AsyncImageLoader getInstance(Type type) {
		if (type == Type.LIFO) {
			return LIFOLoader.instance;
		} else {
			return FIFOLoader.instance;
		}
	}

	private final static byte DEF_THREAD_POOL_SIZE = 3;
	/**
	 * UI线程中的Handler
	 */
	private Handler mUIHandler;
	private byte THREAD_POOL_SIZE = DEF_THREAD_POOL_SIZE;

	private Handler mHandler;

	// 线程池,用以加载图片
	private ExecutorService mThreadPool;
	// 处理图片加载的主线程
	private ExecutorService mMainThread;
	// 任务队列
	private LinkedList<Request> mTaskQueue;
	// 图片缓存
	private LruCache<String, Bitmap> mImageCache;

	// 这个东东还要再看看
	private Semaphore mSemaphore = new Semaphore(0);
	private Semaphore mThreadSemaphore;

	private Context mContext;

	private Type mType;

	public static enum Type {
		FIFO, LIFO
	}

	private AsyncImageLoader(Type type) {
		mType = type;
		mMainThread = Executors.newSingleThreadExecutor();
		mMainThread.execute(new Runnable() {

			@Override
			public void run() {
				Looper.prepare();
				mHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						try {
							Request request = null;
							if (mType == Type.LIFO)
								request = mTaskQueue.pollLast();
							else if (mType == Type.FIFO)
								request = mTaskQueue.pollFirst();
							if (request == null)
								return;
							mThreadSemaphore.acquire();
							mThreadPool.execute(buildTask(request));
						} catch (EmptyStackException e) {
						} catch (InterruptedException e) {
						}
					}
				};
				mSemaphore.release();
				Looper.loop();
			}
		});
		mTaskQueue = new LinkedList<Request>();
		mImageCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {

			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}

		};
		mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		mThreadSemaphore = new Semaphore(THREAD_POOL_SIZE);
		mUIHandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					// 获取得到图片，为imageview回调设置图片
					Request holder = (Request) msg.obj;
					Bitmap bm = holder.getBitmap();
					ImageView imageview = holder.getTargetView();
					String path = holder.getUrl();
					// 将path与getTag存储路径进行比较
					if (imageview.getTag().toString().equals(path)) {
						imageview.setImageBitmap(bm);
					}
				} catch (NullPointerException e) {
					Log.w(getClass().getSimpleName(), "mUIHandler:NullPointer");
				}

			};
		};
	}

	private void performRefersh(final Request rq) {
		// 当按back键退出程序后，再从后台管理界面进入程序，使用view.post发现ImageView没有刷新图片，因此使用Handler，具体原因还不清楚
		// if (bitmap == null)
		// return;
		// if (((String) rq.targetView.getTag()).equals(rq.url)) {
		// rq.targetView.post(new Runnable() {
		// @Override
		// public void run() {
		// rq.targetView.setImageBitmap(bitmap);
		// }
		// });
		// }
		Message message = Message.obtain();
		rq.setBitmap(Utils.getDiffSizeBitmap(rq.getType(), rq.getBitmap()));
		message.obj = rq;

		mUIHandler.sendMessage(message);
	}

	public synchronized void loadImage(Context context, String url, ImageView targetView) {
		try {
			mContext = context;
			targetView.setTag(url);
			Request request = new Request(url, targetView);
			boolean isExist = mTaskQueue.contains(request);
			// 如果已经存在相同请求，那么把请求置顶
			if (!isExist) {
				mTaskQueue.add(request);
				try {
					if (mHandler == null) {
						mSemaphore.acquire();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				mHandler.sendEmptyMessage(0);
			} else {
				mTaskQueue.remove(request);
				if (mType == Type.FIFO)
					mTaskQueue.add(0, request);
				else if (mType == Type.LIFO)
					mTaskQueue.add(request);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	private Runnable buildTask(final Request request) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					final Bitmap bitmap = mImageCache.get(request.getUrl());
					// step1:查看缓存
					if (bitmap != null) {
						request.setBitmap(bitmap);
						performRefersh(request);
					} else {
						Log.i(getClass().getSimpleName(), "step1:null");
						Bitmap bmp = Utils.getBitmapFromFile(mContext, request.getUrl(), request.getTargetView().getWidth(), request.getTargetView().getHeight());
						if (bmp != null) {
							mImageCache.put(request.getUrl(), bmp);
							request.setBitmap(bmp);
							performRefersh(request);
						} else {// 从网络读取
							Log.i(getClass().getSimpleName(), "step2:null");
							File file = new File(Utils.getCachePath(mContext), Md.MD5(request.getUrl()));
							if (Utils.downloadImgByUrlIntoFile(request.getUrl(), file)) {
								bmp = Utils.getBitmapFromFile(file, request.getTargetView().getWidth(), request.getTargetView().getHeight());
								if (bmp != null) {
									mImageCache.put(request.getUrl(), bmp);
									request.setBitmap(bmp);
									performRefersh(request);
								} else {
									Log.i(getClass().getSimpleName(), "step3:null");
								}
							}
						}
					}

				} catch (Exception e) {
				} finally {
					mThreadSemaphore.release();
				}
			}
		};
	}

}
