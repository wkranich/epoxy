package com.airbnb.epoxy;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

public class StickyLayoutManager extends LinearLayoutManager {
  private RecyclerView recyclerView;
  private Recycler recycler;
  private StickyRecyclerView stickyParent;

  public StickyLayoutManager(Context context) {
    super(context);
  }

  @Override
  public void onAttachedToWindow(RecyclerView view) {
    super.onAttachedToWindow(view);
    recyclerView = view;
    stickyParent = (StickyRecyclerView) recyclerView.getParent();
  }

  HeaderData currentHeader;

  class HeaderData {
    View headerView;
    EpoxyModel<?> model;
    LayoutParams originalRecylerViewParams;

    public HeaderData(View headerView, EpoxyModel<?> model,
        LayoutParams originalRecylerViewParams) {
      this.headerView = headerView;
      this.model = model;
      this.originalRecylerViewParams = originalRecylerViewParams;
    }
  }

  @Override
  public void onLayoutChildren(Recycler recycler, State state) {
    super.onLayoutChildren(recycler, state);
    this.recycler = recycler;

    addHeaderIfcan(recycler);
  }

  private void addHeaderIfcan(Recycler recycler) {
    if (recyclerView.getChildCount() == 0 || currentHeader != null) {
      return;
    }

    for (int i = 0; i < recyclerView.getChildCount(); i++) {
      View child = recyclerView.getChildAt(i);
      EpoxyViewHolder holder =
          ((EpoxyViewHolder) recyclerView.getChildViewHolder(child));
      EpoxyModel<?> model = holder.getModel();

      if ((model.isSticky())) {
        if (child.getTop() <= 0) {
          View headerView = recycler.getViewForPosition(holder.getAdapterPosition());
          currentHeader = new HeaderData(headerView, model,
              (LayoutParams) headerView.getLayoutParams());
          stickyParent.setHeader(currentHeader.headerView);
          return;
        }
      }
    }
  }

  private void removeCurrentHeader() {
    stickyParent.removeView(currentHeader.headerView);
    currentHeader.headerView.setLayoutParams(currentHeader.originalRecylerViewParams);
    recycler.recycleView(currentHeader.headerView);
    currentHeader = null;
  }

  @Override
  public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
    int i = super.scrollVerticallyBy(dy, recycler, state);
    addHeaderIfcan(recycler);
    if (currentHeader != null) {
//      currentHeader.setTranslationY(currentHeader.getTranslationY() - i);
    }
    return i;
  }

  @Override
  public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
    return super.scrollHorizontallyBy(dx, recycler, state);
  }

  @Override
  public void onLayoutCompleted(State state) {
    super.onLayoutCompleted(state);
  }
}
