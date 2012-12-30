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
//    <name>��������ʵʱ��ѯ</name>
//    <package>com.thorqq.magictimer</package>
//    <versionCode>10</versionCode>
//    <versionName>2.0<versionName>
//    <url>http://thorqq.duapp.com/download/wuxibus/WuxiBus.2.0.apk</url>
//    <remark>���汾ȡ������֤�����룬���������˲�ѯ�ٶȣ�����Լ���� </remark>
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

    /* ���������XML��Ϣ */
    HashMap<String, String> mHashMap;
    /* ���ر���·�� */
    private String mSavePath;
    /* �Ƿ�ȡ������ */
    private boolean mCancelUpdate = false;
    // �Ƿ�ǿ�Ƹ���
    private boolean mbForce = false;

    private Context mContext;
    /* ���½����� */
    private ProgressBar mProgress;
    private TextView mTextProgress;
    private Dialog mDownloadDialog;
    
    public HashMap<String, String> parseXml(InputStream inStream) throws Exception
    {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        
        // ʵ����һ���ĵ�����������
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // ͨ���ĵ�������������ȡһ���ĵ�������
        DocumentBuilder builder = factory.newDocumentBuilder();
        // ͨ���ĵ�ͨ���ĵ�����������һ���ĵ�ʵ��
        Document document = builder.parse(inStream);
        //��ȡXML�ļ����ڵ�
        Element root = document.getDocumentElement();
        //��������ӽڵ�
        NodeList childNodes = root.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++)
        {
            //�����ӽڵ�
            Node childNode = (Node) childNodes.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childElement = (Element) childNode;
                //�������
                if (NODE_NAME.equalsIgnoreCase(childElement.getNodeName()))
                {
                    hashMap.put(NODE_NAME,childElement.getFirstChild().getNodeValue());
                }
                //����
                else if ((NODE_PACKAGE.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_PACKAGE,childElement.getFirstChild().getNodeValue());
                }
                //�汾��
                else if ((NODE_VERSIONCODE.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_VERSIONCODE,childElement.getFirstChild().getNodeValue());
                }
                //�汾����
                else if ((NODE_VERSIONNAME.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_VERSIONNAME,childElement.getFirstChild().getNodeValue());
                }
                //���ص�ַ
                else if ((NODE_URL.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_URL,childElement.getFirstChild().getNodeValue());
                }
                //������Ϣ
                else if ((NODE_REMARK.equalsIgnoreCase(childElement.getNodeName())))
                {
                    hashMap.put(NODE_REMARK,childElement.getFirstChild().getNodeValue().replace('|', '\n'));
                }
                //����ʱ��
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
            // ��ȡ����汾��
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
                mPD = ProgressDialog.show(mContext, null, "���ڲ�ѯ���°汾", true, false);
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
     * ����������
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
     * �������Ƿ��и��°汾
     * 
     * @return
     */
    private boolean isUpdate()
    {
        // ��ȡ��ǰ����汾
        int versionCode = getVersionCode(mContext);

        InputStream inStream = null;
        try{
            URL url = new URL(UPDATE_FILE_LIST_URL);
            // ��������
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            // ����������
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
     * ��ʾ������¶Ի���
     */
    private void showNoUpdateDialog()
    {
        // ����Ի���
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("��ʾ");
        builder.setMessage("�Ѿ������°汾���������");
        builder.setPositiveButton("ȷ��", new OnClickListener()
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
     * ��ʾ������¶Ի���
     */
    private void showUpdateDialog()
    {
        // ����Ի���
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("�������");
        builder.setMessage("��⵽�°汾"+ mHashMap.get(NODE_VERSIONNAME) +"("+ 
                mHashMap.get(NODE_DATE) +"����)���Ƿ��������£�\n\n" +
                " �������ݣ�\n" +
                mHashMap.get(NODE_REMARK));
        // ����
        builder.setPositiveButton("��������", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                // ��ʾ���ضԻ���
                showDownloadDialog();
            }
        });
        // �Ժ����
        builder.setNegativeButton("�´���˵", new OnClickListener()
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
     * ��ʾ������ضԻ���
     */
    private void showDownloadDialog()
    {
        // ����������ضԻ���
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("���ڸ���");
        // �����ضԻ������ӽ�����
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        mTextProgress = (TextView) v.findViewById(R.id.textViewProgress);
        builder.setView(v);
        // ȡ������
        builder.setNegativeButton("ȡ��", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                // ����ȡ��״̬
                mCancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();

        // �����ļ�
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
            //����ʧ��
            if(!result)
            {
                //�����û�����ȡ�����������������
                if(!mCancelUpdate)
                {
                    AlertDialog.Builder builder = new Builder(mContext);
                    builder.setTitle("����");
                    builder.setMessage("��������ʧ��");
                    builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener()
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
            // ͨ��Intent��װAPK�ļ�
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
         * ����apk�ļ�
         */
        private boolean downloadApk()
        {
            int length = 0;
            int count = 0;

            try
            {
                // ��ô洢����·��
                mSavePath = Util.getBaseDir().toString();
                
                URL url = new URL(mHashMap.get(NODE_URL));
                // ��������
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                // ��ȡ�ļ���С
                length = conn.getContentLength();
                // ����������
                InputStream is = conn.getInputStream();

                File file = new File(mSavePath);
                // �ж��ļ�Ŀ¼�Ƿ����
                if (!file.exists())
                {
                    file.mkdir();
                }
                Util.log("mSavePath: " + mSavePath);
                File apkFile = new File(mSavePath, mHashMap.get(NODE_PACKAGE));
                FileOutputStream fos = new FileOutputStream(apkFile);
                // ����
                byte buf[] = new byte[1024];
                // д�뵽�ļ���
                do
                {
                    // д���ļ�
                    int numread = is.read(buf);
                    if(numread <= 0)
                    {
                        //�������
                        break;
                    }
                    fos.write(buf, 0, numread);
                    
                    // ���½���
                    count += numread;
                    publishProgress((int) (((float) count / length) * 100)); 
                    
                } while (!mCancelUpdate);// ���ȡ����ֹͣ����.
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

            // ȡ�����ضԻ�����ʾ
            mDownloadDialog.dismiss();
            
            // �Ƚ��ļ���С���ж��Ƿ�������ȫ
            if(count < length)
            {
                return false;
            }            
            //TODO ��¼����ʱ�䡢��������Ϣ����վ
            StatHelper.getInstance().statUpdateAppVersionName();
            
            return true;
        }    
    }

}