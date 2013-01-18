package com.thorqq.magictimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.thorqq.magictimer.UpdateManager;
import com.baidu.mobstat.StatService;
import com.thorqq.magictimer.action.ActionMgr;
import com.thorqq.magictimer.core.QuickSetting;
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
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MagicTimerActivity extends Activity implements MsgReceiver
{
    private static final int REQUEST_CODE_TIMER_SETTING = 1;

    private ArrayList<Map<String, Object>> mTimerItemList = new ArrayList<Map<String, Object>>();
    private AdapterTimerItem mTimerItemAdapter;
    private LayoutInflater mInflater;
    
    private View mTimerItemHeader;
    private ListViewNoScroll mTimerListview;
    private View mAddNewTimerLayout;
        
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //TODO 全局异常捕获。临时删除
//        MyCrashHandler handler = MyCrashHandler.getInstance();
//        handler.init(getApplicationContext());
//        Thread.setDefaultUncaughtExceptionHandler(handler);

        mInflater = getLayoutInflater();
        
        //Timer item list view
        mTimerListview = (ListViewNoScroll)findViewById(R.id.ListViewTimerItem);
        mTimerListview.setDivider(this.getResources().getDrawable(R.drawable.gradient_shape_hor));
        mTimerListview.setDividerHeight(1);
        
        mTimerItemAdapter = new AdapterTimerItem(this, mTimerItemList);
        mTimerListview.setAdapter(mTimerItemAdapter);
        
        mTimerItemHeader = mInflater.inflate(R.layout.timer_item_header, null);
        mTimerListview.addHeaderView(mTimerItemHeader);
        
        mAddNewTimerLayout = (View)findViewById(R.id.linearLayoutAddNewTimer);
                       
        //TODO 预置数据。测试用。发布时应删除
        preset();
        
        // 启动闹铃
        TimerMgr.setMsgReceiver(this);
        TimerMgr.setNextTimer(this, TimerMgr.SPEC_TIME_ID_ALL);
        //更新界面
        updateLayout();
        //
        registerListener();
        
        //检查更新
        UpdateManager manager = new UpdateManager(this);
        manager.checkUpdate(UpdateManager.SHOW_NEED_UPDATE_NOTICE_NO);
        
        StatHelper.getInstance(this).printParams();
    }
    
    public void startTimerSettingActivity(Timer timer, int request_code)
    {
        Intent i = new Intent(this, SettingActivityTimer.class);
        i.putExtra(TimerMgr.ALARM_INTENT_EXTRA, timer);
        startActivityForResult(i, request_code);
    }
        
    private void updateLayout()
    {
        Util.log("updateLayout");
        mTimerItemList.clear();
        Vector<Timer> timers = TimerMgr.getAllTimers();

        int i = 0;
        for (Timer t : timers)
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("timer", t);
            
            map.put("listenerNameInfo", new ListenerNameInfo(this, t));
            map.put("listenerTime", new ListenerTime(this, t));
            map.put("listenerMore", new ListenerMore(this,t));
            
            map.put("childView", new ChildViewTimerItem(this, t, this));
            map.put("visibility", View.GONE);
            
            mTimerItemList.add(map);
            ++i;
        }
        
        mTimerItemAdapter.notifyDataSetChanged(); 
        
    }

    private void registerListener()
    {
        // 新增一个timer
        mAddNewTimerLayout.setOnClickListener(new LinearLayout.OnClickListener()
        {
            public void onClick(View v)
            {
                //生成一个timer，并初始化
                Timer t = new Timer();
                
                t.setTimerDef(QuickSetting.setHourMinute(7, 00));
                t.setLoopPolicy(QuickSetting.everyday());
                t.setAction(ActionMgr.getIntance().createAction(t.getTimerDef().getID(), 
                        0, ActionMgr.ACTION_KLAXON,"param1"));
                t.setAction(ActionMgr.getIntance().createAction(t.getTimerDef().getID(), 
                        1, ActionMgr.ACTION_DIALOG,"param1"));
                
                //进入设置界面
                startTimerSettingActivity(t, REQUEST_CODE_TIMER_SETTING);
            }

        });
    }
    
    public void deleteTimer(int timer_id)
    {
        Util.log("delete timer: " + timer_id);
        
        TimerMgr.deleteTimer(timer_id);
        TimerMgr.setNextTimer(this, timer_id);
        
        for(int i = 0; i < mTimerItemList.size(); i++)
        {
            Timer t = (Timer)mTimerItemList.get(i).get("timer");
            if(t.getID() == timer_id)
            {
                mTimerItemList.remove(i);
            }
        }
        
        mTimerItemAdapter.notifyDataSetChanged(); 
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Util.log("requestCode = " + requestCode);
        if(RESULT_OK != resultCode)
        {
            return;
        }
        
        switch (requestCode)
        {
        case REQUEST_CODE_TIMER_SETTING:            
            Timer timer = data.getParcelableExtra(TimerMgr.ALARM_INTENT_EXTRA);
            Util.log("MagicTimerActivity.onActivityResult:" + timer.getTimerDef().getDescription() + " " + timer.getTimerDef().getName());
            Vector<Timer> timers = TimerMgr.getAllTimers();
            if(timer != null)
            {
                int i = 0;
                for(; i < timers.size(); i++)
                {
                    if(timers.get(i).getID() == timer.getID())
                    {
                        timers.set(i, timer);
                        break;
                    }
                }
                
                if(i == timers.size())
                {
                    timers.add(timer);
                }

                TimerMgr.setNextTimer(this, timer.getID());
            }
            
            for(Timer t : timers)
            {
                Util.log("MagicTimerActivity.onActivityResult2:" + t.getTimerDef().getDescription() + " " + t.getTimerDef().getName());
            }
            break;
        default:
            break;
        }
        
        updateLayout();
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * 此处调用基本统计代码
         */
        StatService.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        /**
         * 此处调用基本统计代码
         */
        StatService.onPause(this);
    }

    @Override
    protected void onDestroy()
    {
        DBHelper.getIntance().close();
        super.onDestroy();
        
        //TODO 测试用。发布时应删除
        System.exit(0); 
    }

    public void displayToast(String str)
    {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
    
    public void displayToastLong(String str)
    {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            dialogExit();
        }

        super.onKeyDown(keyCode, event);

        return true;
    }

    public void dialogExit()
    {
        AlertDialog.Builder builder = new Builder(this);
        builder.setMessage("确认退出吗？");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {

            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void getMessage(int msgCode, Object obj)
    {
        Message msg = new Message(); 
        msg.what = msgCode; 
        msg.obj = obj;
        mHandler.sendMessage(msg);         
    }

    public Handler mHandler = new Handler(){  
        
        public void handleMessage(Message msg) 
        {  
            try
            {
                if(MsgReceiver.MSG_EXCEPTION == msg.what)
                {
                    displayToastLong((String)msg.obj); 
                }
                else if(MsgReceiver.MSG_DELETE_TIMER == msg.what)
                {
                    deleteTimer((Integer)msg.obj);
                }
                else if(MsgReceiver.MSG_UPDATE_TIMER == msg.what)
                {
//                    updateLayout();
//                    Timer timer = (Timer)msg.obj;
//                    if(timer != null)
//                    {
//                        mMainTitle.setText(timer.getName() + "\n" + Util.MillisToStr(timer.getNextTime()));
//                    }
//                    else
//                    {
//                        mMainTitle.setText("没有提醒");
//                    }
                }
            }
            catch(Exception e)
            {
                Util.log_ex(e);
                displayToastLong("程序异常");
            }
        }  
    };           

    //**************************************************************************//
    class ListenerTime implements OnClickListener 
    {
        private Timer mTimer;
        private Context mContext;

        ListenerTime(Context context, Timer timer) 
        {
            mTimer = timer;
            mContext = context;
        }

        @Override
        public void onClick(View arg0)
        {
            if(mTimer.getTimerDef().isEnable() == 0)
            {
                mTimer.getTimerDef().setEnable(1);
            }
            else
            {
                mTimer.getTimerDef().setEnable(0);
            }
            DBHelper.getIntance().updateTimerDef(mTimer.getTimerDef());
            
            mTimerItemAdapter.notifyDataSetChanged(); 
            TimerMgr.setNextTimer(mContext, TimerMgr.SPEC_TIME_ID_NONE);
        }
    }
    
    class ListenerNameInfo implements OnClickListener 
    {
        private Timer mTimer;
        @SuppressWarnings("unused")
        private Context mContext;

        ListenerNameInfo(Context context, Timer timer) 
        {
            mContext = context;
            mTimer = timer;
        }
        
        @Override
        public void onClick(View v) 
        {
            Util.log("ListenerNameInfo.onClick:" + mTimer.getTimerDef().getDescription() + " " + mTimer.getTimerDef().getName());
            startTimerSettingActivity(mTimer, REQUEST_CODE_TIMER_SETTING);
        }
    }

    class ListenerMore implements OnClickListener 
    {
        private Timer mTimer;

        ListenerMore(Context context, Timer timer) 
        {
            mTimer = timer;
        }
 
        @Override
        public void onClick(View v) 
        {
            for(int i = 0; i < mTimerItemList.size(); i++)
            {
                Timer timer = (Timer)mTimerItemList.get(i).get("timer");
                if(timer.getID() == mTimer.getID())
                {
                    if((Integer)mTimerItemList.get(i).get("visibility") == View.VISIBLE)
                    {
                        mTimerItemList.get(i).put("visibility", View.GONE);
                    }
                    else
                    {
                        mTimerItemList.get(i).put("visibility", View.VISIBLE);
                    }
                }
                else
                {
                    mTimerItemList.get(i).put("visibility", View.GONE);
                }
            }
            mTimerItemAdapter.notifyDataSetChanged(); 
            
        }
    }



    //**************************************************************************//
    private void preset()
    {
        // 预置数据
        Util.log("preset timer data");
        DBHelper.getIntance().deleteAllTimer();

        {
            int iPolicyOrder = 0;
            // 1分钟后响
            //TTimerDef timeDef = QuickSetting.countdownFromNowOn(1);
            TTimerDef timeDef = QuickSetting.setHourMinute(7, 25);
            timeDef.setName("起床");
            timeDef.setInterval(1);
            timeDef.setMaxCount(1);
            DBHelper.getIntance().insertTimerDef(timeDef);
    
            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
                    QuickSetting.everyday(), TLoopPolicy.LOOP_PARAM_INCLUDE);
    
            DBHelper.getIntance().insertAction(
                    timeDef.getID(),
                    0,
                    ActionMgr.getIntance().createAction(timeDef.getID(), 
                            0, ActionMgr.ACTION_KLAXON,"param1"));
            DBHelper.getIntance().insertAction(
                    timeDef.getID(),
                    1,
                    ActionMgr.getIntance().createAction(timeDef.getID(), 
                            1, ActionMgr.ACTION_NOTIFY,"param2"));

            DBHelper.getIntance().insertAction(
                    timeDef.getID(),
                    1,
                    ActionMgr.getIntance().createAction(timeDef.getID(), 
                            1, ActionMgr.ACTION_DIALOG,"param3"));

        }
        
        {
            int iPolicyOrder = 0;
            // 每天，早上10点 + 除去每个月的第一个周六周日            
            TTimerDef timeDef = QuickSetting.setHourMinute(10, 1);
            timeDef.setEnable(0);
            DBHelper.getIntance().insertTimerDef(timeDef);
    
            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
                    QuickSetting.everyday(), TLoopPolicy.LOOP_PARAM_INCLUDE);
            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
                    QuickSetting.monthAndWeekDayCycle(new char[]
                    { '0', '0', '0', '0', '1' }, new char[]
                    { '1', '1', '1', '1', '1', '1', '1' }),
                    TLoopPolicy.LOOP_PARAM_EXCLUDE);
            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
                    QuickSetting.monthAndWeekDayCycle(
                            new char[]{'1', '1', '1', '1', '1', '1', '1', '0', '0', '0', '0', '1'},
                            new char[]{ '0', '0', '0', '0', '1' }, 
                            new char[]{ '1', '1', '1', '1', '1', '1', '1' }),
                    TLoopPolicy.LOOP_PARAM_EXCLUDE);
    
            DBHelper.getIntance().insertAction(
                    timeDef.getID(),
                    0,
                    ActionMgr.getIntance().createAction(timeDef.getID(), 
                            0, ActionMgr.ACTION_KLAXON,"param1"));
            DBHelper.getIntance().insertAction(
                    timeDef.getID(),
                    1,
                    ActionMgr.getIntance().createAction(timeDef.getID(), 
                            1, ActionMgr.ACTION_NOTIFY,"param2"));

            DBHelper.getIntance().insertAction(
                    timeDef.getID(),
                    1,
                    ActionMgr.getIntance().createAction(timeDef.getID(), 
                            1, ActionMgr.ACTION_DIALOG,"param3"));

        }
        
        {
            int iPolicyOrder = 0;
            TTimerDef timeDef = QuickSetting.setHourMinute(10, 0);
            timeDef.setName("贷款销户");
            timeDef.setRemark("12月18日销户");
            DBHelper.getIntance().insertTimerDef(timeDef);
    
            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
                    QuickSetting.specifiedDate(2012, 12, 17, 0),
                    TLoopPolicy.LOOP_PARAM_INCLUDE);

    
            DBHelper.getIntance().insertAction(
                    timeDef.getID(),
                    0,
                    ActionMgr.getIntance().createAction(timeDef.getID(), 
                            0, ActionMgr.ACTION_NOTIFY,"param2"));
        }
        
