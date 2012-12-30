/**
 * Copyright (C) 2009 The Android Open Source Project
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

package com.thorqq.magictimer.action;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import java.util.Calendar;

import com.thorqq.magictimer.AlarmReceiver;
import com.thorqq.magictimer.R;
import com.thorqq.magictimer.ConfigurationActivity;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.util.Util;

//import com.android.deskclock2.R;

/***
 * Alarm Clock alarm alert: pops visible indicator and plays alarm
 * tone. This activity is the full screen version which shows over the lock
 * screen with the wallpaper as the background.
 */
//ȫ���������ѡ������Ի��򲢲���������ȫ���汾���Ա�ֽ��Ϊ����
public class AlarmAlertFullScreen extends Activity {

    // These defaults must match the values in res/xml/settings.xml
    private static final String DEFAULT_SNOOZE = "5"; //������
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    protected static final String SCREEN_OFF = "screen_off";

//    protected Alarm mAlarm;
    protected Timer mTimer;
    private int mVolumeBehavior;

    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other applications
    // ���� ALARM_KILLED  ALARM_SNOOZE_ACTION  ALARM_DISMISS_ACTION ��Ϣ
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TimerMgr.ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(TimerMgr.ALARM_DISMISS_ACTION)) {
                dismiss(false);
            } else {
                //ALARM_INTENT_EXTRA : ���ݹ���һ��alarm
                Timer timer = intent.getParcelableExtra(TimerMgr.ALARM_INTENT_EXTRA);
                if (timer != null && mTimer.getTimerDef().getID() == timer.getTimerDef().getID()) {
                    dismiss(true);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mTimer = getIntent().getParcelableExtra(TimerMgr.ALARM_INTENT_EXTRA);

        // Get the volume/camera button behavior setting
        // ��߰��������ã�������������̰˯���������
        final String vol =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(ConfigurationActivity.KEY_VOLUME_BEHAVIOR,
                        DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);

        // ���ô������չ����: �ޱ���
        // Ч��ͬstyles.xml�е�<item name="android:windowNoTitle">true</item>
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        // FLAG_SHOW_WHEN_LOCKED:����Ļ����ʱ��Ҳ����ʾ
        // FLAG_DISMISS_KEYGUARD:���������
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass.
        // FLAG_KEEP_SCREEN_ON ���˴���Ϊ�û��ɼ�ʱ�������豸���������������Ȳ���
        // FLAG_TURN_SCREEN_ON ����һ����ʾ������ϵͳ��������Ļ�������û������豸����
        // FLAG_ALLOW_LOCK_WHILE_SCREEN_ON ����window���û��ɼ���ʱ����������
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) 
        {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//TODO  WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON 0x00000001 ���Ҫ����Ϊʲô��֧��
                    | 0x00000001);
        }

        updateLayout();

        // Register to get the alarm killed/snooze/dismiss intent.
        // ע���������ֻ���� ɾ��/����/���
        IntentFilter filter = new IntentFilter(TimerMgr.ALARM_KILLED);
        filter.addAction(TimerMgr.ALARM_SNOOZE_ACTION);
        filter.addAction(TimerMgr.ALARM_DISMISS_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void setTitle() {
        String label = mTimer.getTimerDef().getName();
        TextView title = (TextView) findViewById(R.id.alertTitle);
        title.setText(label);
    }

    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        setContentView(inflater.inflate(R.layout.alarm_alert, null));
        

        /** snooze behavior: pop a snooze confirmation view, kick alarm
           manager. */
        // ���ﰴť��һ��ʱ��֮������
        Button snooze = (Button) findViewById(R.id.snooze);
        if(mTimer.getTimerDef().getMaxCount() == -1 || mTimer.getTimerDef().getMaxCount() > 1)
        {
            //�����ѭ�����ӣ�������̰˯����
            snooze.setEnabled(false);
        }
        else
        {
            snooze.requestFocus();
        }
        snooze.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Util.log("click snooze");
                snooze();
            }
        });

        /** dismiss button: close notification */
        //�����ť���ر�����
        findViewById(R.id.dismiss).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Util.log("click dismiss");
                        dismiss(false);
                    }
                });

        /** Set the title from the passed in alarm */
        setTitle();
    }

    // Attempt to snooze this alert.
    // ��������ʱ���û�����̰˯��ťʱ�����ô˷���
    private void snooze() {
        // Do not snooze if the snooze button is disabled.
        //���ﰴť�����ã���ֱ�ӷ���
        if (!findViewById(R.id.snooze).isEnabled()) {
            dismiss(false);
            return;
        }
        //��ȡ����ʱ��:Ĭ��Ϊ10����
        final String snooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(ConfigurationActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        //��һ������ʱ��
        final long snoozeTime = System.currentTimeMillis()
                + (1000 * 60 * snoozeMinutes);
        TimerMgr.saveSnoozeTimer(AlarmAlertFullScreen.this, mTimer,
                snoozeMinutes);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        // ��label��������:(snoozed)
        String label = mTimer.getName();
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        //���������֪ͨ�������û����֪ͨ��֪ͨʱ����ȡ������
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(TimerMgr.CANCEL_SNOOZE);
        cancelSnooze.putExtra(TimerMgr.ALARM_ID, mTimer.getID());

        //��ȡһ��PendingIntent������mAlarm.id�㲥��cancelSnooze
        PendingIntent broadcast =
                PendingIntent.getBroadcast(this, mTimer.getID(), cancelSnooze, 0);
        
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, 0);
        //�ڴ˴�������nority�б���ĸ�norifycation����ʾ������������ִ��broadcastȡ������
        n.setLatestEventInfo(this, label,
                getString(R.string.alarm_notify_snooze_text, // Alarm set for %s. Select to cancel.
                    Util.formatTime(c)), broadcast);
        //FLAG_AUTO_CANCEL �û�������Զ�ȡ��
        //FLAG_ONGOING_EVENT ����֪ͨ�ŵ�֪ͨ����"Ongoing"��"��������"����
        n.flags |= Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        nm.notify(mTimer.getTimerDef().getID(), n);

        // Snoozing for %d minutes.
        String displayTime = getString(R.string.alarm_alert_snooze_set,
                snoozeMinutes);
        // Intentionally log the snooze time for debugging.

        // Display the snooze minutes in a toast.
        Toast.makeText(AlarmAlertFullScreen.this, displayTime,
                Toast.LENGTH_LONG).show();

        //ֹͣAlarmAlert����
        stopService(new Intent(TimerMgr.ALARM_ALERT_ACTION));
        finish();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    // ��������ʱ���û����½����ťʱ�����ô˷���
    private void dismiss(boolean killed) {
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            //ȡ��֪ͨ����Ϣ
            NotificationManager nm = getNotificationManager();
            nm.cancel(mTimer.getTimerDef().getID());
            stopService(new Intent(TimerMgr.ALARM_ALERT_ACTION));
            
            //��������˼�����ѣ�����ֹͣ����
            if(mTimer.getTimerDef().getMaxCount() == 1)
            {
                TimerMgr.disableSnoozeTimer(this, mTimer);
            }
        }
        finish();
    }

    /***
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    // ��ǰ���Ǹ����崰�ڻ��ڵ�ʱ�򣬵ڶ������崥����
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // ���������Ϊ�ڶ�������
        mTimer = intent.getParcelableExtra(TimerMgr.ALARM_INTENT_EXTRA);

        setTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the alarm was deleted at some point, disable snooze.
        // ������屻ɾ���ˣ������̰˯
        if (TimerMgr.getTimer(mTimer.getTimerDef().getID()) == null) {
            Button snooze = (Button) findViewById(R.id.snooze);
            snooze.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // No longer care about the alarm being killed.
        unregisterReceiver(mReceiver);
    }

    //���� ����������������Խ��� ʱ����������̰˯���߽��
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        // KeyEvent.ACTION_UP  �������ͷ�
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            // ����������������Խ���
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        return;
    }
}

