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
//全屏闹钟提醒。弹出对话框并播放铃声。全屏版本，以壁纸作为背景
public class AlarmAlertFullScreen extends Activity {

    // These defaults must match the values in res/xml/settings.xml
    private static final String DEFAULT_SNOOZE = "5"; //打盹间隔
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    protected static final String SCREEN_OFF = "screen_off";

//    protected Alarm mAlarm;
    protected Timer mTimer;
    private int mVolumeBehavior;

    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other applications
    // 接收 ALARM_KILLED  ALARM_SNOOZE_ACTION  ALARM_DISMISS_ACTION 消息
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TimerMgr.ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(TimerMgr.ALARM_DISMISS_ACTION)) {
                dismiss(false);
            } else {
                //ALARM_INTENT_EXTRA : 传递过来一个alarm
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
        // 侧边按键的设置，可以用于设置贪睡、解除闹铃
        final String vol =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(ConfigurationActivity.KEY_VOLUME_BEHAVIOR,
                        DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);

        // 启用窗体的扩展特性: 无标题
        // 效果同styles.xml中的<item name="android:windowNoTitle">true</item>
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        // FLAG_SHOW_WHEN_LOCKED:当屏幕被锁时，也能显示
        // FLAG_DISMISS_KEYGUARD:解除键盘锁
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass.
        // FLAG_KEEP_SCREEN_ON 当此窗口为用户可见时，保持设备常开，并保持亮度不变
        // FLAG_TURN_SCREEN_ON 窗口一旦显示出来，系统将点亮屏幕，正如用户唤醒设备那样
        // FLAG_ALLOW_LOCK_WHILE_SCREEN_ON 当该window对用户可见的时候，允许锁屏
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) 
        {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//TODO  WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON 0x00000001 这个要看看为什么不支持
                    | 0x00000001);
        }

        updateLayout();

        // Register to get the alarm killed/snooze/dismiss intent.
        // 注册接收器，只接收 删除/打盹/解除
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
        // 打盹按钮，一定时间之后再响
        Button snooze = (Button) findViewById(R.id.snooze);
        if(mTimer.getTimerDef().getMaxCount() == -1 || mTimer.getTimerDef().getMaxCount() > 1)
        {
            //如果是循环闹钟，则不启用贪睡功能
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
        //解除按钮，关闭闹钟
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
    // 当闹铃响时，用户按下贪睡按钮时，调用此方法
    private void snooze() {
        // Do not snooze if the snooze button is disabled.
        //打盹按钮被禁用，则直接返回
        if (!findViewById(R.id.snooze).isEnabled()) {
            dismiss(false);
            return;
        }
        //获取打盹时长:默认为10分钟
        final String snooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(ConfigurationActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        //下一次闹铃时间
        final long snoozeTime = System.currentTimeMillis()
                + (1000 * 60 * snoozeMinutes);
        TimerMgr.saveSnoozeTimer(AlarmAlertFullScreen.this, mTimer,
                snoozeMinutes);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        // 在label后面增加:(snoozed)
        String label = mTimer.getName();
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        //用于下面的通知栏，当用户点击通知栏通知时，则取消打盹
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(TimerMgr.CANCEL_SNOOZE);
        cancelSnooze.putExtra(TimerMgr.ALARM_ID, mTimer.getID());

        //获取一个PendingIntent，即将mAlarm.id广播给cancelSnooze
        PendingIntent broadcast =
                PendingIntent.getBroadcast(this, mTimer.getID(), cancelSnooze, 0);
        
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, 0);
        //在此处设置在nority列表里的该norifycation得显示情况。点击后则执行broadcast取消打盹
        n.setLatestEventInfo(this, label,
                getString(R.string.alarm_notify_snooze_text, // Alarm set for %s. Select to cancel.
                    Util.formatTime(c)), broadcast);
        //FLAG_AUTO_CANCEL 用户点击后自动取消
        //FLAG_ONGOING_EVENT 将此通知放到通知栏的"Ongoing"即"正在运行"组中
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

        //停止AlarmAlert服务
        stopService(new Intent(TimerMgr.ALARM_ALERT_ACTION));
        finish();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    // 当闹铃响时，用户按下解除按钮时，调用此方法
    private void dismiss(boolean killed) {
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            //取消通知栏消息
            NotificationManager nm = getNotificationManager();
            nm.cancel(mTimer.getTimerDef().getID());
            stopService(new Intent(TimerMgr.ALARM_ALERT_ACTION));
            
            //如果设置了间隔提醒，则不能停止闹铃
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
    // 当前面那个闹铃窗口还在的时候，第二个闹铃触发了
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 将标题更换为第二个闹铃
        mTimer = intent.getParcelableExtra(TimerMgr.ALARM_INTENT_EXTRA);

        setTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the alarm was deleted at some point, disable snooze.
        // 如果闹铃被删除了，则禁用贪睡
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

    //按下 音量键、照相键、对焦键 时，可以设置贪睡或者解除
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        // KeyEvent.ACTION_UP  按键被释放
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            // 音量键、照相键、对焦键
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

