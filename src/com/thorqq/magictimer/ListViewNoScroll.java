package com.thorqq.magictimer;


import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

//无滚动效果的简易ListView
public class ListViewNoScroll extends LinearLayout
{    
    class AdapterDataSetObserver extends DataSetObserver 
    { 
        @Override 
        public void onChanged() { 
            mView.updateView();
        } 
     
        @Override 
        public void onInvalidated() { 
            mView.updateView();
        } 
        
        public void setView(ListViewNoScroll v)
        {
            mView = v;
        }
                
        ListViewNoScroll mView;
    } 
    
    private BaseAdapter mAdapter;
    private OnClickListener onClickListener = null;
    AdapterDataSetObserver mDataSetObserver;
    private Drawable mDivider;
    private int mDividerHeight;
    private boolean mClipDivider; 
    
    private View mHeaderView;
    private View mFooterView;
    
    private ArrayList<View> mCacheView = new ArrayList<View>();
            
    public void fillLinearLayout()
    {
        removeAllViews();
        
        if(mHeaderView != null)
        {
            addView(mHeaderView);
        }
        
        int count = mAdapter.getCount();
        if(mCacheView.size() != count)
        {
            mCacheView.clear();
        }
        
        for (int i = 0; i < count; i++)
        {
            View srcView = null;
            if(mCacheView.size() > i)
            {
                srcView = mCacheView.get(i);
            }
            
            View v = mAdapter.getView(i, srcView, null);
            if(mCacheView.size() > i)
            {
                mCacheView.set(i, v);
            }
            else
            {
                mCacheView.add(v);
            }
            
            v.setOnClickListener(this.onClickListener);
            addView(v);
        }
        
        if(mFooterView != null)
        {
            addView(mFooterView);
        }

    }
    
    public void updateView()
    {
        fillLinearLayout();
    }

    public ListViewNoScroll(Context context)
    {
        super(context);
    }

    public ListViewNoScroll(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public android.widget.BaseAdapter getAdpater()
    {
        return mAdapter;
    }

    public void setAdapter(android.widget.BaseAdapter adpater)
    {
        this.mAdapter = adpater;
        
        mDataSetObserver = new AdapterDataSetObserver();
        adpater.registerDataSetObserver(mDataSetObserver); 
        mDataSetObserver.setView(this);
        
        fillLinearLayout();
    }

    public OnClickListener getOnclickListner()
    {
        return onClickListener;
    }

    public void setOnclickLinstener(OnClickListener onClickListener)
    {
        this.onClickListener = onClickListener;
    }

    public void addFooterView(View v) 
    {
        mFooterView = v;
    }

    public void addHeaderView(View v) 
    {
        mHeaderView = v;
    }

    public void setDivider(Drawable divider) {
        
        if (divider != null) {
            mDividerHeight = divider.getIntrinsicHeight();
            mClipDivider = divider instanceof ColorDrawable;
        } else {
            mDividerHeight = 0;
            mClipDivider = false;
        }
        
        mDivider = divider;
    }
    
    public void setDividerHeight(int height) {
        mDividerHeight = height;
    }

    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        // This widget draws the same divider for all children
        final Drawable divider = mDivider;
        final boolean clipDivider = mClipDivider;

        if (!clipDivider) {
            divider.setBounds(bounds);
        } else {
            canvas.save();
            canvas.clipRect(bounds);
        }

        divider.draw(canvas);

        if (clipDivider) {
            canvas.restore();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) 
    {
        // Draw the indicators (these should be drawn above the dividers) and children
        super.dispatchDraw(canvas);

        // Draw the dividers
        final int dividerHeight = mDividerHeight;

        if (dividerHeight > 0 && mDivider != null) 
        {
//            final Rect mTempRect = new Rect();
//            // Only modify the top and bottom in the loop, we set the left and right here
//            getDrawingRect(mTempRect);
            final Rect bounds = new Rect();
            int mPaddingLeft = super.getPaddingLeft();
            int mPaddingRight = super.getPaddingRight();
            int mRight = super.getRight();
            int mLeft = super.getLeft();
            int mBottom = super.getBottom();
            int mTop = super.getTop();

            bounds.left = mPaddingLeft;
            bounds.right = mRight - mLeft - mPaddingRight;

            final int count = getChildCount();

            int bottom;
            int listBottom = mBottom - mTop - 0;//mListPadding.bottom;

            for (int i = 0; i < count; i++) 
            {
                View child = getChildAt(i);
                
                bottom = child.getBottom();
                // Don't draw dividers next to items that are not enabled
                if (bottom <= listBottom) 
                {
                    bounds.top = bottom;
                    bounds.bottom = bottom + dividerHeight;
                    drawDivider(canvas, bounds, i);
                }
            }
        }
    }
}

