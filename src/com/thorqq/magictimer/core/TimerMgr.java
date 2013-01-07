package com.thorqq.magictimer.core;

import java.util.Vector;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

import com.thorqq.magictimer.MsgReceiver;
import com.thorqq.magictimer.action.TAction;
import com.thorqq.magictimer.db.DBHelper;
import com.thorqq.magictimer.util.Util;

/**
 * @author THORQQ<br>
 *         从数据库读取所有定时器信息，并保存到内部结构中
 */
public class TimerMgr
{
    // This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    // 触发AlarmReceiver和AlarmKlaxon
    public static final String ALARM_ALERT_ACTION = "com.thorqq.magictimer.ALARM_ALERT";

    // A public action sent by AlarmKlaxon when the alarm has stopped sounding
    // for any reason (e.g. because it has been dismissed from
    // AlarmAlertFullScreen,
    // or killed due to an incoming phone call, etc).
    public static final String ALARM_DONE_ACTION = "com.thorqq.magictimer.ALARM_DONE";

    // AlarmAlertFullScreen listens for this broadcast intent, so that other
    // applications
    // can snooze the alarm (after ALARM_ALERT_ACTION and before
    // ALARM_DONE_ACTION).
    // AlarmAlertFullScreen
    // 监听其他程序广播的这个消息，打盹模式
    public static final String ALARM_SNOOZE_ACTION = "com.thorqq.magictimer.ALARM_SNOOZE";

    // AlarmAlertFullScreen listens for this broadcast intent, so that other
    // applications
    // can dismiss the alarm (after ALARM_ALERT_ACTION and before
    // ALARM_DONE_ACTION).
    // 解除闹铃
    public static final String ALARM_DISMISS_ACTION = "com.thorqq.magictimer.ALARM_DISMISS";

    // This is a private action used by the AlarmKlaxon to update the UI to
    // show the alarm has been killed.
    public static final String ALARM_KILLED = "alarm_killed";

    // Extra in the ALARM_KILLED intent to indicate to the user how long the
    // alarm played before being killed.
    public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";

    // This string is used to indicate a silent alarm in the db.
    public static final String ALARM_ALERT_SILENT = "silent";

    // This intent is sent from the notification when the user cancels the
    // snooze alert.
    public static final String CANCEL_SNOOZE = "cancel_snooze";

    // This string is used when passing an Alarm object through an intent.
    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    public static final String ALARM_INTENT_TIMER_DEF = "intent.time_def.alarm";
    public static final String ALARM_INTENT_LOOP_POLICY = "intent.loop_policy.alarm";
    public static final String ALARM_INTENT_ACTION = "intent.action.alarm";
    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    // This string is used to identify the alarm id passed to SetAlarm from the
    // list of alarms.
    public static final String ALARM_ID = "alarm_id";

    final static String PREF_SNOOZE_ID = "snooze_id";
    final static String PREF_SNOOZE_TIME = "snooze_time";
    
    public final static int SPEC_TIME_ID_NONE = -1;
    public final static int SPEC_TIME_ID_ALL = -2;

    // Shared with DigitalClock
    final static String M24 = "kk:mm";

    static final String PREFERENCES = "MagicTimer";

    private static Vector<Timer> mTimerArray = new Vector<Timer>();
    private static MsgReceiver mMsgReceiver;

    public static void setMsgReceiver(MsgReceiver receiver)
    {
        mMsgReceiver = receiver;
    }
    
    public static void sendMessage(int msgCode, Timer timer)
    {
        if(mMsgReceiver != null)
        {
            mMsgReceiver.getMessage(msgCode, timer);
        }
    }
    
    public static void reset()
    {
        mTimerArray.clear();
    }
    
