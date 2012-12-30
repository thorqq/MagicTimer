package com.thorqq.magictimer.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Environment;
import android.util.Log;

public class Util
{
    final static int m_debug = 1;
    static String m_logFileName;
    static File m_baseDir;

    public final static String LOG_NAME = "MagicTimer.log";
    public final static String LOG_PATH = "MagicTimer";

    public static int getLineNumber()
    {
        return Thread.currentThread().getStackTrace()[4].getLineNumber();
    }

    public static String getFileName()
    {
        return Thread.currentThread().getStackTrace()[4].getFileName();
    }

    public static void printCallStatck()
    {
        StackTraceElement[] stackElements = Thread.currentThread()
                .getStackTrace();
        if (stackElements != null)
        {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < stackElements.length; i++)
            {
                buf.append(stackElements[i].getClassName()).append("\t")
                        .append(stackElements[i].getFileName()).append(":")
                        .append(stackElements[i].getLineNumber()).append("\n");
            }
            Util.log(buf.toString());
        }
    }

    public static String getDate()
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .format(new Date());
    }

    public static File getBaseDir()
    {
        if (m_baseDir != null)
        {
            return m_baseDir;
        }

        String path = Environment.getExternalStorageDirectory().getPath() + "/"
                + LOG_PATH;
        m_baseDir = new File(path);
        if (!m_baseDir.exists() && !m_baseDir.mkdir())
        {
            return null;
        }

        return m_baseDir;
    }

    public static void saveData(String filename, String str)
    {
        try
        {
            FileOutputStream fout = new FileOutputStream(getBaseDir().getPath()
                    + "/" + filename);
            byte[] bytes = str.getBytes();
            fout.write(bytes);
            fout.close();
        }
        catch (Exception e)
        {
            Util.log(e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public static void initLog(String filename)
    {
        if (m_debug == 0)
        {
            return;
        }

        try
        {
            m_logFileName = filename == null ? LOG_NAME : filename;
            File file = new File(getBaseDir().getPath() + "/" + m_logFileName);
            if (file.exists())
            {
                file.delete();
            }
            log("Log initital success.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    public static boolean is_log()
    {
        return m_debug == 0 ? false : true;
    }

    public static void log_ex(Exception e)
    {
        e.printStackTrace();
        if (m_debug == 0 || m_logFileName == null)
        {
            return;
        }

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Util.log(sw.getBuffer().toString());
    }

    @SuppressWarnings("unused")
    public static void log(String str)
    {

        if (m_debug == 0)
        {
            return;
        }

        try
        {
            byte[] bytes = ("[" + getDate() + "][" + getFileName() + ":"
                    + getLineNumber() + "]>> " + str + "\n").getBytes();

            if (m_logFileName == null)
            {
                System.out.print(new String(bytes));
            }
            else
            {
                boolean append = true;
                FileOutputStream fout = new FileOutputStream(getBaseDir()
                        .getPath() + "/" + m_logFileName, append);
                fout.write(bytes);
                fout.close();
            }
            Log.v(getFileName()+ ":" + getLineNumber(), str);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    // 将字符串 yyyy-MM-dd HH:mm:ss转换为时间类型
    public static Date strToDate(String strDate, String format)
    {
        SimpleDateFormat formatter = null;
        Date strtodate = null;

        try
        {
            if (format == null)
            {
                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            else
            {
                formatter = new SimpleDateFormat(format);
            }

            strtodate = formatter.parse(strDate, new ParsePosition(0));
        }
        catch (Exception e)
        {
            log_ex(e);
        }

        return strtodate;
    }

    public static Date strToDate(String strDate)
    {
        return strToDate(strDate, null);
    }

    public static Date yyyymmddToDate(String strDate)
    {
        return strToDate(strDate, "yyyy-MM-dd");
    }

    public static Calendar getCalendar(String strDate)
    {
        Date dd = yyyymmddToDate(strDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dd);

        return calendar;
    }

    // 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
    public static String dateToStr(Date dateDate, String format)
    {
        SimpleDateFormat formatter = null;
        String dateString = null;
        try
        {
            if (format == null)
            {
                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            else
            {
                formatter = new SimpleDateFormat(format);
            }

            dateString = formatter.format(dateDate);
        }
        catch (Exception e)
        {
            log_ex(e);
        }
        return dateString;
    }

    public static String dateToStr(Date dateDate)
    {
        return dateToStr(dateDate, null);
    }

    public static String dateToYYYYMMDD(Date dateDate)
    {
        return dateToStr(dateDate, "yyyy-MM-dd");
    }

    static private Calendar tmpForMillisToStr1 = Calendar.getInstance();

    public static String MillisToStr(long millis)
    {
        tmpForMillisToStr1.setTimeInMillis(millis);
        return dateToStr(tmpForMillisToStr1.getTime());
    }

    static private Calendar tmpForMillisToStr2 = Calendar.getInstance();

    public static String MillisToHHMM(long millis)
    {
        tmpForMillisToStr2.setTimeInMillis(millis);
        return ""
                + formatTwoNumber(tmpForMillisToStr2.get(Calendar.HOUR_OF_DAY))
                + ":"
                + formatTwoNumber(tmpForMillisToStr2.get(Calendar.MINUTE));
    }

    static private Calendar tmpForMillisToStr3 = Calendar.getInstance();

    public static String MillisToYYYYMMDD(long millis)
    {
        tmpForMillisToStr3.setTimeInMillis(millis);
        return "" + formatTwoNumber(tmpForMillisToStr3.get(Calendar.YEAR))
                + "-" + formatTwoNumber(tmpForMillisToStr3.get(Calendar.MONTH)+1)
                + "-" + formatTwoNumber(tmpForMillisToStr3.get(Calendar.DATE));
    }

    public static String formatTwoNumber(int n)
    {
        return new DecimalFormat("00").format(n);
    }

    // 整形与字符串互转
    public static int strToInt(String s)
    {
        return Integer.parseInt(s);
    }

    public static String intToStr(int i)
    {
        return new String(i + "");
    }

    public static String intToStr(long i)
    {
        return new String(i + "");
    }

    // 统一半角全角括号
    public static String Bracket(String src)
    {
        return src.replace('(', '（').replace(')', '）');
    }

    public static void unZip(String zipFile, String targetDir)
    {
        int BUFFER = 4096; // 这里缓冲区我们使用4KB，
        String strEntry; // 保存每个zip的条目名称

        try
        {
            BufferedOutputStream dest = null; // 缓冲输出流
            FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(fis));
            ZipEntry entry; // 每个zip条目的实例

            while ((entry = zis.getNextEntry()) != null)
            {

                try
                {
                    int count;
                    byte data[] = new byte[BUFFER];
                    strEntry = entry.getName();

                    File entryFile = new File(targetDir + strEntry);
                    File entryDir = new File(entryFile.getParent());
                    if (!entryDir.exists())
                    {
                        entryDir.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1)
                    {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
                catch (Exception e)
                {
                    log_ex(e);
                }
            }
            zis.close();
        }
        catch (Exception e)
        {
            log_ex(e);
        }
    }

    public static final String PARAMES_SEPARATOR = "\\|"; // 分隔符
    public static final String PARAMES_HYPHEN = "|"; // 连字符

    public static String[] splitParames(String param_in)
    {
        return param_in.split(PARAMES_SEPARATOR);
    }

    public static String arrayToString(char[] array)
    {
        return array == null ? new String("-1") : new String(array);
    }

    public static void initializeCharArray(char[] a, char defaultValue)
    {
        for (int i = 0; i < a.length; i++)
        {
            a[i] = defaultValue;
        }
    }

    public static String formatTime(Calendar c)
    {
        return "未完成";
    }

}
