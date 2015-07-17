package com.chen.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.librarychen.R;

/**
 * 一個簡單的圓角進度條 支持橫向和縱向，只需控制width和height 就可以實現縱向和橫向的切換
 * 
 * @author chenxuex
 * 
 */
public class RoundCornorProgressBar extends View {

	private enum Direction {
		Horizontal, VERTICAL
	}

	private int what;

	private int mHeight;
	private int mWidth;

	private Paint mPaint;
	private Direction mDirection;

	private static int Max_Progress = 100;
	private int mTargetProgress;
	private int mCurProgress;

	private Handler mHandler;
	private RectF mRect;
	private RectF mProRect;

	// attrs
	private int backgroundColor;
	private int progressColor;

	public RoundCornorProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);// 充满
		mPaint.setAntiAlias(true);// 设置画笔的锯齿效果

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerProgressBar);
		backgroundColor = typedArray.getColor(R.styleable.RoundCornerProgressBar_BackGround, Color.parseColor("#e5e5e5"));
		progressColor = typedArray.getColor(R.styleable.RoundCornerProgressBar_ProgressColor, Color.RED);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mHeight = getMeasuredHeight();
		mWidth = getMeasuredWidth();
		mRect = new RectF(0, 0, mWidth, mHeight);
		mProRect = new RectF(mRect);
		judgeDirection();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// draw background
		mPaint.setStyle(Paint.Style.FILL);// 充满
		mPaint.setColor(backgroundColor);
		if (mDirection == Direction.VERTICAL) {
			canvas.drawRoundRect(mRect, mWidth / 2, mWidth / 2, mPaint);//
		} else {
			canvas.drawRoundRect(mRect, mHeight / 2, mHeight / 2, mPaint);//
		}
		drawProgress(canvas);

	}

	private void judgeDirection() {
		if (mWidth > mHeight) {
			mDirection = Direction.Horizontal;
		} else {
			mDirection = Direction.VERTICAL;
		}
	}

	public void setProgress(int i) {
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {

					try {
						if (mTargetProgress > mCurProgress) {
							if (++mCurProgress <= mTargetProgress) {
								invalidate();
								mHandler.sendEmptyMessageDelayed(what, 16);
							}
						} else if (mTargetProgress < mCurProgress) {
							if (--mCurProgress >= mTargetProgress) {
								invalidate();
								mHandler.sendEmptyMessageDelayed(what, 16);
							}
						}
					} catch (Exception e) {

					}
				}
			};
		}
		if (mTargetProgress == mCurProgress) {
			this.mTargetProgress = (i >= 0 && i <= Max_Progress) ? i : (i < 0 ? 0 : 100);
			mHandler.sendEmptyMessageDelayed(what, 16);
		} else {
			this.mTargetProgress = (i >= 0 && i <= Max_Progress) ? i : (i < 0 ? 0 : 100);
		}

	}

	public int getProgress() {
		return mTargetProgress > mCurProgress ? mTargetProgress : mCurProgress;
	}

	@Override
	protected void onDetachedFromWindow() {
		//不知道為什麼在退出Activity的時候，onDetachedFromWindow會被多次調用，導致出現空指針異常
		try {
			mHandler.removeMessages(what);
		} catch (NullPointerException e) {

		}
		super.onDetachedFromWindow();
	}

	private void drawProgress(Canvas canvas) {
		mPaint.setColor(progressColor);
		if (mDirection == Direction.Horizontal) {
			mProRect.set(0, 0, mWidth, mHeight);
			float right = mCurProgress * 1.0f / Max_Progress * mWidth;
			mProRect.right = right;
			if (right >= mHeight) {
				canvas.drawRoundRect(mProRect, mHeight / 2, mHeight / 2, mPaint);// 第二个参数是x半径，第三个参数是y半径
			} else {
				mProRect.set(0, 0, mHeight, mHeight);
				right /= 2;
				float r = mHeight / 2.0f;
				float degree = (float) Math.toDegrees(Math.acos((r - right) / r) * 2);
				Log.i(getClass().getSimpleName(), degree + "");
				canvas.drawArc(mProRect, 180 - degree / 2, degree, false, mPaint);
				// 如果不-1的話，中間會有一條線
				mProRect.set(-(mHeight - 2 * right) - 1, 0, 2 * right - 1, mHeight);
				canvas.drawArc(mProRect, -degree / 2, degree, false, mPaint);
			}

		} else {
			mProRect.set(0, 0, mWidth, mHeight);
			float top = (Max_Progress - mCurProgress) * 1.0f / Max_Progress * mHeight;
			mProRect.top = top;
			if ((mHeight - top) >= mWidth) {
				canvas.drawRoundRect(mProRect, mWidth / 2, mWidth / 2, mPaint);// 第二个参数是x半径，第三个参数是y半径)
			} else {
				mProRect.set(0, mHeight - mWidth, mWidth, mHeight);
				float h = (mHeight - top) / 2.0f;
				float r = mWidth / 2.0f;
				float degree = (float) Math.toDegrees(Math.acos((r - h) / r) * 2);
				canvas.drawArc(mProRect, 90 - degree / 2, degree, false, mPaint);
				// 如果不+1的話，中間會有一條線
				mProRect.set(0, mHeight - 2 * h + 1, mWidth, mHeight + mWidth - 2 * h + 1);
				canvas.drawArc(mProRect, -90 - degree / 2, degree, false, mPaint);
			}
		}

	}

}
