package com.thorqq.magictimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.thorqq.magictimer.action.TAction;
import com.thorqq.magictimer.core.TLoopPolicy;
import com.thorqq.magictimer.core.TTimerDef;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.db.DBHelper;
import com.thorqq.magictimer.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivityTimer extends Activity
{
    public static final int REQUEST_CODE_TIME_SETTING = 1;
    public static final int REQUEST_CODE_LOOP_SETTING = 2;
    public static final int REQUEST_CODE_ACTION_SETTING = 3;
    
    public static final int BUTTON_NAME = 0;
    public static final int BUTTON_REMARK = 1;
    
    private Timer mTimer;

    private EditText mEtTimerName;
    private EditText mEtTimerRemark;

    private Button mBtnTime;

    private Button mBtnSave;
    private Button mBtnCancel;
    
    private Button mBtnAddLoopPolicy;
    private Button mBtnAddAction;

    private ArrayList<Map<String, Object>> mLoopPolicyList = new ArrayList<Map<String, Object>>();
    private AdapterLoopPolicy mLoopPolicyAdapter;
    private ListViewNoScroll mLoopPolicyListView;
    private View mPolicyItemHeader;

    private ArrayList<Map<String, Object>> mActionList = new ArrayList<Map<String, Object>>();
    private AdapterAction mActionAdapter;
    private ListViewNoScroll mActionListView;
    private View mActionItemHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_setting);

        mTimer = getIntent().getParcelableExtra(TimerMgr.ALARM_INTENT_EXTRA);
        if (mTimer == null)
        {
            Util.log("mTimer is null");
            return;
        }

        initLayout();
        updateLayout();
        registerListener();
    }
    
    private void initLayout()
    {
        mEtTimerName = (EditText) findViewById(R.id.editTextTimerNmae);
        mEtTimerRemark = (EditText) findViewById(R.id.editTextRemark);
        
        mBtnTime = (Button) findViewById(R.id.btnStartTime);

        mBtnSave = (Button) findViewById(R.id.btnSave);
        mBtnCancel = (Button) findViewById(R.id.btnCancel);
                
        //ѭ������
        mLoopPolicyListView = (ListViewNoScroll)findViewById(R.id.ListViewNoScrollLoopPolicy);
        mLoopPolicyAdapter = new AdapterLoopPolicy(this, mLoopPolicyList);
        mLoopPolicyListView.setAdapter(mLoopPolicyAdapter);
        mLoopPolicyListView.setDivider(this.getResources().getDrawable(R.drawable.gradient_shape_hor));
        mLoopPolicyListView.setDividerHeight(1);
        
        mPolicyItemHeader = getLayoutInflater().inflate(R.layout.policy_item_header, null);
        mLoopPolicyListView.addHeaderView(mPolicyItemHeader);
        mBtnAddLoopPolicy = (Button) mPolicyItemHeader.findViewById(R.id.btnAddLoopPolicy);

        //��ʱ����
        mActionListView = (ListViewNoScroll)findViewById(R.id.ListViewNoScrollAction);
        mActionAdapter = new AdapterAction(this, mActionList);
        mActionListView.setAdapter(mActionAdapter);
        mActionListView.setDivider(this.getResources().getDrawable(R.drawable.gradient_shape_hor));
        mActionListView.setDividerHeight(1);
        
        mActionItemHeader = getLayoutInflater().inflate(R.layout.timing_action_item_header, null);
        mActionListView.addHeaderView(mActionItemHeader);
        mBtnAddAction = (Button) mActionItemHeader.findViewById(R.id.btnAddAction);
    }
    
    private void updatePolicyLayout()
    {
        // ѭ������
        mLoopPolicyList.clear();
        for (TLoopPolicy policy : mTimer.getLoopPolicys())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("policy", policy);
            map.put("listener", new LoopPolicyButtonListener(this, policy));
            mLoopPolicyList.add(map);
        }

        mLoopPolicyAdapter.notifyDataSetChanged();        
    }
    
    private void updateTimerDefLayout()
    {
        // ����ʱ��
        mBtnTime.setText(mTimer.getTimerDef().getDescription());
    }

    private void updateActionLayout()
    {
        // ��ʱ����
        mActionList.clear();
        for (TAction action : mTimer.getTActions())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("action", action);
            mActionList.add(map);
        }

        mActionAdapter.notifyDataSetChanged();
    }
    
    private void updateLayout()
    {
        // ���ơ���ע
        mEtTimerName.setText(mTimer.getName());
        mEtTimerRemark.setText(mTimer.getRemark());

        updateTimerDefLayout();
        updatePolicyLayout();
        updateActionLayout();
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

//    private void displayInputDialog(String title, final int btnType)
//    {
//        //TODO: ������editText������ַ���
//        final EditText editText = new EditText(this);
//        if(btnType == BUTTON_NAME)
//        {
//            editText.setText(mTimer.getName());
//        }
//        else if(btnType == BUTTON_REMARK)
//        {
//            editText.setText(mTimer.getRemark());
//        }
//        
//        AlertDialog.Builder builder = new Builder(this);
//        builder.setTitle(title);
//        builder.setView(editText);
//        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                
//                if(btnType == BUTTON_NAME)
//                {
//                    if(editText.getText().length() <= 0)
//                    {
//                        displayValidDialog("��������������");
//                    }
//                    else
//                    {
//                        mBtnTimerName.setText(editText.getText());
//                        mTimer.setName(editText.getText().toString());
//                    }
//                }
//                else if(btnType == BUTTON_REMARK)
//                {
//                    mBtnTimerRemark.setText(editText.getText());
//                    mTimer.setRemark(editText.getText().toString());
//                }
//            }
//        });
//        builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        builder.create().show();
//    }

    private void registerListener()
    {
//        // ����
//        mBtnTimerName.setOnClickListener(new Button.OnClickListener()
//        {
//            public void onClick(View v)
//            {
//                displayInputDialog("��������������", BUTTON_NAME);
//            }
//
//        });
//
//        // ��ע
//        mBtnTimerRemark.setOnClickListener(new Button.OnClickListener()
//        {
//            public void onClick(View v)
//            {
//                displayInputDialog("�����뱸ע��Ϣ", BUTTON_REMARK);
//            }
//
//        });

        // ����ʱ������
        mBtnTime.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                // ��������ʱ�����ý���
                startTimerSettingActivity(mTimer);
            }

        });

        // ����ѭ������
        mBtnAddLoopPolicy.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent i = new Intent(SettingActivityTimer.this, SettingActivityLoopNew.class);
                startActivityForResult(i, REQUEST_CODE_LOOP_SETTING);
            }

        });
        
        // ��������
        mBtnAddAction.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
