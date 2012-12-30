/**
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thorqq.magictimer;

import java.util.Calendar;

import com.thorqq.magictimer.action.ActionMgr;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerCalculator;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.util.Util;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.Parcel;


/***
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity.  Passes through Alarm ID.
 */
// 广播接收器，用来接收闹钟的广播事件，然后进行相关处理。接收闹铃id
public class AlarmReceiver extends BroadcastReceiver {

    /*** If the alarm is older than STALE_WINDOW seconds, ignore.  It
        is probably the result of a time or timezone change */
    // 如果闹铃提醒时间已经超过了30分钟，则直接忽略。可能是时间或时区变了
    private final static int STALE_WINDOW = 60 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        
        Util.log("onReceive: " + intent.getAction());
        
        //如果闹铃被删了，则更新通知栏
        if (TimerMgr.ALARM_KILLED.equals(intent.getAction())) 
        {
            Util.log("TimerMgr.ALARM_KILLED.equals(intent.getAction())");
            // The alarm has been killed, update the notification
            updateNotification(context, (Timer)
                    intent.getParcelableExtra(TimerMgr.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(TimerMgr.ALARM_KILLED_TIMEOUT, -1));
            return;
        } 
        //清除贪睡，设置下一个闹铃
        else if (TimerMgr.CANCEL_SNOOZE.equals(intent.getAction())) 
        {
            //TODO 这里应该只清除指定timerid的贪睡
            Util.log("TimerMgr.CANCEL_SNOOZE.equals(intent.getAction())");
            Timer timer = TimerMgr.getTimer(intent.getIntExtra(TimerMgr.ALARM_ID, -1));
            TimerMgr.disableSnoozeTimer(context, timer);
            return;
        }

        Timer timer = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        // 从intent中获取原始数据，并解包到Parcel中，然后生成alarm
        final byte[] data = intent.getByteArrayExtra(TimerMgr.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            timer = Timer.CREATOR.createFromParcel(in);
        }

        if (timer == null) {
            return;
        }
        
        //如果是循环闹铃，则要更新最近一次启动时间
        if(timer.getTimerDef().getMaxCount() == TimerCalculator.INFINITY_COUNT 
                || timer.getTimerDef().getMaxCount() > 1)
        {
            long millis = Calendar.getInstance().getTimeInMillis();
            TimerMgr.setLastAlertTime(timer, millis);
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();
        // 如果闹铃提醒时间已经超过了30分钟，则直接忽略。可能是时间或时区变了
        if (now > timer.getNextTime() + STALE_WINDOW * 1000) {

            return;
        }

        //TODO  当 onReceive() 方法在 10 秒内没有执行完毕， Android 会认为该程序无响应 . 
        //所以在BroadcastReceiver 里不能做一些比较耗时的操作 , 
        //否侧会弹出 ANR(Application No Response) 的对话框
        ActionMgr.getIntance().run(context, timer);

        TimerMgr.reset();
        TimerMgr.setNextTimer(context, TimerMgr.SPEC_TIME_ID_NONE);
    }

    private void updateNotification(Context context, Timer alarm, int timeout) {
//        NotificationManager nm = getNotificationManager(context);
//
//        // If the alarm is null, just cancel the notification.
//        // 如果闹铃为空，则直接取消通知栏
//        if (alarm == null) {
//
//            return;
//        }
//
//        // Launch SetAlarm when clicked.
//        // 当点击通知栏时，启动闹铃设置界面
//        Intent viewAlarm = new Intent(context, SetAlarm.class);
//        viewAlarm.putExtra(TimerMgr.ALARM_ID, alarm.getID());
//        PendingIntent intent =
//                PendingIntent.getActivity(context, alarm.getID(), viewAlarm, 0);
//
//        // Update the notification to indicate that the alert has been
//        // silenced.
//        // Alarm silenced after %d minutes
//        String label = alarm.getName();
//        Notification n = new Notification(R.drawable.stat_notify_alarm,
//                label, alarm.getNextTime());
//        n.setLatestEventInfo(context, label,
//                context.getString(R.string.alarm_alert_alert_silenced, timeout),
//                intent);
//        n.flags |= Notification.FLAG_AUTO_CANCEL;
//        // We have to cancel the original notification since it is in the
//        // ongoing section and we want the "killed" notification to be a plain
//        // notification.
//        // 取消原来的通知，换成新的通知
//        nm.cancel(alarm.getID());
//        nm.notify(alarm.getID(), n);
    }
}

