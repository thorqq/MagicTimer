package com.thorqq.magictimer;

import com.thorqq.magictimer.core.Timer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;


public class ChildViewTimerItem implements ChildViewInterface
{
    private View mView;
    private Timer mTimer;
    private Context mContext;
    private LayoutInflater mInflater;
    
    private View mLayoutTest;
    private View mLayoutDelete;
    
    private boolean mInitFlag = false;
    
    public ChildViewTimerItem(Context context, Timer timer)
    {
        mContext = context;
        mTimer = timer;
    }

    @Override
    public void initLayout()
    {
        mInflater = LayoutInflater.from(mContext);
        mView = mInflater.inflate(R.layout.timer_item_child, null);
        
        mLayoutTest   = (LinearLayout)mView.findViewById(R.id.LayoutTest);
        mLayoutDelete = (LinearLayout)mView.findViewById(R.id.LayoutDelete);
        
        updateLayout();
        registerListener();
        
        mInitFlag = true;
    }

    @Override
    public void updateLayout()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateData()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void registerListener()
    {
        mLayoutTest.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                
            }
        });

        mLayoutDelete.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    public View getLayoutView()
    {
        if(mInitFlag == false)
        {
            initLayout();
        }
        return mView;
    }
    
  
    
}
