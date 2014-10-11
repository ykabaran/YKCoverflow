package com.yildizkabaran.coverflow;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Adapter;
import android.widget.Scroller;

public class Coverflow extends ViewGroup {

  private static final String TAG = "Coverflow";
  private static final float SCROLL_SPEED = 200F;

  public Coverflow(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  public Coverflow(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public Coverflow(Context context) {
    super(context);
    initialize();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    touchBinder.setParentView(getParent());
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int numChildren = getChildCount();
    for(int i=0; i<numChildren; ++i){
      View v = getChildAt(i);
      measureChild(v, widthMeasureSpec, heightMeasureSpec);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int centerX = (r - l) / 2;
    int centerY = (b - t) / 2;
    int numChildren = getChildCount();
    for(int i=0; i<numChildren; ++i){
      View v = getChildAt(i);
      int childHalfWidth = v.getMeasuredWidth() / 2;
      int childHalfHeight = v.getMeasuredHeight() / 2;
      v.layout(centerX - childHalfWidth, centerY - childHalfHeight,
          centerX + childHalfWidth, centerY + childHalfHeight);
    }
  }

  private GestureDetector gestureDetector;
  private Scroller scroller;
  private Scroller centerScroller;
  private EventBinder touchBinder = new EventBinder(EventBinder.DIRECTION_X);
  private CoverflowItemClickListener itemClickListener;

  private Runnable scrollRunner = new Runnable() {
    @Override
    public void run() {
      if (scroller.isFinished()) {
        if (BuildConfig.DEBUG) {
          Log.d(TAG, "scrolling finished");
        }
        if (!touchBinder.isStarted()) {
          stopScrolling();
        }
        return;
      }

      scroller.computeScrollOffset();
      int newScrollPos = scroller.getCurrX();
      scrollTo(newScrollPos);
      post(this);
    }
  };

  private Runnable centerRunner = new Runnable() {
    @Override
    public void run() {
      if (centerScroller.isFinished()) {
        if (BuildConfig.DEBUG) {
          Log.d(TAG, "centering finished");
        }
        return;
      }

      centerScroller.computeScrollOffset();
      int newScrollPos = centerScroller.getCurrX();
      scrollTo(newScrollPos);
      post(this);
    }
  };

  private void initialize() {
    Context context = getContext();

    gestureDetector = new GestureDetector(context, new CustomGestureDetector());
    scroller = new Scroller(context);
    centerScroller = new Scroller(context, new OvershootInterpolator());
  }

  private boolean shouldOverrideTouchCapture = false;

  public void setOverrideTouchCapture(boolean override) {
    shouldOverrideTouchCapture = override;

    if (shouldOverrideTouchCapture) {
      touchBinder.setThreshold(0);
    } else {
      touchBinder.setThreshold(EventBinder.THRESHOLD_DEFAULT);
    }
  }

  public boolean overridesTouchCapture() {
    return shouldOverrideTouchCapture;
  }

  private Adapter adapter;

  public void setAdapter(Adapter adapter) {
    this.adapter = adapter;

    if (this.adapter != null) {
      itemCount = this.adapter.getCount();
    } else {
      itemCount = 0;
    }
    offset = 0;
    centerItemIndex = 0;

    refreshVirtualItemCount();
    clearView();
    refresh();
  }

  public void setOnItemClickListener(CoverflowItemClickListener listener){
    this.itemClickListener = listener;
  }

  private void clearView() {
    clearViewCache();
    removeAllViews();
  }

  private class CustomGestureDetector implements OnGestureListener {
    private static final int SCALE = 2;

    @Override
    public boolean onDown(MotionEvent e) {
      if (BuildConfig.DEBUG) {
        Log.d(TAG, "coverflow down");
      }
      abortCentering();
      touchBinder.onDown();
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      if (touchBinder.onScroll(-distanceX, 0)) {
        if (BuildConfig.DEBUG) {
          Log.d(TAG, "coverflow touch scroll dx: " + (-distanceX) + " dy: " + (-distanceY));
        }
        scrollItemsBy(-Math.round(distanceX));
      }
      return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      if (BuildConfig.DEBUG) {
        Log.d(TAG, "coverflow fling " + velocityX / SCALE);
      }

      if (!touchBinder.isBound()) {
        touchBinder.onUp();
        return false;
      }

      abortCentering();
      boolean startedAgain = scroller.isFinished();
      scroller.fling(offset, 0,
          Math.round(velocityX / SCALE), 0,
          Integer.MIN_VALUE, Integer.MAX_VALUE,
          0, 0);
      if (startedAgain) {
        post(scrollRunner);
      }

      touchBinder.onUp();

      return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      touchBinder.onUp();

      if (itemClickListener != null && adapter != null && itemCount > 0) {
        View v = getView(centerItemIndex);
        int adapterIndex = centerItemIndex % itemCount;
        itemClickListener.onItemClick(Coverflow.this, v, adapterIndex, v.getId());
        return true;
      }

      return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    boolean detectorHandled = gestureDetector.onTouchEvent(event);
    if (detectorHandled) {
      return true;
    }

    // check to see if touch up or cancel
    if (event.getAction() == MotionEvent.ACTION_UP ||
        event.getAction() == MotionEvent.ACTION_CANCEL) {
      if (BuildConfig.DEBUG) {
        Log.d(TAG, "coverflow touch up or cancel " + event.getAction());
      }

      touchBinder.onUp();
      stopScrolling();
      return true;
    }

    // we cant handle the event
    return false;
  }

  private int offset = 0;

  private void scrollItemsBy(int dx) {
    if (!scroller.isFinished()) {
      return;
    }

    int diff = dx;

    if (diff == 0) {
      return;
    }

    offset += diff;
    refresh();
  }

  private void scrollTo(int newOffset) {
    if (offset == newOffset) {
      return;
    }

    offset = newOffset;
    refresh();
  }

  private ICoverflowTransformer transformer;

  public void setTransformer(ICoverflowTransformer transformer) {
    this.transformer = transformer;
    sideVisibleCount = this.transformer.getSideVisibleCount();
    virtualItemCount = sideVisibleCount * 2 + 1;

    refreshVirtualItemCount();
    clearView();
    refresh();
  }

  private void refreshVirtualItemCount() {
    if (virtualItemCount < itemCount) {
      virtualItemCount = itemCount;
    } else if (itemCount > 0 && virtualItemCount % itemCount != 0) {
      virtualItemCount = ((virtualItemCount / itemCount) + 1) * itemCount;
    }
  }

  private int centerItemIndex = 0;
  private SparseArray<View> mViewMap = new SparseArray<View>();

  private void clearViewCache() {
    mViewMap.clear();
  }

  private View getCachedView(int index) {
    return mViewMap.get(index);
  }

  private void cacheView(int index, View view) {
    mViewMap.put(index, view);
  }

  private View getView(int index) {
    View v = getCachedView(index);
    if (v != null) {
      if (v.getParent() == null) {
        addView(v);
      }
      return v;
    }

    int adapterIndex = index % itemCount;
    v = adapter.getView(adapterIndex, null, this);
    addView(v);
    cacheView(index, v);

    return v;
  }

  private int itemCount = 0;
  private int virtualItemCount = 0;
  private int sideVisibleCount = 0;

  private void refresh() {
    if (adapter == null || transformer == null || itemCount < 1) {
      return;
    }

    float normalizedOffset = (float) offset / SCROLL_SPEED;
    int roundedNormalizedOffset = Math.round(normalizedOffset);
    int currentCenter = Util.positiveMod(-roundedNormalizedOffset, virtualItemCount);

    if (currentCenter != centerItemIndex) {
      // remove all views to fix their z-order
      removeAllViews();
      centerItemIndex = currentCenter;
    }
    float centerOffset = normalizedOffset - roundedNormalizedOffset;

    // add the right side items
    for (int i = sideVisibleCount; i >= 1; --i) {
      int currItem = Util.positiveMod((centerItemIndex + i), virtualItemCount);
      View v = getView(currItem);
      transformer.transform(v, i + centerOffset);
    }

    // add the left side items
    for (int i = -sideVisibleCount; i <= -1; ++i) {
      int currItem = Util.positiveMod((centerItemIndex + i), virtualItemCount);
      View v = getView(currItem);
      transformer.transform(v, i + centerOffset);
    }

    // add the center item
    View v = getView(centerItemIndex);
    transformer.transform(v, centerOffset);
  }

  private static final int CENTER_ANIM_DURATION = 1000;

  private void abortCentering() {
    if (!centerScroller.isFinished()) {
      centerScroller.forceFinished(true);
    }
  }

  private void stopScrolling() {
    int centerOffset = Math.round(offset - SCROLL_SPEED * Math.round(((float) offset / SCROLL_SPEED)));

    abortCentering();
    centerScroller.startScroll(offset, 0, -centerOffset, 0, CENTER_ANIM_DURATION);
    post(centerRunner);
  }

  public Adapter getAdapter() {
    return adapter;
  }
}
