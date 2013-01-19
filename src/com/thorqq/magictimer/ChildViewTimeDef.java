package com.thorqq.magictimer;

import com.thorqq.magictimer.core.TTimerDef;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerCalculator;
import com.thorqq.magictimer.util.Util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;


public class ChildViewTimeDef extends ChildViewInterface
{
    private ChildViewParent mParent;
    private View mView;
    private Timer mTimer;
    private Context mContext;
    private LayoutInflater mInflater;

    private TTimerDef mTimerDef;
    private TimePicker mTimePicker;
    private EditText mEtMaxCount;
    private EditText mEtInterval;
    private CheckBox mCBInfinity;
    
//    private Button mBtnSave;
//    private Button mBtnCancel;

//    private boolean mInitFlag = false;
    
    public ChildViewTimeDef(Context context, Timer timer)
    {
        mContext = context;
        mTimer = timer;
        mTimerDef = mTimer.getTimerDef();
    }

    @Override
    public View getLayoutView()
    {
        return mView;
    }

    @Override
    public void updateLayout()
    {        
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
    protected void doInitLayout(ChildViewParent parent)
    {
        Util.log("init ChildViewTimeDef");
        mInflater = LayoutInflater.from(mContext);
        mView = mInflater.inflate(R.layout.timedef_item_child, null);
        mParent = parent;
        
        mTimePicker = (TimePicker) mView.findViewById(R.id.timePicker);
        mEtMaxCount = (EditText) mView.findViewById(R.id.editTextMaxCount);
        mCBInfinity = (CheckBox) mView.findViewById(R.id.checkBoxInfinity);
        mEtInterval = (EditText) mView.findViewById(R.id.editTextInterval);        
   }

    protected void registerListener()
    {
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener()
        {
            @Override
            public void onTimeChanged(TimePicker view , int hourOfDay , int minute )
            {
                mTimerDef.setStartHour(hourOfDay);
                mTimerDef.setStartMinute(minute);     
                
                mParent.dataUpdateNotify();
            }
        });
                
        mCBInfinity.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked)
            {
                mEtMaxCount.setEnabled(!isChecked);
                
                if(isChecked)
                {
                    mTimerDef.setMaxCount(TimerCalculator.INFINITY_COUNT);
                }
                else
                {
                    mTimerDef.setMaxCount(Util.strToInt(mEtMaxCount.getText().toString()));
                }
            }
        });
        
        mEtMaxCount.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable arg0)
            {                
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
            {                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(mCBInfinity.isChecked())
                {
                    mTimerDef.setMaxCount(TimerCalculator.INFINITY_COUNT);
                }
                else
                {
                    mTimerDef.setMaxCount(Util.strToInt(s.toString()));
                }                
            }
            
        });

        mEtInterval.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable arg0)
            {                
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
            {                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                mTimerDef.setInterval(Util.strToInt(s.toString()));
            }
            
        });

    }
    
}
