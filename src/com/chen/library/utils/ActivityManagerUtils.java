package com.chen.library.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

/**
 * 
 * @author liulj
 * 
 */
public class ActivityManagerUtils {
	/**
	 * 判断当前应用是否在所有任务的最前台
	 * 
	 * @param packageName
	 * @param context
	 * @return
	 */
	public static boolean isMyApplicationRunning(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// 获取当前活动的task栈
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo != null && tasksInfo.size() > 0) {
			if (context.getApplicationInfo().packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断当前运行在前台的任务栈中的最前端activity
	 * 
	 * @param context
	 * @param className
	 * @return
	 */
	public static boolean isTopActivity(Context context, String className) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo != null && tasksInfo.size() > 0) {
			if (className.equals(tasksInfo.get(0).topActivity.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断当前运行在前台的任务栈中的最前端activity是否在以下列表中
	 * 
	 * @param context
	 * @param className
	 * @return
	 */
	public static boolean isTargetActivityOnTop(Context context, List<String> nameList) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo != null && tasksInfo.size() > 0) {
			for (String classname : nameList) {
				if (classname == tasksInfo.get(0).topActivity.getClassName()) {
					return true;
				}
			}
		}
		return false;
	}
}