////////////////////////////////////////////////////////////////////////////////
//        {
//            int iPolicyOrder = 0;
//            TTimerDef timeDef = QuickSetting.setHourMinute(7, 30);
//            DBHelper.getIntance().insertTimerDef(timeDef);
//    
//            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
//                    QuickSetting.weekCycle(new char[]{'0', '0', '0', '0', '0', '0', '1'  }),
//                    TLoopPolicy.LOOP_PARAM_INCLUDE);
//
//            DBHelper.getIntance().insertAction(
//                    timeDef.getID(),
//                    0,
//                    ActionMgr.getIntance().createAction(timeDef.getID(), 
//                            0, ActionMgr.ACTION_NOTIFY,"param2"));
//        }
//        {
//            int iPolicyOrder = 0;
//            TTimerDef timeDef = QuickSetting.setHourMinute(7, 30);
//            DBHelper.getIntance().insertTimerDef(timeDef);
//    
//            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
//                    QuickSetting.weekCycle(new char[]{'0', '0', '0', '0', '0', '0', '1'  }),
//                    TLoopPolicy.LOOP_PARAM_INCLUDE);
//
//            DBHelper.getIntance().insertAction(
//                    timeDef.getID(),
//                    0,
//                    ActionMgr.getIntance().createAction(timeDef.getID(), 
//                            0, ActionMgr.ACTION_NOTIFY,"param2"));
//        }
//        {
//            int iPolicyOrder = 0;
//            TTimerDef timeDef = QuickSetting.setHourMinute(7, 30);
//            DBHelper.getIntance().insertTimerDef(timeDef);
//    
//            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
//                    QuickSetting.weekCycle(new char[]{'0', '0', '0', '0', '0', '0', '1'  }),
//                    TLoopPolicy.LOOP_PARAM_INCLUDE);
//
//            DBHelper.getIntance().insertAction(
//                    timeDef.getID(),
//                    0,
//                    ActionMgr.getIntance().createAction(timeDef.getID(), 
//                            0, ActionMgr.ACTION_NOTIFY,"param2"));
//        }
//        {
//            int iPolicyOrder = 0;
//            TTimerDef timeDef = QuickSetting.setHourMinute(7, 30);
//            DBHelper.getIntance().insertTimerDef(timeDef);
//    
//            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
//                    QuickSetting.weekCycle(new char[]{'0', '0', '0', '0', '0', '0', '1'  }),
//                    TLoopPolicy.LOOP_PARAM_INCLUDE);
//
//            DBHelper.getIntance().insertAction(
//                    timeDef.getID(),
//                    0,
//                    ActionMgr.getIntance().createAction(timeDef.getID(), 
//                            0, ActionMgr.ACTION_NOTIFY,"param2"));
//        }
//        {
//            int iPolicyOrder = 0;
//            TTimerDef timeDef = QuickSetting.setHourMinute(7, 30);
//            DBHelper.getIntance().insertTimerDef(timeDef);
//    
//            DBHelper.getIntance().insertLoopPolicy(timeDef.getID(), iPolicyOrder++,
//                    QuickSetting.weekCycle(new char[]{'0', '0', '0', '0', '0', '0', '1'  }),
//                    TLoopPolicy.LOOP_PARAM_INCLUDE);
//
//            DBHelper.getIntance().insertAction(
//                    timeDef.getID(),
//                    0,
//                    ActionMgr.getIntance().createAction(timeDef.getID(), 
//                            0, ActionMgr.ACTION_NOTIFY,"param2"));
//        }

    }

}
