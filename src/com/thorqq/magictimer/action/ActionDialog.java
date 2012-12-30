package com.thorqq.magictimer.action;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;

import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.util.Util;

/**
 * 对话框提醒
 * 
 */
public class ActionDialog extends TAction
{
    private static final long serialVersionUID = -2189876892433369300L;

    public ActionDialog(int id, int execOrder, int actionType, String param)
    {
        super(id, execOrder, actionType, param);
    }

    @Override
    public boolean run(Context context, Timer timer)
    {
        Util.log("ActionDialog run: " + timer.getName() + "|" + timer.getRemark());

        /** Close dialogs and window shade */
        // 关闭对话框和系统阴影
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        // Decide which activity to start based on the state of the
        // keyguard.
        // 如果键盘锁住，则调用AlarmAlertFullScreen，否则调用AlarmAlert
        @SuppressWarnings("rawtypes")
        Class c = AlarmAlert.class;
        KeyguardManager km = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode())
        {
            // Use the full screen activity for security.
            c = AlarmAlertFullScreen.class;
        }

        /**
         * launch UI, explicitly stating that this is not due to user action so
         * that the current app's notification management is not disturbed
         */
        // 启动提醒对话框，启动这个Intent不是通过用户的操作进行的，这样，当前应用的通知栏管理不会被弄乱
        Intent alarmAlert = new Intent(context, c);
        alarmAlert.putExtra(TimerMgr.ALARM_INTENT_EXTRA, timer);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(alarmAlert);

        return false;
    }

    @Override
    public String getDescription()
    {
        return "对话框: " + super.getParam();
    }

}
