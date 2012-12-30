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
// �㲥�������������������ӵĹ㲥�¼���Ȼ�������ش�����������id
public class AlarmReceiver extends BroadcastReceiver {

    /*** If the alarm is older than STALE_WINDOW seconds, ignore.  It
        is probably the result of a time or timezone change */
    // �����������ʱ���Ѿ�������30���ӣ���ֱ�Ӻ��ԡ�������ʱ���ʱ������
    private final static int STALE_WINDOW = 60 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        
        Util.log("onReceive: " + intent.getAction());
        
        //������屻ɾ�ˣ������֪ͨ��
        if (TimerMgr.ALARM_KILLED.equals(intent.getAction())) 
        {
            Util.log("TimerMgr.ALARM_KILLED.equals(intent.getAction())");
            // The alarm has been killed, update the notification
            updateNotification(context, (Timer)
                    intent.getParcelableExtra(TimerMgr.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(TimerMgr.ALARM_KILLED_TIMEOUT, -1));
            return;
        } 
        //���̰˯��������һ������
        else if (TimerMgr.CANCEL_SNOOZE.equals(intent.getAction())) 
        {
            //TODO ����Ӧ��ֻ���ָ��timerid��̰˯
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
        // ��intent�л�ȡԭʼ���ݣ��������Parcel�У�Ȼ������alarm
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
        
        //�����ѭ�����壬��Ҫ�������һ������ʱ��
        if(timer.getTimerDef().getMaxCount() == TimerCalculator.INFINITY_COUNT 
                || timer.getTimerDef().getMaxCount() > 1)
        {
            long millis = Calendar.getInstance().getTimeInMillis();
            TimerMgr.setLastAlertTime(timer, millis);
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();
        // �����������ʱ���Ѿ�������30���ӣ���ֱ�Ӻ��ԡ�������ʱ���ʱ������
        if (now > timer.getNextTime() + STALE_WINDOW * 1000) {

            return;
        }

        //TODO  �� onReceive() ������ 10 ����û��ִ����ϣ� Android ����Ϊ�ó�������Ӧ . 
        //������BroadcastReceiver �ﲻ����һЩ�ȽϺ�ʱ�Ĳ��� , 
        //���ᵯ�� ANR(Application No Response) �ĶԻ���
        ActionMgr.getIntance().run(context, timer);

        TimerMgr.reset();
        TimerMgr.setNextTimer(context, TimerMgr.SPEC_TIME_ID_NONE);
    }

    private void updateNotification(Context context, Timer alarm, int timeout) {
//        NotificationManager nm = getNotificationManager(context);
//
//        // If the alarm is null, just cancel the notification.
//        // �������Ϊ�գ���ֱ��ȡ��֪ͨ��
//        if (alarm == null) {
//
//            return;
//        }
//
//        // Launch SetAlarm when clicked.
//        // �����֪ͨ��ʱ�������������ý���
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
//        // ȡ��ԭ����֪ͨ�������µ�֪ͨ
//        nm.cancel(alarm.getID());
//        nm.notify(alarm.getID(), n);
    }
}

