package hikvision.zhanyun.com.hikvision;

import android.util.Log;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.PTZCommand;

/**
 * Created by ZY004Engineer on 2018/6/11.
 */

public class HikVisionUtils {

    private String TAG = HikVisionUtils.class.getSimpleName();
    private static HikVisionUtils mInstance = null;

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
        String logFilePath = "/mnt/sdcard/sdklog/";
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
        return iLogID;
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


}
