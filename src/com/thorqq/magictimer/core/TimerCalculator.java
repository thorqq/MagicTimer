package com.thorqq.magictimer.core;

import java.util.Calendar;
import java.util.Vector;

import com.thorqq.magictimer.util.ChineseCalendar;
import com.thorqq.magictimer.util.Util;



/** ʱ�䷽������
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
     * @param now - Calendar ��ǰʱ��
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
    
    //���ַ����н�������
    public void setTime(TTimerDef timerDef)
    {
        mTimeCal.setTTimerParam(timerDef);
    }
    
    //���ַ����н�������
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
        
        //���ò���
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

    //������Ч����
    public long caculate(long lastAlertTime)
    {
        //����ԭʼ��mBase
        long baseTimeInMillisBak = mBase.getTimeInMillis();
        int tmpMaxValidDay = 0;
        int cumulateAddDayNum = 0;
        int loopNum = 0;
        
        //������һ���ڵ���Чʱ�䣬�������ѭ��5��
        for(; cumulateAddDayNum < MAX_CALCULATE_ADD_DAY_NUM || loopNum < MAX_LOOP_NUM; 
              cumulateAddDayNum += tmpMaxValidDay, loopNum++)
        {
            Util.log("mBase : " + Util.dateToStr(mBase.getTime()));
            mValidDays.clear();
            
            mValidDays.setMaxNum(MAX_CALCULATE_VALID_DAYS * mLoopCal.size());
            //������Ч��
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
            
            //��Ч�յ����ֵ
            tmpMaxValidDay = mValidDays.get(mValidDays.size() - 1);
            Util.log("Orgin mValidDays: " + mValidDays.toString() );
            
            //�����ų���
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
            
            //���mValidDaysȫ����remove���ˣ���������Һ������Ч��
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
        
        //��ԭ
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
        
        //�����´�����ʱ��
        return mTimeCal.caculate(mValidDays, lastAlertTime);
    }
    
    //*************************************************************************
    //ʱ�����ò���
    //*************************************************************************
    private class TimeCalculator
    {        
        private TTimerDef mTimerDef;
        
        public void setTTimerParam(TTimerDef timerDef)
        {
            mTimerDef = timerDef;
        }
        
        //��������ʱ�����ķ�����
        private long intervalMinute(Calendar c1, Calendar c2)
        {
            return c1.getTimeInMillis()/1000/60 - c2.getTimeInMillis()/1000/60;
        }
        
        //������Ч���ڡ��ϴ�����ʱ�䣬����´�����ʱ��(������ʾ����������0��ʾ��ǰ������������Ҫ������)
        public long caculate(SortedIntArray validDays, long lastAlertTime)
        {
            //������һ������ʱ��
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

            // ��δ������ʱ�䣬��һ��ִ��
            if(interval > 0)
            {
                //do nothing
            }            
            //�Ѿ���������ʱ�䣬��һ��ִ�У�����Ϊ������һ����Ч������
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
            //lastAlertTime > 0����ʾ�Ѿ�ִ�й�
            else if(interval <= 0 && lastAlertTime > 0)
            {
                //ѭ�����壬������ʱ�俪ʼ����
                if(mTimerDef.getMaxCount() > 1 || mTimerDef.getMaxCount() == INFINITY_COUNT)
                {
                    long cnt = (mBase.getTimeInMillis() - caResult.getTimeInMillis())/mTimerDef.getInterval()/60/1000 + 1;
                    if(mTimerDef.getMaxCount() != INFINITY_COUNT 
                            && cnt >= mTimerDef.getMaxCount())
                    {
                        //�ѳ��������������
                        Util.log("Excceed mMaxCount("+ mTimerDef.getMaxCount() +"). cnt = " + cnt);
                        return 0;
                    }
                    else
                    {
                        nextAlertTime = caResult.getTimeInMillis() + cnt * mTimerDef.getInterval() * 60 * 1000;    
                        caResult.setTimeInMillis(nextAlertTime);
                                         
                    }
                }
                //һ�������壬������̰˯����lastAlertTime��ʼ���㣨̰˯ʱ�������õ�interval�У�
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
    //ѭ���������ò��������ӿ�
    //*************************************************************************
    interface LoopCalculator
    {
        public void setLoopPolicy(TLoopPolicy loopPolicy);
        //������һ��������ʱ��
        public boolean caculate(SortedIntArray validDays);
//        public String toString();
    }

    //*************************************************************************
    //����ѭ�����ò���
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

                //���㽫����Ч������
                for(int i = 0; i < maxLoopNum && !validDays.isFull(); i++)
                {
                    if(mDayLoopPolicy.getDayMask()[i%loopDays] == '1')
                    {
                        validDays.add(i + interval);
                    }
                }
            }
            else  //����Ч
            {
                interval *= -1;
                //int interval = TimeSchema.calendarIntervalDays(mBase, mStartDate) + 1;
                //��ʧЧ
                if(maxCount != INFINITY_COUNT && interval >= loopDays * maxCount)
                {
                    Util.log("Excceed mMaxCount. mMaxCount = " + maxCount + ", mLoopDays = " + loopDays);
                    return false;
                }
                
                //���㽫����Ч��10��
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
    //����ѭ�����ò���
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
            
            //�ж��Ƿ�������
            int CALENDAR_MONTH = Calendar.MONTH;
            int CALENDAR_DATE  = Calendar.DATE;
            if(lunarFlag == 1)
            {
                CALENDAR_MONTH = ChineseCalendar.CHINESE_MONTH;
                CALENDAR_DATE  = ChineseCalendar.CHINESE_DATE;
            }
            
            Calendar mChineseBase = createCalendar(mBase, lunarFlag);
            int maxLoopNum = MONTH_NUM_OF_YEAR + mChineseBase.get(CALENDAR_MONTH) + 1;
            
            //������Ч���·�
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

            //ת��Ϊ"���µĵ�һ��"�뵱ǰ�·�1�ŵ�����֮��
            Calendar caFirstDayOfBase = createCalendar(mBase, lunarFlag);
            caFirstDayOfBase.set(CALENDAR_DATE, 1);
            
            //validMonthsTmp�Ѿ����������ظ����������������Vector<Integer>
            //validDayNumOfMonthsֻ����Vector����Ϊ�������ظ�Ԫ��
            Vector<Integer> validFirstDayOfMonths = new Vector<Integer>();//��Ч�·ݵĵ�һ��
            Vector<Integer> validDayNumOfMonths = new Vector<Integer>();//��Ч���Ǹ��µ�����
            
            Calendar caTmp = createCalendar(mBase, lunarFlag);
            caTmp.add(CALENDAR_MONTH, validMonthsTmp.get(0) - 0);
            caTmp.set(CALENDAR_DATE, 1);
            int inter = TimerCalculator.calendarIntervalDays(caTmp, caFirstDayOfBase);
            
            validFirstDayOfMonths.add(inter);
            validDayNumOfMonths.add(caTmp.getActualMaximum(CALENDAR_DATE));//����getActualMaximumʱ��DAY_OF_MONTH����ҪС��29
            
            for(int i = 1; i < validMonthsTmp.size(); i++)
            {
                caTmp.add(CALENDAR_MONTH, validMonthsTmp.get(i) - validMonthsTmp.get(i-1));
                caTmp.set(CALENDAR_DATE, 1);
                validFirstDayOfMonths.add(TimerCalculator.calendarIntervalDays(caTmp, caFirstDayOfBase));
                validDayNumOfMonths.add(caTmp.getActualMaximum(CALENDAR_DATE));
            }
            
            //���ڰ���ѭ��
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
                    
                    //���ڵ����һ��
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
            //���ڰ���ѭ��
            else if(subLoopType == TLoopPolicyMon.MONTH_LOOP_SUB_LOOP_WEEK)
            {
                Calendar tmp = (Calendar) caFirstDayOfBase.clone();
                for(int i = 0; i < validFirstDayOfMonths.size() - 1 && !validDays.isFull(); i++)
                {
                    //�ڼ���
                    for(int m = 0; m < 5 && !validDays.isFull(); m++)
                    {
                        //�ܼ�
                        for(int n = 0; n < 7 && !validDays.isFull(); n++)
                        {
                            if(mMonLoopPolicy.getWeekMask()[m] == '1' 
                                && mMonLoopPolicy.getWeekDayMask()[n] == '1')
                            {
                                //���һ��
                                int m2 = m+1;
                                if(m == 4)
                                {
                                    m2 = -1;
                                }
                                
                                long t = tmp.getTimeInMillis();
                                tmp.add(Calendar.DAY_OF_YEAR, validFirstDayOfMonths.get(i));
                                tmp.set(Calendar.DAY_OF_WEEK, n+1);//DAY_OF_WEEK���������������ֱ���1-7
                                tmp.set(Calendar.DAY_OF_WEEK_IN_MONTH, m2);
                                int a = TimerCalculator.calendarIntervalDays(tmp, mBase);
                                if(a >= 0)
                                {
                                    validDays.add(a);
                                }
                                //��ԭ
                                tmp.setTimeInMillis(t);
                            }
                        }
                    }                    
                }                
            }
            
            return true;
        }
    }
    
    //�����������������
    private static int calendarIntervalDays(Calendar c1, Calendar c2)
    {
        //�����޸������գ���Ϊc1,c2����������
        Calendar a1 = (Calendar) c1.clone();
        a1.set(Calendar.HOUR_OF_DAY, 0);
        a1.set(Calendar.MINUTE, 0);
        a1.set(Calendar.SECOND, 0);
        
        Calendar a2 = (Calendar) c2.clone();
        a2.set(Calendar.HOUR_OF_DAY, 0);
        a2.set(Calendar.MINUTE, 0);
        a2.set(Calendar.SECOND, 0);
        
        //����1000����������΢��
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
    //����Vector
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
            //TODO ����������
            
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
        
        //��С���������ظ���ֱ������
        public boolean add(Integer element)
        {
            //Ϊ��
            if(mArray.size() == 0)
            {
                return mArray.add(element);
            }

            //�嵽��һ��
            if(mArray.get(0) > element)
            {
                mArray.add(0, element);
                return true;
            }
            
            //�嵽�м�
            int i = 0;
            for(; i < mArray.size() - 1; i++)
            {
                //����Ѿ����ڣ��򲻲���
                if(mArray.get(i).equals(element))
                {
                    return true;
                }
                else if(mArray.get(i) < element && mArray.get(i+1) > element)
                {
                    mArray.add(i+1, element);
                }
            }
            
            //�嵽���һ��
            boolean result = true;
            if(i ==  mArray.size() - 1 && mArray.get(i) < element)
            {
                result = mArray.add(element);
            }
            
            //�������ֵ����ɾ�����һ��
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