package com.yildizkabaran.coverflow;

import android.view.View;

public class LinearTransformer implements ICoverflowTransformer {

  private int width;
  private int separation;
  
  public void setItemWidth(int width){
    this.width = width;
  }
  
  public int getItemWidth(){
    return width;
  }
  
  public void setSeparation(int separation){
    this.separation = separation;
  }
  
  public int getSeparation(){
    return separation;
  }

  @Override
  public void transform(View view, float offset) {
    float translationX = offset * (width + separation);
    
    view.setTranslationX(translationX);
  }

  @Override
  public int getSideVisibleCount() {
    return 1;
  }
}
