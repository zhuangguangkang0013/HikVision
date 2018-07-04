package hikvision.zhanyun.com.hikvision;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_JPEGPARA;
import com.hikvision.netsdk.NET_DVR_TIME;

import java.io.File;

/**
 * Created by ZY004Engineer on 2018/6/11.
 */

public class HikVisionUtils {

    private String TAG = HikVisionUtils.class.getSimpleName();
    private static HikVisionUtils mInstance = null;
    private int mLoginId = -1;
    public static String FILE_PATH = null;
    private NET_DVR_TIME netDvrTime = new NET_DVR_TIME();

    public static HikVisionUtils getInstance() {

        if (mInstance == null) {
            synchronized (HikVisionUtils.class) {
                if (mInstance == null) {
                    mInstance = new HikVisionUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化sdk
     *
     * @return true:成功  false:失败
     */
    public boolean initSDK() {
        Boolean isNetDVRInit = HCNetSDK.getInstance().NET_DVR_Init();
        if (!isNetDVRInit) {
            Log.e(TAG, "HCNetSDK init is failed!" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return false;
        }
        String logFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "HikVisionSDKLog/";
        //nLogLevel[in] 日志的等级（默认为0）：
        // 0-表示关闭日志，1-表示只输出ERROR错误日志，
        // 2-输出ERROR错误信息和DEBUG调试信息，
        // 3-输出ERROR错误信息、DEBUG调试信息和INFO普通信息等所有信息
        int logLevel = 3;
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(logLevel, logFilePath, true);
        return true;
    }

    /**
     * 设备登录
     *
     * @param address  地址
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @return 结果
     */
    public int loginNormalDevice(String address, int port, String user, String password) {
        // get instance
        NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return -1;
        }
        // call NET_DVR_Login_v30 to login on, port 8000 as default
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(address, port,
                user, password, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "NET_DVR_Login is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }
//        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
//            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
//             m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;
//        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
//            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
//            m_iChanNum = m_oNetDvrDeviceInfoV30.byIPChanNum
//                    + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256;
//        }
        Log.i(TAG, "NET_DVR_Login is Successful!");
        mLoginId = iLogID;
        return iLogID;
    }


    /**
     * @param dwYear   年
     * @param dwMonth  月
     * @param dwDay    日
     * @param dwHour   时
     * @param dwMinute 分
     * @param dwSecond 秒
     * @return ture 或者 flas  ture表示成功  flas表示失败
     */
    boolean setDateTime(int dwYear, int dwMonth, int dwDay, int dwHour, int dwMinute, int dwSecond) {
        if (netDvrTime == null) netDvrTime = new NET_DVR_TIME();
        netDvrTime.dwYear = dwYear;
        netDvrTime.dwMonth = dwMonth;
        netDvrTime.dwDay = dwDay;
        netDvrTime.dwHour = dwHour;
        netDvrTime.dwMinute = dwMinute;
        netDvrTime.dwSecond = dwSecond;
        return HCNetSDK.getInstance()
                .NET_DVR_SetDVRConfig(mLoginId, HCNetSDK.NET_DVR_SET_TIMECFG,
                        0xFFFFFFFF, netDvrTime);
    }

    /**
     * @return 获取设备时间对象
     */
    public NET_DVR_TIME getNetDvrTime() {
        if (netDvrTime == null) netDvrTime = new NET_DVR_TIME();

        if (mLoginId != -1) {
            HCNetSDK.getInstance()
                    .NET_DVR_GetDVRConfig(mLoginId, HCNetSDK.NET_DVR_GET_TIMECFG, 0, netDvrTime);
        }
        return netDvrTime;
    }

    /**
     * @return 获取规约6个字节的时间
     */
    public byte[] getNetDvrTimeByte() {
        byte[] timeByte = new byte[0];
        if (getNetDvrTime() != null)
            timeByte = new byte[]{(byte) (getNetDvrTime().dwYear - 2000)
                    , (byte) getNetDvrTime().dwMonth, (byte) getNetDvrTime().dwDay
                    , (byte) getNetDvrTime().dwHour, (byte) getNetDvrTime().dwMinute
                    , (byte) getNetDvrTime().dwSecond};
        return timeByte;
    }

    /**
     * 终端复位
     *
     * @param i2 操作云台预置点命令  SET_PRESET--设置预置点  CLE_PRESET--清除预置点   GOTO_PRESET--转到预置点
     * @param i3 预置点的序号
     * @return
     */
    public boolean terminalReduction(int i2, int i3) {
        return HCNetSDK.getInstance()
                .NET_DVR_PTZPreset_Other(mLoginId, 1, i2, i3);
    }

    /**
     * 返回最后操作的错误码。
     *
     * @return 返回值为错误码 具体请看SDK——NET_DVR_GetLastError
     */
    public int GetLastError() {
        int LastError = HCNetSDK.getInstance()
                .NET_DVR_GetLastError();
        return LastError;
    }

    /**
     * @return exception instance
     */
    public Boolean getExceptionCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack() {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle) {
                Log.i(TAG, "recv exception, type:" + iType);
            }
        };

        return HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(oExceptionCbf);
    }

    /**
     * 云台移动 NET_DVR_PTZControl_Other参数：(播放标记, 通道， 指令码, 开始标记0或停止标记1)
     *
     * @param orientation 九宫格数字方向
     */
    public void onPTZControl(int orientation) {
        if (mLoginId < 0) {
            return;
        }
        boolean is = HCNetSDK.getInstance().NET_DVR_PTZControl_Other(mLoginId, 1, orientation, 0);
        if (!is) {
            Log.e(TAG, "onPTZControl: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        SystemClock.sleep(200);
        HCNetSDK.getInstance().NET_DVR_PTZControl_Other(mLoginId, 1, orientation, 1);
    }

    /**
     * 拍照并保存照片
     *
     * @param filePath  文件路径
     * @param fileName  文件名
     * @param imageSize 图片大小
     * @return true成功，则失败
     */
    public boolean onCaptureJPEGPicture(String filePath, String fileName, int imageSize) {
        NET_DVR_JPEGPARA netDvrJpegpara = new NET_DVR_JPEGPARA();
        switch (imageSize) {
            case 1://320 X 240
                netDvrJpegpara.wPicSize = 23;
                break;
            case 2://640 X 480
                netDvrJpegpara.wPicSize = 6;
                break;
            case 3://704 X 576
                netDvrJpegpara.wPicSize = 2;
                break;
            case 4://800 X 600
                netDvrJpegpara.wPicSize = 4;
                break;
            case 5://1024 X 768
                netDvrJpegpara.wPicSize = 25;
                break;
            case 6://1280 X 1024
                netDvrJpegpara.wPicSize = 17;
                break;
            case 7://1280 X 720
                netDvrJpegpara.wPicSize = 5;
                break;
            case 8://1920 X 1080
                netDvrJpegpara.wPicSize = 9;
                break;
            case 9://2048 X 1536
                break;
            case 10://2560 X 1440
                break;
            case 11://2560 X 1920
                break;
            case 12://2592 X 1944
                break;
            case 13://3200 X 2400
                break;
            case 14://3264 X 2448
                break;
            case 15://3840 X 2160
                break;
            case 16://4160 X 2340
                break;
            case 17://4000 X 3000
                break;
            case 18://4608 X 3456
                break;
        }
        netDvrJpegpara.wPicQuality = 2;

        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        FILE_PATH = filePath + fileName;
        return HCNetSDK.getInstance().NET_DVR_CaptureJPEGPicture(mLoginId, 1, netDvrJpegpara, FILE_PATH);
    }

}
