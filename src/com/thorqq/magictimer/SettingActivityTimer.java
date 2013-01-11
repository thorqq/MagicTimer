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
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingActivityTimer extends Activity
{
    public static final int REQUEST_CODE_TIME_SETTING = 1;
    public static final int REQUEST_CODE_LOOP_SETTING = 2;
    public static final int REQUEST_CODE_ACTION_SETTING = 3;
        
    private Timer mTimer;

    //time & name
    private LinearLayout mLayoutTime;
    private TextView mTvTime;
    private TextView mTvName;
    
    //policy
    private Button mBtnAddLoopPolicy;
    private ArrayList<Map<String, Object>> mLoopPolicyList = new ArrayList<Map<String, Object>>();
    private AdapterLoopPolicy mLoopPolicyAdapter;
    private ListViewNoScroll mLoopPolicyListView;
    private View mPolicyItemHeader;

    //action
    private Button mBtnAddAction;
    private ArrayList<Map<String, Object>> mActionList = new ArrayList<Map<String, Object>>();
    private AdapterAction mActionAdapter;
    private ListViewNoScroll mActionListView;
    private View mActionItemHeader;

    //save & cancel
    private Button mBtnSave;
    private Button mBtnCancel;

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
        //time
        mTvTime = (TextView) findViewById(R.id.tvTime);
        mLayoutTime = (LinearLayout) findViewById(R.id.LinearLayoutTimeSettingTime);
        mTvTime.setTypeface(((MagicTimerApp)getApplicationContext()).getNumTypeFace());
        
        //name
        mTvName = (TextView) findViewById(R.id.tvName);
                
        //循环策略
        mLoopPolicyListView = (ListViewNoScroll)findViewById(R.id.ListViewNoScrollLoopPolicy);
        mLoopPolicyAdapter = new AdapterLoopPolicy(this, mLoopPolicyList);
        mLoopPolicyListView.setAdapter(mLoopPolicyAdapter);
        mLoopPolicyListView.setDivider(this.getResources().getDrawable(R.drawable.gradient_shape_hor));
        mLoopPolicyListView.setDividerHeight(1);
        
        mPolicyItemHeader = getLayoutInflater().inflate(R.layout.policy_item_header, null);
        mLoopPolicyListView.addHeaderView(mPolicyItemHeader);
        mBtnAddLoopPolicy = (Button) mPolicyItemHeader.findViewById(R.id.btnAddLoopPolicy);

        //定时动作
        mActionListView = (ListViewNoScroll)findViewById(R.id.ListViewNoScrollAction);
        mActionAdapter = new AdapterAction(this, mActionList);
        mActionListView.setAdapter(mActionAdapter);
        mActionListView.setDivider(this.getResources().getDrawable(R.drawable.gradient_shape_hor));
        mActionListView.setDividerHeight(1);
        
        mActionItemHeader = getLayoutInflater().inflate(R.layout.action_item_header, null);
        mActionListView.addHeaderView(mActionItemHeader);
        mBtnAddAction = (Button) mActionItemHeader.findViewById(R.id.btnAddAction);
        
        //
        mBtnSave = (Button) findViewById(R.id.btnSave);
        mBtnCancel = (Button) findViewById(R.id.btnCancel);
    }
    
    private void updatePolicyLayout()
    {
        // 循环策略
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
        //time
        mTvTime.setText(mTimer.getTimerDef().getDescription() + " ");
        //name
        mTvName.setText(mTimer.getName());
    }

    private void updateActionLayout()
    {
        // 定时动作
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
        updateTimerDefLayout();
        updatePolicyLayout();
        updateActionLayout();
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

    private void registerListener()
    {
        // 启动时间设置
        mLayoutTime.setOnClickListener(new LinearLayout.OnClickListener()
        {
            public void onClick(View v)
            {
                Util.log("mLayoutTime onClick");
                // 弹出启动时间设置界面
                startTimerSettingActivity(mTimer);
            }

        });

        // 新增循环策略
        mBtnAddLoopPolicy.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent i = new Intent(SettingActivityTimer.this, SettingActivityLoopNew.class);
                startActivityForResult(i, REQUEST_CODE_LOOP_SETTING);
            }

        });
        
        // 新增动作
        mBtnAddAction.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
//                Intent i = new Intent(SettingActivityTimer.this, SettingActivityActionNew.class);
//                startActivityForResult(i, REQUEST_CODE_ACTION_SETTING);
            }

        });
        

        // 保存
        mBtnSave.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                if(mTvName.getText().length() == 0)
                {
                    displayValidDialog("请输入闹钟名称");
                    return;
                }
                
                mTimer.setName(mTvName.getText().toString());
                
                //循环策略
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
                    displayValidDialog("没有一个有效的循环策略");
                    return;
                }
                

                //定时动作
                mTimer.getTActions().clear();
                cnt = mActionList.size();
                if(cnt == 0)
                {
                    displayValidDialog("没有一个有效的定时动作");
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

        // 取消
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
        Intent i = new Intent(this, SettingActivityTimeDef.class);
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
