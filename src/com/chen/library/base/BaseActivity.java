package com.chen.library.base;

import com.chen.library.utils.NetUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseActivity extends FragmentActivity {
	public BaseApplication myApplication;
	// public LoadingDialog mLoadingDialog;// 普通加载对话框
	public BaseActivity mBaseActivity;
	public SharedPreferences mPreferences;
	public Object mUserInfoVo;
	private Toast mToast;

	@Override
	protected final void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mBaseActivity = this;
		// 获得MyApplication实例，也可直接静态调用
		myApplication = (BaseApplication) getApplication();
		// 将activity添加到集合中
		myApplication.addCurrentActivity(this);
		// loading对话框
		// mLoadingDialog = new LoadingDialog(this, true);
		getIntentData();
		findViews(arg0);
		addListeners();
		initViews();
		requestOnCreate();
	}

	/**
	 * 判断是否是空字符串
	 * 
	 * @param str
	 * @return
	 */
	public boolean isEmpty(String str) {
		if (str != null && str.length() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * String转int
	 */
	public int String2Int(String str) {
		int num = 0;
		try {
			num = Integer.parseInt(str);
		} catch (Exception e) {

		}
		return num;
	}

	/**
	 * String转float
	 */
	public float String2Float(String str) {
		float num = 0;
		try {
			num = Float.parseFloat(str);
		} catch (Exception e) {

		}
		return num;
	}

	/**
	 * String转double
	 */
	public double String2Double(String str) {
		double num = 0;
		try {
			num = Double.parseDouble(str);
		} catch (Exception e) {

		}
		return num;
	}

	/**
	 * String转long
	 */
	public long String2Long(String str) {
		long num = 0;
		try {
			num = Long.parseLong(str);
		} catch (Exception e) {

		}
		return num;
	}

	/**
	 * 判断网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public boolean isNetworkAvailable() {
		return NetUtils.isNetWorkConnected(mBaseActivity);
	}

	/**
	 * 共通的toast
	 * 
	 * @param content
	 */
	public void showToast(String content) {
		if (content == null) {
			content = "";
		}
		if (mToast == null) {
			mToast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
		} else {
			mToast.cancel();
			mToast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	public void setText(TextView textView, String destination) {
		setText(textView, Html.fromHtml(destination).toString(), "");
	}

	public void setText(TextView textView, String destination, String defult) {
		textView.setText(destination == null ? defult : destination);
	}
	
	public void goActivity(Class<?> cls)
	{
		Intent intent = new Intent(mBaseActivity, cls);
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 将activity从集合中移除
		myApplication.removeCurrentActivity(this);
	}

	/**
	 * 验证是否登录
	 * 
	 * @param view
	 *            点击的view 防止连续点击弹出多个对话框
	 * @return 已经登录返回true 游客登录返回false
	 */
	public boolean checkLogin(final View view) {
		return false;
	}
	
	/**
	 * 获取界面传递数据
	 */
	protected abstract void getIntentData();

	/**
	 * 初始化布局中的空间，首先要调用setContentView
	 */
	protected abstract void findViews(Bundle savedInstanceState);

	/**
	 * 添加监听器
	 */
	protected abstract void addListeners();

	/**
	 * 初始化本地数据
	 */
	protected abstract void initViews();

	/**
	 * 在onCreate中请求服务
	 */
	protected abstract void requestOnCreate();
	
}
