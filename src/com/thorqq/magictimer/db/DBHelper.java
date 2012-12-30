package com.thorqq.magictimer.db;


import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thorqq.magictimer.action.*;
import com.thorqq.magictimer.core.*;
import com.thorqq.magictimer.util.Util;


public class DBHelper {

    private static final int ANY = -1;
    
    private SQLiteOpenHelper mOpenHelper;
    private static Context mContext;
                	
    private DBHelper(){}
    
    private static DBHelper mDBHelper = new DBHelper();

    public static DBHelper getIntance()
    {        
        return getIntance(mContext);
    }

    public static DBHelper getIntance(Context context)
    {        
        mContext = context;
        if(mDBHelper == null)
        {
            mDBHelper = new DBHelper();
        }
        return mDBHelper;
    }
    
    public static void initialize(Context context)
    {        
        mContext = context;
        mDBHelper = new DBHelper();
    }    

    private static class DatabaseHelper extends SQLiteOpenHelper {
        
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "magictimer.db";  
        
//        private static final String TAB_TIME_DEF      = "t_timer_def";
//        private static final String TAB_LOOP_POLICY   = "t_loop_policy";
//        private static final String TAB_TIMING_ACTION = "t_action";
//        private static final String TAB_CONFIG        = "t_config";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS t_timer_def (" +
                         "timer_id        integer, " +
                         "display_order   integer, " +
                         "start_hour      integer, " + 
                         "start_minute    integer, " + 
                         "max_count       integer, " + 
                         "interval        integer, " + 
                         "last_alert_time integer, " + 
                         "enable          integer, " +
                         "timer_name      varchar, " +
                         "remark          varchar);");    

            db.execSQL("CREATE TABLE IF NOT EXISTS t_loop_policy (" +
                         "timer_id      integer, " +
                         "display_order integer, " +
                         "policy_type   integer, " +
                         "loop_params   varchar, " +
                         "exclude_flag  integer);");    

            db.execSQL("CREATE TABLE IF NOT EXISTS t_action (" +
                         "timer_id      integer, " +
                         "exec_order    integer, " +
                         "action_type   integer, " +
                         "action_params varchar);");    