    /**
     * 查询所有的定时器
     * 
     * @return 定时器数组
     */
    public static Vector<Timer> getAllTimers()
    {
        if(mTimerArray != null && mTimerArray.size() > 0)
        {
            return mTimerArray;
        }
        
        // 查找所有的定时器定义信息
        Vector<TTimerDef> timerDefArray = DBHelper.getIntance().queryAllTimerDef();
        if (timerDefArray == null || timerDefArray.size() == 0)
        {
            mTimerArray.clear();
            return mTimerArray;
        }

        for (TTimerDef timerDef : timerDefArray)
        {
            Timer timer = new Timer();
            timer.setTimerDef(timerDef);

            // 查找所有定时器的循环策略和定时动作信息
            Vector<TLoopPolicy> loopPolicys = DBHelper.getIntance().queryLoopPolicy(
                    timerDef.getID());
            if (loopPolicys != null && loopPolicys.size() > 0)
            {
                timer.setLoopPolicys(loopPolicys);
            }

            Vector<TAction> actions = DBHelper.getIntance().queryAction(timerDef.getID());
            if (actions != null && actions.size() > 0)
            {
                timer.setActions(actions);
            }

            mTimerArray.add(timer);
            Util.log("Found new timer: " + timer.getID() + ". " + timer.getName());
        }

        return mTimerArray;
    }
    
    public static void addTimer(Timer t)
    {
        mTimerArray.add(t);
    }
    
    public static void deleteTimer(Timer t)
    {
        deleteTimer(t.getID());
    }
    
    public static void deleteTimer(int timer_id)
    {
        DBHelper.getIntance().deleteTimer(timer_id);
        
        for(int i = 0; i < mTimerArray.size(); i++)
        {
            if(mTimerArray.get(i).getID() == timer_id)
            {
                mTimerArray.remove(i);
            }
        }
    }

    /**
     * 根据id查找定时器
     * 
     * @param timerid
     *            定时器id
     * @return 找到的定时器
     */
    public static Timer getTimer(int timerid)
    {
        for (Timer t : mTimerArray)
        {
            if (t.getTimerDef().getID() == timerid)
            {
                return t;
            }
        }

        return null;
    }

    /**
     * 查找所有定时器，查找下一个启动的定时器
     * 
     * @return 下一个启动的定时器
     */
    private static Timer getNextTimer(long nowInMillis, int spec_timer_id)
    {
        Util.log("getNextTimer("+ Util.MillisToStr(nowInMillis) + "), spec_timer_id = " + spec_timer_id);

        long nextTime = Long.MAX_VALUE;
        Timer timer = null;

        getAllTimers();

        for (Timer t : mTimerArray)
        {
            //如果定时器的设置没有改变或者只改变了enable，并且当前时间小于该定时器的下一次提醒时间，则可以不重新计算
            if(spec_timer_id == SPEC_TIME_ID_ALL 
                    || t.getID() == spec_timer_id 
                    || (t.getNextTime() <= nowInMillis && t.getNextTime() > 0))
            {
                t.calculate(nowInMillis);
            }
            
            //如果闹钟已经结束，则需要清空LastAlertTime
            if(t.getNextTime() == 0 && t.getTimerDef().getLastAlertTime() > 0)
            {
                t.getTimerDef().setLastAlertTime(0);
                DBHelper.getIntance().updateTimerDef(t.getTimerDef());
            }
            
            //定时器被禁用
            if(t.getTimerDef().isEnable() == 0)
            {
                continue;
            }

            //取最近一个闹铃
            if (t.getNextTime() > 0 
                    && t.getNextTime()/1000/60 > nowInMillis/1000/60 // 分钟数大于当前
                    && t.getNextTime() < nextTime)
            {
                nextTime = t.getNextTime();
                timer = t;
            }
        }

        return timer;
    }

    /**
     * 使已经过期的定时器失效
     */
    public static void disableExpiredTimers()
    {
        // TODO
    }

    public static void saveSnoozeTimer(final Context context, final Timer timer, final int time)
    {
        Util.log("saveSnoozeTimer: timer_id=" + timer.getID() + ", time=" + time);
        // 贪睡功能已经放到t_timer_def表中配置（通过max_count，interval，last_alert_time来实现）
        timer.getTimerDef().setLastAlertTime(System.currentTimeMillis());
        timer.getTimerDef().setMaxCount(1);
        timer.getTimerDef().setInterval(time);
        DBHelper.getIntance().updateTimerDef(timer.getTimerDef());
        
        // Set the next alert after updating the snooze.
        // 设置下一个闹铃。这里必须要重新获取一下所有定时器，因为之前可能已经修改过了
        reset();
        setNextTimer(context, timer.getID());
    }

