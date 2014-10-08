package com.yildizkabaran.coverflow;

import android.view.View;

public class CoverflowTransformer implements ICoverflowTransformer {

  private int width = 200;
  private int padding = 0;
  private int separation = 25;
  private int maxRotation = 30;
  private int maxYOffset = 20;
  private float minScale = 0.75f;
  
  public void setItemWidth(int width){
    this.width = width;
  }
  
  public int getItemWidth(){
    return width;
  }
  
  public void setCenterPadding(int padding){
    this.padding = padding;
  }
  
  public int getCenterPadding(){
    return padding;
  }
  
  public void setSeparation(int separation){
    this.separation = separation;
  }
  
  public int getSeparation(){
    return separation;
  }
  
  public void setMaxRotation(int maxRotation){
    this.maxRotation = maxRotation;
  }
  
  public int getMaxRotation(){
    return maxRotation;
  }
  
  public void setMaxYOffset(int maxYOffset){
    this.maxYOffset = maxYOffset;
  }
  
  public int getMaxYOffset(){
    return maxYOffset;
  }
  
  public void setMinScale(float minScale){
    this.minScale = minScale;
  }
  
  public float getMinScale(){
    return minScale;
  }

  @Override
  public void transform(View view, float offset) {
    float translationX = 0;
    float translationY = 0;
    float scale = 1;
    float rotationY = 0;
    
    // far left case
    if(offset<-1){
      // beginning of left positions
      translationX = -(width + padding);
      // shift depending on offset
      translationX += (offset+1)* separation;
      rotationY = maxRotation;
      scale = minScale;
      translationY = maxYOffset;
    }
    // far right case
    else if(offset>1){
      // beginning of right positions
      translationX = (width + padding);
      // shift depending on offset
      translationX += (offset-1)* separation;
      rotationY = -maxRotation;
      scale = minScale;
      translationY = maxYOffset;
    }
    // center case
    else {
      translationX = (width + padding) * offset;
      rotationY = -maxRotation * offset;
      scale = 1 - Math.abs(offset) * (1 - minScale);
      translationY = maxYOffset * Math.abs(offset);
    }
    
    view.setTranslationX(translationX);
    view.setRotationY(rotationY);
    view.setTranslationY(translationY);
    view.setScaleX(scale);
    view.setScaleY(scale);
  }

  private int sideCount = 4;
  
  public void setSideCount(int count){
    sideCount = count;
  }
  
  @Override
  public int getSideVisibleCount() {
    return sideCount;
  }
}
