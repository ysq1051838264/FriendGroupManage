package com.example.ysq.friendgroupmanage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * 自定义可拖动Item排序ListView
 */
public class DragListView extends ListView {

	private ImageView mDragImageView;// 被拖拽的项(item)，其实就是一个ImageView
	private int mStartPosition;// 手指拖动项原始在列表中的位置
	private int mDragPosition;// 手指点击准备拖动的时候,当前拖动项在列表中的位置
	private int mLastPosition;// 手指点击准备拖动的时候,当前拖动项在列表中的位置
	private int mDragPoint;// 在当前数据项中的位置
	private int mDragOffset;// 当前视图和屏幕的距离(这里只使用了y方向上)
	private int mUpScrollBounce;// 拖动的时候，开始向上滚动的边界
	private int mDownScrollBounce;// 拖动的时候，开始向下滚动的边界
	private final static int mStep = 1;// ListView 滑动步伐
	private int mCurrentStep;// 当前步伐
	private DragItemInfo mDragItemInfo;// 用于存放Item信息的对象
	private int mItemVerticalSpacing = 0;// Item垂直区域空间
	private int mHoldPosition;// 标记最后停靠的Position

	/** windows窗口控制类 */
	private WindowManager mWindowManager;
	/** 用于控制拖拽项的显示的参数 */
	private WindowManager.LayoutParams mWindowParams;
	/** 停止状态 */
	public static final int MSG_DRAG_STOP = 0x1001;
	/** 移动状态 */
	public static final int MSG_DRAG_MOVE = 0x1002;
	/** 动画时长(一个动画的耗时) */
	private static final int ANIMATION_DURATION = 200;
	/** 标识是否上锁 */
	private boolean isLock;
	/** 标识是否处于移动状态 */
	private boolean isMoving = false;
	/** 是否拖动Item */
	private boolean isDragItemMoving = false;
	/** 标识是否获取到间距 */
	private boolean bHasGetSapcing = false;

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mDragItemInfo = new DragItemInfo();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		mWindowManager = (WindowManager) getContext().getSystemService("window");
	}

	/**
	 * 接收消息并完成对应动作
	 */
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_DRAG_STOP:// 停止
				stopDrag();
				onDrop(msg.arg1);
				break;
			case MSG_DRAG_MOVE:// 移动
				onDrag(msg.arg1);
				break;
			}
		};
	};

	/**
	 * 获取间距--获取上下滚动间距
	 */
	private void getSpacing() {
		bHasGetSapcing = true;

		mUpScrollBounce = getHeight() / 3;// 取得向上滚动的边际，大概为该控件的1/3
		mDownScrollBounce = getHeight() * 2 / 3;// 取得向下滚动的边际，大概为该控件的2/3

		int[] firstTempLocation = new int[2];
		int[] secondTempLocation = new int[2];

		ViewGroup firstItemView = (ViewGroup) getChildAt(0);// 第一行
		ViewGroup secondItemView = (ViewGroup) getChildAt(1);// 第二行

		if (firstItemView != null) {
			firstItemView.getLocationOnScreen(firstTempLocation);
		} else {
			return;
		}

		if (secondItemView != null) {
			secondItemView.getLocationOnScreen(secondTempLocation);
			mItemVerticalSpacing = Math.abs(secondTempLocation[1] - firstTempLocation[1]);
		} else {
			return;
		}
	}

	/***
	 * touch事件拦截 在这里我进行相应拦截，
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 按下
		if (ev.getAction() == MotionEvent.ACTION_DOWN && !isLock && !isMoving && !isDragItemMoving) {

			int x = (int) ev.getX();// 获取相对与ListView的x坐标
			int y = (int) ev.getY();// 获取相应与ListView的y坐标
			mLastPosition = mStartPosition = mDragPosition = pointToPosition(x, y);

			// 无效不进行处理
			if (mDragPosition == AdapterView.INVALID_POSITION) {
				return super.onInterceptTouchEvent(ev);
			}

			if (false == bHasGetSapcing) {
				getSpacing();
			}

			// 获取当前位置的视图(可见状态)
			ViewGroup dragger = (ViewGroup) getChildAt(mDragPosition - getFirstVisiblePosition());

			DragListAdapter adapter = (DragListAdapter) getAdapter();

			mDragItemInfo.obj = adapter.getItem(mDragPosition - getFirstVisiblePosition());

			// 获取到的dragPoint其实就是在你点击指定item项中的高度.
			mDragPoint = y - dragger.getTop();
			// 这个值是固定的:其实就是ListView这个控件与屏幕最顶部的距离（一般为标题栏+状态栏）.
			mDragOffset = (int) (ev.getRawY() - y);

			// 获取可拖拽的图标
			View draggerIcon = dragger.findViewById(R.id.drag_item_image);
			if (draggerIcon.getVisibility() == View.VISIBLE) {// 只有在按钮为可见的情况下才允许移动

				// x > dragger.getLeft() - 20这句话为了更好的触摸（-20可以省略）
				if (draggerIcon != null && x > draggerIcon.getLeft() - 20) {

					dragger.destroyDrawingCache();
					dragger.setDrawingCacheEnabled(true);// 开启cache.
					dragger.setBackgroundColor(0xffefefef);
					Bitmap bm = Bitmap.createBitmap(dragger
							.getDrawingCache(true));// 根据cache创建一个新的bitmap对象.
					hideDropItem();
					adapter.setInvisiblePosition(mStartPosition);
					adapter.notifyDataSetChanged();
					startDrag(bm, y);// 初始化影像
					isMoving = false;

					adapter.copyList();
				}
			}
		}

		return super.onInterceptTouchEvent(ev);
	}

	/**
	 * 获取依个缩放动画
	 * 
	 * @return
	 */
	public Animation getScaleAnimation() {
		Animation scaleAnimation = new ScaleAnimation(0.0f, 0.0f, 0.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setFillAfter(true);
		return scaleAnimation;
	}

	/**
	 * 隐藏下降的Item
	 */
	private void hideDropItem() {
		final DragListAdapter adapter = (DragListAdapter) this.getAdapter();
		adapter.showDropItem(false);
	}

	/**
	 * 触摸事件处理
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// item的view不为空，且获取的dragPosition有效
		if (mDragImageView != null && mDragPosition != INVALID_POSITION && !isLock) {
			int action = ev.getAction();
			switch (action) {
			case MotionEvent.ACTION_UP:
				int upY = (int) ev.getY();
				stopDrag();
				onDrop(upY);
				break;
			case MotionEvent.ACTION_MOVE:
				int moveY = (int) ev.getY();
				onDrag(moveY);
				itemMoveAnimation(moveY);
				break;
			case MotionEvent.ACTION_DOWN:
				break;
			}
			return true;// 取消ListView滑动.
		}

		return super.onTouchEvent(ev);
	}

	/**
	 * 是否为相同方向拖动的标记
	 */
	private boolean isSameDragDirection = true;
	/**
	 * 移动方向的标记，-1为默认值，0表示向下移动，1表示向上移动
	 */
	private int lastFlag = -1;
	private int mFirstVisiblePosition, mLastVisiblePosition;// 第一个、最后一个的位置
	private int turnUpPosition, turnDownPosition;// 向上、下的位置

	/**
	 * 动态改变Item内容
	 * @param last
	 *            // 最后一项的位置
	 * @param current
	 *            // 当前位置
	 */
	private void onChangeCopy(int last, int current) {
		DragListAdapter adapter = (DragListAdapter) getAdapter();
		if (last != current) {// 判断是否移动到最后一项
			adapter.exchangeCopy(last, current);
		}
	}

	/**
	 * Item移动动画
	 * 
	 * @param y
	 */
	private void itemMoveAnimation(int y) {
		final DragListAdapter adapter = (DragListAdapter) getAdapter();
		int tempPosition = pointToPosition(0, y);

		if (tempPosition == INVALID_POSITION || tempPosition == mLastPosition) {
			return;
		}

		mFirstVisiblePosition = getFirstVisiblePosition();
		mDragPosition = tempPosition;
		onChangeCopy(mLastPosition, mDragPosition);
		int MoveNum = tempPosition - mLastPosition;// 计算移动项--移动距离
		int count = Math.abs(MoveNum);

		for (int i = 1; i <= count; i++) {
			int xAbsOffset, yAbsOffset;
			// 向下拖动
			if (MoveNum > 0) {
				if (lastFlag == -1) {
					lastFlag = 0;
					isSameDragDirection = true;
				}

				if (lastFlag == 1) {
					turnUpPosition = tempPosition;
					lastFlag = 0;
					isSameDragDirection = !isSameDragDirection;
				}

				if (isSameDragDirection) {
					mHoldPosition = mLastPosition + 1;
				} else {
					if (mStartPosition < tempPosition) {
						mHoldPosition = mLastPosition + 1;
						isSameDragDirection = !isSameDragDirection;
					} else {
						mHoldPosition = mLastPosition;
					}
				}

				xAbsOffset = 0;
				yAbsOffset = -mItemVerticalSpacing;
				mLastPosition++;

			} else {// 向上拖动
				if (lastFlag == -1) {
					lastFlag = 1;
					isSameDragDirection = true;
				}

				if (lastFlag == 0) {
					turnDownPosition = tempPosition;
					lastFlag = 1;
					isSameDragDirection = !isSameDragDirection;
				}

				if (isSameDragDirection) {
					mHoldPosition = mLastPosition - 1;
				} else {
					if (mStartPosition > tempPosition) {
						mHoldPosition = mLastPosition - 1;
						isSameDragDirection = !isSameDragDirection;
					} else {
						mHoldPosition = mLastPosition;
					}
				}

				xAbsOffset = 0;
				yAbsOffset = mItemVerticalSpacing;
				mLastPosition--;
			}

			adapter.setHeight(mItemVerticalSpacing);
			adapter.setIsSameDragDirection(isSameDragDirection);
			adapter.setLastFlag(lastFlag);

			ViewGroup moveView = (ViewGroup) getChildAt(mHoldPosition - getFirstVisiblePosition());

			Animation animation;
			if (isSameDragDirection) {// 相同方向拖动
				animation = getFromSelfAnimation(xAbsOffset, yAbsOffset);
			} else {// 不相同方向拖动
				animation = getToSelfAnimation(xAbsOffset, -yAbsOffset);
			}
			// 启用对应的动画
			moveView.startAnimation(animation);
		}
	}

	private void onDrop(int x, int y) {
		final DragListAdapter adapter = (DragListAdapter) getAdapter();
		adapter.setInvisiblePosition(-1);
		adapter.showDropItem(true);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 准备拖动，初始化拖动项的图像
	 * 
	 * @param bm
	 * @param y
	 */
	private void startDrag(Bitmap bm, int y) {
		/***
		 * 初始化window.
		 */
		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.TOP;
		mWindowParams.x = 0;
		mWindowParams.y = y - mDragPoint + mDragOffset;
		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE// 不需获取焦点
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE// 不需接受触摸事件
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON// 保持设备常开，并保持亮度不变。
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;// 窗口占满整个屏幕，忽略周围的装饰边框（例如状态栏）。此窗口需考虑到装饰边框的内容。

		// windowParams.format = PixelFormat.TRANSLUCENT;// 默认为不透明，这里设成透明效果.
		mWindowParams.windowAnimations = 0;// 窗口所使用的动画设置

		mWindowParams.alpha = 0.8f;
		mWindowParams.format = PixelFormat.TRANSLUCENT;

		ImageView imageView = new ImageView(getContext());
		imageView.setImageBitmap(bm);

		mWindowManager.addView(imageView, mWindowParams);
		mDragImageView = imageView;
	}

	/**
	 * 拖动执行，在Move方法中执行
	 * 
	 * @param y
	 */
	public void onDrag(int y) {
		int drag_top = y - mDragPoint;// 拖拽view的top值不能＜0，否则则出界.
		if (mDragImageView != null && drag_top >= 0) {
			mWindowParams.alpha = 1.0f;
			mWindowParams.y = y - mDragPoint + mDragOffset;
			mWindowManager.updateViewLayout(mDragImageView, mWindowParams);// 时时移动.
		}

		doScroller(y);// listview移动.
	}

	/***
	 * ListView的移动.
	 * 要明白移动原理：当我移动到下端的时候，ListView向上滑动，当我移动到上端的时候，ListView要向下滑动。正好和实际的相反.
	 * 
	 */
	public void doScroller(int y) {
		// ListView需要下滑
		if (y < mUpScrollBounce) {
			mCurrentStep = mStep + (mUpScrollBounce - y) / 10;// 时时步伐
		}// ListView需要上滑
		else if (y > mDownScrollBounce) {
			mCurrentStep = -(mStep + (y - mDownScrollBounce)) / 10;// 时时步伐
		} else {
			mCurrentStep = 0;
		}

		// 获取你拖拽滑动到位置及显示item相应的view上（注：可显示部分）（position）
		View view = getChildAt(mDragPosition - getFirstVisiblePosition());
		// 真正滚动的方法setSelectionFromTop()
		setSelectionFromTop(mDragPosition, view.getTop() + mCurrentStep);
	}

	/**
	 * 停止拖动，删除影像
	 */
	public void stopDrag() {
		isMoving = false;

		if (mDragImageView != null) {
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}

		isSameDragDirection = true;
		lastFlag = -1;
		DragListAdapter adapter = (DragListAdapter) getAdapter();
		adapter.setLastFlag(lastFlag);
		adapter.pastList();
	}

	/**
	 * 拖动放下的时候
	 * 
	 * @param y
	 */
	public void onDrop(int y) {
		onDrop(0, y);
	}

	/**
	 * 获取自身出现的动画
	 * @param x
	 * @param y
	 * @return
	 */
	private Animation getFromSelfAnimation(int x, int y) {
		TranslateAnimation translateAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, x,
				Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, y);
		translateAnimation
				.setInterpolator(new AccelerateDecelerateInterpolator());
		translateAnimation.setFillAfter(true);
		translateAnimation.setDuration(ANIMATION_DURATION);
		translateAnimation.setInterpolator(new AccelerateInterpolator());
		return translateAnimation;
	}

	/**
	 * 获取自身离开的动画
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private Animation getToSelfAnimation(int x, int y) {
		TranslateAnimation translateAnimation = new TranslateAnimation(
				Animation.ABSOLUTE, x, Animation.RELATIVE_TO_SELF, 0,
				Animation.ABSOLUTE, y, Animation.RELATIVE_TO_SELF, 0);
		translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		translateAnimation.setFillAfter(true);
		translateAnimation.setDuration(ANIMATION_DURATION);
		translateAnimation.setInterpolator(new AccelerateInterpolator());
		return translateAnimation;
	}

}