//                Intent i = new Intent(SettingActivityTimer.this, SettingActivityActionNew.class);
//                startActivityForResult(i, REQUEST_CODE_ACTION_SETTING);
            }

        });
        

        // ����
        mBtnSave.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                if(mEtTimerName.getText().length() == 0)
                {
                    displayValidDialog("��������������");
                    return;
                }
                
                mTimer.setName(mEtTimerName.getText().toString());
                mTimer.setRemark(mEtTimerRemark.getText().toString());
                
                //ѭ������
                mTimer.getLoopPolicys().clear();
                int cnt = mLoopPolicyList.size();
                int excludeFlag = 1;
                for(int i = 0; i < cnt; i++)
                {
                    TLoopPolicy policy = (TLoopPolicy) mLoopPolicyList.get(i).get("policy");
                    mTimer.getLoopPolicys().add(policy);
                    
                    if(policy.getExcludeFlag() == 0)
                    {
                        excludeFlag = 0;
                    }
                }
                if(excludeFlag == 1)
                {
                    displayValidDialog("û��һ����Ч��ѭ������");
                    return;
                }
                

                //��ʱ����
                mTimer.getTActions().clear();
                cnt = mActionList.size();
                if(cnt == 0)
                {
                    displayValidDialog("û��һ����Ч�Ķ�ʱ����");
                    return;
                }
                for(int i = 0; i < cnt; i++)
                {
                    TAction action = (TAction) mActionList.get(i).get("action");
                    mTimer.getTActions().add(action);
                }

                DBHelper.getIntance().updateTimer(mTimer);

                Intent i = new Intent();
                i.putExtra(TimerMgr.ALARM_INTENT_EXTRA, mTimer);
                setResult(RESULT_OK, i);
                finish();
            }
        });

        // ȡ��
        mBtnCancel.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
            }
        });

    }

    private void startTimerSettingActivity(Timer timer)
    {
        Intent i = new Intent(this, SettingActivityTime.class);
        i.putExtra(TimerMgr.ALARM_INTENT_TIMER_DEF, timer.getTimerDef());
        startActivityForResult(i, REQUEST_CODE_TIME_SETTING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (RESULT_OK != resultCode)
        {
            return;
        }

        switch (requestCode)
        {
        case REQUEST_CODE_TIME_SETTING:
            TTimerDef timerDef = (TTimerDef) data.getSerializableExtra(TimerMgr.ALARM_INTENT_TIMER_DEF);
            if (timerDef != null)
            {
                mTimer.setTimerDef(timerDef);
                updateTimerDefLayout();
            }
            break;
        case REQUEST_CODE_LOOP_SETTING:
            TLoopPolicy policy = (TLoopPolicy) data.getSerializableExtra(TimerMgr.ALARM_INTENT_LOOP_POLICY);
            if (policy != null)
            {
                Util.log("mTimer.setLoopPolicy: " + policy.getDisplayOrder());
                mTimer.setLoopPolicy(policy);
                updatePolicyLayout();
            }
            break;
        default:
            break;
        }
    }

    ///////////////
    class LoopPolicyButtonListener implements OnClickListener 
    {
        private TLoopPolicy mPolicy;
        private Context mContext;

        LoopPolicyButtonListener(Context context, TLoopPolicy policy) 
        {
            mContext = context;
            mPolicy = policy;
        }
        
        @Override
        public void onClick(View v) 
        {
            Util.log("Click policy " + mPolicy.getPolicyType());
            
            Intent i = null;
            if(mPolicy.getPolicyType() == TLoopPolicy.LOOP_POLICY_DAY)
            {
                i = new Intent(mContext, SettingActivityDayLoop.class);
            }
            else if(mPolicy.getPolicyType() == TLoopPolicy.LOOP_POLICY_MONTH)
            {
                i = new Intent(mContext, SettingActivityMonLoop.class);
            }
            else if(mPolicy.getPolicyType() == TLoopPolicy.LOOP_POLICY_WEEK)
            {
                i = new Intent(mContext, SettingActivityWeekLoop.class);
            }
            else
            {
                Util.log("Unknown policy type: " + mPolicy.getPolicyType());
                return;
            }
            i.putExtra(TimerMgr.ALARM_INTENT_LOOP_POLICY, mPolicy);
            startActivityForResult(i, SettingActivityTimer.REQUEST_CODE_LOOP_SETTING);
        }
    }
}
