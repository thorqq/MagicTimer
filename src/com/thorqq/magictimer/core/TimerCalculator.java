package com.thorqq.magictimer.core;

import java.util.Calendar;
import java.util.Vector;

import com.thorqq.magictimer.util.ChineseCalendar;
import com.thorqq.magictimer.util.Util;



/** 时间方案设置
 * @author THORQQ
 *
 */
public class TimerCalculator
{
    public static final int INVALID_TIME   = -1;
    public static final int INFINITY_COUNT = -1;
    
    private static final int MAX_CALCULATE_VALID_DAYS = 10;
    private static final int MAX_CALCULATE_ADD_DAY_NUM = 366;
    private static final int MAX_LOOP_NUM = 5;
    
    private TimeCalculator mTimeCal = new TimeCalculator();
    private Vector<LoopCalculator> mLoopCal = new Vector<LoopCalculator>();
    private Vector<LoopCalculator> mExLoopCal = new Vector<LoopCalculator>();
    private SortedIntArray mValidDays = new SortedIntArray();
    private Calendar mBase = Calendar.getInstance();;
    private long mResult = 0;
    
    /**
     * @param now - Calendar 当前时间
     */
    public TimerCalculator(long nowInMillis)
    {
        mBase.setTimeInMillis(nowInMillis);
    }

    public TimerCalculator() {}

    public void setTimeInMillis(long nowInMillis)
    {
        mBase.setTimeInMillis(nowInMillis);
    }
    
    public void reset()
    {
        mLoopCal.clear();
        mExLoopCal.clear();
        mValidDays.clear();
        mResult = 0;
    }
    
    public Vector<LoopCalculator> getLoopPolicy(int excludeFlag)
    {
        if(TLoopPolicy.LOOP_PARAM_EXCLUDE == excludeFlag)
        {
            return mExLoopCal;
        }
        else
        {
            return mLoopCal;
        }
    }
    
    //从字符串中解析参数
    public void setTime(TTimerDef timerDef)
    {
        mTimeCal.setTTimerParam(timerDef);
    }
    
    //从字符串中解析参数
    public boolean addLoopPolicy(TLoopPolicy loopPolicy)
    {
        return addLoopPolicy(loopPolicy, loopPolicy.getExcludeFlag());
    }
    
    public boolean addLoopPolicy(TLoopPolicy loopPolicy, int excludeFlag)
    {
        LoopCalculator loopCalculator = null;
        if(TLoopPolicy.LOOP_POLICY_DAY == loopPolicy.getPolicyType())
        {
            loopCalculator = new DayLoopCalculator();
        }
        else if(TLoopPolicy.LOOP_POLICY_WEEK == loopPolicy.getPolicyType())
        {
            loopCalculator = new DayLoopCalculator();
        }
        else if(TLoopPolicy.LOOP_POLICY_MONTH == loopPolicy.getPolicyType())
        {
            loopCalculator = new MonLoopCalculator();
        }
        
        //设置参数
        loopCalculator.setLoopPolicy(loopPolicy);
        
        if(TLoopPolicy.LOOP_PARAM_EXCLUDE == excludeFlag)
        {
            mExLoopCal.add(loopCalculator);
        }
        else
        {
            mLoopCal.add(loopCalculator);
        }
        
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuffer sff = new StringBuffer();
        sff.append("TimeParam: ").append(mTimeCal.toString()).append("\n");
        
        for(LoopCalculator lp : mLoopCal)
        {
           sff.append("LoopParam: ").append(lp.toString()).append("\n");
        }
        for(LoopCalculator lp : mExLoopCal)
        {
           sff.append("ExcludeLoopParam: ").append(lp.toString()).append("\n");
        }           
        sff.append("ValidDays: ").append(mValidDays).append("\n");
        sff.append("Result: ").append(Util.MillisToStr(mResult));
        
        return sff.toString();
    }    

