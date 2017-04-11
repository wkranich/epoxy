package com.airbnb.epoxy;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.SimpleOnItemTouchListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class StickyModelClickListener extends SimpleOnItemTouchListener {

  private final GestureDetector gestureDetector;
  private final StickyModelDecoration decoration;
  private OnHeaderClickListener listener;

  public interface OnHeaderClickListener {
    void onHeaderClick(long headerId);
  }

  public StickyModelClickListener(RecyclerView parent, StickyModelDecoration decoration) {
    this.gestureDetector = new GestureDetector(parent.getContext(), new SingleTapDetector(parent));
    this.decoration = decoration;
  }

  @Override
  public boolean onInterceptTouchEvent(RecyclerView parent, MotionEvent e) {
    return listener != null && gestureDetector.onTouchEvent(e);
  }

  public void setListener(OnHeaderClickListener listener) {
    this.listener = listener;
  }

  private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {

    private final RecyclerView parent;

    public SingleTapDetector(RecyclerView parent) {
      this.parent = parent;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      return decoration.findHeaderViewUnder(e.getX(), e.getY()) != null;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {

      View view = decoration.findHeaderViewUnder(e.getX(), e.getY());
      if (view != null) {
        long headerId = (long) view.getTag();
        listener.onHeaderClick(headerId);
        return true;
      }

      return false;
    }
  }
}
