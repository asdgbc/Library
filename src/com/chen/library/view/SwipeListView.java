package com.chen.library.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import com.chen.library.utils.DensityUtils;
import com.example.librarychen.R;

/**
 * 在安卓2.3.5下，headerview使用FrameLayout会有问题
 * 当设置margintop为完全不可见时，还是会显示headerview
 * 改用linearlayout
 * @author chenxuex
 *
 */
public class SwipeListView extends ListView {
	private Boolean mIsHorizontal;

	public View mPreItemView;

	private View mCurrentItemView;

	private int itemPosition;

	private float mFirstX;

	private float mFirstY;

	private int mRightViewWidth;

	// private boolean mIsInAnimation = false;
	private final int mDuration = 100;

	private final int mDurationStep = 10;

	public boolean mIsShown;

	private int minimunFreshTime = 200;
	private long freshTime = 0;
	
	// 下拉刷新相关
	private View mHeadView = null;
	private TextView mHeadContent = null;
	private ImageView mHeadImage = null;
	private LinearLayout mHeadRoot = null;
	private int mHeadViewHeight;
	private boolean isRefreshing = false;
	float pX = 0;
	float pY = 0;
	private OnRefershListener refershListener = null;
	private AnimationDrawable adrawable = null;
	private Scroller mScroller = null;

	public SwipeListView(Context context) {
		this(context, null);
	}

