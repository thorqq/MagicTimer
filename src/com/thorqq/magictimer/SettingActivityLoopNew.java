package com.thorqq.magictimer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingActivityLoopNew extends Activity
{
    private Button mBtnLoopPolicyDay;
    private Button mBtnLoopPolicyWeek;
    private Button mBtnLoopPolicyMon;
    private Button mBtnLoopPolicyYYmd;

    private Button mBtnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initLayout();
        updateLayout();
        regListener();
    }
    
    private void regListener()
    {

        mBtnLoopPolicyDay.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            }
        });

        mBtnLoopPolicyWeek.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            }
        });

        mBtnLoopPolicyMon.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            }
        });

        mBtnLoopPolicyYYmd.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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

    private void initLayout()
    {
        setContentView(R.layout.loop_setting_add_new);

        mBtnCancel = (Button) findViewById(R.id.btnTimeSettingCancel);
        
        mBtnLoopPolicyDay = (Button) findViewById(R.id.buttonAddLoopDay);
        mBtnLoopPolicyWeek = (Button) findViewById(R.id.buttonAddLoopWeek);
        mBtnLoopPolicyMon = (Button) findViewById(R.id.buttonAddLoopMon);
        mBtnLoopPolicyYYmd = (Button) findViewById(R.id.buttonAddLoopYYmd);
    }

    private void updateLayout()
    {
    }

}