            db.execSQL("CREATE TABLE IF NOT EXISTS t_config (" +
                         "name      varchar, " +
                         "value    varchar);");         
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        }
    }
    
    public void open()
	{  
        mOpenHelper = new DatabaseHelper(mContext);
    }  
	
    public void close()
    {  
        mOpenHelper.close();
    }
        
    private void closeCur(Cursor cur)
    {
        if(cur != null)
        {
            cur.close();
        }
    }

    /**产生一个新的定时器ID
     * @return 新的定时器ID
     */
    public int genTimerID()
    {
        //递增序列
        Cursor cur = null;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        try
        {
            String sql = "select max(timer_id) from t_timer_def";
            cur = db.rawQuery(sql, null);  
            if(cur.moveToNext())
            {
                return cur.getInt(0) + 1;
            }
        }
        catch(Exception e)
        {
            Util.log_ex(e);
        }
        finally
        {
            closeCur(cur);
        }

        return 0;
    }

    /**删除所有定时器
     * @return true： 成功<br> false： 失败
     */
    public boolean deleteAllTimer()
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteTimerDef(db, ANY);
            deleteLoopPolicy(db, ANY, ANY);
            deleteAction(db, ANY, ANY);
            db.setTransactionSuccessful();
            result = true;
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }
    
    /**删除某个定时器
     * @param timeid 定时器ID
     * @return true： 成功<br> false： 失败
     */
    public boolean deleteTimer(int timeid)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteTimerDef(db, timeid);
            deleteLoopPolicy(db, timeid, ANY);
            deleteAction(db, timeid, ANY);
            db.setTransactionSuccessful();
            result = true;
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }
    
    public boolean updateTimer(Timer timer)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteTimerDef(db, timer.getID());
            deleteLoopPolicy(db, timer.getID(), ANY);
            deleteAction(db, timer.getID(), ANY);
            if(insertTimerDef(db, timer.getTimerDef())
                    &&insertLoopPolicy(db, timer.getLoopPolicys())
                    &&insertAction(db, timer.getTActions()))
            {
                db.setTransactionSuccessful();
                result = true;
            }
            
            
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }
    
    //**************************************************************************
    // t_timer_def 操作函数
    //**************************************************************************
    public Vector<TTimerDef> queryAllTimerDef()
    {
        Vector<TTimerDef> timerDefArray = new Vector<TTimerDef>();
        Cursor cur = null;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        try
        {
            String sql = "select timer_id,display_order,start_hour,start_minute,max_count," +
            		     "       interval,last_alert_time,enable,timer_name,remark " +
            		     "  from t_timer_def order by display_order asc";
            cur = db.rawQuery(sql, null);  
            while(cur.moveToNext())
            {
                int i = 0;
                timerDefArray.add(new TTimerDef(cur.getInt(i++), 
                                            cur.getInt(i++), 
                                            cur.getInt(i++),
                                            cur.getInt(i++),
                                            cur.getInt(i++),
                                            cur.getInt(i++),
                                            cur.getLong(i++),
                                            cur.getInt(i++),
                                            cur.getString(i++), 
                                            cur.getString(i++)
                                            ));
            }
        }
        catch(Exception e)
        {
            Util.log_ex(e);
        }
        finally
        {
            closeCur(cur);
            db.close();
        }        
        
        return timerDefArray;
    }

    public TTimerDef queryTimerDef(int timeid)
    {
        Cursor cur = null;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        TTimerDef timeDef = null;
        String args[] = {Util.intToStr(timeid)};
        try
        {
            String sql = "select * from t_timer_def where timer_id = ? and enable = 1 order by display_order asc";
            cur = db.rawQuery(sql, args);  
            while(cur.moveToNext())
            {
                int i = 0;
                timeDef =  new TTimerDef(cur.getInt(i++), 
                        cur.getInt(i++), 
                        cur.getInt(i++),
                        cur.getInt(i++),
                        cur.getInt(i++),
                        cur.getInt(i++),
                        cur.getLong(i++),
                        cur.getInt(i++),
                        cur.getString(i++), 
                        cur.getString(i++)
                        );
            }
        }
        catch(Exception e)
        {
            Util.log_ex(e);
        }
        finally
        {
            closeCur(cur);
            db.close();
        }        
        
        return timeDef;
    }
    
    public boolean insertTimerDef(TTimerDef timerDef)
    {        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            if(insertTimerDef(db, timerDef))
            {
                db.setTransactionSuccessful();
                result = true;
            }
        } 
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public boolean deleteTimerDef(int timeid)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteTimerDef(db, timeid);
            db.setTransactionSuccessful();
            result = true;
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public boolean updateTimerDef(TTimerDef timerDef)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteTimerDef(db, timerDef.getID());
            if(insertTimerDef(db, timerDef))
            {
                db.setTransactionSuccessful();
                result = true;
            }
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    private boolean insertTimerDef(SQLiteDatabase db, TTimerDef timerDef)
    {
        long result = -1;
        ContentValues cv = new ContentValues();
        cv.put("timer_id",       timerDef.getID());
        cv.put("display_order",  timerDef.getDisplayOrder());
        cv.put("start_hour",     timerDef.getStartHour());
        cv.put("start_minute",   timerDef.getStartMinute());
        cv.put("max_count",      timerDef.getMaxCount());
        cv.put("interval",       timerDef.getInterval());
        cv.put("last_alert_time",timerDef.getLastAlertTime());
        cv.put("enable",         timerDef.isEnable());
        cv.put("timer_name",     timerDef.getName());
        cv.put("remark",         timerDef.getRemark());
        result = db.insert("t_timer_def", null, cv);         
        
        return (result == -1 ? false : true);
    }
    
    private void deleteTimerDef(SQLiteDatabase db, int timeid)
    {
        if(timeid == ANY)
        {
            db.delete ("t_timer_def", null, null);
        }
        else
        {
            String args[] = {Util.intToStr(timeid)};
            db.delete ("t_timer_def", "timer_id = ?", args);
        }          
    }
    
    //**************************************************************************
    // t_loop_policy 操作函数
    //**************************************************************************
    public Vector<TLoopPolicy> queryLoopPolicy(int timeid)
    {
        Vector<TLoopPolicy> loopPolicyArray = new Vector<TLoopPolicy>();
        Cursor cur = null;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String args[] = {Util.intToStr(timeid)};
        try
        {
            String sql = "select * from t_loop_policy where timer_id = ? order by display_order asc";
            cur = db.rawQuery(sql, args);  
            while(cur.moveToNext())
            {
                int i = 0;
                int policyType = cur.getInt(2);
                TLoopPolicy policy = null;
                if(policyType == TLoopPolicy.LOOP_POLICY_DAY)
                {
                    policy = new TLoopPolicyDay(cur.getInt(i++), 
                            cur.getInt(i++), 
                            cur.getInt(i++), 
                            cur.getString(i++),
                            cur.getInt(i++));
                }
                else if(policyType == TLoopPolicy.LOOP_POLICY_WEEK)
                {
                    policy =new TLoopPolicyWeek(cur.getInt(i++), 
                            cur.getInt(i++), 
                            cur.getInt(i++), 
                            cur.getString(i++),
                            cur.getInt(i++));
                }
                else if(policyType == TLoopPolicy.LOOP_POLICY_MONTH)
                {
                    policy =new TLoopPolicyMon(cur.getInt(i++), 
                            cur.getInt(i++), 
                            cur.getInt(i++), 
                            cur.getString(i++),
                            cur.getInt(i++));
                }
                
                policy.parseStringParam();
                loopPolicyArray.add(policy);
                
            }
        }
        catch(Exception e)
        {
            Util.log_ex(e);
            //TODO: 删掉试试
            //return null;
        }
        finally
        {
            closeCur(cur);
            db.close();
        }        
        
        return loopPolicyArray;
    }
    
    public boolean insertLoopPolicy(TLoopPolicy loopPolicy)
    {
        return insertLoopPolicy(loopPolicy.getID(), loopPolicy.getDisplayOrder(), 
                loopPolicy, loopPolicy.getExcludeFlag());
    }

    public boolean insertLoopPolicy(int timeid, int displayOrder, TLoopPolicy loopPolicy, int excludeFlag)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();        
        boolean result = false;
        try
        {
            db.beginTransaction();
            if(insertLoopPolicy(db,timeid, displayOrder,loopPolicy, excludeFlag))
            {
                db.setTransactionSuccessful();
                result = true;
            }
        }
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
                
        return result;
    }

    public boolean updateLoopPolicy(TLoopPolicy loopPolicy)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteLoopPolicy(db, loopPolicy.getID(), loopPolicy.getDisplayOrder());
            if(insertLoopPolicy(db, loopPolicy))
            {
                db.setTransactionSuccessful();
                result = true;
            }
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }
 
    public boolean deleteLoopPolicy(int timeid, int displayOrder)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteLoopPolicy(db, timeid, displayOrder);
            db.setTransactionSuccessful();
            result = true;
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public boolean deleteLoopPolicy(int timeid)
    {
        return deleteLoopPolicy(timeid, ANY);
    }

    public boolean deleteAllLoopPolicy()
    {
        return deleteLoopPolicy(ANY, ANY);
    }

    //私有方法
    private void deleteLoopPolicy(SQLiteDatabase db, int timeid, int displayOrder)
    {
        if(timeid == ANY)
        {
            db.delete ("t_loop_policy", null, null);
        }
        else if(timeid != ANY && displayOrder == ANY)
        {
            String args[] = {Util.intToStr(timeid)};
            db.delete ("t_loop_policy", "timer_id = ?", args);
        }
        else if(timeid != ANY && displayOrder != ANY)
        {
            String args[] = {Util.intToStr(timeid), Util.intToStr(displayOrder)};
            db.delete ("t_loop_policy", "timer_id = ?, display_order = ?", args);
        }
    }

    private boolean insertLoopPolicy(SQLiteDatabase db, final Vector<TLoopPolicy> loopPolicys)
    {
        for(TLoopPolicy policy : loopPolicys)
        {
            if(!insertLoopPolicy(db, policy))
            {
                return false;
            }
        }
        return true;
    }

    private boolean insertLoopPolicy(SQLiteDatabase db, TLoopPolicy loopPolicy)
    {
        return insertLoopPolicy(db, loopPolicy.getID(), loopPolicy.getDisplayOrder(), 
                loopPolicy, loopPolicy.getExcludeFlag());
    }

    private boolean insertLoopPolicy(SQLiteDatabase db, int timeid, int displayOrder, 
            TLoopPolicy loopPolicy, int excludeFlag)
    {
        loopPolicy.paramToString();
        
        long result = -1;
        ContentValues cv = new ContentValues();
        cv.put("timer_id",      timeid);
        cv.put("display_order", displayOrder);
        cv.put("policy_type",   loopPolicy.getPolicyType());
        cv.put("loop_params",   loopPolicy.getLoopParam());
        cv.put("exclude_flag",  excludeFlag);
        result = db.insert("t_loop_policy", null, cv); 
                
        return (result == -1 ? false : true);
    }

    //**************************************************************************
    // t_action 操作函数
    //**************************************************************************
    public Vector<TAction> queryAction(int timeid)
    {
        Vector<TAction> actionArray = new Vector<TAction>();
        Cursor cur = null;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String args[] = {Util.intToStr(timeid)};
        try
        {
            String sql = "select * from t_action where timer_id = ? order by exec_order asc";
            cur = db.rawQuery(sql, args);  
            while(cur.moveToNext())
            {
                int i = 0;
                actionArray.add(ActionMgr.getIntance().createAction(cur.getInt(i++), 
                        cur.getInt(i++), 
                        cur.getInt(i++), 
                        cur.getString(i++)));
            }
        }
        catch(Exception e)
        {
            Util.log_ex(e);
            return null;
        }
        finally
        {
            closeCur(cur);
            db.close();
        }      
        
        return actionArray;
    }
    
    public boolean insertAction(TAction action)
    {
        return insertAction(action.getID(), action.getExecOrder(), action);
    }

    public boolean insertAction(int timeid, int execOrder, TAction action)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();       
        boolean result = false;
        try
        {
            db.beginTransaction();
            if(insertAction(db, timeid, execOrder, action))
            {
                db.setTransactionSuccessful();
                result = true;
            }
        }
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
       
        return result;
    }
    
    public boolean updateAction(TAction action)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteAction(db, action.getID(), action.getExecOrder());
            if(insertAction(db, action))
            {
                db.setTransactionSuccessful();
                result = true;
            }
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }
        
    public boolean deleteAction(int timeid, int execOrder)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        try
        {
            db.beginTransaction();
            deleteAction(db, timeid, execOrder);
            db.setTransactionSuccessful();
            result = true;
        } 
        catch(Exception e)
        {
            Util.log_ex(e);
            result = false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public boolean deleteAction(int timeid)
    {
        return deleteAction(timeid, ANY);
    }

    public boolean deleteAllAction()
    {
        return deleteAction(ANY, ANY);
    }

    //私有方法
    private void deleteAction(SQLiteDatabase db, int timeid, int execOrder)
    {
        if(timeid == ANY)
        {
            db.delete ("t_action", null, null);
        }
        else if(timeid != ANY && execOrder == ANY)
        {
            String args[] = {Util.intToStr(timeid)};
            db.delete ("t_action", "timer_id = ?", args);
        }
        else if(timeid != ANY && execOrder != ANY)
        {
            String args[] = {Util.intToStr(timeid), Util.intToStr(execOrder)};
            db.delete ("t_action", "timer_id = ?, exec_order = ?", args);
        }
    }
    
    private boolean insertAction(SQLiteDatabase db, final Vector<TAction> actions)
    {
        for(TAction action : actions)
        {
            if(!insertAction(db, action))
            {
                return false;
            }
        }
        return true;
    }

    private boolean insertAction(SQLiteDatabase db, TAction action)
    {
        return insertAction(db, action.getID(), action.getExecOrder(), action);
    }

    private boolean insertAction(SQLiteDatabase db, int timeid, int execOrder, TAction action)
    {
        long result = -1;
        ContentValues cv = new ContentValues();
        cv.put("timer_id",      timeid);
        cv.put("exec_order",    execOrder);
        cv.put("action_type",   action.getActionType());
        cv.put("action_params", action.getParam());
        result = db.insert("t_action", null, cv); 
                
        return (result == -1 ? false : true);
    }
}





