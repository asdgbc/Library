package com.chen.library.view;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

public class PickerView extends View {

	public static final boolean DEBUG = true;

	// 数据源
	private List<String> mDataSource;

	private int mViewHeight;
	private int mViewWidth;

	private int mTextSize = 90;

	private Paint mPaint = new Paint();

	// 滑动事件产生的位移偏移量
	private float mOffSet;
	// 第一次按下的纵向位置
	private float mDownY;
	// 位于View中央的Item的index
	private int mCenterItem = 0;
	// 自动补偿的步长
	private int mStep = 5;
	// 自动居中的判断系数，
	private float mScrollBackRate = 0.25f;

	private OnSelectChangedListener mOnSelectChangedListener = null;
	private GestureDetectorCompat mDetector = null;
	private Scroller mScroller = null;

	/**
	 * 惯性滑动结束，通过该变量模拟一次up事件
	 */
	private boolean isUp = false;
	/**
	 * 当没有产生惯性滑动时，通过该变量捕捉ontouch方法中的up事件
	 */
	private boolean isSup = true;

	private Timer timer = new Timer();
	private TimerTask task = null;

	private class RefershTask extends TimerTask {

		@Override
		public void run() {
			if (Float.compare(mOffSet, 0.0f) == 0) {// 如果当前偏移量等于0，取消刷新任务
				this.cancel();
				return;
			} else if (Float.compare(mOffSet, 0.0f) > 0) {// 当前偏移量大于0
				if (mOffSet > mTextSize) {// 累加mOffSet，步长mStep一旦超过文字限制高度，则置为0
					if (DEBUG) {
						Log.e(getClass().getName(), "set 0");
					}
					this.cancel();
					mOffSet = 0;
					mCenterItem = cyclicReduce(mDataSource, mCenterItem);
					performSelect(mCenterItem);
				} else if (mOffSet <= mStep) {
					this.cancel();
					mOffSet = 0;
					performSelect(mCenterItem);
				} else if (mOffSet > mScrollBackRate * mTextSize) {// 偏移量不足单位文字高度则累加
					mOffSet += mStep;
				} else if (mOffSet <= mScrollBackRate * mTextSize) {
					mOffSet -= mStep;
				}
				PickerView.this.postInvalidate();
			} else if (Float.compare(mOffSet, 0.0f) < 0) {// 当前偏移量小于0
				if (mOffSet < -mTextSize) {// 累减mOffSet，步长mStep一旦超过文字限制高度，则置为0
					this.cancel();
					if (DEBUG) {
						Log.e(getClass().getName(), "set 0");
					}
					mOffSet = 0;
					mCenterItem = cyclicAdd(mDataSource, mCenterItem);
					performSelect(mCenterItem);
				} else if (mOffSet >= -mStep) {
					this.cancel();
					mOffSet = 0;
					performSelect(mCenterItem);
				} else if (mOffSet < -mScrollBackRate * mTextSize) {// 偏移量不足单位文字高度则累减
					mOffSet -= mStep;
				} else if (mOffSet >= -mScrollBackRate * mTextSize) {
					mOffSet += mStep;
				}
				PickerView.this.postInvalidate();
			}
		}
	}

	public PickerView(Context context) {
		super(context);
		init();
	}

