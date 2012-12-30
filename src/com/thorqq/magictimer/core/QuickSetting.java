package com.thorqq.magictimer.core;


import java.util.Calendar;

import com.thorqq.magictimer.db.DBHelper;
import com.thorqq.magictimer.util.Util;


public class QuickSetting
{
    static protected Calendar mNow = Calendar.getInstance();
    static protected final String NEW_TIMER_NAME = "新提醒";
    static protected final String NEW_TIMER_REMARK = "备注";

    /**启动时刻设置
     * @param startHour 时
     * @param startMinute 分
     * @return TTimer
     */
    static public TTimerDef setHourMinute(int startHour, int startMinute)
    {
        int maxCount = 1;
        int intervalMinutes = 0;
        return QuickSetting.setTime(maxCount, intervalMinutes, startHour, startMinute);
    }
    
    /**倒计时
     * @param intervalMinutes 倒计时的分钟数，即在intervalMinutes分钟后启动。
     * @return TTimer
     */
    static public TTimerDef countdownFromNowOn(int intervalMinutes)
    {
        int maxCount = 1;
        int startHour = mNow.get(Calendar.HOUR_OF_DAY);
        int startMinute = mNow.get(Calendar.MINUTE) + intervalMinutes;
        return QuickSetting.setTime(maxCount, 0, startHour, startMinute);
    }
    
    /**整点报时
     * @return TTimer
     */
    static public TTimerDef fullHour()
    {
        int maxCount = -1;
        int intervalMinutes = 60;
        int startHour = mNow.get(Calendar.HOUR_OF_DAY);
        int startMinute = 0;
        return QuickSetting.setTime(maxCount, intervalMinutes, startHour, startMinute);
    }
    
    /**从现在开始，每隔xx分钟提醒
     * @param intervalMinutes 间隔分钟数
     * @return TTimer
     */
    static public TTimerDef loopBySpecificMinutes(int intervalMinutes)
    {
        int maxCount = -1;
        int startHour = mNow.get(Calendar.HOUR_OF_DAY);
        int startMinute = mNow.get(Calendar.MINUTE);
        return QuickSetting.setTime(maxCount, intervalMinutes, startHour, startMinute);
    }
    
    /** 内部函数
     */
    static public TTimerDef setTime(int maxCount, int intervalMinutes, int startHour, int startMinute)
    {
        int id = DBHelper.getIntance().genTimerID();
        //默认displayOrder等于id
        int enable = 1;
        int lastAlertTime = -1;
        String name = NEW_TIMER_NAME + "-"+ id;
        String remark = NEW_TIMER_REMARK + "-"+ id;
        
        TTimerDef t = new TTimerDef(id, id, 
                startHour, startMinute, maxCount, intervalMinutes, lastAlertTime, enable, 
                name, remark);
        return t;
    }    
    
    //************************************************************************//
    //************************************************************************//
    
    /**每天循环
     * @return TLoopPolicy
     */
    static public TLoopPolicy everyday()
    {
        return specificDayNumCycle(new char[]{'1'});
    }
    
    /**每周循环
     * @param dayMask 循环日掩码
     * @return TLoopPolicy
     */
    static public TLoopPolicy weekCycle(char[] dayMask)
    {
        TLoopPolicyWeek tlp = new TLoopPolicyWeek(TLoopPolicy.LOOP_POLICY_WEEK, null);
        
//        long tmp = mNow.getTimeInMillis();
//        //mNow.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
//        int dayOfWeek = mNow.get(Calendar.DAY_OF_WEEK);
//        //TODO 如果当天是1号，那么add一个负数后，是否会回到上一年？？
//        mNow.add(Calendar.DAY_OF_YEAR, -dayOfWeek+1);
//        int loopDays = 7; 
//        int maxCount = -1;
//        int lunarFlag = 0;
//        
//        tlp.setParam(loopDays, maxCount, dayMask, mNow, lunarFlag);
//
//        mNow.setTimeInMillis(tmp);

        tlp.setParam(dayMask);

        return tlp;
    }

    /**指定循环天数
     * @param dayMask 循环日掩码
     * @return TLoopPolicy
     */
    static public TLoopPolicy specificDayNumCycle(char[] dayMask)
    {
        TLoopPolicyDay lp = new TLoopPolicyDay(TLoopPolicy.LOOP_POLICY_DAY, null);
        int loopDays = dayMask.length; 
        int maxCount = -1;
        int lunarFlag = 0;
        lp.setParam(loopDays, maxCount, dayMask, mNow, lunarFlag);
        
        return lp;
    }
    
    /**按天倒计时
     * @param countOfDay 倒计时的天数，即在countOfDay天后启动
     * @return TLoopPolicy
     */
    static public TLoopPolicy countdownByDay(int countOfDay)
    {
        TLoopPolicyDay lp = new TLoopPolicyDay(TLoopPolicy.LOOP_POLICY_DAY, null);
        int loopDays = 1; 
        int maxCount = countOfDay;
        int lunarFlag = 0;
        char[] dayMask = new char[]{'1'};
        lp.setParam(loopDays, maxCount, dayMask, mNow, lunarFlag);

        return lp;
    }    
    
