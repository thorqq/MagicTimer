package com.thorqq.magictimer;

import com.thorqq.magictimer.db.DBHelper;
import com.thorqq.magictimer.util.Util;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;

public class MagicTimerApp extends Application
{
    
    Typeface mNumTypeFase;

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        
        //初始化日志
        Util.initLog(null);
        logVersion();
        
        //初始化数据库
        DBHelper.initialize(this);
        DBHelper.getIntance().open();
        
        //字体
        mNumTypeFase = Typeface.createFromAsset (getAssets() , "fonts/lcdD.ttf");
        
        Util.log("MagicTimerApp.onCreate success");

    }
    
    public Typeface getNumTypeFace()
    {
        return mNumTypeFase;
    }

    private void logVersion()
    {
        PackageManager packageManager = getPackageManager();
        try
        {
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
            String versionName = packInfo.versionName;
            int versionCode = packInfo.versionCode;
            
            Util.log(getPackageName() + " " + versionName + "(" + versionCode + ")");
        }
        catch (NameNotFoundException e)
        {
            Util.log_ex(e);
        }
    }
}
