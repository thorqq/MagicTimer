package com.thorqq.magictimer;

import com.thorqq.magictimer.core.TTimerDef;
import com.thorqq.magictimer.core.TimerCalculator;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;

public class SettingActivityTimeDef extends Activity
{
    private TTimerDef mTimerDef;
    private TimePicker mTimePicker;
    private EditText mEtMaxCount;
    private EditText mEtInterval;
    private Button mBtnOK;
    private Button mBtnCancel;
    private CheckBox mCBInfinity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mTimerDef = (TTimerDef) getIntent().getSerializableExtra(TimerMgr.ALARM_INTENT_TIMER_DEF);
        if (mTimerDef == null)
        {
            Util.log("mTimer is null");
            finish();
            return;
        }

        initLayout();
        updateLayout();

    }
    
    private void initLayout()
    {
        setContentView(R.layout.time_setting);
        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        mEtMaxCount = (EditText) findViewById(R.id.editTextMaxCount);
        mCBInfinity = (CheckBox) findViewById(R.id.checkBoxInfinity);
        mEtInterval = (EditText) findViewById(R.id.editTextInterval);
        mBtnOK      = (Button) findViewById(R.id.btnTimeSettingSave);
        mBtnCancel  = (Button) findViewById(R.id.btnTimeSettingCancel);

        mBtnOK.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                // TODO 需检验输入值的合法性
                mTimePicker.clearFocus();
                getValueFromLayout();
                String validInfo = mTimerDef.CheckValidity();
                if(validInfo != null)
                {
                    displayValidDialog(validInfo);               
                    return;
                }
                setResult();
            }
        });

        mBtnCancel.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
            }
        });
        
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
    
    //参数校验错误提示对话框
    private void displayValidDialog(String validInfo)
    {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("错误");
        builder.setMessage(validInfo);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void updateLayout()
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
    
    private void getValueFromLayout()
    {
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

    private void setResult()
    {
        Intent i = new Intent();
        i.putExtra(TimerMgr.ALARM_INTENT_TIMER_DEF, mTimerDef);

        setResult(RESULT_OK, i);
        finish();
   }

}
