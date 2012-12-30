package com.thorqq.magictimer;

import com.thorqq.magictimer.core.TLoopPolicyMon;
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

public class SettingActivityMonLoop extends Activity
{
    private TLoopPolicyMon mMonLoopPolicy;

    private Button mBtnOK;
    private Button mBtnCancel;
    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mMonLoopPolicy = (TLoopPolicyMon) getIntent().getSerializableExtra(
                TimerMgr.ALARM_INTENT_LOOP_POLICY);
        if (mMonLoopPolicy == null)
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
                String validInfo = mMonLoopPolicy.CheckValidity();
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
        setContentView(R.layout.loop_setting_mon);

        mBtnOK = (Button) findViewById(R.id.btnTimeSettingSave);
        mBtnCancel = (Button) findViewById(R.id.btnTimeSettingCancel);
    }

    private void updateLayout()
    {
    }

    private void getValueFromLayout()
    {
    }    

    private void setResult()
    {
        Intent i = new Intent();
        i.putExtra(TimerMgr.ALARM_INTENT_LOOP_POLICY, mMonLoopPolicy);

        setResult(RESULT_OK, i);
        finish();
    }

}
