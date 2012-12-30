package com.thorqq.magictimer.action;

import android.content.Context;

import com.thorqq.magictimer.core.Timer;

/**
 * @author THORQQ<br>
 *         ��ʱ����������<br>
 *         ʹ�÷���:<br>
 *         ActionMgr.getIntance().getAction(int actionType).run();
 */
public class ActionMgr
{
    public static final int ACTION_KLAXON = 0; // ��������+��
    public static final int ACTION_NOTIFY = 1; // ֪ͨ������
    public static final int ACTION_DIALOG = 2; // �Ի�������

    private static ActionMgr mActionMgr;

    public static ActionMgr getIntance()
    {
        if (mActionMgr == null)
        {
            mActionMgr = new ActionMgr();
        }
        return mActionMgr;
    }

    public TAction createAction(int id, int execOrder, int actionType, String param)
    {
        switch (actionType)
        {
        case ACTION_KLAXON: return new ActionKlaxon(id, execOrder, actionType, param);
        case ACTION_NOTIFY: return new ActionNotify(id, execOrder, actionType, param);
        case ACTION_DIALOG: return new ActionDialog(id, execOrder, actionType, param);
        default:
            return null;
        }
    }

    public void run(Context context, final Timer timer)
    {
        for (TAction action : timer.getTActions())
        {
            action.run(context, timer);
        }

    }    
    

}
