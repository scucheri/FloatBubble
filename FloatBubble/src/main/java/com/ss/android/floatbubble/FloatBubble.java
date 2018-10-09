package com.ss.android.floatbubble;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import demo.android.ss.com.floatbubble.R;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;


public class FloatBubble extends FrameLayout {
    private static final String TAG = FloatBubble.class.getName();
    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    int lastTouchX, lastTouchY;
    int windowParamX, windowParamY;
    int absoluteGravity = Gravity.TOP | Gravity.LEFT;
    int dragMode = FBConstant.DRAG_MODE.FREE_DRAGGABLE;
    int wParamUnitX = 1;
    int wParamUnitY = 1;
    int wParamMinX, wParamMinY = 0;
    int wParamMaxX, wParamMaxY = 0;
    private int axisY = 0;
    private int axisX = 0;
    private int animationMode = FBConstant.ANIMATION.NO_ANIMATION;
    private Timer animationTimer;
    private TimerTask animationTask;
    private long animationPeriod = 16;
    private int screenWidth;
    private int screenHeight;
    private boolean screenParamsInited = false;
    private OnDismissListener onDismissListener;
    private OnShowListener onShowListener;
    private boolean hasCloseIcon = false;
    private FrameLayout floatRootView;
    private boolean isShown;

    public FloatBubble(Context context) {
        super(context);
    }

    public FloatBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 新建一个应用内显示的浮窗
     * 调用此方法前，请先调用FloatBubblePermission.requestFloatPermission进行权限申请
     *
     */
    @Nullable
    public static FloatBubble makeGlobalFloat(@NonNull Application application, @NonNull View contentView) {
        if (FloatBubblePermission.isPermissionGranted(application)) {
            return make(application, contentView);
        } else {
            Log.e(TAG, "没有浮层权限，请先调用FloatBubblePermission.isPermissionGranted进行权限判断；调用FloatBubblePermission.requestFloatPermission进行权限申请");
            return null;
        }
    }

    /**
     * 新建一个Activity内显示的浮窗
     */
    @Nullable
    public static FloatBubble makeLocalFloat(@NonNull Activity activity, @NonNull View contentView) {
        return make(activity, contentView);
    }

    private static FloatBubble make(@NonNull Context context, @NonNull View contentView) {
        if (context == null || contentView == null) {
            Log.e(TAG, "context and contentView can not be null");
            return null;
        }
        FloatBubble floatBubble = new FloatBubble(context);
        floatBubble.mContext = context;
        floatBubble.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        floatBubble.setContentView(contentView);
        return floatBubble;
    }

    /**
     * 设置浮窗的拖拽方式
     */
    public FloatBubble setDragMode(int dragMode) {
        this.dragMode = dragMode;
        return this;
    }

    /**
     * 设置Gravity
     */
    public FloatBubble setGravity(int gravity) {
        this.absoluteGravity = gravity;
        return this;
    }

    /**
     * @param x 水平方向坐标，单位dp
     * @param y 竖直方向坐标，单位dp
     */
    public FloatBubble setAxis(int x, int y) {
        this.axisX = FBMeatureUtil.dpToPx(mContext, x);
        this.axisY = FBMeatureUtil.dpToPx(mContext, y);
        return this;
    }

    /**
     * @param animationMode 设置动画模式，见FBConstant.ANIMATION
     */
    public FloatBubble setAnimationMode(int animationMode) {
        this.animationMode = animationMode;
        return this;
    }


