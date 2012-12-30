package com.thorqq.magictimer.action;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.thorqq.magictimer.AlarmAlertWakeLock;
import com.thorqq.magictimer.R;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.util.Util;

/**
 * 播放铃音
 * 
 */
public class ActionKlaxon extends TAction
{
    private static final long serialVersionUID = -5846195123887342209L;

    public ActionKlaxon(int id, int execOrder, int actionType, String param)
    {
        super(id, execOrder, actionType, param);
    }

    @Override
    public boolean run(Context context, Timer timer)
    {
        // TODO 检查action参数
        Util.log("ActionKlaxon run: " + timer.getName() + "|" + timer.getRemark());

        // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
        // pick it up.
        // 管理设备电量
        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        // Play the alarm alert and vibrate the device.
        // 播放闹铃和震动（启动AlarmKlaxon服务）
        // ALARM_ALERT_ACTION 会触发 AlarmReceiver和AlarmKlaxon
        Intent playAlarm = new Intent(TimerMgr.ALARM_ALERT_ACTION);
        playAlarm.putExtra(TimerMgr.ALARM_INTENT_EXTRA, timer);
        context.startService(playAlarm);

        // Trigger a notification that, when clicked, will show the alarm
        // alert
        // dialog. No need to check for fullscreen since this will always be
        // launched from a user action.
        // 触发一个通知，单击时，显示闹铃提醒对话框。无需检测是否是全屏，因为这个肯定是用户触发的。
        Intent notify = new Intent(context, AlarmAlert.class);
        notify.putExtra(TimerMgr.ALARM_INTENT_EXTRA, timer);
        PendingIntent pendingNotify = PendingIntent.getActivity(context,
                timer.getID(), notify, 0);

        // Use the alarm's label or the default label as the ticker text and
        // main text of the notification.
        String label = timer.getName();
        Notification n = new Notification(R.drawable.stat_notify_alarm, label,
                timer.getNextTime());
        n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_notify_text), pendingNotify);
        n.flags |= Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_ONGOING_EVENT;
        n.defaults |= Notification.DEFAULT_LIGHTS;

        // Send the notification using the alarm id to easily identify the
        // correct notification.
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(timer.getID(), n);

        return true;
    }

    @Override
    public String getDescription()
    {
        return "闹铃+震动: " + super.getParam();
    }
    
    
}
