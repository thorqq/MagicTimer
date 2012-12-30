package com.thorqq.magictimer.core;


import java.io.Serializable;

import com.thorqq.magictimer.util.Util;


/**
 * @author THORQQ<br>
 * ���ݿ�� loop_policy
 */
public abstract class TLoopPolicy implements Serializable
{    
    private static final long serialVersionUID = 4461385796023608949L;
    
    public static final int LOOP_PARAM_INCLUDE = 0;
    public static final int LOOP_PARAM_EXCLUDE = 1;

    public static final int LOOP_POLICY_DAY = 0;
    public static final int LOOP_POLICY_MONTH = 1;
    public static final int LOOP_POLICY_WEEK = 2;

    public static final String[] ZH_WEEK = new String[]{"��","һ","��","��","��","��","��"};
    public static final String[] ZH_LUNAR_MON = new String[]{"��","��","��","��","��","��","��","��","��","ʮ","ʮһ","��"};
    public static final String[] ZH_MON = new String[]{"һ","��","��","��","��","��","��","��","��","ʮ","ʮһ","ʮ��"};
    public static final String[] ZH_WEEK_IN_MON = new String[]{"һ","��","��","��","���һ"};
    public static final String[] ZH_DAY_IN_MON = new String[]{
        "һ","��","��","��","��","��","��","��","��","ʮ",
        "ʮһ","ʮ��","ʮ��","ʮ��","ʮ��","ʮ��","ʮ��","ʮ��","ʮ��","��ʮ",
        "��ʮһ","��ʮ��","��ʮ��","��ʮ��","��ʮ��","��ʮ��","��ʮ��","��ʮ��","��ʮ��","��ʮ","��ʮһ","���һ��"};
    public static final String[] ZH_LUNAR_DAY_IN_MON = new String[]{
        "��һ","����","����","����","����","����","����","����","����","��ʮ",
        "ʮһ","ʮ��","ʮ��","ʮ��","ʮ��","ʮ��","ʮ��","ʮ��","ʮ��","إ",
        "إһ","إ��","إ��","إ��","إ��","إ��","إ��","إ��","إ��","��ʮ","��ʮһ","���һ��"};

    protected int mID             = -1;
    protected int mDisplayOrder   = -1;
    protected int mPolicyType     = -1;
    protected String mLoopParam    = null;
    protected int mExcludeFlag    = -1;
    
    public TLoopPolicy(int loopPolicyType, String loopParam)
    {
        mID              = -1;
        mDisplayOrder    = -1;
        mPolicyType      = loopPolicyType;
        mLoopParam       = loopParam;
        mExcludeFlag     = LOOP_PARAM_INCLUDE;
    }

    public TLoopPolicy(int id, int displayOrder, int loopPolicyType, String loopParam, int excludeFlag)
    {
        mID              = id;
        mDisplayOrder    = displayOrder;
        mPolicyType  = loopPolicyType;
        mLoopParam       = loopParam;
        mExcludeFlag     = excludeFlag;
    }

    @Override
    public String toString()
    {
        StringBuffer sff = new StringBuffer();
        sff.append("mID = ").append(Util.intToStr(mID)).append("\n")
           .append("mDisplayOrder = ").append(mDisplayOrder).append("\n")
           .append("mPolicyType = ").append(mPolicyType).append("\n")
           .append("mLoopParam = ").append(mLoopParam = paramToString()).append("\n")
           .append("mExcludeFlag = ").append(Util.intToStr(mExcludeFlag)).append("\n")
           ;        
        return sff.toString();    
    }

    public abstract boolean parseStringParam();
    public abstract String paramToString();
    public abstract String getDescription();
    
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

    public int getPolicyType()
    {
        return mPolicyType;
    }
    
    public void setPolicyType(int policyType)
    {
        mPolicyType = policyType;
    }    

    public String getLoopParam()
    {
        this.paramToString();
        return mLoopParam;
    }

    public void setLoopParam(String loopParam)
    {
        mLoopParam = loopParam;
    }
    
    public int getExcludeFlag()
    {
        return mExcludeFlag;
    }
    
    public void setExcludeFlag(int excludeFlag)
    {
        mExcludeFlag = excludeFlag;
    }

}

