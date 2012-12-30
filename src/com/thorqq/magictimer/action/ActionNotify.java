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
 * ֪ͨ������
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
        // TODO ���action����
        Util.log("ActionNotify run: " + timer.getName() + "|" + timer.getRemark());

        // ����һ��֪ͨ������ʱ����ʾ�������ѶԻ����������Ƿ���ȫ������Ϊ����϶����û������ġ�
        Intent notify = new Intent(context, AlarmAlert.class);
        notify.putExtra(TimerMgr.ALARM_INTENT_EXTRA, timer);
        PendingIntent pendingNotify = PendingIntent.getActivity(context,
                timer.getID(), notify, 0);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.stat_notify_alarm, timer.getName(), System.currentTimeMillis());

        // �ڴ˴�������nority�б���ĸ�norifycation����ʾ�����
        notification.setLatestEventInfo(context, timer.getName(), timer.getRemark(), pendingNotify);
        // FLAG_AUTO_CANCEL �û�������Զ�ȡ��
        // FLAG_ONGOING_EVENT ����֪ͨ�ŵ�֪ͨ����"Ongoing"��"��������"����
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        //FLAG_AUTO_CANCEL   ��֪ͨ�ܱ�״̬���������ť������� 
        //FLAG_NO_CLEAR      ��֪ͨ���ܱ�״̬���������ť������� 
        //FLAG_ONGOING_EVENT ֪ͨ�������������� 
        //FLAG_INSISTENT     �Ƿ�һֱ���У���������һֱ���ţ�֪���û���Ӧ 
        //FLAG_ONGOING_EVENT; // ����֪ͨ�ŵ�֪ͨ����"Ongoing"��"��������"����    
        //FLAG_NO_CLEAR; // �����ڵ����֪ͨ���е�"���֪ͨ"�󣬴�֪ͨ�������������FLAG_ONGOING_EVENTһ��ʹ��    
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;    
        //DEFAULT_ALL     ʹ������Ĭ��ֵ�������������𶯣������ȵ� 
        //DEFAULT_LIGHTS  ʹ��Ĭ��������ʾ 
        //DEFAULT_SOUNDS  ʹ��Ĭ����ʾ���� 
        //DEFAULT_VIBRATE ʹ��Ĭ���ֻ��𶯣������<uses-permission android:name="android.permission.VIBRATE" />Ȩ�� 
        notification.defaults = Notification.DEFAULT_SOUND;  
        //����Ч������ 
        //notification.defaults=Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND; 
        //notification.ledARGB = Color.BLUE;    
        //notification.ledOnMS = 5000; //����ʱ�䣬���� 

        nm.notify(timer.getID(), notification);

        return false;
    }

    @Override
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return "֪ͨ: " + super.getParam();
    }

}