    public static void disableSnoozeTimer(final Context context)
    {
        Util.log("disableSnoozeTimer");
        Vector<Timer> timers = getAllTimers();
        if(timers == null || timers.size() == 0)
        {
            return;
        }
        
        reset();
        for(Timer t : timers)
        {
            disableSnoozeTimer(context, t, false);
        }
    }
    
    public static void disableSnoozeTimer(final Context context, final Timer timer)
    {
        disableSnoozeTimer(context, timer, true);
    }
    
    public static void setLastAlertTime(final Timer timer, long lastAlertTime)
    {
        Util.log("setLastAlertTimer: " + timer.getID() + ", lastAlertTime: " +lastAlertTime);

        timer.getTimerDef().setLastAlertTime(lastAlertTime);
        DBHelper.getIntance().updateTimerDef(timer.getTimerDef());
    }
    
    protected static void disableSnoozeTimer(final Context context, final Timer timer, boolean needReset)
    {
        Util.log("disableSnoozeTimer: " + timer.getID());
        if (timer.getTimerDef().getLastAlertTime() > 0)
        {
            Util.log("disableSnoozeTimer: " + timer.getID());
            timer.getTimerDef().setLastAlertTime(0);
            timer.getTimerDef().setMaxCount(1);
            DBHelper.getIntance().updateTimerDef(timer.getTimerDef());

            //停止所有定时器
            //disableTimer(context);

            if(needReset)
            {
                reset();
            }
            setNextTimer(context, timer.getID());

            // 清除通知栏里的前一个通知
            NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(timer.getID());
        }
    }

    /***
     * Sets alert in AlarmManger and StatusBar. This is what will actually
     * launch the alert when the alarm triggers.
     * 在系统中设置下一个闹铃，并设置右上角图标，否则去掉右上角闹钟图标
     * 
     * @param alarm
     *            Alarm.
     * @param atTimeInMillis
     *            milliseconds since epoch
     */
    public static void setNextTimer(Context context, int spec_timer_id)
    {
        Timer timer = TimerMgr.getNextTimer(System.currentTimeMillis(), spec_timer_id);
        if (timer != null)
        {
            TimerMgr.enableTimer(context, timer);
        } else
        {
            TimerMgr.disableTimer(context);
        }
        
        sendMessage(MsgReceiver.MSG_UPDATE_TIMER, timer);
    }

    public static void enableTimer(Context context, Timer timer)
    {
        Util.log("enableTimer: " + timer.getID());
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ALARM_ALERT_ACTION);

        // This is a slight hack to avoid an exception in the remote
        // AlarmManagerService process. The AlarmManager adds extra data to
        // this Intent which causes it to inflate. Since the remote process
        // does not know about the Alarm class, it throws a
        // ClassNotFoundException.
        //
        // To avoid this, we marshall the data ourselves and then parcel a plain
        // byte[] array. The AlarmReceiver class knows to build the Alarm
        // object from the byte[] array.
        Parcel out = Parcel.obtain();
        timer.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());

        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Util.log("Set system alarm: " + timer.toString());
        try
        {
            am.set(5, timer.getNextTime(), sender);
            Util.log("so pity, it's Ophone");
        }catch(Exception e)
        {
            Util.log("good luck, it's not Ophone");
            am.set(AlarmManager.RTC_WAKEUP, timer.getNextTime(), sender);
        }

        // 设置状态栏提示
        setStatusBarIcon(context, true);
    }

    /***
     * Disables alert in AlarmManger and StatusBar. 在状态栏和闹铃管理器里关闭闹铃
     * 
     * @param id
     *            Alarm ID.
     */
    public static void disableTimer(Context context)
    {
        Util.log("disableTimer");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                new Intent(ALARM_ALERT_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
        
        //去掉右上角图标。此处有点问题，虽然图标被去掉了，但是系统设置的闹铃不会被删除
        //TODO 需改为使用自己的图表
        setStatusBarIcon(context, false);
    }

    /***
     * Tells the StatusBar whether the alarm is enabled or disabled
     */
    // 发送广播，告诉状态栏闹铃是启用还是停止。图标显示在右上角
    private static void setStatusBarIcon(Context context, boolean enabled)
    {
        Util.log("setStatusBarIcon: " + enabled);
        Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
        alarmChanged.putExtra("alarmSet", enabled);
        context.sendBroadcast(alarmChanged);
    }

}
