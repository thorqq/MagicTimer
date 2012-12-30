package com.thorqq.magictimer.core;

import java.util.Calendar;

import com.thorqq.magictimer.util.Util;


public class TLoopPolicyDay extends TLoopPolicy
{
    private static final long serialVersionUID = -6319428876086320225L;
    protected static final int INFINITY_COUNT = -1;
    protected final int PARAM_NUM = 5;

    protected int     mLoopDays = 1;  //循环天数
    protected int     mMaxCount = INFINITY_COUNT; //循环次数
    protected char[]  mDayMask;       //日掩码
    protected Calendar mStartDate;     //开始年月日
    protected int     mLunarFlag = 0;  //mStartDate的阴历标志

    public TLoopPolicyDay(int loopPolicyType, String loopParam)
    {
        super(loopPolicyType, loopParam);
    }

    public TLoopPolicyDay(int id, int displayOrder, int loopPolicyType, String loopParam, int excludeFlag)
    {
        super(id, displayOrder, loopPolicyType, loopParam, excludeFlag);
    }

    @Override
    public String toString()
    {
        return super.toString();    
    }

    @Override
    public String getDescription()
    {
        StringBuffer sff = new StringBuffer();
        if(mLoopDays == 1)
        {
            sff.append("每天");
        }
        else if(mLoopDays > 1)
        {
            if(mLoopDays > mDayMask.length)
            {
                return "无效的循环周期：" + mLoopDays + "天";
            }
            int flag = 0;
            sff.append(mLoopDays).append("天一个循环，第");
            for(int i = 0; i < mLoopDays; i++)
            {
                if(mDayMask[i] == '1')
                {
                    if(flag == 0)
                    {
                        sff.append(i+1);
                        flag = 1;
                    }
                    else
                    {
                        sff.append("、").append(i+1);
                    }
                }
            }
            sff.append("天启动");
        }
        
        return sff.toString();
    }

    public void setParam(
            int     loopDays,
            int     maxCount,
            char[]  dayMask,
            Calendar startDate,
            int     lunarFlag)
    {
        mLoopDays  = loopDays;
        mMaxCount  = maxCount;
        mDayMask   = dayMask.clone();
        mStartDate = (Calendar) startDate.clone();
        mLunarFlag = lunarFlag;
        
        this.paramToString();
    }

    @Override
    public boolean parseStringParam()
    {
        String[] array = Util.splitParames(mLoopParam);
        if(array.length != PARAM_NUM)
        {
            Util.log("TimeParam setParam error. param = " + mLoopParam);
            return false;
        }
                    
        mLoopDays  = Util.strToInt(array[0]);
        mMaxCount  = Util.strToInt(array[1]);
        mDayMask   = array[2].toCharArray();
        mStartDate = (Calendar)Util.getCalendar(array[3]).clone();
        mLunarFlag = Util.strToInt(array[4]);

        return true;
    }

    @Override
    public String paramToString()
    {
        StringBuffer sff = new StringBuffer();
        sff.append(Util.intToStr(mLoopDays))
           .append(Util.PARAMES_HYPHEN).append(Util.intToStr(mMaxCount))
           .append(Util.PARAMES_HYPHEN).append(Util.arrayToString(mDayMask))
           .append(Util.PARAMES_HYPHEN).append(Util.dateToYYYYMMDD(mStartDate.getTime()))
           .append(Util.PARAMES_HYPHEN).append(Util.intToStr(mLunarFlag))
           ;
        
        return super.mLoopParam = sff.toString();
    }
    
    public String CheckValidity ()
    {
//        protected int     mLoopDays = 1;  //循环天数
        if(mLoopDays < 1)
        {
            return "循环天数必须大于零";
        }

//        protected int     mMaxCount = INFINITY_COUNT; //循环次数
        if(mMaxCount < TimerCalculator.INFINITY_COUNT || mMaxCount == 0)
        {
            return "“循环次数”不合法，须大于零";
        }

//        protected char[]  mDayMask;       //日掩码
        if(mLoopDays != mDayMask.length)
        {
            return "循环天数与启动日设置不一致";
        }
        
        int flag = 0;
        for(char c: mDayMask)
        {
            if(c == '1')
            {
                flag = 1;
                break;
            }
        }
        
        if(flag == 0)
        {
            return "没有设置启动日";
        }
//        protected Calendar mStartDate;     //开始年月日
//        protected int     mLunarFlag = 0;  //mStartDate的阴历标志
        if(mLunarFlag != 0 && mLunarFlag != 1)
        {
            return "农历标识不合法（" + mLunarFlag + "）";
        }
        
        return null;
    }

    public int getLoopDays()
    {
        return mLoopDays;
    }
    
    public int getMaxCount()
    {
        return mMaxCount;
    }
    
    public char[] getDayMask()
    {
        return mDayMask;
    }
        
    public Calendar getStartDate()
    {
        return mStartDate;
    }
    
    public int getLunarFlag()
    {
        return mLunarFlag;
    }

}
