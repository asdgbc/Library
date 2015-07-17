package com.chen.library.base;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.chen.library.common.Setting;
import com.example.librarychen.R;

public class BaseApplication extends Application {
	public static final String TAG = "carwahing";
	/**
	 * 存放activity的集合
	 */
	public static List<Activity> mActivityList = new ArrayList<Activity>();
	/**
	 * 捕捉全局弹出的dialog
	 */
	private AlertDialog dialog;

	/**
	 * 程序启动时的处理
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {

		super.onCreate();

		// 出现应用级异常时的处理
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			String errMsg = "";

			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				throwable.printStackTrace(pw);
				errMsg = sw.toString();
				new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						if (mActivityList.size() > 0) {

							new AlertDialog.Builder(getCurrentActivity()).setTitle(R.string.app_name).setMessage(R.string.err_msg)
									.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											// 强制退出程序
											finish();
										}
									}).setCancelable(false).show();

						} else {
							Log.e(TAG, errMsg);
							finish();
						}
						Looper.loop();
					}
				}).start();

				// 错误LOG
				Log.e(TAG, throwable.getMessage(), throwable);
			}
		});

		// 初始化信息
		initData();
	}

	/**
	 * 初始化信息
	 */
	private void initData() {
		// 获得屏幕高度（像素）
		Setting.DISPLAY_HEIGHT = getResources().getDisplayMetrics().heightPixels;
		// 获得屏幕宽度（像素）
		Setting.DISPLAY_WIDTH = getResources().getDisplayMetrics().widthPixels;
		// 获得系统状态栏高度（像素）
		Setting.STATUS_BAR_HEIGHT = getStatusBarHeight();
		// 文件路径设置
		String parentPath = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			parentPath = getExternalCacheDir().getPath();
		} else {
			parentPath = getCacheDir().getPath();
		}
		// 临时文件路径设置
		Setting.TEMP_PATH = parentPath + File.separator + "tmp";
		// 图片缓存路径设置
		Setting.PIC_PATH = parentPath + File.separator + "pic";
		// 更新APK路径设置
		Setting.APK_PATH = parentPath + File.separator + "apk";
		// 创建各目录
		new File(Setting.TEMP_PATH).mkdirs();
		new File(Setting.PIC_PATH).mkdirs();
		new File(Setting.APK_PATH).mkdirs();

	}

	public String getUserAgent() {
		WebView webview;
		webview = new WebView(getApplicationContext());
		WebSettings settings = webview.getSettings();
		String ua = settings.getUserAgentString();
		return ua;
	}

	/**
	 * 获得当前最顶层的activity
	 * 
	 * @return 当前最顶层的activity
	 */
	public Activity getCurrentActivity() {
		if (mActivityList.size() >= 1) {
			return mActivityList.get(mActivityList.size() - 1);
		}
		return null;
	}

	/**
	 * 生成Activity存入列表
	 * 
	 * @param activity
	 */
	public void addCurrentActivity(Activity activity) {
		if (activity != null)
			mActivityList.add(activity);
	}

	/**
	 * 移除当前的activity
	 * 
	 * @param activity
	 */
	public void removeCurrentActivity(Activity activity) {
		if (activity != null)
			mActivityList.remove(activity);
	}

	/**
	 * 获得顶层下面的activity
	 * 
	 * @return
	 */
	public Activity getPreviousActivity() {
		if (mActivityList.size() >= 2) {
			return mActivityList.get(mActivityList.size() - 2);
		}
		return null;
	}

	/**
	 * 清除最上层以下所有的activity
	 */
	public void clearBottomActivities() {
		if (mActivityList.size() >= 1) {
			Activity lastActivity = mActivityList.get(mActivityList.size() - 1);
			for (int i = 0; i < mActivityList.size() - 1; i++) {
				Activity activity = mActivityList.get(i);
				if (activity != null)
					activity.finish();
			}
			mActivityList.clear();
			mActivityList.add(lastActivity);
		}
	}

	/**
	 * 清除上上个activity，其他保留，用于login后的passwd的finish（）
	 */
	public void clearBottomActivitie() {
		if (mActivityList.size() >= 2) {
			mActivityList.get(mActivityList.size() - 2).finish();
		}
	}

	/**
	 * 清除所有的activity
	 */
	public void removeAllActivity() {
		for (int i = 0; i < mActivityList.size(); i++) {
			Activity activity = mActivityList.get(i);
			if (activity != null)
				activity.finish();
		}
		mActivityList.clear();
	}

	public void finish() {
		if (dialog != null)
			dialog.dismiss();
		removeAllActivity();
		System.exit(0);
	}

	/**
	 * 获取手机状态栏高度
	 * 
	 * @return 手机状态栏高度
	 */
	private int getStatusBarHeight() {
		try {
			Class<?> cls = Class.forName("com.android.internal.R$dimen");
			Object obj = cls.newInstance();
			Field field = cls.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());
			return getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
		}
		return 0;
	}
}
