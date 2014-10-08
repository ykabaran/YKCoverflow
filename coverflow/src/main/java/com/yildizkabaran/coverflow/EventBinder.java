package com.yildizkabaran.coverflow;

import android.util.Log;
import android.view.ViewParent;


public class EventBinder {

  static final String TAG = "EventBinder";
  
  public static final int DIRECTION_LEFT = 1;
  public static final int DIRECTION_RIGHT = 2;
  public static final int DIRECTION_UP = 3;
  public static final int DIRECTION_DOWN = 4;
  public static final int DIRECTION_X = 5;
  public static final int DIRECTION_Y = 6;
  public static final int DIRECTION_ANY = 7;
  
  public static final float THRESHOLD_DEFAULT = 10;
  
  private ViewParent viewParent = null;
  
  private boolean isStarted = false;
  private boolean isBound = false;
  private float threshold = THRESHOLD_DEFAULT;
  private int direction = DIRECTION_X;
  private boolean bindOnDown = false;
  
  private Checker mChecker;
  private float mTotalDistanceX;
  private float mTotalDistanceY;
  
  public EventBinder(){
    setDirection(DIRECTION_X);
  }
  
  public EventBinder(int direction){
    setDirection(direction);
  }
  
  public ViewParent getParentView(){
    return viewParent;
  }
  
  public void setParentView(ViewParent parentView){
    viewParent = parentView;
  }
  
  public float getThreshold(){
    return threshold;
  }
  
  public void setThreshold(float threshold){
    this.threshold = threshold;
    
    if(this.threshold < 1){
      bindOnDown = true;
    }
  }
  
  public int getDirection(){
    return direction;
  }
  
  public void setDirection(int direction){
    this.direction = direction;
    mChecker = getChecker(this.direction);
  }
  
  public boolean isStarted(){
    return isStarted;
  }
  
  public boolean isBound(){
    return isBound;
  }
  
  public void onDown(){
    reset();
    isStarted = true;
    
    if(bindOnDown){
      if(viewParent != null){
        viewParent.requestDisallowInterceptTouchEvent(true);
      }
      isBound = true;
    }
  }
  
  public boolean onScroll(float distanceX, float distanceY){
    if(!isStarted){
      return false;
    }
    
    if(isBound){
      return true;
    }
    
    mTotalDistanceX += distanceX;
    mTotalDistanceY += distanceY;
    
    isBound = mChecker.check();
    
    if(isBound && viewParent != null){
      viewParent.requestDisallowInterceptTouchEvent(true);
    }
    
    if(BuildConfig.DEBUG){
      Log.d(TAG, "binder scroll tx: " + mTotalDistanceX + " ty: " + mTotalDistanceY + " bound: " + isBound);
    }
    
    return isBound;
  }
  
  public void onUp(){
    reset();
  }
  
  private void reset(){
    mTotalDistanceX = 0;
    mTotalDistanceY = 0;
    isBound = false;
    isStarted = false;
    if(viewParent != null){
      viewParent.requestDisallowInterceptTouchEvent(false);
    }
  }

  public Checker getChecker(int direction){
    Checker result = null;
    switch(direction){
    case DIRECTION_X:
      result = new CheckerX();
      break;
    case DIRECTION_Y:
      result = new CheckerY();
      break;
    case DIRECTION_LEFT:
      result = new CheckerLeft();
      break;
    case DIRECTION_RIGHT:
      result = new CheckerRight();
      break;
    case DIRECTION_UP:
      result = new CheckerUp();
      break;
    case DIRECTION_DOWN:
      result = new CheckerDown();
      break;
    case DIRECTION_ANY:
      result = new CheckerAny();
      break;
    }
    return result;
  }
  
  private abstract class Checker {
    public abstract boolean check();
  }
  
  private class CheckerX extends Checker {
    @Override
    public boolean check(){
      return Math.abs(mTotalDistanceX)> threshold;
    }
  }
  
  private class CheckerLeft extends Checker {
    @Override
    public boolean check(){
      return mTotalDistanceX> threshold;
    }
  }
  
  private class CheckerRight extends Checker {
    @Override
    public boolean check(){
      return mTotalDistanceX<-threshold;
    }
  }
  
  private class CheckerY extends Checker {
    @Override
    public boolean check(){
      return Math.abs(mTotalDistanceY)> threshold;
    }
  }
  
  private class CheckerUp extends Checker {
    @Override
    public boolean check(){
      return mTotalDistanceY> threshold;
    }
  }
  
  private class CheckerDown extends Checker {
    @Override
    public boolean check(){
      return mTotalDistanceY<-threshold;
    }
  }
  
  private class CheckerAny extends Checker {
    @Override
    public boolean check(){
      return Math.abs(mTotalDistanceX)> threshold || Math.abs(mTotalDistanceY)> threshold;
    }
  }
}
