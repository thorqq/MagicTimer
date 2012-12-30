package com.thorqq.magictimer;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thorqq.magictimer.util.Util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;

public class UpdateManager
{
//    <name>无锡公交实时查询</name>
//    <package>com.thorqq.magictimer</package>
//    <versionCode>10</versionCode>
//    <versionName>2.0<versionName>
//    <url>http://thorqq.duapp.com/download/wuxibus/WuxiBus.2.0.apk</url>
//    <remark>本版本取消了验证码输入，极大的提高了查询速度，并节约流量 </remark>
//    <date>2012-10-17</date>

    private final static String UPDATE_FILE_LIST_URL = "http://thorqq.duapp.com/download/magictimer/filelist.xml";

    public final static String NODE_NAME        = "name";
    public final static String NODE_PACKAGE     = "package";
    public final static String NODE_VERSIONCODE = "versionCode";
    public final static String NODE_VERSIONNAME = "versionName";
    public final static String NODE_URL         = "url";
    public final static String NODE_REMARK      = "remark";
    public final static String NODE_DATE        = "date";
    
    public final static int SHOW_NEED_UPDATE_NOTICE_YES = 0;
    public final static int SHOW_NEED_UPDATE_NOTICE_NO = 1;

    /* 保存解析的XML信息 */
    HashMap<String, String> mHashMap;
    /* 下载保存路径 */
    private String mSavePath;
    /* 是否取消更新 */
    private boolean mCancelUpdate = false;
    // 是否强制更新
    private boolean mbForce = false;

    private Context mContext;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private TextView mTextProgress;
    private Dialog mDownloadDialog;
    
