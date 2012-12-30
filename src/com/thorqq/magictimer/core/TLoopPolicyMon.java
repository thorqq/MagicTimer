package com.thorqq.magictimer.core;

import com.thorqq.magictimer.util.Util;


public class TLoopPolicyMon extends TLoopPolicy
{
    private static final long serialVersionUID = -2340196098467841495L;

    public static final int INFINITY_COUNT = -1;

    public static final int MONTH_LOOP_SUB_LOOP_WEEK = 0;
    public static final int MONTH_LOOP_SUB_LOOP_DAY  = 1;

    public static final int PARAM_NUM = 6;
    public static final int LENGTH_OF_MONTH_MASK    = 12;
    public static final int LENGTH_OF_DAY_MASK      = 32;
    public static final int LENGTH_OF_WEEK_MASK     = 5;
    public static final int LENGTH_OF_WEEK_DAY_MASK = 7;

    public int    mLunarFlag   = 0;                                 //阴历标志
    public char[] mMonthMask   = new char[LENGTH_OF_MONTH_MASK];   //月掩码
    public int    mSubLoopType = MONTH_LOOP_SUB_LOOP_WEEK;          //子循环类型
    public char[] mDayMask     = new char[LENGTH_OF_DAY_MASK];     //日掩码，含每月的最后一天
    public char[] mWeekMask    = new char[LENGTH_OF_WEEK_MASK];    //周掩码，含每月的最后一周
    public char[] mWeekDayMask = new char[LENGTH_OF_WEEK_DAY_MASK];//周内的星期掩码，周日至周六

    public TLoopPolicyMon(int loopPolicyType, String loopParam)
    {
        super(loopPolicyType, loopParam);
    }

    public TLoopPolicyMon(int id, int displayOrder, int loopPolicyType, String loopParam, int excludeFlag)
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
        //阳历阴历
        String[] tmp_ZH_MON = null;
        String[] tmp_ZH_DAY_IN_MON = null;
        if(mLunarFlag == 1)
        {
            sff.append("农历");
            tmp_ZH_MON = ZH_LUNAR_MON;
            tmp_ZH_DAY_IN_MON = ZH_LUNAR_DAY_IN_MON;
        }
        else
        {
            tmp_ZH_MON = ZH_MON;
            tmp_ZH_DAY_IN_MON = ZH_DAY_IN_MON;
        }
        
        //月
        int i = 0;
        for(i = 0; i < mMonthMask.length; i++)
        {
            if(mMonthMask[i] != '1')
            {
                break;
            }
        }
        
        if(i != mMonthMask.length)
        {
            for(i = 0; i < mMonthMask.length; i++)
            {
                if(mMonthMask[i] == '1')
                {
                    sff.append(tmp_ZH_MON[i]).append(",");                
                }
            }
            sff.setCharAt(sff.length()-1, '月');
        }
        else
        {
            sff.append("每月");
        }
        sff.append("\n");
        
        //月内日
        if(mSubLoopType == MONTH_LOOP_SUB_LOOP_DAY)
        {
            for(i = 0; i < mDayMask.length; i++)
            {
                if(mDayMask[i] != '1')
                {
                    break;
                }
            }
            
            if(i != mDayMask.length)
            {
                for(i = 0; i < mDayMask.length; i++)
                {
                    if(mDayMask[i] == '1')
                    {
                        sff.append(tmp_ZH_DAY_IN_MON[i]).append(",");                
                    }
                }
                sff.deleteCharAt(sff.length()-1);
            }
            else
            {
                sff.append("每天");
            }
        }
        //月内周
        else if(mSubLoopType == MONTH_LOOP_SUB_LOOP_WEEK)
        {
            //第几周：第一、二、三、最后一周；最后一周
            for(i = 0; i < mWeekMask.length; i++)
            {
                if(mWeekMask[i] != '1')
                {
                    break;
                }
            }
            
            if(i != mWeekMask.length)
            {
                sff.append("第");
                for(i = 0; i < mWeekMask.length; i++)
                {
                    if(mWeekMask[i] == '1')
                    {
                        if(i == mWeekMask.length-1 && sff.charAt(sff.length()-1) == '第')
                        {
                            sff.deleteCharAt(sff.length()-1);
                        }
                        sff.append(ZH_WEEK_IN_MON[i]).append(",");                
                    }
                }
                sff.setCharAt(sff.length()-1, '个');
            }
            else
            {
                sff.append("每个");
            }
            sff.append("\n");
            
            //周几：中的每天；周一、二、三、六
            sff.append("周");
            for(i = 0; i < mWeekDayMask.length; i++)
            {
                if(mWeekDayMask[i] == '1')
                {
                    sff.append(ZH_WEEK[i]).append(",");                
                }
            }
            sff.deleteCharAt(sff.length()-1);
            
        }

