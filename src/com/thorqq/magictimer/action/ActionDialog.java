package com.thorqq.magictimer.action;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;

import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerMgr;
import com.thorqq.magictimer.util.Util;

/**
 * �Ի�������
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
        // �رնԻ����ϵͳ��Ӱ
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        // Decide which activity to start based on the state of the
        // keyguard.
        // ���������ס�������AlarmAlertFullScreen���������AlarmAlert
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
        // �������ѶԻ����������Intent����ͨ���û��Ĳ������еģ���������ǰӦ�õ�֪ͨ�������ᱻŪ��
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
        return "�Ի���: " + super.getParam();
    }

}