    //计算生效日期
    public long caculate(long lastAlertTime)
    {
        //备份原始的mBase
        long baseTimeInMillisBak = mBase.getTimeInMillis();
        int tmpMaxValidDay = 0;
        int cumulateAddDayNum = 0;
        int loopNum = 0;
        
        //最多查找一年内的有效时间，或者最多循环5次
        for(; cumulateAddDayNum < MAX_CALCULATE_ADD_DAY_NUM || loopNum < MAX_LOOP_NUM; 
              cumulateAddDayNum += tmpMaxValidDay, loopNum++)
        {
            Util.log("mBase : " + Util.dateToStr(mBase.getTime()));
            mValidDays.clear();
            
            mValidDays.setMaxNum(MAX_CALCULATE_VALID_DAYS * mLoopCal.size());
            //计算有效日
            SortedIntArray tmp = new SortedIntArray(MAX_CALCULATE_VALID_DAYS);
            for(LoopCalculator lp : mLoopCal)
            {
                tmp.clear();
                if(!lp.caculate(tmp))
                {
                    Util.log("Include ILoopParam.caculate error");
                    return 0;
                }
                mValidDays.add(tmp);
            }
            
            if(mValidDays.size() == 0)
            {
                Util.log("After include ILoopParam.caculate, mValidDays.size() == 0");
                return 0;
            }
            
            //生效日的最大值
            tmpMaxValidDay = mValidDays.get(mValidDays.size() - 1);
            Util.log("Orgin mValidDays: " + mValidDays.toString() );
            
            //计算排除日
            int max = (mValidDays.get(mValidDays.size()-1) + 1) * mExLoopCal.size();
            SortedIntArray excludeValidDays = new SortedIntArray(max);
            for(LoopCalculator lp : mExLoopCal)
            {
                tmp.clear();
                tmp.setMaxNum(max);
                if(!lp.caculate(tmp))
                {
                    Util.log("Exclude ILoopParam.caculate error");
                    return 0;
                }
                excludeValidDays.add(tmp);
            }
            Util.log("Orgin excludeValidDays: " + excludeValidDays.toString() );
            
            //如果mValidDays全部被remove掉了，则继续查找后面的生效日
            mValidDays.remove(excludeValidDays);
            if(mValidDays.size() == 0)
            {
                Util.log("After remove exclude valid days, mValidDays.size() == 0");
                mBase.add(Calendar.DAY_OF_YEAR, tmpMaxValidDay);
                continue;
            }
            else
            {
                Util.log("Final mValidDays: " + mValidDays.toString() );  
                break;
            }
        }
        
        //复原
        mBase.setTimeInMillis(baseTimeInMillisBak);
        
        if(mValidDays.size() > 0)
        {
            for(int j = 0; j < mValidDays.size(); j++)
            {
                int tmp = cumulateAddDayNum + mValidDays.get(j);
                mValidDays.set(j, tmp);
            }
        }
        else
        {
            Util.log("Finally, mValidDays.size() == 0");
            return 0;
        }
        
        //计算下次提醒时间
        return mTimeCal.caculate(mValidDays, lastAlertTime);
    }
    
    //*************************************************************************
    //时刻设置参数
    //*************************************************************************
    private class TimeCalculator
    {        
        private TTimerDef mTimerDef;
        
        public void setTTimerParam(TTimerDef timerDef)
        {
            mTimerDef = timerDef;
        }
        
        //计算两个时间间隔的分钟数
        private long intervalMinute(Calendar c1, Calendar c2)
        {
            return c1.getTimeInMillis()/1000/60 - c2.getTimeInMillis()/1000/60;
        }
        
