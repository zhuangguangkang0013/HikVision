package hikvision.zhanyun.com.hikvision;

import android.os.Environment;
import android.util.Log;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_JPEGPARA;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.hikvision.netsdk.PTZCommand;

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
     * <<<<<<< HEAD
     *
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
     * @param iReturn NET_DVR_Login_V40等登录接口的返回值
     *                i1 通道号
     *                i2 操作云台预置点命令  8--设置预置点  9--清除预置点   39--转到预置点
     *                i3 预置点的序号
     */
    public boolean TerminalReduction(int iReturn) {
        HCNetSDK.getInstance()
                .NET_DVR_PTZPreset_Other(iReturn, 1, 39, 1);
        boolean trReturn = false;
        return trReturn;
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
     * @param m_iLogID    播放标记
     * @param tag         开始标记0 停止标记1
     */
    public void onPTZControl(int orientation, int m_iLogID, int tag) {
        if (m_iLogID < 0) {
            return;
        }
        switch (orientation) {
            case 9:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.UP_RIGHT, tag);
                break;
            case 8:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.TILT_UP, tag);
                break;
            case 7:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.UP_LEFT, tag);
                break;
            case 6:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.PAN_RIGHT, tag);
                break;
            case 5:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.PAN_AUTO, tag);
                break;
            case 4:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.PAN_LEFT, tag);
                break;
            case 3:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.DOWN_RIGHT, tag);
                break;
            case 2:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.TILT_DOWN, tag);
                break;
            case 1:
                HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, 1,
                        PTZCommand.DOWN_LEFT, tag);
                break;
            default:
                break;
        }
    }


    /**
     * 拍照并保存照片
     *
     * @return true成功，则失败
     */
    public boolean onCaptureJPEGPicture() {
        NET_DVR_JPEGPARA netDvrJpegpara = new NET_DVR_JPEGPARA();

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "HikVisionPicture/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        FILE_PATH = filePath + "picture.jpg";
        return HCNetSDK.getInstance().NET_DVR_CaptureJPEGPicture(mLoginId, 1, netDvrJpegpara, FILE_PATH);
    }
}
