package com.chen.library.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by chenxuex on 2015/6/8.
 */
public class ScaleImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {

	private final static boolean DEBUG = true;

	private int viewHeight;
	private int viewWidth;

	private Matrix mMatrix = new Matrix();

	private float minScale = 1.0f;
	private float midScale = 2.0f;
	private float maxScale = 4.0f;

	// 缩放相关
	private ScaleGestureDetector mScaleGestureDetector;
	private float[] values = new float[9];

	// 移动相关
	private float mLastX;
	private float mLastY;

	// 记录多点触控的点的数量
	private int mLastPointCount;

	// 双击自动缩放
	private GestureDetector mGestureDetector;
	private Timer mTimer;
	private TimerTask mScaleTask;

	private OnClickListener mClickListener;

	public ScaleImageView(Context context) {
		// 必须使用该构造方法，否则如果在代码中初始化一个实例无法正常功能
		this(context, null);
	}

	public ScaleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTimer = new Timer();
		// 设置缩放类型为矩阵类型
		setScaleType(ScaleType.MATRIX);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		setOnTouchListener(this);
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (mScaleTask != null)
					mScaleTask.cancel();
				mScaleTask = null;
				RectF rf = getMatrixRectF(mMatrix);
				if (!rf.contains(e.getX(), e.getY()))
					return true;
				float scale = getScale(mMatrix);
				if (scale < midScale) {
					mScaleTask = new ScaleTask(e.getX(), e.getY(), midScale, true);
				} else if (scale >= midScale && scale < maxScale) {
					mScaleTask = new ScaleTask(e.getX(), e.getY(), maxScale, true);
				} else if (scale == maxScale) {
					mScaleTask = new ScaleTask(e.getX(), e.getY(), minScale, false);
				}
				// mScaleTask
				mTimer.schedule(mScaleTask, 16, 16);
				return true;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (ScaleImageView.this.mClickListener != null) {
					ScaleImageView.this.mClickListener.onClick(ScaleImageView.this);
					return true;
				}
				return super.onSingleTapConfirmed(e);
			}
		});
	}

	class ScaleTask extends TimerTask {
		/**
		 * 放大速率
		 */
		private final static float upRate = 1.07f;
		/**
		 * 缩小速率
		 */
		private final static float downRate = 0.93f;
		float scaleXPos;
		float scaleYPos;
		float finalScale;
		boolean isUp;

		/**
		 * 
		 * @param x
		 *            缩放中心
		 * @param y
		 *            缩放中心
		 * @param finalScale
		 *            最终缩放大小
		 * @param isUp
		 *            指定放大还是缩小，true放大 false缩小
		 */
		protected ScaleTask(float x, float y, float finalScale, boolean isUp) {
			super();
			this.scaleXPos = x;
			this.scaleYPos = y;
			this.finalScale = finalScale;
			this.isUp = isUp;
		}

		@Override
		public void run() {
			ScaleImageView.this.post(new Runnable() {
				@Override
				public void run() {
					float scale = getScale(mMatrix);
					if (scale == finalScale) {
						ScaleTask.this.cancel();
						return;
					}
					if (isUp) {
						if (scale * upRate > finalScale)
							scale = finalScale / scale;
						else
							scale = upRate;
					} else {
						if (scale * downRate < finalScale)
							scale = finalScale / scale;
						else
							scale = downRate;
					}
					mMatrix.postScale(scale, scale, ScaleTask.this.scaleXPos, ScaleTask.this.scaleYPos);
					judgeToTranslate(mMatrix);
					setImageMatrix(mMatrix);
				}
			});
		}
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale(mMatrix);
		float scaleFactor = detector.getScaleFactor();
		if (Float.compare(detector.getScaleFactor(), 1.0f) > 0) {
			if (scale * scaleFactor > maxScale)
				scaleFactor = maxScale / scale;
			else if (scale == maxScale)
				return true;
		} else {
			if (scale * scaleFactor < minScale)
				scaleFactor = minScale / scale;
			else if (scale == minScale)
				return true;
		}

		// postScale,在原有的scale基础上进行缩放，
		// 也就是说下面这段代码执行完后再执行getScale，
		// 那么getScale的值为当前的scale*detector.getScaleFactor
		mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusX());
		judgeToTranslate(mMatrix);
		setImageMatrix(mMatrix);
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {

	}

	@Override
	public void setOnTouchListener(OnTouchListener l) {
		super.setOnTouchListener(l);
	}

	// 防止越界
	private void judgeToTranslate(Matrix m) {
		RectF rf = getMatrixRectF(m);
		float transX = 0;
		float transY = 0;
		if (rf.width() > viewWidth) {
			if (rf.left > 0)
				transX = -rf.left;
			else if (rf.right < viewWidth)
				transX = viewWidth - rf.right;
		} else {
			transX = viewWidth / 2 - (rf.left + rf.right) / 2;
		}
		if (rf.height() > viewHeight) {
			if (rf.top > 0)
				transY = -rf.top;
			else if (rf.bottom < viewHeight)
				transY = viewHeight - rf.bottom;
		} else {
			transY = viewHeight / 2 - (rf.top + rf.bottom) / 2;
		}
		m.postTranslate(transX, transY);
	}

	private RectF getMatrixRectF(Matrix m) {
		Drawable d = getDrawable();
		if (d == null)
			return null;
		RectF rf = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		if (m.mapRect(rf))
			return rf;
		return null;
	}

	private float getScale(Matrix matrix) {
		matrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;
		mScaleGestureDetector.onTouchEvent(event);

		float evgX = 0;
		float evgY = 0;
		for (int i = 0; i < event.getPointerCount(); i++) {
			evgX += event.getX(i);
			evgY += event.getY(i);
		}

		evgX = evgX / event.getPointerCount();
		evgY = evgY / event.getPointerCount();
		// 触控点数量变化，重新设置mLastX和mLastY，防止图片产生瞬移的效果
		if (mLastPointCount != event.getPointerCount()) {
			mLastPointCount = event.getPointerCount();
			mLastX = evgX;
			mLastY = evgY;
		}
		if (DEBUG)
			Log.i(getClass().getSimpleName(), "mLastPointCount:" + mLastPointCount + ";PointerCount=" + event.getPointerCount());
		// 多点触控，当触控点数大于1，增加或者减少触控点都不触发DOWN,UP,CANCEL事件
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			disallowIntercept();
			mLastX = event.getX();
			mLastY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			disallowIntercept();
			mMatrix.postTranslate(evgX - mLastX, evgY - mLastY);
			judgeToTranslate(mMatrix);
			setImageMatrix(mMatrix);
			mLastX = evgX;
			mLastY = evgY;
			break;
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		viewHeight = getMeasuredHeight();
		viewWidth = getMeasuredWidth();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	public void onGlobalLayout() {
		getViewTreeObserver().removeGlobalOnLayoutListener(this);

		Drawable d = getDrawable();
		if (d == null)
			return;

		int bmHeight = d.getIntrinsicHeight();
		int bmWidth = d.getIntrinsicWidth();

		float scale = 0;
		if (Float.compare(viewHeight * 1.0f / viewWidth, bmHeight * 1.0f / bmWidth) >= 0) {
			// 如果不乘1.0F，由于viewWidth和bmWidth都为int数据类型，相除之后返回还是int型，导致scale为0
			scale = viewWidth * 1.0f / bmWidth;
		} else {
			scale = viewHeight * 1.0f / bmHeight;
		}

		minScale = scale;
		midScale = Math.round(minScale) + 2;
		maxScale = midScale + 2;
		mMatrix.postScale(scale, scale);
		mMatrix.postTranslate((viewWidth - bmWidth * scale) / 2, (viewHeight - bmHeight * scale) / 2);
		setImageMatrix(mMatrix);
	}

	private void disallowIntercept() {
		if (getScale(mMatrix) > minScale) {
			try {
				getParent().requestDisallowInterceptTouchEvent(true);
			} catch (NullPointerException e) {

			}
		}
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		this.mClickListener = l;
	}

}
