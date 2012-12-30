package com.thorqq.magictimer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import com.baidu.mobstat.StatService;
import com.thorqq.magictimer.util.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class StatHelper
{
    public static final String STAT_UPDATE = "Update software";
    
    public static final String DEVICE_PLATFORM_TYPE = "android";

    private static Context mContext;
    HashMap<String, String> mHashMap = new HashMap<String, String>();;
    private static StatHelper statHelperInstance = null;

    static final String EMULATOR_DEVICE_ID = "emulatorDeviceId";
    static final String PRIMARY_COLOR = "PrimaryColor";
    static final String PREFERENCE = "appPrefrences";
    static final String REFERRAL_URL = "InstallReferral";

    static final String NOTIFY_ID = "nyid";
    // 系统当前时间
    static final String CURRENT_TIME = "at";

    // URL 参数名.
    final String DEVICE_SCREEN_WIDTH = "device_width"; // 布局大小.
    final String DEVICE_SCREEN_HEIGHT = "device_height"; // 布局大小.
    final String MAC_ADDRESS = "mac";
    final String NET_TYPE = "net";
    final String DEVICE_SIM_IMSI = "imsi"; // 设备为手机时的SIM唯一识别码
    final String DEVICE_ID_NAME = "udid"; // 该设备的唯一ID (IMEI or MEID).
    final String DEVICE_NAME = "device_name"; // 具体的设备名(iPod touch 2G, iPhone
    // 3GS, ...)
    final String DEVICE_TYPE_NAME = "device_type"; // 平台类型 (Android, iPhone,
    // iPad).
    final String DEVICE_OS_VERSION_NAME = "os_version"; // 操作系统版本.
    final String DEVICE_COUNTRY_CODE = "country_code"; // 国家代码.
    final String DEVICE_LANGUAGE = "language"; // 语言代码.
    final String APP_VERSION_NAME = "app_version"; // 应用程序版本.
    final String APP_ACT = "act"; //
    final String APP_CHANNEL = "channel"; // 应用推广渠道.

    private String deviceID = "";
    private String deviceName = "";
    private String deviceType = "";
    private String deviceOSVersion = "";
    private String deviceCountryCode = "";
    private String deviceLanguage = "";
    private String appVersion = "";
    private String channel = "";

    private int deviceScreenWidth = 0;
    private int deviceScreenHeight = 0;
    private String simIMSI = "";
    private String netType = "";
    private String mac_address = "";
    
    public void statUpdateAppVersionName()
    {
        StatService.onEvent(mContext, STAT_UPDATE, get(APP_VERSION_NAME));
        Util.log("Statics '" + STAT_UPDATE + "' : " + get(APP_VERSION_NAME));
    }

    private String get(String key)
    {
        return mHashMap.get(key);
    }
    
    static public StatHelper getInstance(Context context)
    {
        if(statHelperInstance == null)
        {
            statHelperInstance = new StatHelper(context);
        }
        return statHelperInstance;
    }

    static public StatHelper getInstance()
    {
        return statHelperInstance;
    }
    
    private StatHelper(Context context)
    {
        mContext = context;
        initMetaData();
        setParams();
    }

    /**
     * 从manifest文件初始化设备信息，这些数据用在每次的连接服务器中
     */
    private void initMetaData() {
        PackageManager manager = mContext.getPackageManager();
        ApplicationInfo info;

        try {
            info = manager.getApplicationInfo(mContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (info == null || info.metaData == null) 
            {
                return;
            }
            String metaDataValue = "";

            // 获取到渠道.
            String pid = info.metaData.getString("BaiduMobAd_CHANNEL");
            if (pid == null || "".equals(pid.trim())) {
                pid = info.metaData.getString("APP_PID");
            }
            
            if (pid != null && !"".equals(pid.trim())) {
                channel = pid;
            } else {
                channel = "Baidu";
            }

            // 获取应用程序版本.
            PackageInfo packageInfo = manager.getPackageInfo(
                    mContext.getPackageName(), 0);
            appVersion = packageInfo.versionName;

            // 设备类型.
            deviceType = DEVICE_PLATFORM_TYPE;

            // 设备名.
            deviceName = android.os.Build.MODEL;

            // 操作系统版本.
            deviceOSVersion = android.os.Build.VERSION.RELEASE;

            // 国家和语言代码.
            deviceCountryCode = Locale.getDefault().getCountry();
            deviceLanguage = Locale.getDefault().getLanguage();
            
            TelephonyManager phoneMgr=(TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            // 获取SIM卡的IMSI码
            simIMSI = phoneMgr.getSubscriberId();
            
            try {
                // 获取设备的物理地址
                WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String strMacAddress = wifiInfo.getMacAddress();
                if(strMacAddress != null)
                {
                    mac_address = "mac" + wifiInfo.getMacAddress().replaceAll(":", "");
                    Util.log(mac_address);
                }
            } catch (Exception e2) {
                Util.log_ex(e2);
                Util.log("Permission.ACCESS_WIFI_STATE is not found or the device is Emulator, Please check it!");
            }
            
            try {
                ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netWorkInfo = connManager.getActiveNetworkInfo();
                if(netWorkInfo != null && !netWorkInfo.getTypeName().toLowerCase().equals("mobile")){
                    netType = netWorkInfo.getTypeName().toLowerCase();
                }else{
                    netType = netWorkInfo.getExtraInfo().toLowerCase();
                }

            } catch (Exception e1) {
                Util.log_ex(e1);
            }

            SharedPreferences settings = mContext.getSharedPreferences(
                    PREFERENCE, 0);

            // 设备ID.
            metaDataValue = info.metaData.getString("DEVICE_ID");
            
            // 设备ID是否已经存在于 manifest中?
            if (metaDataValue != null && !metaDataValue.equals("")) {
                deviceID = metaDataValue;
            } else {
                TelephonyManager telephonyManager = (TelephonyManager) mContext
                        .getSystemService(Context.TELEPHONY_SERVICE);

                if (telephonyManager != null) {
                    //the IMEI for GSM and the MEID or ESN for CDMA phones
                    deviceID = telephonyManager.getDeviceId();

                    // 设备ID是否为空?
                    if (deviceID == null || deviceID.length() == 0) {

                        // 设置设备ID为0 如果是空，生成一个模拟的设备ID.
                        deviceID = "0";
                    }
                    
                    // 如果deviceID全是0，则置为"0"
                    int len = 0;
                    for(; len < deviceID.length(); len++)
                    {
                        if(deviceID.charAt(len) == '0')
                        {
                            break;
                        }
                    }
                    if(len == deviceID.length())
                    {
                        deviceID = "0";
                    }
                    
                    try {
                        // 设备ID转为小写.
                        deviceID = deviceID.toLowerCase();

                        //Integer devTag = Integer.parseInt(deviceID);

                        // 设备ID是否0.
                        if (deviceID.equals("0") && mac_address.equals("")) {
                            StringBuffer buff = new StringBuffer();
                            buff.append("EMULATOR");
                            String deviceId = settings.getString(
                                    EMULATOR_DEVICE_ID, null);

                            if (deviceId != null
                                    && !deviceId.equals("")) {
                                deviceID = deviceId;
                            } else {
                                String constantChars = "1234567890abcdefghijklmnopqrstuvw";
                                for (int i = 0; i < 32; i++) {
                                    int randomChar = (int) (Math
                                            .random() * 100);
                                    int ch = randomChar % 30;
                                    buff.append(constantChars
                                            .charAt(ch));
                                }

                                deviceID = buff.toString()
                                        .toLowerCase();

                                SharedPreferences.Editor editor = settings
                                        .edit();
                                editor.putString(EMULATOR_DEVICE_ID,
                                        deviceID);
                                editor.commit();
                            }
                        }
                        // IMEI 不存在，并且mac地址可获取时，自定义一个udid并进行固化存储
                        else if(deviceID.equals("0") && mac_address != null && !"".equals(mac_address.trim())){
                            
                            deviceID = mac_address;
                        }
                    } catch (NumberFormatException ex) {
                        Util.log_ex(ex);
                    }
                    
                } else {
                    deviceID = null;
                }
            }
            
            try {
                DisplayMetrics metrics = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getMetrics(metrics);
                deviceScreenWidth = metrics.widthPixels;
                deviceScreenHeight = metrics.heightPixels;
            } catch (Exception e) {
                Util.log_ex(e);
            }
            
        } catch (Exception e) {
            Util.log_ex(e);
        }        
    }
    
    private void setParams() 
    {
        mHashMap.put(DEVICE_ID_NAME, deviceID);
        mHashMap.put(DEVICE_SIM_IMSI, simIMSI);
        mHashMap.put(NET_TYPE, netType);
        mHashMap.put(APP_VERSION_NAME, appVersion);
        mHashMap.put(DEVICE_NAME, deviceName);
        mHashMap.put(DEVICE_TYPE_NAME, deviceType);
        mHashMap.put(DEVICE_OS_VERSION_NAME, deviceOSVersion);
        mHashMap.put(DEVICE_COUNTRY_CODE, deviceCountryCode);
        mHashMap.put(DEVICE_LANGUAGE, deviceLanguage);
        mHashMap.put(APP_ACT, mContext.getPackageName() + "." + mContext.getClass().getSimpleName());
        
        mHashMap.put(APP_CHANNEL, channel);
        mHashMap.put(DEVICE_SCREEN_WIDTH, deviceScreenWidth+"");
        mHashMap.put(DEVICE_SCREEN_HEIGHT, deviceScreenHeight+"");
    }
    
    public void printParams()
    {
        StringBuffer sff = new StringBuffer();
        
        sff.append("MetaData:\n");
        for(Iterator<String> it =  mHashMap.keySet().iterator(); it.hasNext(); )   
        { 
            String key = (String)it.next();
            sff.append(key + " = " + mHashMap.get(key)).append("\n");
        }
        Util.log(sff.toString());        
    }
}

