package com.thorqq.magictimer;

import java.util.Calendar;

import com.thorqq.magictimer.core.TLoopPolicyDay;
import com.thorqq.magictimer.core.TimerCalculator;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;

public class SettingActivityDayLoop extends Activity
{
    private TLoopPolicyDay mDayLoopPolicy;
    private DatePicker mDatePicker;
    private EditText mEtMaxCount;
    private EditText mEtLoopDays;
    private Button   mBtnDayMask;
    private CheckBox mCBInfinity;
    private Button mBtnOK;
    private Button mBtnCancel;
    
    boolean[] mCheckedItems = null;
    boolean[] mTmpCheckedItems = null;
    
    //mLunarFlag �ݲ�ʹ��

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mDayLoopPolicy = (TLoopPolicyDay) getIntent().getSerializableExtra(
                TimerMgr.ALARM_INTENT_LOOP_POLICY);
        if (mDayLoopPolicy == null)
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
        mBtnDayMask.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int days = Util.strToInt(mEtLoopDays.getText().toString());
                Util.log("days = " + days);
                if(days <= 0)
                {
                    AlertDialog.Builder builder = new Builder(SettingActivityDayLoop.this);
                    builder.setTitle("����");
                    builder.setMessage("ѭ�������Ƿ�������������");
                    builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();
                    return;
                }
                
                //��ѡ��ʾ������
                String[] arrayItems = new String[days];
                for(int i = 0; i < days; i++)
                {
                    arrayItems[i] = "��" + (i+1) + "��";
                }
                
                //�ļ���Ҫ��ѡ��
                mTmpCheckedItems = mCheckedItems.clone();
                if(mTmpCheckedItems.length != days)
                {
                    mTmpCheckedItems = new boolean[days];
                }
                
                new AlertDialog.Builder(SettingActivityDayLoop.this)
                        .setTitle("��ѡ��������")
                        .setMultiChoiceItems(arrayItems, mTmpCheckedItems, 
                                new OnMultiChoiceClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which, boolean isChecked)
                                    {
        
                                    }
                                })
                        .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() 
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                int flag = 0;
                                for(boolean b: mTmpCheckedItems)
                                {
                                    if(b == true)
                                    {
                                        flag = 1;
                                        break;
                                    }
                                }
                                
                                if(flag == 0)
                                {
                                    displayValidDialog("û������������");
                                    return;
                                }
                                
                                mCheckedItems = mTmpCheckedItems.clone();
                            }
                        })
                        .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
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

        mBtnOK.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //��������ֵ�ĺϷ���
                if(Util.strToInt(mEtLoopDays.getText().toString()) != mCheckedItems.length)
                {
                    displayValidDialog("������������");
                    return;
                }
                getValueFromLayout();
                String validInfo = mDayLoopPolicy.CheckValidity();
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

    //����У�������ʾ�Ի���
    private void displayValidDialog(String validInfo)
    {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("����");
        builder.setMessage(validInfo);
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void initLayout()
    {
        setContentView(R.layout.loop_setting_day);
        mDatePicker = (DatePicker) findViewById(R.id.datePicker);
        mEtMaxCount = (EditText) findViewById(R.id.editTextMaxCount);
        mEtLoopDays = (EditText) findViewById(R.id.editTextLoopDays);
        mBtnDayMask = (Button) findViewById(R.id.btnDayMask);
        mCBInfinity = (CheckBox) findViewById(R.id.checkBoxInfinity);
        mBtnOK = (Button) findViewById(R.id.btnTimeSettingSave);
        mBtnCancel = (Button) findViewById(R.id.btnTimeSettingCancel);
    }

    private void updateLayout()
    {
        int year = mDayLoopPolicy.getStartDate().get(Calendar.YEAR);
        int monthOfYear = mDayLoopPolicy.getStartDate().get(Calendar.MONTH);
        int dayOfMonth = mDayLoopPolicy.getStartDate().get(Calendar.DATE);
        mDatePicker.init(year, monthOfYear, dayOfMonth, new DatePicker.OnDateChangedListener()
        {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                mDayLoopPolicy.getStartDate().set(Calendar.YEAR,  year);
                mDayLoopPolicy.getStartDate().set(Calendar.MONTH, monthOfYear);
                mDayLoopPolicy.getStartDate().set(Calendar.DATE,  dayOfMonth);
            }
        });
        //mEtMaxCount.setText(Util.intToStr(mDayLoopPolicy.getMaxCount()));
        mEtLoopDays.setText(Util.intToStr(mDayLoopPolicy.getLoopDays()));
        
        if(mDayLoopPolicy.getMaxCount() == TimerCalculator.INFINITY_COUNT)
        {
            mCBInfinity.setChecked(true);
            mEtMaxCount.setEnabled(false);
            mEtMaxCount.setText("1");
        }
        else if(mDayLoopPolicy.getMaxCount() >= 1)
        {
            mCBInfinity.setChecked(false);
            mEtMaxCount.setEnabled(true);
            mEtMaxCount.setText(Util.intToStr(mDayLoopPolicy.getMaxCount()));
        }
        else
        {
            mCBInfinity.setChecked(false);
            mEtMaxCount.setEnabled(true);
            mEtMaxCount.setText("1");
        }
    
        int len = mDayLoopPolicy.getDayMask().length;
        char[] arr = mDayLoopPolicy.getDayMask();
        mCheckedItems = new boolean[len];
        for(int i = 0; i < len; i++)
        {
            mCheckedItems[i] = (arr[i] == '1' ? true : false);
        }
    }

    private void getValueFromLayout()
    {
        int maxCount = 1;
        if(mCBInfinity.isChecked())
        {
            maxCount = TimerCalculator.INFINITY_COUNT;
        }
        else
        {
            maxCount = Util.strToInt(mEtMaxCount.getText().toString());
        }
        
        char[] dayMask = new char[mCheckedItems.length];
        for(int i = 0; i < mCheckedItems.length; i++)
        {
            dayMask[i] = (mCheckedItems[i] == true ? '1' : '0');
        }
        
        mDayLoopPolicy.setParam(
                Util.strToInt(mEtLoopDays.getText().toString()), 
                maxCount, 
                dayMask, 
                mDayLoopPolicy.getStartDate(), 
                mDayLoopPolicy.getLunarFlag());
        
    }    

    private void setResult()
    {
        Intent i = new Intent();
        i.putExtra(TimerMgr.ALARM_INTENT_LOOP_POLICY, mDayLoopPolicy);

        setResult(RESULT_OK, i);
        finish();
    }

}
