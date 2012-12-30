package com.thorqq.magictimer.core;


import java.util.Calendar;

import com.thorqq.magictimer.db.DBHelper;
import com.thorqq.magictimer.util.Util;


public class QuickSetting
{
    static protected Calendar mNow = Calendar.getInstance();
    static protected final String NEW_TIMER_NAME = "������";
    static protected final String NEW_TIMER_REMARK = "��ע";

    /**����ʱ������
     * @param startHour ʱ
     * @param startMinute ��
     * @return TTimer
     */
    static public TTimerDef setHourMinute(int startHour, int startMinute)
    {
        int maxCount = 1;
        int intervalMinutes = 0;
        return QuickSetting.setTime(maxCount, intervalMinutes, startHour, startMinute);
    }
    
    /**����ʱ
     * @param intervalMinutes ����ʱ�ķ�����������intervalMinutes���Ӻ�������
     * @return TTimer
     */
    static public TTimerDef countdownFromNowOn(int intervalMinutes)
    {
        int maxCount = 1;
        int startHour = mNow.get(Calendar.HOUR_OF_DAY);
        int startMinute = mNow.get(Calendar.MINUTE) + intervalMinutes;
        return QuickSetting.setTime(maxCount, 0, startHour, startMinute);
    }
    
    /**���㱨ʱ
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
    
    /**�����ڿ�ʼ��ÿ��xx��������
     * @param intervalMinutes ���������
     * @return TTimer
     */
    static public TTimerDef loopBySpecificMinutes(int intervalMinutes)
    {
        int maxCount = -1;
        int startHour = mNow.get(Calendar.HOUR_OF_DAY);
        int startMinute = mNow.get(Calendar.MINUTE);
        return QuickSetting.setTime(maxCount, intervalMinutes, startHour, startMinute);
    }
    
    /** �ڲ�����
     */
    static public TTimerDef setTime(int maxCount, int intervalMinutes, int startHour, int startMinute)
    {
        int id = DBHelper.getIntance().genTimerID();
        //Ĭ��displayOrder����id
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
    
    /**ÿ��ѭ��
     * @return TLoopPolicy
     */
    static public TLoopPolicy everyday()
    {
        return specificDayNumCycle(new char[]{'1'});
    }
    
    /**ÿ��ѭ��
     * @param dayMask ѭ��������
     * @return TLoopPolicy
     */
    static public TLoopPolicy weekCycle(char[] dayMask)
    {
        TLoopPolicyWeek tlp = new TLoopPolicyWeek(TLoopPolicy.LOOP_POLICY_WEEK, null);
        
//        long tmp = mNow.getTimeInMillis();
//        //mNow.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
//        int dayOfWeek = mNow.get(Calendar.DAY_OF_WEEK);
//        //TODO ���������1�ţ���ôaddһ���������Ƿ��ص���һ�ꣿ��
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

    /**ָ��ѭ������
     * @param dayMask ѭ��������
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
    
    /**���쵹��ʱ
     * @param countOfDay ����ʱ������������countOfDay�������
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
    
    /**����ѭ�����ڲ�����ѭ��
     * @param monthMask ������
     * @param dayMask ������
     * @param lunarFlag �Ƿ�����
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

    /**ÿ�µ�ĳ����
     * @param dayMask ������
     * @param lunarFlag �Ƿ�����
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

    /**��������ѭ�����ڲ�����ѭ��
     * @param monthMask ������
     * @param weekMask ������
     * @param weekDayMask ����������
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

    /**ÿ�µ�ĳ�����ܼ�
     * @param weekMask ������
     * @param weekDayMask ����������
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

    /**ÿ���ָ������
     * @param month ��
     * @param day ��
     * @param lunarFlag �Ƿ�������1���ǣ�0����
     * @return TLoopPolicy
     */
    static public TLoopPolicy annualCycle(int month, int day, int lunarFlag)
    {
        int maxCount = -1;
        int year = mNow.get(Calendar.YEAR);
        return setSpecificDay(year, month, day, maxCount, lunarFlag);
    }    
            
    /**ָ�������ա�һ���Ե�
     * @param year ��
     * @param month ��
     * @param day ��
     * @param lunarFlag �Ƿ�������1���ǣ�0����
     * @return TLoopPolicy
     */
    static public TLoopPolicy specifiedDate(int year, int month, int day, int lunarFlag)
    {
        int maxCount = 1;
        return setSpecificDay(year, month, day, maxCount, lunarFlag);
    }
    
    /** �ڲ�����
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