    /**按月循环，内部按日循环
     * @param monthMask 月掩码
     * @param dayMask 日掩码
     * @param lunarFlag 是否阴历
     * @return TLoopPolicy
     */
    static public TLoopPolicy monthAndDayCycle(char[] monthMask, char[] dayMask, int lunarFlag)
    {
        TLoopPolicyMon lp = new TLoopPolicyMon(TLoopPolicy.LOOP_POLICY_MONTH, null);
                
        char[] weekMask = null;
        char[] weekDayMask = null;
        int subLoopType = TLoopPolicyMon.MONTH_LOOP_SUB_LOOP_DAY;
        lp.setParam(lunarFlag, monthMask, subLoopType, dayMask, weekMask, weekDayMask);

        return lp;
    }

    /**每月的某几日
     * @param dayMask 日掩码
     * @param lunarFlag 是否阴历
     * @return TLoopPolicy
     */
    static public TLoopPolicy monthAndDayCycle(char[] dayMask, int lunarFlag)
    {
        TLoopPolicyMon lp = new TLoopPolicyMon(TLoopPolicy.LOOP_POLICY_MONTH, null);
                
        char[] monthMask = new char[TLoopPolicyMon.LENGTH_OF_MONTH_MASK];
        Util.initializeCharArray(monthMask, '1');
        char[] weekMask = null;
        char[] weekDayMask = null;
        int subLoopType = TLoopPolicyMon.MONTH_LOOP_SUB_LOOP_DAY;
        lp.setParam(lunarFlag, monthMask, subLoopType, dayMask, weekMask, weekDayMask);

        return lp;
    }

    /**按阳历月循环，内部按周循环
     * @param monthMask 月掩码
     * @param weekMask 周掩码
     * @param weekDayMask 周内日掩码
     * @return TLoopPolicy
     */
    static public TLoopPolicy monthAndWeekDayCycle(char[] monthMask, char[] weekMask, char[] weekDayMask)
    {
        TLoopPolicyMon lp = new TLoopPolicyMon(TLoopPolicy.LOOP_POLICY_MONTH, null);
                
        char[] dayMask = null;
        int lunarFlag = 0;
        int subLoopType = TLoopPolicyMon.MONTH_LOOP_SUB_LOOP_WEEK;
        
        lp.setParam(lunarFlag, monthMask, subLoopType, dayMask, weekMask, weekDayMask);

        return lp;
    }

    /**每月的某几个周几
     * @param weekMask 周掩码
     * @param weekDayMask 周内日掩码
     * @return TLoopPolicy
     */
    static public TLoopPolicy monthAndWeekDayCycle(char[] weekMask, char[] weekDayMask)
    {
        TLoopPolicyMon lp = new TLoopPolicyMon(TLoopPolicy.LOOP_POLICY_MONTH, null);
                
        char[] monthMask = new char[TLoopPolicyMon.LENGTH_OF_MONTH_MASK];
        Util.initializeCharArray(monthMask, '1');
        char[] dayMask = null;
        int lunarFlag = 0;
        int subLoopType = TLoopPolicyMon.MONTH_LOOP_SUB_LOOP_WEEK;
        
        lp.setParam(lunarFlag, monthMask, subLoopType, dayMask, weekMask, weekDayMask);

        return lp;
    }

    /**每年的指定日期
     * @param month 月
     * @param day 日
     * @param lunarFlag 是否阴历。1：是，0：否
     * @return TLoopPolicy
     */
    static public TLoopPolicy annualCycle(int month, int day, int lunarFlag)
    {
        int maxCount = -1;
        int year = mNow.get(Calendar.YEAR);
        return setSpecificDay(year, month, day, maxCount, lunarFlag);
    }    
            
    /**指定年月日。一次性的
     * @param year 年
     * @param month 月
     * @param day 日
     * @param lunarFlag 是否阴历。1：是，0：否
     * @return TLoopPolicy
     */
    static public TLoopPolicy specifiedDate(int year, int month, int day, int lunarFlag)
    {
        int maxCount = 1;
        return setSpecificDay(year, month, day, maxCount, lunarFlag);
    }
    
    /** 内部函数
     */
    static protected TLoopPolicy setSpecificDay(int year, int month, int day, int maxCount, int lunarFlag)
    {
        TLoopPolicyDay tlp = new TLoopPolicyDay(TLoopPolicy.LOOP_POLICY_DAY, null);
        char[] dayMask = new char[]{'1'};
        int loopDays = dayMask.length; 

        long tmp = mNow.getTimeInMillis();
        mNow.set(year, month-1, day, 0, 0, 0);
        
        tlp.setParam(loopDays, maxCount, dayMask, mNow, lunarFlag);
        mNow.setTimeInMillis(tmp);

        return tlp;
    }
}

