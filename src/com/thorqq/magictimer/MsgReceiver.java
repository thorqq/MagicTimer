package com.thorqq.magictimer;

public interface MsgReceiver
{
    public static final int MSG_EXCEPTION = -1;
    public static final int MSG_UPDATE_TIMER = 0;
    public static final int MSG_DELETE_TIMER = 1;
    
    public void getMessage(int msgCode, Object obj);

}
