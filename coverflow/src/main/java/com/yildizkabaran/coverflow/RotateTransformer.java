package com.yildizkabaran.coverflow;

import android.view.View;

public class RotateTransformer implements ICoverflowTransformer {
  
  private int width = 200;
  private int padding = 0;
  private int separation = 25;
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

  private static float MIN_ALPHA = 0.25f;
  
  @Override
  public void transform(View view, float offset) {
    float translationX = 0;
    float translationY = 0;
    float scale = 1;
    float rotation = 0;
    float alpha = 1;
    
    // far left case
    if(offset<-1){
      translationX = -(width + padding);
      translationX += (offset+1)* separation;
      
      float yCoef = -maxYOffset * (offset+2-SIDE_COUNT) / (2f*(SIDE_COUNT-1));
      translationY = (float) Math.cos(offset*Math.PI) * yCoef;
      
      scale = minScale;
      
      alpha = 1 - ((1-MIN_ALPHA) * (-offset-1) / (SIDE_COUNT-1));
      if(alpha>1){
        alpha = 1;
      }
    }
    // far right case
    else if(offset>1){
      translationX = (width + padding);
      translationX += (offset-1)* separation;
      
      float yCoef = -maxYOffset * (offset-2+SIDE_COUNT) / (2f*(SIDE_COUNT-1));
      translationY = (float) Math.cos(offset*Math.PI) * yCoef;
      
      scale = minScale;
      
      alpha = 1 - ((1-MIN_ALPHA) * (offset-1) / (SIDE_COUNT-1));
      if(alpha>1){
        alpha = 1;
      }
    }
    // center case
    else {
      translationX = (width + padding) * offset;
      translationY = (float) Math.sin(offset*Math.PI/2) * maxYOffset / 2;
      rotation = 360 * offset;
      scale = 1 - Math.abs(offset) * (1 - minScale);
    }
    
    view.setTranslationX(translationX);
    view.setTranslationY(translationY);
    view.setRotation(rotation);
    view.setScaleX(scale);
    view.setScaleY(scale);
    view.setAlpha(alpha);
  }

  private static final int SIDE_COUNT = 6;
  
  @Override
  public int getSideVisibleCount() {
    return SIDE_COUNT;
  }
}
