package com.thorqq.magictimer.action;

import java.io.Serializable;

import android.content.Context;

import com.thorqq.magictimer.core.Timer;


/**
 * @author THORQQ<br>
 * Êý¾Ý¿â±í£º timeing_action
 */
public abstract class TAction implements Serializable
{
    private static final long serialVersionUID = -4061536221414749800L;
    
    protected int mID = -1;
    protected int mExecOrder = 0;
    protected int mActionType;
    protected String mParam;
    
    public TAction(int id, int execOrder, int actionType, String param)
    {
        mID = id;
        mExecOrder = execOrder;
        mActionType = actionType;
        mParam = param;
    }
    
    public abstract String getDescription();
    public abstract boolean run(Context context, Timer timer);
    
    public int getID()
    {
        return mID;
    }
 
    public int getExecOrder()
    {
        return mExecOrder;
    }

    public void setExecOrder(int execOrder)
    {
        mExecOrder = execOrder;
    }

    public int getActionType()
    {
        return mActionType;
    }
    
    public String getParam()
    {
        return mParam;
    }
}
