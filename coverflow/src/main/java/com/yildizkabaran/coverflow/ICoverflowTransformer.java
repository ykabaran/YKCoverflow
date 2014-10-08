package com.yildizkabaran.coverflow;

import android.view.View;

public interface ICoverflowTransformer {
  public void transform(View view, float offset);
  public int getSideVisibleCount();
}