        return sff.toString();
    }

    public void setParam(
            int    lunarFlag,
            char[] monthMask,
            int    subLoopType,
            char[] dayMask,
            char[] weekMask,
            char[] weekDayMask)
    {
        mLunarFlag   = lunarFlag;
        System.arraycopy(monthMask,   0, mMonthMask,  0, LENGTH_OF_MONTH_MASK);
        mSubLoopType = subLoopType;
        
        if(mSubLoopType == MONTH_LOOP_SUB_LOOP_DAY)
        {
            System.arraycopy(dayMask,     0, mDayMask,    0, LENGTH_OF_DAY_MASK);
            
            Util.initializeCharArray(mWeekMask, '0');
            Util.initializeCharArray(mWeekDayMask, '0');
        }
        else if(mSubLoopType == MONTH_LOOP_SUB_LOOP_WEEK)
        {
            System.arraycopy(weekMask,    0, mWeekMask,   0, LENGTH_OF_WEEK_MASK);
            System.arraycopy(weekDayMask, 0, mWeekDayMask,0, LENGTH_OF_WEEK_DAY_MASK);    

            Util.initializeCharArray(mDayMask, '0');
        }
    }
    
    @Override
    public boolean parseStringParam()
    {
        String[] array = Util.splitParames(super.mLoopParam);

        //校验参数
        if(array.length != PARAM_NUM)
        {
            Util.log("TimeParam setParam error. param = " + super.mLoopParam);
            return false;
        }    
        
        //不支持阴历+月内周循环
        if(mLunarFlag == 1 && mSubLoopType == MONTH_LOOP_SUB_LOOP_WEEK)
        {
            Util.log("Error param. mLunarFlag == 1 && mSubLoopType == MONTH_LOOP_SUB_LOOP_WEEK(0)");
            return false;
        }
        
        if(mSubLoopType != MONTH_LOOP_SUB_LOOP_DAY && mSubLoopType != MONTH_LOOP_SUB_LOOP_WEEK)
        {
            Util.log("Error mSubLoopType. mSubLoopType = " + mSubLoopType);
            return false;
        }
        
        mLunarFlag   = Util.strToInt(array[0]);
        mMonthMask = array[1].toCharArray();
        mSubLoopType = Util.strToInt(array[2]);
        mDayMask = array[3].toCharArray();
        mWeekMask = array[4].toCharArray();
        mWeekDayMask = array[5].toCharArray();
        
        if(mSubLoopType == MONTH_LOOP_SUB_LOOP_DAY)
        {
            Util.initializeCharArray(mWeekMask, '0');
            Util.initializeCharArray(mWeekDayMask, '0');
        }
        else if(mSubLoopType == MONTH_LOOP_SUB_LOOP_WEEK)
        {
            Util.initializeCharArray(mDayMask, '0');
        }

        return true;
    }

    @Override
    public String paramToString()
    {
        StringBuffer sff = new StringBuffer();
        sff.append(Util.intToStr(mLunarFlag))
           .append(Util.PARAMES_HYPHEN).append(Util.arrayToString(mMonthMask))
           .append(Util.PARAMES_HYPHEN).append(Util.intToStr(mSubLoopType))
           .append(Util.PARAMES_HYPHEN).append(Util.arrayToString(mDayMask))
           .append(Util.PARAMES_HYPHEN).append(Util.arrayToString(mWeekMask))
           .append(Util.PARAMES_HYPHEN).append(Util.arrayToString(mWeekDayMask))
           ;
        
        return super.mLoopParam = sff.toString();
    }
    
    public String CheckValidity ()
    {
        //TODO
        return null;
    }

    public int getLunarFlag()
    {
        return mLunarFlag;
    }
    
    public char[] getMonthMask()
    {
        return mMonthMask;
    }
    
    public int getSubLoopType()
    {
        return mSubLoopType;
    }
    
    public char[] getDayMask()
    {
        return mDayMask;
    }
    
    public char[] getWeekMask()
    {
        return mWeekMask;
    }

    public char[] getWeekDayMask()
    {
        return mWeekDayMask;
    }

}
