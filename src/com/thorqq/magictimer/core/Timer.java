package com.thorqq.magictimer.core;

import java.util.Vector;

import android.os.Parcel;
import android.os.Parcelable;

import com.thorqq.magictimer.action.TAction;
import com.thorqq.magictimer.util.Util;

//原始的定时器数据结构
public class Timer implements Parcelable
{
    private TTimerDef mTimerDef;
    private Vector<TLoopPolicy> mLoopPolicys;
    private Vector<TAction> mActions;
    private long mNextTime = -1;

    @Override
    public String toString()
    {
        return mTimerDef.getName() + " : " + Util.MillisToStr(mNextTime) + "("+ mNextTime +")";
    }
    
    /**
     * 计算下一次启动时间
     * 
     * @return 下一次启动时间，毫秒
     */
    public long calculate(long nowInMillis)
    {
        Util.log("Calculate " + getName());
        TimerCalculator tc = new TimerCalculator(nowInMillis);

        tc.setTime(mTimerDef);
        for (TLoopPolicy tlp : mLoopPolicys)
        {
//            if (tlp.parseStringParam())
            {
                tc.addLoopPolicy(tlp);
            }
        }
        
        mNextTime = tc.caculate(getTimerDef().getLastAlertTime());
        Util.log(getName() + ": " + Util.MillisToStr(mNextTime));

        return mNextTime;
    }

    public int getID()
    {
        return getTimerDef().getID();
    }

    public String getName()
    {
        return getTimerDef().getName();
    }

    public String getRemark()
    {
        return getTimerDef().getRemark();
    }

    public TTimerDef getTimerDef()
    {
        return mTimerDef;
    }

    public Vector<TLoopPolicy> getLoopPolicys()
    {
        return mLoopPolicys;
    }

    public Vector<TAction> getTActions()
    {
        return mActions;
    }

    public long getNextTime()
    {
        return mNextTime;
    }

    public void setName(String name)
    {
        getTimerDef().setName(name);
    }

    public void setRemark(String remark)
    {
        getTimerDef().setRemark(remark);
    }

    public void setTimerDef(TTimerDef t)
    {
        mTimerDef = t;
    }

    public void setLoopPolicys(Vector<TLoopPolicy> loopPolicys)
    {
        mLoopPolicys = loopPolicys;
    }

    public void setLoopPolicy(TLoopPolicy policy)
    {
        int iMaxDisplayOrder = 0;
        int i = 0;
        for(; i < mLoopPolicys.size(); i++)
        {
            if(policy.getDisplayOrder() == mLoopPolicys.get(i).getDisplayOrder())
            {
                mLoopPolicys.set(policy.getDisplayOrder(), policy);
                return;
            }
            
            if(iMaxDisplayOrder < mLoopPolicys.get(i).getDisplayOrder())
            {
                iMaxDisplayOrder = mLoopPolicys.get(i).getDisplayOrder();
            }
        }
        
        if(i == mLoopPolicys.size())
        {
            policy.setDisplayOrder(iMaxDisplayOrder + 1);
            mLoopPolicys.add(policy);
        }
    }

    public void setActions(Vector<TAction> actions)
    {
        mActions = actions;
    }

    public void setAction(TAction action)
    {
        int iMaxDisplayOrder = 0;
        int i = 0;
        for(; i < mActions.size(); i++)
        {
            if(action.getExecOrder() == mActions.get(i).getExecOrder())
            {
                mActions.set(action.getExecOrder(), action);
                return;
            }
            
            if(iMaxDisplayOrder < mActions.get(i).getExecOrder())
            {
                iMaxDisplayOrder = mActions.get(i).getExecOrder();
            }
        }
        
        if(i == mActions.size())
        {
            action.setExecOrder(iMaxDisplayOrder + 1);
            mActions.add(action);
        }
        
    }

    public void setNextTime(long timeInMillis)
    {
        mNextTime = timeInMillis;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags)
    {
        p.writeLong(mNextTime);
        p.writeSerializable(mTimerDef);
        p.writeSerializable(mLoopPolicys);
        p.writeSerializable(mActions);
    }

    @SuppressWarnings("unchecked")
    public Timer(Parcel p)
    {
        mNextTime = p.readLong();
        mTimerDef = (TTimerDef)p.readSerializable();
        mLoopPolicys = (Vector<TLoopPolicy>) p.readSerializable();
        mActions = (Vector<TAction>)p.readSerializable();
    }

    public Timer()
    {
        mTimerDef = new TTimerDef();
        mLoopPolicys = new Vector<TLoopPolicy>();
        mActions = new Vector<TAction>();
        mNextTime = -1;
    }

    public static final Parcelable.Creator<Timer> CREATOR = new Parcelable.Creator<Timer>()
    {
        public Timer createFromParcel(Parcel p)
        {
            return new Timer(p);
        }

        public Timer[] newArray(int size)
        {
            return new Timer[size];
        }
    };

}
