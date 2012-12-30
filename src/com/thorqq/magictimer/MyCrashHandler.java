package com.thorqq.magictimer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;

import com.thorqq.magictimer.util.Util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * �Զ���� �쳣������ , ʵ���� UncaughtExceptionHandler�ӿ�
 * 
 * @author Administrator
 * 
 */
public class MyCrashHandler implements UncaughtExceptionHandler
{
    // ������ ����Ӧ�ó��� ֻ��һ�� MyCrash-Handler
    private static MyCrashHandler myCrashHandler;
    private Context context;
    // private DoubanService service;
    //private SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    // 1.˽�л����췽��
    private MyCrashHandler()
    {

    }

    public static synchronized MyCrashHandler getInstance()
    {
        if (myCrashHandler != null)
        {
            return myCrashHandler;
        } else
        {
            myCrashHandler = new MyCrashHandler();
            return myCrashHandler;
        }
    }

    public void init(Context context)
    {
        this.context = context;
        // this.service = service;
    }

    public void uncaughtException(Thread arg0, Throwable arg1)
    {
        System.out.println("����ҵ��� ");
        // 1.��ȡ��ǰ����İ汾��. �汾��id
        String versioninfo = getVersionInfo();

        // 2.��ȡ�ֻ���Ӳ����Ϣ.
        String mobileInfo = getMobileInfo();

        // 3.�Ѵ���Ķ�ջ��Ϣ ��ȡ����
        String errorinfo = getErrorInfo(arg1);

        // 4.�����е���Ϣ ������Ϣ��Ӧ��ʱ�� �ύ��������
        try
        {
            // service.createNote(new PlainTextConstruct(dataFormat.format(new
            // Date())),
            // new PlainTextConstruct(versioninfo+mobileInfo+errorinfo),
            // "public", "yes");
            Util.log("VersionInfo: \n" + versioninfo);
            Util.log("MobileInfo: \n" + mobileInfo);
            Util.log("ErrorInfo: \n" + errorinfo);
        } catch (Exception e)
        {
            Util.log_ex(e);
        }

        // �ɵ���ǰ�ĳ���
        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * ��ȡ�������Ϣ
     * 
     * @param arg1
     * @return
     */
    private String getErrorInfo(Throwable arg1)
    {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        arg1.printStackTrace(pw);
        pw.close();
        String error = writer.toString();
        return error;
    }

    /**
     * ��ȡ�ֻ���Ӳ����Ϣ
     * 
     * @return
     */
    private String getMobileInfo()
    {
        StringBuffer sb = new StringBuffer();
        // ͨ�������ȡϵͳ��Ӳ����Ϣ
        try
        {

            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields)
            {
                // �������� ,��ȡ˽�е���Ϣ
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                sb.append(name + "=" + value);
                sb.append("\n");
            }
        } catch (Exception e)
        {
            Util.log_ex(e);
        }
        return sb.toString();
    }

    /**
     * ��ȡ�ֻ��İ汾��Ϣ
     * 
     * @return
     */
    private String getVersionInfo()
    {
        try
        {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e)
        {
            Util.log_ex(e);
            return "�汾��δ֪";
        }
    }
}