        //根据有效日期、上次提醒时间，算出下次提醒时间(负数表示不再启动，0表示当前已启动，不需要再启动)
        public long caculate(SortedIntArray validDays, long lastAlertTime)
        {
            //计算下一次提醒时间
            long nextAlertTime = INVALID_TIME;
            Calendar caResult = Calendar.getInstance();
            caResult.setTimeInMillis(mBase.getTimeInMillis());
            caResult.add(Calendar.DAY_OF_YEAR, validDays.get(0));
            caResult.set(Calendar.HOUR_OF_DAY, mTimerDef.getStartHour());
            caResult.set(Calendar.MINUTE, mTimerDef.getStartMinute());
            caResult.set(Calendar.SECOND, 0);
            
            long interval = intervalMinute(caResult, mBase);
            
            Util.log("Caculate time: lastAlertTime = " + Util.MillisToStr(lastAlertTime) +
                     ", Maxcount = " + mTimerDef.getMaxCount() +
                     ", Interval = " + mTimerDef.getInterval() +
                     ", StartHour= " + mTimerDef.getStartHour() +
                     ", StartMin = " + mTimerDef.getStartMinute()
                     );

            Util.log("caResult = " + Util.MillisToStr(caResult.getTimeInMillis()));

            // 尚未到启动时间，第一次执行
            if(interval > 0)
            {
                //do nothing
            }            
            //已经超过启动时间，第一次执行，则认为是在下一个生效日启动
            else if(interval <= 0 && lastAlertTime <= 0)
            {
                if(validDays.size() > 1)
                {
                    caResult.add(Calendar.DAY_OF_YEAR, validDays.get(1) - validDays.get(0));
                }
                else
                {
                    Util.log("No more validDays found, validDays.size() = " + validDays.size());
                    return 0;
                }
            } 
            //lastAlertTime > 0，表示已经执行过
            else if(interval <= 0 && lastAlertTime > 0)
            {
                //循环闹铃，从启动时间开始计算
                if(mTimerDef.getMaxCount() > 1 || mTimerDef.getMaxCount() == INFINITY_COUNT)
                {
                    long cnt = (mBase.getTimeInMillis() - caResult.getTimeInMillis())/mTimerDef.getInterval()/60/1000 + 1;
                    if(mTimerDef.getMaxCount() != INFINITY_COUNT 
                            && cnt >= mTimerDef.getMaxCount())
                    {
                        //已超过最大启动次数
                        Util.log("Excceed mMaxCount("+ mTimerDef.getMaxCount() +"). cnt = " + cnt);
                        return 0;
                    }
                    else
                    {
                        nextAlertTime = caResult.getTimeInMillis() + cnt * mTimerDef.getInterval() * 60 * 1000;    
                        caResult.setTimeInMillis(nextAlertTime);
                                         
                    }
                }
                //一次性闹铃，设置了贪睡，从lastAlertTime开始计算（贪睡时间已设置到interval中）
                else if(mTimerDef.getMaxCount() == 1)
                {
                    nextAlertTime = lastAlertTime + mTimerDef.getInterval() * 60 * 1000;   
                    
                    Util.log("lastAlertTime : " + Util.MillisToStr(lastAlertTime));
                    Util.log("nextAlertTime : " + Util.MillisToStr(nextAlertTime));
                    caResult.setTimeInMillis(nextAlertTime);
                }
                
                if(intervalMinute(caResult, mBase) < 0)
                {
                    return 0;
                }


            }            
            
            return caResult.getTimeInMillis();
        }
    }

    //*************************************************************************
    //循环策略设置参数解析接口
    //*************************************************************************
    interface LoopCalculator
    {
        public void setLoopPolicy(TLoopPolicy loopPolicy);
        //计算下一次启动的时间
        public boolean caculate(SortedIntArray validDays);
//        public String toString();
    }

    //*************************************************************************
    //按天循环设置参数
    //*************************************************************************
    private class DayLoopCalculator implements LoopCalculator
    {
        private TLoopPolicyDay mDayLoopPolicy = null;

        @Override
        public void setLoopPolicy(TLoopPolicy loopPolicy)
        {
            mDayLoopPolicy = (TLoopPolicyDay) loopPolicy;
        }