	public SwipeListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.swipelistviewstyle);

		// 获取自定义属性和默认值
		mRightViewWidth = (int) mTypedArray.getDimension(R.styleable.swipelistviewstyle_right_width, 200);
		mTypedArray.recycle();

		mScroller = new Scroller(context);
		mHeadView = LayoutInflater.from(context).inflate(R.layout.listheader_layout, null);
		
		mHeadImage = (ImageView) mHeadView.findViewById(R.id.img);
		mHeadContent = (TextView) mHeadView.findViewById(R.id.tv);
		
		mHeadContent.setText("下拉即可刷新数据");
		
		mHeadRoot = (LinearLayout) mHeadView.findViewById(R.id.root);
		
		mHeadImage.setImageResource(R.drawable.set_animation);
		adrawable = (AnimationDrawable) mHeadImage.getDrawable();
		mHeadViewHeight = DensityUtils.dp2px(context, 70);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, mHeadViewHeight);
		lp.topMargin = -mHeadViewHeight;
		mHeadRoot.setLayoutParams(lp);
		addHeaderView(mHeadView, null, false);
	}

	public void setRefershing() {
		freshTime = System.currentTimeMillis();
		setSelection(0);
		isRefreshing = true;
		mScroller.abortAnimation();
		mScroller.startScroll(0, 0, 0, mHeadViewHeight, 500);
		if (this.refershListener!=null) {
			this.refershListener.onRefersh();
		}
		adrawable.start();
		mHeadContent.setText("正在加载……");
	}

	public void compeleteRefershing() {
		if (freshTime!=0&&System.currentTimeMillis()-freshTime<minimunFreshTime) {
			this.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					compeleteRefershing();
				}
			}, minimunFreshTime);
			return;
		}
		freshTime = 0;
		setSelection(0);
		isRefreshing = false;
		mScroller.abortAnimation();
		mScroller.startScroll(0, mHeadViewHeight + ((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin, 0, -mHeadViewHeight
				- ((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin, 500);
		adrawable.stop();
		mHeadContent.setText("下拉即可刷新数据");
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin = -(mHeadViewHeight - mScroller.getCurrY());
			mHeadView.requestLayout();
		}
	}

	/**
	 * return true, deliver to listView. return false, deliver to child. if move, return true
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float lastX = ev.getX();
		float lastY = ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsHorizontal = null;
			System.out.println("onInterceptTouchEvent----->ACTION_DOWN");
			mFirstX = lastX;
			mFirstY = lastY;
			itemPosition = pointToPosition((int) mFirstX, (int) mFirstY);
			if (itemPosition > 0) {
				View currentItemView = getChildAt(itemPosition - getFirstVisiblePosition());
				mPreItemView = mCurrentItemView;
				System.out.println("mPreItemView==" + mPreItemView);
				mCurrentItemView = currentItemView;
			} else if (itemPosition < 0 && mIsShown) {
			}
			break;

		case MotionEvent.ACTION_MOVE:
			float dx = lastX - mFirstX;
			float dy = lastY - mFirstY;

			if (Math.abs(dx) >= 5 && Math.abs(dy) >= 5) {
				return true;
			}
			break;

		case MotionEvent.ACTION_UP:
			System.out.println("onInterceptTouchEvent----->ACTION_UP");
		case MotionEvent.ACTION_CANCEL:
			System.out.println("onInterceptTouchEvent----->ACTION_CANCEL");
			if (mIsShown && (mPreItemView != mCurrentItemView || isHitCurItemLeft(lastX))) {
				System.out.println("1---> hiddenRight");
				/**
				 * 情况一：
				 * <p>
				 * 一个Item的右边布局已经显示，
				 * <p>
				 * 这时候点击任意一个item, 那么那个右边布局显示的item隐藏其右边布局
				 */
				hiddenRight(mPreItemView);
			} else if (itemPosition < 0) {
				// Hid
			}
			break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	private boolean isHitCurItemLeft(float x) {
		return x < getWidth() - mRightViewWidth;
	}

	/**
	 * @param dx
	 * @param dy
	 * @return judge if can judge scroll direction
	 */
	private boolean judgeScrollDirection(float dx, float dy) {
		boolean canJudge = true;

		if (Math.abs(dx) > 30 && Math.abs(dx) > 2 * Math.abs(dy)) {
			mIsHorizontal = true;
			System.out.println("mIsHorizontal---->" + mIsHorizontal);
		} else if (Math.abs(dy) > 30 && Math.abs(dy) > 2 * Math.abs(dx)) {
			mIsHorizontal = false;
			System.out.println("mIsHorizontal---->" + mIsHorizontal);
		} else {
			canJudge = false;
		}

		return canJudge;
	}

	/**
	 * return false, can't move any direction. return true, cant't move vertical, can move horizontal. return super.onTouchEvent(ev), can move both.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		float lastX = ev.getX();
		float lastY = ev.getY();

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("onTouchEvent============ACTION_DOWN");
			if (mIsShown) {
				return true;
			} else {
				break;
			}
		case MotionEvent.ACTION_MOVE:
			float dx = lastX - mFirstX;
			float dy = lastY - mFirstY;
			// confirm is scroll direction
			if (mIsHorizontal == null) {
				if (!judgeScrollDirection(dx, dy)) {
					break;
				}
			}
			if (mIsHorizontal) {
				if (mIsShown && mPreItemView != mCurrentItemView) {
					System.out.println("2---> hiddenRight");
					/**
					 * 情况二：
					 * <p>
					 * 一个Item的右边布局已经显示，
					 * <p>
					 * 这时候左右滑动另外一个item,那个右边布局显示的item隐藏其右边布局
					 * <p>
					 * 向左滑动只触发该情况，向右滑动还会触发情况五
					 */
					hiddenRight(mPreItemView);
				}

				if (mIsShown && mPreItemView == mCurrentItemView) {
					dx = dx - mRightViewWidth;
					System.out.println("======dx " + dx);
				}

				// can't move beyond boundary
				if (dx < 0 && dx > -mRightViewWidth && mCurrentItemView != null && itemPosition > 0) {
					mCurrentItemView.scrollTo((int) (-dx), 0);
					clearPressedState();
				}
				return true;
			} else {
				if (mIsShown) {
					System.out.println("3---> hiddenRight");
					/**
					 * 情况三：
					 * <p>
					 * 一个Item的右边布局已经显示，
					 * <p>
					 * 这时候上下滚动ListView,那么那个右边布局显示的item隐藏其右边布局
					 */
					hiddenRight(mPreItemView);
				}

				if (getFirstVisiblePosition() == 0 && Math.abs(((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin) == mHeadViewHeight && pY == 0) {
					pX = ev.getX();
					pY = ev.getY();
				}
				if (((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin > 0 && !isRefreshing) {
					mHeadContent.setText("放开即可刷新数据");
				} else if (((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin < 0 && !isRefreshing) {
					mHeadContent.setText("下拉即可刷新数据");
				}
				if (pY != 0) {
					int visiableHeight = (int) (ev.getY() - pY);
//					if (Math.abs(-(mHeadViewHeight - visiableHeight)) <= 2 * mHeadViewHeight) {
						((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin = (int) -(mHeadViewHeight - visiableHeight / 2);
						requestLayout();
//					}
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			pY = 0;
			final int margin = ((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin;
			System.out.println("onTouchEvent============ACTION_UP");
			if (((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin > 0) {
				
				mHeadContent.setText("正在加载……");
				adrawable.start();
				isRefreshing = true;
				mScroller.abortAnimation();
				mScroller.startScroll(0, margin + mHeadViewHeight, 0, -margin,
						500);
				if (this.refershListener != null) {
					this.refershListener.onRefersh();
				}else{
					postDelayed(new Runnable() {
						
						@Override
						public void run() {
							mScroller.abortAnimation();
							mScroller.startScroll(0, margin, 0, -margin, 500);
						}
					}, 500);
				}
			} else if (!isRefreshing) {
				mScroller.abortAnimation();
				mScroller.startScroll(0, mHeadViewHeight + ((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin, 0, -mHeadViewHeight
						- ((LinearLayout.LayoutParams) mHeadRoot.getLayoutParams()).topMargin, 300);
			}
		case MotionEvent.ACTION_CANCEL:
			pY = 0;
			System.out.println("onTouchEvent============ACTION_CANCEL");
			clearPressedState();
			if (mIsShown) {
				if (itemPosition > 0) {
					System.out.println("4---> hiddenRight");
					/**
					 * 情况四：
					 * <p>
					 * 一个Item的右边布局已经显示，
					 * <p>
					 * 这时候左右滑动当前一个item,那个右边布局显示的item隐藏其右边布局
					 */
					hiddenRight(mPreItemView);
				} else {
					System.out.println("4_1---> hiddenRight");
					/**
					 * 情况四：
					 * <p>
					 * 一个Item的右边布局已经显示，
					 * <p>
					 * 这时候左右滑动当前一个item,那个右边布局显示的item隐藏其右边布局
					 */
					hiddenRight(mCurrentItemView);
				}

				return true;
			}
			if (mIsHorizontal != null && mIsHorizontal) {
				if (mFirstX - lastX > mRightViewWidth / 2) {
					showRight(mCurrentItemView);
				} else {
					System.out.println("5---> hiddenRight");
					/**
					 * 情况五：
					 * <p>
					 * 向右滑动一个item,且滑动的距离超过了右边View的宽度的一半，隐藏之。
					 */
					hiddenRight(mCurrentItemView);
				}
				return true;
			}
			break;
		}

		return super.onTouchEvent(ev);
	}

	private void clearPressedState() {
		// System.out.println("=========clearPressedState");
		// TODO current item is still has background, issue
		if (mCurrentItemView != null) {
			mCurrentItemView.setPressed(false);
			mCurrentItemView.setSelected(false);
		}
		setPressed(false);
		setSelected(false);
		refreshDrawableState();
		// invalidate();
	}

	private void showRight(View view) {
		System.out.println("=========showRight  view==" + view + "--mPreItemView==" + mPreItemView + "--mCurrentItemView==" + mCurrentItemView);
		if (view != null && itemPosition > 0) {
			Message msg = new MoveHandler().obtainMessage();
			msg.obj = view;
			msg.arg1 = view.getScrollX();
			msg.arg2 = mRightViewWidth;
			msg.sendToTarget();
			mIsShown = true;
		}
	}

	public void hiddenRight(View view) {
		System.out.println("=========hiddenRight  view==" + view + "--mPreItemView==" + mPreItemView + "--mCurrentItemView==" + mCurrentItemView);
		if (view != null && mCurrentItemView != null) {
			Message msg = new MoveHandler().obtainMessage();//
			msg.obj = view;
			msg.arg1 = view.getScrollX();
			msg.arg2 = 0;
			msg.sendToTarget();
			mIsShown = false;
		}
	}

	/**
	 * show or hide right layout animation
	 */
	@SuppressLint("HandlerLeak")
	class MoveHandler extends Handler {
		int stepX = 0;

		int fromX;

		int toX;

		View view;

		private boolean mIsInAnimation = false;

		private void animatioOver() {
			mIsInAnimation = false;
			stepX = 0;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			System.out.println("执行了隐藏亦或显示的操作!  stepX==" + stepX);
			if (stepX == 0) {
				if (mIsInAnimation) {
					return;
				}
				mIsInAnimation = true;
				view = (View) msg.obj;
				fromX = msg.arg1;
				toX = msg.arg2;
				stepX = (int) ((toX - fromX) * mDurationStep * 1.0 / mDuration);
				if (stepX < 0 && stepX > -1) {
					stepX = -1;
				} else if (stepX > 0 && stepX < 1) {
					stepX = 1;
				}
				if (Math.abs(toX - fromX) < 10) {
					view.scrollTo(toX, 0);
					animatioOver();
					clearPressedState();
					return;
				}
			} else {
				clearPressedState();
			}

			fromX += stepX;
			boolean isLastStep = (stepX > 0 && fromX > toX) || (stepX < 0 && fromX < toX);
			if (isLastStep) {
				fromX = toX;
			}

			view.scrollTo(fromX, 0);
			invalidate();

			if (!isLastStep) {
				this.sendEmptyMessageDelayed(0, mDurationStep);
			} else {
				animatioOver();
			}
		}
	}

	public int getRightViewWidth() {
		return mRightViewWidth;
	}

	public void setRightViewWidth(int mRightViewWidth) {
		this.mRightViewWidth = mRightViewWidth;
	}

	public void setOnRefershListener(OnRefershListener listener) {
		this.refershListener = listener;
	}

	public interface OnRefershListener {
		public void onRefersh();
	}

	public boolean isRefreshing() {
		return isRefreshing;
	}

}
