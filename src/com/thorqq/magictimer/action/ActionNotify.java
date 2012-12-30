package com.thorqq.magictimer.action;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.thorqq.magictimer.R;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.util.Util;

/**
 * 通知栏提醒
 * 
 */
public class ActionNotify extends TAction
{
    private static final long serialVersionUID = -8433026171837598772L;

    public ActionNotify(int id, int execOrder, int actionType, String param)
    {
        super(id, execOrder, actionType, param);
    }

    @Override
    public boolean run(Context context, Timer timer)
    {
        // TODO 检查action参数
        Util.log("ActionNotify run: " + timer.getName() + "|" + timer.getRemark());

        // 触发一个通知，单击时，显示闹铃提醒对话框。无需检测是否是全屏，因为这个肯定是用户触发的。
        Intent notify = new Intent(context, AlarmAlert.class);
        notify.putExtra(TimerMgr.ALARM_INTENT_EXTRA, timer);
        PendingIntent pendingNotify = PendingIntent.getActivity(context,
                timer.getID(), notify, 0);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.stat_notify_alarm, timer.getName(), System.currentTimeMillis());

        // 在此处设置在nority列表里的该norifycation得显示情况。
        notification.setLatestEventInfo(context, timer.getName(), timer.getRemark(), pendingNotify);
        // FLAG_AUTO_CANCEL 用户点击后自动取消
        // FLAG_ONGOING_EVENT 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        //FLAG_AUTO_CANCEL   该通知能被状态栏的清除按钮给清除掉 
        //FLAG_NO_CLEAR      该通知不能被状态栏的清除按钮给清除掉 
        //FLAG_ONGOING_EVENT 通知放置在正在运行 
        //FLAG_INSISTENT     是否一直进行，比如音乐一直播放，知道用户响应 
        //FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中    
        //FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用    
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;    
        //DEFAULT_ALL     使用所有默认值，比如声音，震动，闪屏等等 
        //DEFAULT_LIGHTS  使用默认闪光提示 
        //DEFAULT_SOUNDS  使用默认提示声音 
        //DEFAULT_VIBRATE 使用默认手机震动，需加上<uses-permission android:name="android.permission.VIBRATE" />权限 
        notification.defaults = Notification.DEFAULT_SOUND;  
        //叠加效果常量 
        //notification.defaults=Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND; 
        //notification.ledARGB = Color.BLUE;    
        //notification.ledOnMS = 5000; //闪光时间，毫秒 

        nm.notify(timer.getID(), notification);

        return false;
    }

    @Override
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return "通知: " + super.getParam();
    }

}