        @Override
        public boolean caculate(SortedIntArray validDays)
        {
            int maxCount = mDayLoopPolicy.getMaxCount();
            int loopDays = mDayLoopPolicy.getLoopDays();
            
            int maxLoopNum = 0;
            if(maxCount == INFINITY_COUNT)
            {
                maxLoopNum = loopDays * 1000;
            }
            else
            {
                maxLoopNum = loopDays * maxCount;
            }

            int interval = TimerCalculator.calendarIntervalDays(mDayLoopPolicy.getStartDate(), mBase);
            if(interval > 0)
            {
                //int interval = TimeSchema.calendarIntervalDays(mStartDate, mBase);

                //计算将来生效的日期
                for(int i = 0; i < maxLoopNum && !validDays.isFull(); i++)
                {
                    if(mDayLoopPolicy.getDayMask()[i%loopDays] == '1')
                    {
                        validDays.add(i + interval);
                    }
                }
            }
            else  //已生效
            {
                interval *= -1;
                //int interval = TimeSchema.calendarIntervalDays(mBase, mStartDate) + 1;
                //已失效
                if(maxCount != INFINITY_COUNT && interval >= loopDays * maxCount)
                {
                    Util.log("Excceed mMaxCount. mMaxCount = " + maxCount + ", mLoopDays = " + loopDays);
                    return false;
                }
                
                //计算将来生效的10天
                for(int i = 0; i < maxLoopNum - interval && !validDays.isFull(); i++)
                {
                    if(mDayLoopPolicy.getDayMask()[(i + interval)%loopDays] == '1')
                    {
                        validDays.add(i);
                    }
                }
            }
            
            if(validDays.size() <= 0)
            {
                Util.log("validDays.size() = " + validDays.size());
                return false;
            }
            
            return true;
        }

    }
        
    //*************************************************************************
    //按月循环设置参数
    //*************************************************************************
    private class MonLoopCalculator implements LoopCalculator
    {

        private TLoopPolicyMon mMonLoopPolicy = null;

        @Override
        public void setLoopPolicy(TLoopPolicy loopPolicy)
        {
            mMonLoopPolicy = (TLoopPolicyMon) loopPolicy;
        }

