package com.thorqq.magictimer.core;

import java.io.Serializable;


import com.thorqq.magictimer.util.Util;

/**
 * @author THORQQ<br>
 *         * ���ݿ�� timer
 */
public class TTimerDef implements Serializable
{
    private static final long serialVersionUID = 1608113076741432475L;

    public static final int INVALID_TIME = -1;

    // ���ݿ��ֶ�
    private int mID = -1;
    private int mDisplayOrder = -1;

    // ��ʱ�����ò���
    private int mStartHour = INVALID_TIME;
    private int mStartMinute = INVALID_TIME;
    private int mMaxCount = 1;
    private int mInterval = 10;

    private long mLastAlertTime = INVALID_TIME;
    private int mEnable = 0;

    private String mName = null;
    private String mRemark = null;

    public TTimerDef()
    {

    }

    public TTimerDef(int id, int displaOrder, int startHour, int startMinute,
            int maxCount, int interval, long lastAlertTime, int enable,
            String name, String remark)
    {
        mID = id;
        mDisplayOrder = displaOrder;

        mStartHour = startHour;
        mStartMinute = startMinute;
        mMaxCount = maxCount;
        mInterval = interval;

        mLastAlertTime = lastAlertTime;
        mEnable = enable;

        mName = name;
        mRemark = remark;
    }
    
    public String CheckValidity ()
    {
        if(mStartHour < 0 || mStartHour > 23)
        {
            return "��Сʱ�����Ϸ�";
        }
        
        if(mStartMinute < 0 || mStartMinute > 59)
        {
            return "�����ӡ����Ϸ�";
        }
        
        if(mMaxCount < TimerCalculator.INFINITY_COUNT || mMaxCount == 0)
        {
            return "��ѭ�����������Ϸ����������";
        }

        if(mInterval < 0 || (mMaxCount != 1 && mInterval == 0))
        {
            return "�����ʱ�䡱���Ϸ����������";
        }

        return null;
    }

    @Override
    public String toString()
    {
        StringBuffer sff = new StringBuffer();
        sff.append("mID = ").append(Util.intToStr(mID)).append("\n")
                .append("mDisplayOrder = ")
                .append(Util.intToStr(mDisplayOrder)).append("\n")
                .append("mStartHour = ").append(Util.intToStr(mStartHour))
                .append("\n").append("mStartMinute = ")
                .append(Util.intToStr(mStartMinute)).append("\n")
                .append("mMaxCount = ").append(Util.intToStr(mMaxCount))
                .append("\n").append("mInterval = ")
                .append(Util.intToStr(mInterval)).append("\n")
                .append("mLastAlertTime = ")
                .append(Util.intToStr(mLastAlertTime)).append("(").append(Util.MillisToStr(mLastAlertTime)).append(")").append("\n")
                .append("mEnable = ").append(Util.intToStr(mEnable))
                .append("\n").append("mName = ").append(mName).append("\n")
                .append("mRemark = ").append(mRemark).append("\n");
        return sff.toString();
    }

    public String getDescription()
    {
        String str = "";
        String startTime = Util.formatTwoNumber(getStartHour()) + ":"
                + Util.formatTwoNumber(getStartMinute());

        if (getMaxCount() == -1)
        {
            str = "��" + startTime + "��ʼ��ÿ��" + getInterval() + "��������һ��";
        }
        else if (getMaxCount() == 1)
        {
            str = startTime;
        }
        else if (getMaxCount() > 1)
        {
            str = "��" + startTime + "��ʼ��ÿ��" + getInterval() + "��������һ�Σ�������"
                    + getMaxCount() + "��";
        }

        return str;
    }

    // ���ݿ��ֶ�
    public int getID()
    {
        return mID;
    }

    public void setID(int id)
    {
        mID = id;
    }

    public int getDisplayOrder()
    {
        return mDisplayOrder;
    }

    public void setDisplayOrder(int displayOrder)
    {
        mDisplayOrder = displayOrder;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public String getRemark()
    {
        return mRemark;
    }

    public void setRemark(String remark)
    {
        mRemark = remark;
    }

    public int isEnable()
    {
        return mEnable;
    }

    public void setEnable(int enable)
    {
        mEnable = enable;
    }

    // ��ʱ�����ò���
    public int getMaxCount()
    {
        return mMaxCount;
    }

    public int getInterval()
    {
        return mInterval;
    }

    public int getStartHour()
    {
        return mStartHour;
    }

    public int getStartMinute()
    {
        return mStartMinute;
    }

    public long getLastAlertTime()
    {
        return mLastAlertTime;
    }

    //
    public void setMaxCount(int maxCount)
    {
        mMaxCount = maxCount;
    }

    public void setInterval(int interval)
    {
        mInterval = interval;
    }

    public void setStartHour(int startHour)
    {
        mStartHour = startHour;
    }

    public void setStartMinute(int startMinute)
    {
        mStartMinute = startMinute;
    }

    public void setLastAlertTime(long lastAlertTime)
    {
        mLastAlertTime = lastAlertTime;
    }

}
