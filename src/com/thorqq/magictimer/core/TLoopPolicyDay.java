package com.thorqq.magictimer.core;

import java.util.Calendar;

import com.thorqq.magictimer.util.Util;


public class TLoopPolicyDay extends TLoopPolicy
{
    private static final long serialVersionUID = -6319428876086320225L;
    protected static final int INFINITY_COUNT = -1;
    protected final int PARAM_NUM = 5;

    protected int     mLoopDays = 1;  //ѭ������
    protected int     mMaxCount = INFINITY_COUNT; //ѭ������
    protected char[]  mDayMask;       //������
    protected Calendar mStartDate;     //��ʼ������
    protected int     mLunarFlag = 0;  //mStartDate��������־

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
            sff.append("ÿ��");
        }
        else if(mLoopDays > 1)
        {
            if(mLoopDays > mDayMask.length)
            {
                return "��Ч��ѭ�����ڣ�" + mLoopDays + "��";
            }
            int flag = 0;
            sff.append(mLoopDays).append("��һ��ѭ������");
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
                        sff.append("��").append(i+1);
                    }
                }
            }
            sff.append("������");
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
//        protected int     mLoopDays = 1;  //ѭ������
        if(mLoopDays < 1)
        {
            return "ѭ���������������";
        }

//        protected int     mMaxCount = INFINITY_COUNT; //ѭ������
        if(mMaxCount < TimerCalculator.INFINITY_COUNT || mMaxCount == 0)
        {
            return "��ѭ�����������Ϸ����������";
        }

//        protected char[]  mDayMask;       //������
        if(mLoopDays != mDayMask.length)
        {
            return "ѭ�����������������ò�һ��";
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
            return "û������������";
        }
//        protected Calendar mStartDate;     //��ʼ������
//        protected int     mLunarFlag = 0;  //mStartDate��������־
        if(mLunarFlag != 0 && mLunarFlag != 1)
        {
            return "ũ����ʶ���Ϸ���" + mLunarFlag + "��";
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
