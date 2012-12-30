package com.thorqq.magictimer;

import com.thorqq.magictimer.core.TLoopPolicyWeek;
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

public class SettingActivityWeekLoop extends Activity
{
    private TLoopPolicyWeek mWeekLoopPolicy;

    private Button mBtnOK;
    private Button mBtnCancel;
    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mWeekLoopPolicy = (TLoopPolicyWeek) getIntent().getSerializableExtra(
                TimerMgr.ALARM_INTENT_LOOP_POLICY);
        if (mWeekLoopPolicy == null)
        {
            Util.log("mLoopPolicy is null");
            return;
        }

        initLayout();
        updateLayout();
        regListener();
    }
    
    private void regListener()
    {

        mBtnOK.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //TODO 检验输入值的合法性
                getValueFromLayout();
                String validInfo = mWeekLoopPolicy.CheckValidity();
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

    private void initLayout()
    {
        setContentView(R.layout.loop_setting_week);

        mBtnOK = (Button) findViewById(R.id.btnTimeSettingSave);
        mBtnCancel = (Button) findViewById(R.id.btnTimeSettingCancel);
    }

    private void updateLayout()
    {
        char[] arr = mWeekLoopPolicy.getDayMask();
        if(arr.length != TLoopPolicyWeek.DAYS_OF_WEEK)
        {
            return;
        }
            
        ((CheckBox) findViewById(R.id.checkBoxDayOfWeek1)).setChecked(arr[0] == '1' ? true : false);
        ((CheckBox) findViewById(R.id.checkBoxDayOfWeek2)).setChecked(arr[1] == '1' ? true : false);
        ((CheckBox) findViewById(R.id.checkBoxDayOfWeek3)).setChecked(arr[2] == '1' ? true : false);
        ((CheckBox) findViewById(R.id.checkBoxDayOfWeek4)).setChecked(arr[3] == '1' ? true : false);
        ((CheckBox) findViewById(R.id.checkBoxDayOfWeek5)).setChecked(arr[4] == '1' ? true : false);
        ((CheckBox) findViewById(R.id.checkBoxDayOfWeek6)).setChecked(arr[5] == '1' ? true : false);
        ((CheckBox) findViewById(R.id.checkBoxDayOfWeek7)).setChecked(arr[6] == '1' ? true : false);
    }

    private void getValueFromLayout()
    {
        
        char[] dayMask = new char[TLoopPolicyWeek.DAYS_OF_WEEK];
        
        dayMask[0] = (((CheckBox) findViewById(R.id.checkBoxDayOfWeek1)).isChecked() == true ? '1' : '0');
        dayMask[1] = (((CheckBox) findViewById(R.id.checkBoxDayOfWeek2)).isChecked() == true ? '1' : '0');
        dayMask[2] = (((CheckBox) findViewById(R.id.checkBoxDayOfWeek3)).isChecked() == true ? '1' : '0');
        dayMask[3] = (((CheckBox) findViewById(R.id.checkBoxDayOfWeek4)).isChecked() == true ? '1' : '0');
        dayMask[4] = (((CheckBox) findViewById(R.id.checkBoxDayOfWeek5)).isChecked() == true ? '1' : '0');
        dayMask[5] = (((CheckBox) findViewById(R.id.checkBoxDayOfWeek6)).isChecked() == true ? '1' : '0');
        dayMask[6] = (((CheckBox) findViewById(R.id.checkBoxDayOfWeek7)).isChecked() == true ? '1' : '0');

        mWeekLoopPolicy.setParam(dayMask);
    }    

    private void setResult()
    {
        Intent i = new Intent();
        i.putExtra(TimerMgr.ALARM_INTENT_LOOP_POLICY, mWeekLoopPolicy);

        setResult(RESULT_OK, i);
        finish();
    }

}
