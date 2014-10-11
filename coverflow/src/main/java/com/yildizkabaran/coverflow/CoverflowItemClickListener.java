package com.yildizkabaran.coverflow;

import android.view.View;

/**
 * Created by yildizkabaran on 9.10.2014.
 */
public interface CoverflowItemClickListener {
  public void onItemClick(Coverflow coverflow, View view, int position, long id);
}
