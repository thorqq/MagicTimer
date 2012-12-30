package com.thorqq.magictimer;

public interface MsgReceiver
{
    public static final int MSG_EXCEPTION = -1;
    public static final int MSG_UPDATE_TIMER = 0;
    
    public void getMessage(int msgCode, Object obj);

}
