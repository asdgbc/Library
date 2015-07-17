package com.chen.library.utils;

import java.io.File;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * 一些系统intent
 * 
 * @author chenxuex
 * 
 */
public class SysIntentUtils {

	/**
	 * 选择2/3G的界面
	 * 
	 * @return
	 */
	public static Intent getMobileNetworksSettingIntent() {
		Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
		ComponentName cName = new ComponentName("com.android.phone", "com.android.phone.Settings");
		intent.setComponent(cName);
		return intent;
	}

	/**
	 * 跳转网络搜索界面
	 * 
	 * @return
	 */
	public static Intent getNetworksOperatorsIntent() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
		return intent;
	}

	/**
	 * 跳转浏览器
	 * 
	 * @param url
	 *            网址
	 * @return
	 */
	public static Intent getBrowserIntent(String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		return intent;
	}

	/**
	 * 拨打电话的intent
	 * 
	 * @param tel
	 *            电话号码
	 * @return
	 */
	public static Intent getPhoneCallIntent(String tel) {
		Uri uri = Uri.parse(tel);
		Intent intent = new Intent(Intent.ACTION_DIAL, uri);
		return intent;
	}

	/**
	 * 调用发送短信的程序
	 * 
	 * @param body
	 * @return
	 */
	public static Intent getSMSIntent(String body) {
		Intent it = new Intent(Intent.ACTION_VIEW);
		it.putExtra("sms_body", body);
		it.setType("vnd.android-dir/mms-sms");
		return it;
	}

	/**
	 * 播放视频或者音频
	 * 
	 * @param path
	 *            绝对路径
	 * @return
	 */
	public static Intent getMediaIntent(String path) {
		Intent it = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse(path);
		it.setDataAndType(uri, "audio/mp3");
		return it;
	}

	/**
	 * 卸载app
	 * 
	 * @param packageName
	 * @return
	 */
	public static Intent getUninstallAppIntent(String packageName) {
		Uri uri = Uri.fromParts("package", packageName, null);
		Intent it = new Intent(Intent.ACTION_DELETE, uri);
		return it;
	}

	/**
	 * 安装app的intent
	 * 
	 * @param filePath
	 *            app安装包绝对路径
	 * @return
	 */
	public static Intent getInstallAppIntent(String filePath) {
		return getInstallAppIntent(new File(filePath));
	}

	/**
	 * 安装app的intent
	 * 
	 * @param file
	 *            app文件
	 * @return
	 */
	public static Intent getInstallAppIntent(File file) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		return intent;
	}

}