	public PickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize(mTextSize);
		mDetector = new GestureDetectorCompat(getContext(), new ScrollGestureListener());
		mScroller = new Scroller(getContext());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if (isSup) {
				scrollToCenter();
			}
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mViewHeight = getMeasuredHeight();
		mViewWidth = getMeasuredWidth();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawText(canvas);
	}

	class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			mOffSet = e2.getY() - mDownY;
			invalidate();
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			isSup = false;
			if (Float.compare(velocityY, 0.0f) == 1) {// 大于0
				mScroller.fling(0, (int) mOffSet, (int) velocityX, (int) velocityY, 0, 0, (int) mOffSet, (int) (mOffSet + 4000));
			} else if (Float.compare(velocityY, 0.0f) == -1) {
				mScroller.fling(0, (int) mOffSet, (int) velocityX, (int) velocityY, 0, 0, (int) (mOffSet - 4000), (int) mOffSet);
			}
			// 有时候不会执行computeScroll方法，导致无法执行item居中的方法，所以强制刷新
			invalidate();
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			isSup = true;
			mDownY = e.getY();
			mScroller.forceFinished(true);
			if (task != null) {
				task.cancel();
			}
			return true;
		}

	}

	@SuppressLint("NewApi")
	@Override
	public void computeScroll() {
		super.computeScroll();
		if (DEBUG) {
			Log.i(getClass().getSimpleName(), "computeScroll");
		}
		if (mScroller.computeScrollOffset()) {
			if (!isUp) {
				isUp = !isUp;
			}
			if (DEBUG) {
				Log.d(getClass().getName(), mScroller.getCurrY() + ":" + mOffSet + ":" + mScroller.getCurrVelocity());
			}
			mOffSet = mScroller.getCurrY();
			invalidate();
		} else if (isUp) {
			isUp = !isUp;
			scrollToCenter();
		}
	}

	/**
	 * 当惯性滑动停止，调用该方法使文本继续滑动至垂直居中
	 */
	private void scrollToCenter() {
		if (Math.abs(mOffSet) >= mTextSize) {
			int x = (int) mOffSet / mTextSize;
			if (x > 0)
				mCenterItem = cyclicReduce(mDataSource, mCenterItem, x);
			else
				mCenterItem = cyclicAdd(mDataSource, mCenterItem, x);
			mOffSet = (mOffSet / Math.abs(mOffSet)) * (Math.abs(mOffSet) - mTextSize * Math.abs(x));
			invalidate();
		}
		task = new RefershTask();
		timer.schedule(task, 16, 16);
	}

	/**
	 * 绘制文本
	 * 
	 * @param canvas
	 */
	private void drawText(Canvas canvas) {
		if (this.mDataSource == null || this.mDataSource.size() == 0) {
			return;
		}

		// Step1:绘制位于View中间的文字
		// 该方法中的x、y是以文字左下角为基准所以当设置为0,0时是看不到文字的
		canvas.drawText(this.mDataSource.get(mCenterItem), getXposition(this.mDataSource.get(mCenterItem)), getMiddleYByTextSize() + mOffSet, mPaint);

		canvas.drawLine(0, mViewHeight / 2 + mTextSize / 2, mViewWidth, mViewHeight / 2 + mTextSize / 2, mPaint);
		canvas.drawLine(0, mViewHeight / 2 - mTextSize / 2, mViewWidth, mViewHeight / 2 - mTextSize / 2, mPaint);

		// Step2:向上绘制文字
		int curItem = cyclicReduce(mDataSource, mCenterItem);
		for (float i = getMiddleYByTextSize() + mOffSet - mTextSize; i > -Math.abs(mTextSize); i -= mTextSize, curItem = cyclicReduce(mDataSource, curItem)) {
			canvas.drawText(this.mDataSource.get(curItem), getXposition(this.mDataSource.get(curItem)), i, mPaint);
		}

		// Step3:向下绘制
		curItem = cyclicAdd(mDataSource, mCenterItem);
		for (float i = getMiddleYByTextSize() + mOffSet + mTextSize; i < mViewHeight + mTextSize; i += mTextSize, curItem = cyclicAdd(mDataSource, curItem)) {
			canvas.drawText(this.mDataSource.get(curItem), getXposition(this.mDataSource.get(curItem)), i, mPaint);
		}
	}

	/**
	 * 根据数据源的size循环加1
	 * 
	 * @param data
	 * @param x
	 * @param step
	 *            步长
	 * @return
	 */
	private int cyclicAdd(List<?> data, int x, int step) {
		step = Math.abs(step);
		int result = x;
		for (int i = 0; i < step; i++) {
			result = cyclicAdd(data, result);
		}
		return result;
	}

	/**
	 * 根据数据源的size循环加1
	 * 
	 * @param x
	 * @return
	 */
	private int cyclicAdd(List<?> data, int x) {
		if (data == null || data.size() == 0) {
			return 0;
		}
		return ++x >= data.size() ? 0 : x;
	}

	/**
	 * 根据数据源的size循环减1
	 * 
	 * @param data
	 * @param x
	 * @param step
	 *            步长
	 * @return
	 */
	private int cyclicReduce(List<?> data, int x, int step) {
		step = Math.abs(step);
		int result = x;
		for (int i = 0; i < step; i++) {
			result = cyclicReduce(data, result);
		}
		return result;
	}

	/**
	 * 根据数据源的size循环减1
	 * 
	 * @param x
	 * @return
	 */
	private int cyclicReduce(List<?> data, int x) {
		if (data == null || data.size() == 0) {
			return 0;
		}
		return --x < 0 ? data.size() - 1 : x;
	}

	/**
	 * 设置数据源
	 * 
	 * @param data
	 */
	public void setData(List<String> data) {
		this.mDataSource = data;
	}

	/**
	 * 计算能将文字绘制程纵向居中的Y
	 * 
	 * @return
	 */
	private float getMiddleYByTextSize() {
		FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
		return (mViewHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
	}

	/**
	 * 根据文字内容计算X位置，使text处于view中间
	 * 
	 * @param text
	 * @return
	 */
	private float getXposition(String text) {
		return (mViewWidth - getTextLength(text)) / 2;
	}

	/**
	 * 计算文本长度
	 * 
	 * @param text
	 * @return
	 */
	private float getTextLength(String text) {
		return mPaint.measureText(text);
	}

	/**
	 * 执行回调
	 * 
	 * @param index
	 */
	private void performSelect(final int index) {
		PickerView.this.post(new Runnable() {
			@Override
			public void run() {
				if (PickerView.this.mOnSelectChangedListener != null) {
					PickerView.this.mOnSelectChangedListener.onChanged(index);
				}
			}
		});
	}

	/**
	 * 设置监听
	 * 
	 * @param listener
	 */
	public void setOnSelectedChangedListener(OnSelectChangedListener listener) {
		this.mOnSelectChangedListener = listener;
	}

	/**
	 * 回调接口
	 * 
	 * @author chenxuex
	 * 
	 */
	public interface OnSelectChangedListener {
		public void onChanged(int index);
	}

}