        @Override
        public boolean caculate(SortedIntArray validDays)
        {
            final int MONTH_NUM_OF_YEAR = 12;
            
            int lunarFlag   = mMonLoopPolicy.getLunarFlag();
            int subLoopType = mMonLoopPolicy.getSubLoopType();
            
            //判断是否是阴历
            int CALENDAR_MONTH = Calendar.MONTH;
            int CALENDAR_DATE  = Calendar.DATE;
            if(lunarFlag == 1)
            {
                CALENDAR_MONTH = ChineseCalendar.CHINESE_MONTH;
                CALENDAR_DATE  = ChineseCalendar.CHINESE_DATE;
            }
            
            Calendar mChineseBase = createCalendar(mBase, lunarFlag);
            int maxLoopNum = MONTH_NUM_OF_YEAR + mChineseBase.get(CALENDAR_MONTH) + 1;
            
            //查找有效的月份
            SortedIntArray validMonthsTmp = new SortedIntArray();
            int monthOfBase = mChineseBase.get(CALENDAR_MONTH);
            for(int i = mChineseBase.get(CALENDAR_MONTH); i < maxLoopNum && !validDays.isFull(); i++)
            {
                if(mMonLoopPolicy.getMonthMask()[i % MONTH_NUM_OF_YEAR] == '1')
                {
                    validMonthsTmp.add(i - monthOfBase);
                }
            }
            if(validMonthsTmp.size() == 0)
            {
                Util.log("validMonthsTmp.size() == 0");
                return false;
            }

            //转换为"当月的第一天"与当前月份1号的天数之差
            Calendar caFirstDayOfBase = createCalendar(mBase, lunarFlag);
            caFirstDayOfBase.set(CALENDAR_DATE, 1);
            
            //validMonthsTmp已经有序，且无重复，所以这里可以用Vector<Integer>
            //validDayNumOfMonths只能用Vector，因为里面有重复元素
            Vector<Integer> validFirstDayOfMonths = new Vector<Integer>();//有效月份的第一天
            Vector<Integer> validDayNumOfMonths = new Vector<Integer>();//有效的那个月的天数
            
            Calendar caTmp = createCalendar(mBase, lunarFlag);
            caTmp.add(CALENDAR_MONTH, validMonthsTmp.get(0) - 0);
            caTmp.set(CALENDAR_DATE, 1);
            int inter = TimerCalculator.calendarIntervalDays(caTmp, caFirstDayOfBase);
            
            validFirstDayOfMonths.add(inter);
            validDayNumOfMonths.add(caTmp.getActualMaximum(CALENDAR_DATE));//调用getActualMaximum时，DAY_OF_MONTH必须要小于29
            
            for(int i = 1; i < validMonthsTmp.size(); i++)
            {
                caTmp.add(CALENDAR_MONTH, validMonthsTmp.get(i) - validMonthsTmp.get(i-1));
                caTmp.set(CALENDAR_DATE, 1);
                validFirstDayOfMonths.add(TimerCalculator.calendarIntervalDays(caTmp, caFirstDayOfBase));
                validDayNumOfMonths.add(caTmp.getActualMaximum(CALENDAR_DATE));
            }
            
            //月内按天循环
            if(subLoopType == TLoopPolicyMon.MONTH_LOOP_SUB_LOOP_DAY)
            {
                int newBase = mChineseBase.get(CALENDAR_DATE) - 1;
                
                for(int i = 0; i < validFirstDayOfMonths.size() && !validDays.isFull(); i++)
                {
                    for(int j = 0; j < validDayNumOfMonths.get(i) && !validDays.isFull(); j++)
                    {
                        if(mMonLoopPolicy.getDayMask()[j] == '1')
                        {
                            int a = validFirstDayOfMonths.get(i) + j - newBase;
                            if(a >= 0)
                            {
                                validDays.add(a);
                            }
                        }
                    }
                    
                    //月内的最后一天
                    if(mMonLoopPolicy.getDayMask()[TLoopPolicyMon.LENGTH_OF_DAY_MASK - 1] == '1')
                    {
                        int a = validFirstDayOfMonths.get(i) + validDayNumOfMonths.get(i) - 1 - newBase;
                        if(a >= 0 && !validDays.isFull())
                        {
                            validDays.add(a);
                        }
                    }
                }
            }
            //月内按周循环
            else if(subLoopType == TLoopPolicyMon.MONTH_LOOP_SUB_LOOP_WEEK)
            {
                Calendar tmp = (Calendar) caFirstDayOfBase.clone();
                for(int i = 0; i < validFirstDayOfMonths.size() - 1 && !validDays.isFull(); i++)
                {
                    //第几周
                    for(int m = 0; m < 5 && !validDays.isFull(); m++)
                    {
                        //周几
                        for(int n = 0; n < 7 && !validDays.isFull(); n++)
                        {
                            if(mMonLoopPolicy.getWeekMask()[m] == '1' 
                                && mMonLoopPolicy.getWeekDayMask()[n] == '1')
                            {
                                //最后一周
                                int m2 = m+1;
                                if(m == 4)
                                {
                                    m2 = -1;
                                }
                                
                                long t = tmp.getTimeInMillis();
                                tmp.add(Calendar.DAY_OF_YEAR, validFirstDayOfMonths.get(i));
                                tmp.set(Calendar.DAY_OF_WEEK, n+1);//DAY_OF_WEEK从周日至周六，分别是1-7
                                tmp.set(Calendar.DAY_OF_WEEK_IN_MONTH, m2);
                                int a = TimerCalculator.calendarIntervalDays(tmp, mBase);
                                if(a >= 0)
                                {
                                    validDays.add(a);
                                }
                                //复原
                                tmp.setTimeInMillis(t);
                            }
                        }
                    }                    
                }                
            }
            
            return true;
        }
    }
    
