package com.thorqq.magictimer;

import com.thorqq.magictimer.core.Timer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;


public class ChildViewTimeDef implements ChildViewInterface
{
    private View mView;
    private Timer mTimer;
    private Context mContext;
    private LayoutInflater mInflater;
        
    private boolean mInitFlag = false;
    
    public ChildViewTimeDef(Context context, Timer timer)
    {
        mContext = context;
        mTimer = timer;
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

    @Override
    public void updateLayout()
    {
        if(mInitFlag == false)
        {
            initLayout();
        }
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateData()
    {
        if(mInitFlag == false)
        {
            initLayout();
        }
        // TODO Auto-generated method stub
    }

    @Override
    public void initLayout()
    {
        mInflater = LayoutInflater.from(mContext);
        mView = mInflater.inflate(R.layout.timedef_setting_child, null);
        
        
        registerListener();     
        
        mInitFlag = true;
    }

    private void registerListener()
    {

    }
  
    
}
