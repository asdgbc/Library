package com.chen.library.base;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseFragment extends FixedOnActivityResultBugFragment {
	/**
	 * 保存在onDestroyView中remove的view,这样onCreateView的时候不用重新inflater，复用之前的view 这种情况只在attach和detach下，hide和show不用
	 */
	private View mPreView;
	protected BaseActivity mBaseActivity;
	protected BaseApplication myApplication;
	private Toast mToast;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mBaseActivity = (BaseActivity) activity;
		myApplication = (BaseApplication) mBaseActivity.getApplication();
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mPreView == null) {
			return createView(inflater, container, savedInstanceState);
		} else {
			// 由于这里直接返回，createView中的一些刷新数据的操作将不会执行
			return mPreView;
		}
	}

	protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	@Override
	public final void onViewCreated(View view, Bundle savedInstanceState) {
		getIntentData();
		initViews(view);
		addListeners();
		requestonViewCreated();
	}
	/**
	 * 传值
	 */
	protected abstract void getIntentData();

	/**
	 * 添加监听器
	 */
	protected abstract void addListeners();

	/**
	 * 初始化本地数据
	 */
	protected abstract void initViews(View view);

	/**
	 * 在onCreate中请求服务
	 */
	protected abstract void requestonViewCreated();

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
			mToast = Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT);
		} else {
			mToast.cancel();
			mToast = Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	public void setText(TextView textView, String destination) {
		setText(textView, destination, "");
	}

	public void setText(TextView textView, String destination, String defult) {
		textView.setText(destination == null ? defult : destination.toString().trim());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPreView = getView();
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

}