    public HashMap<String, String> parseXml(InputStream inStream) throws Exception
    {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        
        // 实例化一个文档构建器工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 通过文档构建器工厂获取一个文档构建器
        DocumentBuilder builder = factory.newDocumentBuilder();
        // 通过文档通过文档构建器构建一个文档实例
        Document document = builder.parse(inStream);
        //获取XML文件根节点
        Element root = document.getDocumentElement();
        //获得所有子节点
        NodeList childNodes = root.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++)
        {
            //遍历子节点
            Node childNode = (Node) childNodes.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childElement = (Element) childNode;
                //软件名称
                if (NODE_NAME.equalsIgnoreCase(childElement.getNodeName()))
                {
                    hashMap.put(NODE_NAME,childElement.getFirstChild().getNodeValue());
                }
                //包名
                else if ((NODE_PACKAGE.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_PACKAGE,childElement.getFirstChild().getNodeValue());
                }
                //版本号
                else if ((NODE_VERSIONCODE.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_VERSIONCODE,childElement.getFirstChild().getNodeValue());
                }
                //版本名称
                else if ((NODE_VERSIONNAME.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_VERSIONNAME,childElement.getFirstChild().getNodeValue());
                }
                //下载地址
                else if ((NODE_URL.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_URL,childElement.getFirstChild().getNodeValue());
                }
                //更新信息
                else if ((NODE_REMARK.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_REMARK,childElement.getFirstChild().getNodeValue().replace('|', '\n'));
                }
                //更新时间
                else if ((NODE_DATE.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_DATE,childElement.getFirstChild().getNodeValue());
                }
            }
        }
        return hashMap;
    }
    
    private int getVersionCode(Context context)
    {
        int versionCode = 0;
        try
        {
            // 获取软件版本号
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e)
        {
            Util.log_ex(e);
            return -1;
        }
        return versionCode;
    }
    
    private class CheckUpdateTask extends AsyncTask<Void, Boolean, Boolean> 
    {
        int mShowNewVersionNotice = SHOW_NEED_UPDATE_NOTICE_NO;
        ProgressDialog  mPD = null;
        
        CheckUpdateTask(int iShowNewVersionNotice)
        {
            mShowNewVersionNotice = iShowNewVersionNotice;
        }
        
        @Override
        protected Boolean doInBackground(Void... params) 
        {
            
            publishProgress(true); 
            
            boolean result =  isUpdate();
            
            publishProgress(false);

            return result;
        }
        
        @Override
        protected void onProgressUpdate(Boolean... b )
        {
            if(mShowNewVersionNotice == SHOW_NEED_UPDATE_NOTICE_NO)
            {
                return;
            }
            
            if(b[0])
            {
                mPD = ProgressDialog.show(mContext, null, "正在查询最新版本", true, false);
            }
            else
            {
                if (mPD != null)
                {
                    mPD.dismiss();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) 
        {
            if(result)
            {
                showUpdateDialog();
                //mIsUpdate = false;
            }
            else
            {
                if(mShowNewVersionNotice == SHOW_NEED_UPDATE_NOTICE_YES)
                {
                    showNoUpdateDialog();
                }
            }
        }
    }

    public UpdateManager(Context context)
    {
        this.mContext = context;
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate(int iShowNewVersionNotice)
    {
        Util.log("Check update");
        mbForce = false;
        new CheckUpdateTask(iShowNewVersionNotice).execute();
    }

    public void checkUpdate(int iShowNewVersionNotice, boolean bForce)
    {
        Util.log("Check update");
        mbForce = bForce;
        new CheckUpdateTask(iShowNewVersionNotice).execute();
    }

    /**
     * 检查软件是否有更新版本
     * 
     * @return
     */
    private boolean isUpdate()
    {
        // 获取当前软件版本
        int versionCode = getVersionCode(mContext);

        InputStream inStream = null;
        try{
            URL url = new URL(UPDATE_FILE_LIST_URL);
            // 创建连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            // 创建输入流
            inStream = conn.getInputStream();
        }catch(Exception e)
        {
            Util.log_ex(e);
            return false;
        }

        
        try
        {
            mHashMap = parseXml(inStream);
        } catch (Exception e)
        {
            Util.log_ex(e);
            return false;
        }
        
        if (null != mHashMap)
        {
            int serviceCode = Integer.valueOf(mHashMap.get(NODE_VERSIONCODE));
            Util.log("versionCode in xml: " + serviceCode);
            if (serviceCode > versionCode || mbForce)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 显示无需更新对话框
     */
    private void showNoUpdateDialog()
    {
        // 构造对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("已经是最新版本，无需更新");
        builder.setPositiveButton("确定", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }
    
    /**
     * 显示软件更新对话框
     */
    private void showUpdateDialog()
    {
        // 构造对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("软件更新");
        builder.setMessage("检测到新版本"+ mHashMap.get(NODE_VERSIONNAME) +"("+ 
                mHashMap.get(NODE_DATE) +"发布)，是否立即更新？\n\n" +
                " 更新内容：\n" +
                mHashMap.get(NODE_REMARK));
        // 更新
        builder.setPositiveButton("立即更新", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        // 稍后更新
        builder.setNegativeButton("下次再说", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog()
    {
        // 构造软件下载对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("正在更新");
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        mTextProgress = (TextView) v.findViewById(R.id.textViewProgress);
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton("取消", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                // 设置取消状态
                mCancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();

        // 下载文件
        new DownloadTask().execute();
    }
    
    private class DownloadTask extends AsyncTask<Void, Integer, Boolean> 
    {                
        @Override
        protected Boolean doInBackground(Void... params)
        {
            return downloadApk();
        }

        @Override
        protected void onPostExecute(Boolean result) 
        {
            //下载失败
            if(!result)
            {
                //不是用户主动取消（可能是网络错误）
                if(!mCancelUpdate)
                {
                    AlertDialog.Builder builder = new Builder(mContext);
                    builder.setTitle("错误");
                    builder.setMessage("程序下载失败");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();
                }
                
                return;
            }
            
            File apkfile = new File(mSavePath, mHashMap.get(NODE_PACKAGE));
            if (!apkfile.exists())
            {
                return;
            }
            // 通过Intent安装APK文件
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
            mContext.startActivity(i);
        }

        @Override
        protected void onProgressUpdate(Integer... progresses)
        {
            mProgress.setProgress(progresses[0]);
            mTextProgress.setText(progresses[0]+"%");
        }

        /**
         * 下载apk文件
         */
        private boolean downloadApk()
        {
            int length = 0;
            int count = 0;

            try
            {
                // 获得存储卡的路径
                mSavePath = Util.getBaseDir().toString();
                
                URL url = new URL(mHashMap.get(NODE_URL));
                // 创建连接
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                // 获取文件大小
                length = conn.getContentLength();
                // 创建输入流
                InputStream is = conn.getInputStream();

                File file = new File(mSavePath);
                // 判断文件目录是否存在
                if (!file.exists())
                {
                    file.mkdir();
                }
                Util.log("mSavePath: " + mSavePath);
                File apkFile = new File(mSavePath, mHashMap.get(NODE_PACKAGE));
                FileOutputStream fos = new FileOutputStream(apkFile);
                // 缓存
                byte buf[] = new byte[1024];
                // 写入到文件中
                do
                {
                    // 写入文件
                    int numread = is.read(buf);
                    if(numread <= 0)
                    {
                        //下载完成
                        break;
                    }
                    fos.write(buf, 0, numread);
                    
                    // 更新进度
                    count += numread;
                    publishProgress((int) (((float) count / length) * 100)); 
                    
                } while (!mCancelUpdate);// 点击取消就停止下载.
                fos.close();
                is.close();
                
            } catch (MalformedURLException e)
            {
                Util.log_ex(e);
                return false;
            } catch (IOException e)
            {
                Util.log_ex(e);
                return false;
            }

            // 取消下载对话框显示
            mDownloadDialog.dismiss();
            
            // 比较文件大小，判断是否下载完全
            if(count < length)
            {
                return false;
            }            
            //TODO 记录下载时间、下载者信息到网站
            StatHelper.getInstance().statUpdateAppVersionName();
            
            return true;
        }    
    }

}