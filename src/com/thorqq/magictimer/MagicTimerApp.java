package com.thorqq.magictimer;

import com.thorqq.magictimer.db.DBHelper;
import com.thorqq.magictimer.util.Util;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class MagicTimerApp extends Application
{

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        
        //��ʼ����־
        Util.initLog(null);
        logVersion();
        
        //��ʼ�����ݿ�
        DBHelper.initialize(this);
        DBHelper.getIntance().open();
        
        Util.log("MagicTimerApp.onCreate success");

    }

    private void logVersion()
    {
        PackageManager packageManager = getPackageManager();
        try
        {
            // getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
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