    /**
     *  设置浮层隐藏回调
     */
    public FloatBubble setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }

    /**
     *  设置浮层隐藏回调
     */
    public FloatBubble setOnShowListener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    /**
     *  设置是否又关闭图标
     */
    public FloatBubble setCloseIcon(boolean hasCloseIcon){
        this.hasCloseIcon = hasCloseIcon;
        if(floatRootView == null) return this;
        ImageView closeIcon = floatRootView.findViewById(R.id.float_bubble_close_icon);
        if(hasCloseIcon){
            closeIcon.setVisibility(VISIBLE);
            closeIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    FloatBubble.this.dismiss();
                }
            });
        }
        return this;
    }

    /**
     * 关闭浮窗
     *
     * @return
     */
    public void dismiss() {
        if(isShown) {
            isShown = false;
            if (mWindowManager != null) {
                mWindowManager.removeView(this);
            }
            if (onDismissListener != null) {
                onDismissListener.onDismiss();
            }
        }
        cancelAnimationTimer();
    }

    /**
     * 显示浮窗
     *
     * @return
     */
    public void show() {
        try {
            if (mContext == null) return;
            if (mContext instanceof Application) {
                addAsAppLayer();
            } else if (mContext instanceof Activity) {
                addAsActivityLayer();
            }
            if(onShowListener != null){
                onShowListener.onShow();
            }
            isShown = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isShown(){
        return isShown;
    }

    private FloatBubble setContentView(View contentView) {
        if (mContext == null) return this;
        floatRootView = (FrameLayout) View.inflate(getContext(), R.layout.float_bubble_view, this);
        FrameLayout containerView = floatRootView.findViewById(R.id.float_bubble_container);
        containerView.addView(contentView);
        return this;
    }


    private void addAsAppLayer() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mWindowParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, LAYOUT_FLAG,
                FLAG_NOT_FOCUSABLE
                        | FLAG_NOT_TOUCH_MODAL
                        | FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mWindowParams.gravity = absoluteGravity;
        mWindowParams.x = axisX;
        mWindowParams.y = axisY;
        mWindowManager.addView(this, mWindowParams);
    }

    private void addAsActivityLayer() {
        mWindowParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION,
                FLAG_NOT_FOCUSABLE
                        | FLAG_NOT_TOUCH_MODAL
                        | FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mWindowParams.gravity = absoluteGravity;
        mWindowParams.x = axisX;
        mWindowParams.y = axisY;
        mWindowManager.addView(this, mWindowParams);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initScreenParams();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //手指按下的位置
                lastTouchX = (int) event.getRawX();
                lastTouchY = (int) event.getRawY();
                //记录手指按下时,悬浮窗的位置
                windowParamX = mWindowParams.x;
                windowParamY = mWindowParams.y;
                return super.dispatchTouchEvent(event);
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastTouchX;
                int dy = (int) event.getRawY() - lastTouchY;
                mWindowParams.x = windowParamX + dx * wParamUnitX;
                mWindowParams.y = windowParamY + dy * wParamUnitY;
                // 更新悬浮窗位置
                //不能超过边界
                if (mWindowParams.x < wParamMinX) {
                    mWindowParams.x = wParamMinX;
                }
                if (mWindowParams.x > wParamMaxX) {
                    mWindowParams.x = wParamMaxX;
                }
                if (mWindowParams.y < wParamMinY) {
                    mWindowParams.y = wParamMinY;
                }
                if (mWindowParams.y > wParamMaxY) {
                    mWindowParams.y = wParamMaxY;
                }
                mWindowManager.updateViewLayout(FloatBubble.this, mWindowParams);
                return super.dispatchTouchEvent(event);
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                cancelAnimationTimer();
                switch (animationMode) {
                    case FBConstant.ANIMATION.APPEAL_LEFT_OR_RIGHT:
                        appealToLeftRight();
                        Log.i(TAG, "appealToLeftRight");
                        break;
                    case FBConstant.ANIMATION.NO_ANIMATION:
                    default:
                        break;
                }
                int Xdiff = (int) (event.getRawX() - lastTouchX);
                int Ydiff = (int) (event.getRawY() - lastTouchY);
                if (Xdiff < 10 && Ydiff < 10) {
                    Log.i(TAG, "dispatch click event");
                    return super.dispatchTouchEvent(event);
                }
                break;
        }
        return true;
    }

    private void appealToLeftRight() {
        cancelAnimationTimer();
        animationTimer = new Timer();
        animationTask = new AppealLeftOrRightTask();
        animationTimer.schedule(animationTask, 0, animationPeriod);
    }

    private class AppealLeftOrRightTask extends TimerTask {
        int mStepX;
        int mDestX;

        AppealLeftOrRightTask() {
            if (mWindowParams.x >= (wParamMaxX + wParamMinX) / 2) {
                mDestX = wParamMaxX;
                mStepX = (wParamMaxX - mWindowParams.x) / 10;
            } else {
                mDestX = wParamMinX;
                mStepX = (wParamMinX - mWindowParams.x) / 10;
            }
        }

        @Override
        public void run() {
            if (Math.abs(mDestX - mWindowParams.x) <= Math.abs(mStepX)) {
                mWindowParams.x = mDestX;
            } else {
                mWindowParams.x += mStepX;
            }
            FloatBubble.this.post(new Runnable() {
                @Override
                public void run() {
                    if(isShown == false) return;
                    try {
                        mWindowManager.updateViewLayout(FloatBubble.this, mWindowParams);
                        if (mWindowParams.x == mDestX) {
                            cancelAnimationTimer();
                            mWindowManager.updateViewLayout(FloatBubble.this, mWindowParams);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void cancelAnimationTimer() {
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        Log.i(TAG, "timer :  cancel");
    }

    private void initScreenParams() {
        if (screenParamsInited) return;
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        int bubbleWidth = FloatBubble.this.getWidth();
        int bubbleHeight = FloatBubble.this.getHeight();
        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                wParamUnitX = 1;
                wParamMinX = -1 * (screenWidth - bubbleWidth) / 2;
                wParamMaxX = (screenWidth - bubbleWidth) / 2;
                break;
            case Gravity.LEFT:
                wParamUnitX = 1;
                wParamMinX = 0;
                wParamMaxX = screenWidth - bubbleWidth;
                break;
            case Gravity.RIGHT:
                wParamUnitX = -1;
                wParamMinX = 0;
                wParamMaxX = screenWidth - bubbleWidth;
                break;
            default:
                wParamUnitX = 1;
                wParamMinX = 0;
                wParamMaxX = screenWidth - bubbleWidth;
                break;
        }
        switch (absoluteGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.CENTER_VERTICAL:
                wParamUnitY = 1;
                wParamMinY = -1 * (screenHeight - bubbleHeight) / 2;
                wParamMaxY = (screenHeight - bubbleHeight) / 2;
                break;
            case Gravity.TOP:
                wParamUnitY = 1;
                wParamMinY = 0;
                wParamMaxY = screenHeight - bubbleHeight;
                break;

            case Gravity.BOTTOM:
                wParamUnitY = -1;
                wParamMinY = 0;
                wParamMaxY = screenHeight - bubbleHeight;
                break;
            default:
                wParamUnitY = 1;
                wParamMinY = 0;
                wParamMaxY = screenHeight - bubbleHeight;
                break;
        }
        if (dragMode == FBConstant.DRAG_MODE.HORIAONTAL_DRAGGABLE) {
            wParamUnitY = 0;
        } else if (dragMode == FBConstant.DRAG_MODE.VERTICAL_DRAGGABLE) {
            wParamUnitX = 0;
        } else if (dragMode == FBConstant.DRAG_MODE.NON_DRAGGABLE) {
            wParamUnitX = 0;
            wParamUnitY = 0;
        }
        screenParamsInited = true;
    }

    public interface OnDismissListener{
        void onDismiss();
    }

    public interface OnShowListener{
        void onShow();
    }
}