    //计算两天相隔的天数
    private static int calendarIntervalDays(Calendar c1, Calendar c2)
    {
        //不能修改年月日，因为c1,c2可能是阴历
        Calendar a1 = (Calendar) c1.clone();
        a1.set(Calendar.HOUR_OF_DAY, 0);
        a1.set(Calendar.MINUTE, 0);
        a1.set(Calendar.SECOND, 0);
        
        Calendar a2 = (Calendar) c2.clone();
        a2.set(Calendar.HOUR_OF_DAY, 0);
        a2.set(Calendar.MINUTE, 0);
        a2.set(Calendar.SECOND, 0);
        
        //除以1000，舍掉后面的微秒
        long tmp = a1.getTimeInMillis()/1000 - a2.getTimeInMillis()/1000;
        final int oneDay = 24 * 60 * 60;
        
        if(tmp%oneDay == 0)
        {
            return (int)(tmp/oneDay);
        }
        else
        {
            return (int)(tmp/oneDay) + 1;
        }

    }

    private Calendar createCalendar(Calendar ca, int lunarFlag)
    {
        if(lunarFlag == 1)
        {
            return new ChineseCalendar(ca);
        }
        else
        {
            Calendar tmp = Calendar.getInstance();
            tmp.setTimeInMillis(ca.getTimeInMillis());
            return tmp;
        }
    }
    
    //*************************************************************************
    //有序Vector
    //*************************************************************************
    private class SortedIntArray
    {
        private Vector<Integer> mArray = new Vector<Integer>();
        private int mMaxNum = INFINITY_COUNT;

        public SortedIntArray(int maxNum)
        {
            setMaxNum(maxNum);
        }

        public SortedIntArray()
        {
        }

        public void setMaxNum(int maxNum)
        {
            if(maxNum > 0)
            {
                mMaxNum = maxNum;
            }
        }
        
        public boolean isFull()
        {
            return mArray.size() >= mMaxNum ? true : false;
        }
        
        @Override
        public String toString()
        {
            if(mArray.size() == 0)
            {
                return "empty";
            }
            
            StringBuffer sff = new StringBuffer();
            for(Integer i : mArray)
            {
                sff.append(i).append(" ");
            }
            
            return sff.toString();
        }

        public Integer get(int index)
        {
            return mArray.get(index);
        }
        
        public int size()
        {
            return mArray.size();
        }
        
        public boolean set(int index, Integer element)
        {
            if(index >= mArray.size() || index < 0)
            {
                return false;
            }
            //TODO 需排序并剔重
            
            mArray.set(index, element);
            return true;
        }
        
        public boolean add(SortedIntArray array)
        {
            for(int i = 0; i < array.size(); i++)
            {
                if(!this.add(array.get(i)))
                {
                    return false;
                }
            }
            return true;
        }
        
        //从小到大排序，重复的直接抛弃
        public boolean add(Integer element)
        {
            //为空
            if(mArray.size() == 0)
            {
                return mArray.add(element);
            }

            //插到第一个
            if(mArray.get(0) > element)
            {
                mArray.add(0, element);
                return true;
            }
            
            //插到中间
            int i = 0;
            for(; i < mArray.size() - 1; i++)
            {
                //如果已经存在，则不插入
                if(mArray.get(i).equals(element))
                {
                    return true;
                }
                else if(mArray.get(i) < element && mArray.get(i+1) > element)
                {
                    mArray.add(i+1, element);
                }
            }
            
            //插到最后一个
            boolean result = true;
            if(i ==  mArray.size() - 1 && mArray.get(i) < element)
            {
                result = mArray.add(element);
            }
            
            //超过最大值，则删掉最后一个
            if(mMaxNum > 0 && mArray.size() > mMaxNum)
            {
                mArray.remove(mMaxNum-1);
            }
                        
            return result;
        }
        
        public boolean remove(Integer element)
        {
            return mArray.remove(element);
        }
        
        public void remove(SortedIntArray array)
        {
            
            for(int i = 0; i < array.size(); i++)
            {
                this.remove(array.get(i));
            }
        }
        
        public void clear()
        {
            mArray.clear();
        }
    }    

}