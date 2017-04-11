package com.airbnb.epoxy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;

import java.util.HashMap;
import java.util.Map;

public class StickyModelDecoration extends RecyclerView.ItemDecoration {

  public static final long NO_HEADER_ID = -1L;

  private final Map<Long, View> headerCache = new HashMap();
  private final boolean renderInline;
  private final LayoutInflater layoutInflater;
  private final BaseEpoxyAdapter adapter;

  private Integer widthSpec;
  private Integer heightSpec;

  /// The Constructor needs to be dirty because we need instanceOf checks which are not possible
  /// on generic types
  public StickyModelDecoration(Context context, BaseEpoxyAdapter adapter) {
    this(context, adapter, false);
  }

  public StickyModelDecoration(Context context, BaseEpoxyAdapter adapter, boolean renderInline) {
    this.adapter = adapter;
    this.renderInline = renderInline;
    layoutInflater = LayoutInflater.from(context);

    // TODO: getHeaderId is called a ton and calls to it can be optimized 
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view);
    int headerHeight = 0;

    if (position != RecyclerView.NO_POSITION
        && hasHeader(position)
        && showHeaderAboveItem(position)) {

      View header = getHeaderView(parent, position);
      headerHeight = getHeaderHeightForLayout(header);
    }

    outRect.set(0, headerHeight, 0, 0);
  }

  private boolean showHeaderAboveItem(int itemAdapterPosition) {
    if (itemAdapterPosition == 0) {
      return true;
    }
    return getHeaderId(itemAdapterPosition - 1) != getHeaderId(itemAdapterPosition);
  }

  /**
   * Clears the header view cache. Headers will be recreated and
   * rebound on list scroll after this method has been called.
   */
  public void clearHeaderCache() {
    headerCache.clear();
  }

  @Nullable
  public View findHeaderViewUnder(float x, float y) {
    for (View child : headerCache.values()) {
      final float translationX = child.getTranslationX();
      final float translationY = child.getTranslationY();

      if (x >= child.getLeft() + translationX &&
          x <= child.getRight() + translationX &&
          y >= child.getTop() + translationY &&
          y <= child.getBottom() + translationY) {
        return child;
      }
    }

    return null;
  }

  private boolean hasHeader(int position) {
    return getHeaderId(position) != NO_HEADER_ID;
  }

  private View getHeaderView(RecyclerView parent, int position) {
    final long key = getHeaderId(position);

    if (headerCache.containsKey(key)) {
      return headerCache.get(key);
    } else {
      EpoxyModel model = getHeaderModelForPosition(position);
      final View header = layoutInflater.inflate(model.getLayout(), parent, false);
      ((Sticky) model).onStick(header);

      if (widthSpec == null) {
        widthSpec =
            View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
        heightSpec = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
      }

      header.measure(widthSpec, heightSpec);
      header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

      headerCache.put(key, header);
      return header;
    }
  }

  @Override
  public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
    final int count = parent.getChildCount();
    long previousHeaderId = -1;

    for (int layoutPos = 0; layoutPos < count; layoutPos++) {
      final View child = parent.getChildAt(layoutPos);
      final int adapterPos = parent.getChildAdapterPosition(child);

      if (adapterPos != RecyclerView.NO_POSITION && hasHeader(adapterPos)) {
        long headerId = getHeaderId(adapterPos);

        if (headerId != previousHeaderId) {
          previousHeaderId = headerId;
          View header = getHeaderView(parent, adapterPos);
          canvas.save();

          final int left = child.getLeft();
          final int top = getHeaderTop(parent, child, header, adapterPos, layoutPos);

          if (top < header.getMeasuredHeight()) {
            onStickyHeaderGoingOffScreen(header, top);
          }

          canvas.translate(left, top);

          header.setTranslationX(left);
          header.setTranslationY(top);
          header.draw(canvas);
          canvas.restore();
        }
      }
    }
  }

  private int getHeaderTop(RecyclerView parent, View child, View header, int adapterPos,
      int layoutPos) {
    int headerHeight = getHeaderHeightForLayout(header);
    int top = ((int) child.getY()) - headerHeight;
    if (layoutPos == 0) {
      final int count = parent.getChildCount();
      final long currentId = getHeaderId(adapterPos);
      // find next view with header and compute the offscreen push if needed
      for (int i = 1; i < count; i++) {
        int adapterPosHere = parent.getChildAdapterPosition(parent.getChildAt(i));
        if (adapterPosHere != RecyclerView.NO_POSITION) {
          long nextId = getHeaderId(adapterPosHere);
          if (nextId != currentId) {
            final View next = parent.getChildAt(i);
            final int offset =
                ((int) next.getY()) - (headerHeight + getHeaderView(parent, adapterPosHere)
                    .getHeight());
            if (offset < 0) {
              return offset;
            } else {
              break;
            }
          }
        }
      }

      top = Math.max(0, top);
    }

    return top;
  }

  private int getHeaderHeightForLayout(View header) {
    return renderInline ? 0 : header.getHeight();
  }

  public long getHeaderId(int position) {
    if (position == 0) { // don't show header for first item
      return StickyModelDecoration.NO_HEADER_ID;
    }
    EpoxyModel epoxyModel = getHeaderModelForPosition(position);
    return epoxyModel != null ? epoxyModel.id() : 0;
  }

  /**
   * @return the EpoxyModel associated with the header for any item in given adapter.
   */
  @Nullable
  public EpoxyModel<?> getHeaderModelForPosition(int position) {
    for (int i = position; i > 0; i--) {
      // // TODO: Getting models individually like this is slow and show be optimized
      EpoxyModel<?> epoxyModel = adapter.getModelForPosition(i);
      if (epoxyModel instanceof Sticky) {
        return epoxyModel;
      }
    }
    return null;
  }

  /**
   * A callback that is triggered when a sticky header starts going off screen
   */
  private void onStickyHeaderGoingOffScreen(View headerView, int topY) {
    // TODO
  }
}
