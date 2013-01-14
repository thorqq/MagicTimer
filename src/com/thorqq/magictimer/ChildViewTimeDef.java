package com.thorqq.magictimer;

import com.thorqq.magictimer.core.TTimerDef;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerCalculator;
import com.thorqq.magictimer.util.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;


public class ChildViewTimeDef implements ChildViewInterface
{
    private View mView;
    private Timer mTimer;
    private Context mContext;
    private LayoutInflater mInflater;

    private TTimerDef mTimerDef;
    private TimePicker mTimePicker;
    private EditText mEtMaxCount;
    private EditText mEtInterval;
    private CheckBox mCBInfinity;

    private boolean mInitFlag = false;
    
    public ChildViewTimeDef(Context context, Timer timer)
    {
        mContext = context;
        mTimer = timer;
        mTimerDef = mTimer.getTimerDef();
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
        
        mTimePicker.setIs24HourView(true);
        mTimePicker.setCurrentHour(mTimerDef.getStartHour());
        mTimePicker.setCurrentMinute(mTimerDef.getStartMinute());
        mEtInterval.setText(Util.intToStr(mTimerDef.getInterval()));
        
        if(mTimerDef.getMaxCount() == TimerCalculator.INFINITY_COUNT)
        {
            mCBInfinity.setChecked(true);
            mEtMaxCount.setEnabled(false);
            mEtMaxCount.setText("1");
        }
        else if(mTimerDef.getMaxCount() >= 1)
        {
            mCBInfinity.setChecked(false);
            mEtMaxCount.setEnabled(true);
            mEtMaxCount.setText(Util.intToStr(mTimerDef.getMaxCount()));
        }
        else
        {
            mCBInfinity.setChecked(false);
            mEtMaxCount.setEnabled(true);
            mEtMaxCount.setText("1");
        }
        
    }

    @Override
    public void updateData()
    {
        if(mInitFlag == false)
        {
            initLayout();
        }

        mTimerDef.setStartHour(mTimePicker.getCurrentHour());
        mTimerDef.setStartMinute(mTimePicker.getCurrentMinute());
        mTimerDef.setInterval(Util.strToInt(mEtInterval.getText().toString()));
        
        if(mCBInfinity.isChecked())
        {
            mTimerDef.setMaxCount(TimerCalculator.INFINITY_COUNT);
        }
        else
        {
            mTimerDef.setMaxCount(Util.strToInt(mEtMaxCount.getText().toString()));
        }
    }

    @Override
    public void initLayout()
    {
        Util.log("init ChildViewTimeDef");
        mInflater = LayoutInflater.from(mContext);
        mView = mInflater.inflate(R.layout.timedef_setting_child, null);
        
        mTimePicker = (TimePicker) mView.findViewById(R.id.timePicker);
        mEtMaxCount = (EditText) mView.findViewById(R.id.editTextMaxCount);
        mCBInfinity = (CheckBox) mView.findViewById(R.id.checkBoxInfinity);
        mEtInterval = (EditText) mView.findViewById(R.id.editTextInterval);
        
        registerListener();     
        
        mInitFlag = true;
    }

    private void registerListener()
    {
        mCBInfinity.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked)
            {
                mEtMaxCount.setEnabled(!isChecked);
            }
        });
    }
  
    
}
