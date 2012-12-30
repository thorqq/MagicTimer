package com.thorqq.magictimer.core;

import java.util.Calendar;

public class TLoopPolicyWeek extends TLoopPolicyDay
{
    private static final long serialVersionUID = 4162434193286125235L;
    public static final int DAYS_OF_WEEK = 7; 

    public TLoopPolicyWeek(int id, int displayOrder, int loopPolicyType,
            String loopParam, int excludeFlag)
    {
        super(id, displayOrder, loopPolicyType, loopParam, excludeFlag);
    }
    
    public TLoopPolicyWeek(int loopPolicyType, String loopParam)
    {
        super(loopPolicyType, loopParam);
    }

    public void setParam(char[]  dayMask)
    {
        mLoopDays  = DAYS_OF_WEEK;
        mMaxCount  = TimerCalculator.INFINITY_COUNT;
        mDayMask   = dayMask.clone();
        mLunarFlag = 0;

        mStartDate = Calendar.getInstance();
        mStartDate.setFirstDayOfWeek(Calendar.SUNDAY);
        mStartDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        
//        int dayOfWeek = mStartDate.get(Calendar.DAY_OF_WEEK);
//        //TODO 如果当天是1号，那么add一个负数后，是否会回到上一年？？
//        mStartDate.add(Calendar.DAY_OF_YEAR, -dayOfWeek+1);

        super.paramToString();
    }

    @Override
    public String getDescription()
    {
        StringBuffer sff = new StringBuffer();
        sff.append("每周的周");
        for(int i = 0; i < 7; i++)
        {
            if(mDayMask[i] == '1')
            {
                sff.append(ZH_WEEK[i]).append(",");
            }
        }
        sff.deleteCharAt(sff.length()-1);

        return sff.toString();
    }

}
