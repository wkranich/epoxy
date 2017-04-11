package com.airbnb.epoxy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StickyRecyclerView extends FrameLayout {

  @BindView(R.id.recycler_view) RecyclerView recyclerView;

  public StickyRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    LayoutInflater.from(getContext()).inflate(R.layout.sticky_recycler, this, true);
    ButterKnife.bind(this);
    recyclerView.setLayoutManager(new StickyLayoutManager(getContext()));
    recyclerView.addOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
      }

      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
      }
    });
  }

  public void setHeader(View view) {
    ViewGroup.LayoutParams newLayoutParams = generateLayoutParams(view.getLayoutParams());
    view.setLayoutParams(newLayoutParams);
    addView(view);
  }
}
