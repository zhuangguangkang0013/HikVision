package hikvision.zhanyun.com.hikvision;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.PTZCommand;
import com.hikvision.netsdk.PTZPresetCmd;
import com.hikvision.netsdk.RealPlayCallBack;

import org.MediaPlayer.PlayM4.Player;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;


public class MainActivity extends AppCompatActivity implements UdpListenerCallBack {
    private HikVisionUtils hikVisionUtils;
    private static String TAG = MainActivity.class.getSimpleName();
    private int m_iLogId;
    private SPGProtocol spgProtocol;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String password = "admin12345";
    private String cardNumber = "ZJ0002";
    //    private String http = "171.221.207.59";
//    private String http = "10.18.67.225";
//    private int httpPort = 17116;
    //    private String http = "10.18.67.225";
//    private int httpPort = 8989;
//    private int httpPort = 17116;
//        private String http = "10.18.67.225";
    private String http = "192.168.144.100";
    private short httpPort = 9090;
    //    private int httpPort = 9898;
    int acb;
    //主站卡号
    private byte[] carNumbers = {(byte) 0xF1, 0x39, 0x12, 0x34, 0x56, 0x78};

    public Handler mHanlder = new Handler();
    //心跳包间隔
    private int TheHeartbeatPacketsTime = 600;//秒钟
    private int TheHeartbeatPacketsTimes = TheHeartbeatPacketsTime * 1000;
    //采样间隔
    private int SamplingInterval = 600;//十分钟
    private int SamplingIntervals = SamplingInterval * 1000;
    //休眠时长
    private int TheSleepTime;
    //在线时长
    private int TheOnlineTime;
    //硬件重启时间点
    private byte[] HardwareResetTime;
    //密文认证
    public byte[] CipherCertification = {0x31, 0x32, 0x33, 0x34};
    byte[] test = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private List<byte[]> fileData;
    private int packIndex;
    private int count = -1;
    private Timer timer;
    //视频文件名字
    private String fileNames = "test.mp4";
    private String filePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "HikVisionPicture/";

    private SharedPreferences sharedPreferences;
    private SurfaceView surfaceView;
    private int m_iPort = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView=findViewById(R.id.surFaceView);

        hikVisionUtils = HikVisionUtils.getInstance();
        Boolean isSuccess = hikVisionUtils.initSDK();
        if (!isSuccess) {
            this.finish();
            return;
        }
        String address = "10.18.67.64";
        int port = 8000;
        String user = "admin";
        m_iLogId = hikVisionUtils.loginNormalDevice(address, port, user, password);
        if (m_iLogId < 0) {
            Log.e(TAG, "This device login failed!");
            return;
        } else {
            Log.i(TAG, "m_iLogID=" + m_iLogId);
        }
        spgProtocol = new SPGProtocol(this, this);
        spgProtocol.InitUdp(http, httpPort, cardNumber);

        mHanlder.postDelayed(boot, 0);

//        spgProtocol.bootContactInfo();
//
//        String fileName = "picture.jpg";
//        spgProtocol.uploadFile(filePath + fileName);
//        for (int i = 0; i < 50; i++) {
//            try {
//                Thread.sleep(2000);
//                hikVisionUtils.onPTZControl(9, m_iLogId, 0);
//                SystemClock.sleep(200);
//                hikVisionUtils.onPTZControl(9, m_iLogId, 1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }


        //初始化终端密码
        sharedPreferences = getSharedPreferences("password", MODE_PRIVATE);
        String password = sharedPreferences.getString("password", null);
        if (password != null) {
//            spgProtocol.terminalPassword = password;
        }
//        initView();
    }

    public static void verifyStoragePermissions(Activity activity) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //主动开机线程
    private Runnable boot = new Runnable() {
        @Override
        public void run() {
            spgProtocol.bootContactInfo();
//            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "HikVisionPicture/picture.jpg";
//            spgProtocol.uploadFile(filePath);
        }
    };


    @Override
    public void sendSuccess(byte order) {
        switch (order) {
            case SPGProtocol.ORDER_00H:
                break;
            case SPGProtocol.ORDER_01H:
                break;
            case SPGProtocol.ORDER_02H:
                break;
            case SPGProtocol.ORDER_03H:
                break;
            case SPGProtocol.ORDER_04H:
                break;
            case SPGProtocol.ORDER_05H:
                break;
            case SPGProtocol.ORDER_06H:
                break;
            case SPGProtocol.ORDER_07H:
                break;
            case SPGProtocol.ORDER_08H:
                boolean trReturn;
                trReturn = hikVisionUtils.terminalReduction(PTZCommand.GOTO_PRESET, 1);
                if (!trReturn) {
                    int LastError = hikVisionUtils.GetLastError();
                    Log.d("LastError:", String.valueOf(LastError));
                }
                break;
            case SPGProtocol.ORDER_09H:
                break;
            case SPGProtocol.ORDER_0AH:
                break;
            case SPGProtocol.ORDER_0BH:
                break;
            case SPGProtocol.ORDER_0CH:
                break;
            case SPGProtocol.ORDER_0DH:
                break;
            case SPGProtocol.ORDER_21H:
                break;
            case SPGProtocol.ORDER_30H:
                break;
            case SPGProtocol.ORDER_71H:
                break;
            case SPGProtocol.ORDER_72H:
                break;
            case SPGProtocol.ORDER_73H:
                break;
            case SPGProtocol.ORDER_74H:
                break;
            case SPGProtocol.ORDER_75H:
                break;
            case SPGProtocol.ORDER_76H:
                break;
            case SPGProtocol.ORDER_81H:
                break;
            case SPGProtocol.ORDER_82H:
                break;
            case SPGProtocol.ORDER_83H:
                break;
            case SPGProtocol.ORDER_84H:
                break;
            case SPGProtocol.ORDER_85H:
                break;
            case SPGProtocol.ORDER_86H:
                break;
            case SPGProtocol.ORDER_87H:
                break;
            case SPGProtocol.ORDER_88H:
                break;
            case SPGProtocol.ORDER_89H:
                break;
            case SPGProtocol.ORDER_8AH:
                break;
            case SPGProtocol.ORDER_8BH:
                break;
            case SPGProtocol.ORDER_93H:
                break;
            case SPGProtocol.ORDER_94H:
                break;
            case SPGProtocol.ORDER_95H:
                break;
            case SPGProtocol.ORDER_96H:
                break;
            case SPGProtocol.ORDER_97H:
                break;
        }

    }

    @Override
    public void receiveSuccess(byte order) {
        switch (order) {
            case SPGProtocol.ORDER_00H:
//                成功后停止定时循环发送开机请求
                mHanlder.removeCallbacks(boot);
                Log.i(TAG, "服务器返回了信息停止向服务器发送开机请求");
                //开启校时功能
                mHanlder.postDelayed(WhenTheSchool, 5000);
                spgProtocol.schoolTime();
                break;
            case SPGProtocol.ORDER_01H:
                byte[] time1 = hikVisionUtils.getNetDvrTimeByte();
                spgProtocol.terminalHeartBeatInfo(time1, (byte) 0x64, (byte) 0x044);
//
//                //校时成功后停止校时功能
//                mHanlder.removeCallbacks(WhenTheSchool);
//                //开启心跳包
//                mHanlder.postDelayed(TheHeartbeatPackets, 1000)//校时成功后停止校时功能
//                mHanlder.removeCallbacks(WhenTheSchool);
                //开启心跳包
                mHanlder.postDelayed(TheHeartbeatPackets, 1000);
                mHanlder.postDelayed(TheHeartbeatPackets, 5 * 1000);
                break;
            case SPGProtocol.ORDER_02H:
                break;
            case SPGProtocol.ORDER_03H:
                if (password.equals(spgProtocol.password)) {
                    TheHeartbeatPacketsTime = spgProtocol.HeartbeatInterval;//心跳间隔
                    SamplingIntervals = spgProtocol.SamplingInterval;//采样间隔
                    TheSleepTime = spgProtocol.TheSleepTime;//休眠时长
                    TheOnlineTime = spgProtocol.TheOnlineTime;//在线时长
                    HardwareResetTime = spgProtocol.HardwareResetTime;//硬件重启时间点   //重启功能未实现
                    CipherCertification = spgProtocol.CipherCertification;//密文认证
                    //TODO 处理休眠
                }
                break;
            case SPGProtocol.ORDER_04H:
                break;
            case SPGProtocol.ORDER_05H:

                //判断心跳包返回的指令  如果是原指令择每隔两分钟重复发送心跳
                if (spgProtocol.mReceiveData != null && spgProtocol.mReceiveData[7] == SPGProtocol.ORDER_05H) {
                    Log.i(TAG, "run: " + spgProtocol.mReceiveData[7]);
                    mHanlder.postDelayed(TheHeartbeatPackets, TheHeartbeatPacketsTimes);
                } else {
                    //否则关闭心跳包,两分钟后再重新开启定时心跳线程
                    mHanlder.removeCallbacks(TheHeartbeatPackets);
                    //执行指令
                    byte[] time = HikVisionUtils.getInstance().getNetDvrTimeByte();
                    spgProtocol.terminalHeartBeatInfo(time, (byte) 0x64, (byte) 0x44);
                    mHanlder.postDelayed(TheHeartbeatPackets, TheHeartbeatPacketsTimes);
                }
                break;
            case SPGProtocol.ORDER_06H:
                if (password.equals(spgProtocol.password)) {
                    spgProtocol.judges = true;
                    if (password.equals(spgProtocol.password) && spgProtocol.Http.equals(spgProtocol.Https) && Arrays.equals(spgProtocol.port, spgProtocol.ports) && Arrays.equals(spgProtocol.cardNumber, spgProtocol.cardNumbers)) {
//                        spgProtocol.setOrder(SPGProtocol.ORDER_06H);
//                        spgProtocol.PowerOn();
                        spgProtocol.judge = true;
                        //TODO 更改端口IP
                        //更改IP
                        http = String.valueOf(spgProtocol.mReceiveDatas[14] + "." + spgProtocol.mReceiveData[15] + "." + spgProtocol.mReceiveData[16] + "." + spgProtocol.mReceiveData[17]);
                        Log.i(TAG, "receiveSuccess: " + http);
                        //更改端口
                        int a = spgProtocol.mReceiveDatas[18];
                        int b = spgProtocol.mReceiveDatas[19];
                        int c = 0;
                        int d = 0;
                        if (a > b) {
                            c = a;
                            d = b;
                        } else {
                            c = b;
                            d = a;
                        }
//                        httpPort = c * 256 + d;
                        //更改卡号 暂不知道服务器发送的卡号是纯数字还是 F?H 格式   需格式判断或询问清格式
                        int ChangeTheNumber_a = spgProtocol.mReceiveDatas[26];
                        int ChangeTheNumber_b = spgProtocol.mReceiveDatas[27];
                        int ChangeTheNumber_c = spgProtocol.mReceiveDatas[28];
                        int ChangeTheNumber_d = spgProtocol.mReceiveDatas[29];
                        int ChangeTheNumber_e = spgProtocol.mReceiveDatas[30];
                        int ChangeTheNumber_f = spgProtocol.mReceiveDatas[31];
                        cardNumber = String.valueOf(ChangeTheNumber_a + ChangeTheNumber_b + ChangeTheNumber_c + ChangeTheNumber_d + ChangeTheNumber_e + ChangeTheNumber_f);
                    }
                }
                break;
            case SPGProtocol.ORDER_07H:
                //端口取高字节
                byte mainVersion = (byte) ((httpPort & 0xFF00) >> 8);
                //端口取低字节
                byte minorVersion = (byte) (httpPort & 0xFF);
                //高字节*256+低字节
                byte[] portNumber = new byte[]{(byte) (mainVersion * 256 + minorVersion)};
                Log.i(TAG, "receiveSuccess: " + portNumber);
                //IP
                int a, b, c, d;
                //先找到IP地址字符串中.的位置
                int position1 = http.indexOf(".");
                int position2 = http.indexOf(".", position1 + 1);
                int position3 = http.indexOf(".", position2 + 1);
                //将每个.之间的字符串转换成整型
                a = Integer.parseInt(http.substring(0, position1));
                b = Integer.parseInt(http.substring(position1 + 1, position2));
                c = Integer.parseInt(http.substring(position2 + 1, position3));
                d = Integer.parseInt(http.substring(position3 + 1));
                //启动
                spgProtocol.queryMasterStation(new byte[]{(byte) a, (byte) b, (byte) c, (byte) d}
                        , portNumber, carNumbers);
                break;
            case SPGProtocol.ORDER_08H:
                boolean trReturn;
                trReturn = hikVisionUtils.terminalReduction(PTZCommand.GOTO_PRESET, 1);
                if (!trReturn) {
                    int LastError = hikVisionUtils.GetLastError();
                    Log.d("LastError:", String.valueOf(LastError));
                }
                break;
            case SPGProtocol.ORDER_09H:
                break;
            case SPGProtocol.ORDER_0AH:
                break;
            case SPGProtocol.ORDER_0BH:
                break;
            case SPGProtocol.ORDER_0CH:
                break;
            case SPGProtocol.ORDER_0DH:
                byte[] time = HikVisionUtils.getInstance().getNetDvrTimeByte();
                spgProtocol.theQueryTime(time);
                break;
            case SPGProtocol.ORDER_21H:
                break;
            case SPGProtocol.ORDER_30H:
                break;
            case SPGProtocol.ORDER_71H:
                spgProtocol.setFileNameList();
                break;
            case SPGProtocol.ORDER_72H:
                break;
            case SPGProtocol.ORDER_73H:
                break;
            case SPGProtocol.ORDER_74H:
                break;
            case SPGProtocol.ORDER_75H:
                break;
            case SPGProtocol.ORDER_76H:
                break;
            case SPGProtocol.ORDER_81H:
                break;
            case SPGProtocol.ORDER_82H:
                break;
            case SPGProtocol.ORDER_83H:
                break;
            case SPGProtocol.ORDER_84H:
                break;
            case SPGProtocol.ORDER_85H:
                break;
            case SPGProtocol.ORDER_86H:
                break;
            case SPGProtocol.ORDER_87H:
                break;
            case SPGProtocol.ORDER_88H:
                break;
            case SPGProtocol.ORDER_89H:
                RealPlayCallBack fRealDataCallBack = getRealPlayerCbf();
                if (fRealDataCallBack == null) {

                    Log.e(TAG, "fRealDataCallBack object is failed!");
                    return;
                }

                NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
                previewInfo.lChannel = 1;
                previewInfo.dwStreamType = 1;                                                             //子码流
                previewInfo.bBlocked = 1;
                acb = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogId, previewInfo, fRealDataCallBack);
                Log.i(TAG, "receiveSuccess: " + acb);

                break;
            case SPGProtocol.ORDER_8AH:
                HCNetSDK.getInstance().NET_DVR_StopSaveRealData(acb);
                HCNetSDK.getInstance().NET_DVR_StopRealPlay(acb);
                break;
            case SPGProtocol.ORDER_8BH:
                break;
            case SPGProtocol.ORDER_93H:
                //移动摄像头
//                    boolean as = hikVisionUtils.terminalReduction(1, PTZCommand.GOTO_PRESET, spgProtocol.mReceiveDatas[11]);
                spgProtocol.setOrder(SPGProtocol.ORDER_93H);
                spgProtocol.sendPack();
                int lChannel = spgProtocol.mReceiveDatas[10];
                int dwStreamTpye = spgProtocol.mReceiveDatas[11];
                int shootingTime = spgProtocol.mReceiveDatas[12];
                //TODO 下发数据参数应用到配置参数，可能需要转换类型
                hikVisionUtils.onCaptureVideo(4, lChannel, dwStreamTpye, 0, 0, 1, 0, filePath, fileNames, shootingTime * 1000);
                spgProtocol.uploadFile(filePath, fileNames);
                break;
            case SPGProtocol.ORDER_94H:
                break;
            case SPGProtocol.ORDER_95H:
                break;
            case SPGProtocol.ORDER_96H:
                break;
            case SPGProtocol.ORDER_97H:
                break;
        }
    }

    //定时发送心跳包
    public Runnable TheHeartbeatPackets = new Runnable() {
        @Override
        public void run() {
            byte[] time = HikVisionUtils.getInstance().getNetDvrTimeByte();
            spgProtocol.terminalHeartBeatInfo(time, (byte) 0x64, (byte) 0x44);
        }
    };

    //定时发送心跳包
    public Runnable TheHeartbeatPacketss = new Runnable() {
        @Override
        public void run() {
            byte[] time = HikVisionUtils.getInstance().getNetDvrTimeByte();
            spgProtocol.terminalHeartBeatInfo(time, (byte) 0x64, (byte) 0x44);
            mHanlder.postDelayed(TheHeartbeatPacketss, 1000);
        }
    };


    //主动校时线程
    public Runnable WhenTheSchool = new Runnable() {
        public void run() {

            mHanlder.postDelayed(this, 5000);
        }
    };

    //终端休眠通知
    public Runnable NotificationsDormancy = new Runnable() {
        @Override
        public void run() {
            spgProtocol.terminalSleepNotification();
        }
    };

    @Override
    public void onErrMsg(int message) {

        switch (message) {
            case SPGProtocol.ERR_ORDER_00H:
                if (spgProtocol.mReceiveData == null) {
                    //若无接收到服务器返回的信息。延迟2分钟,再次执行发送-开机-请求直到接收到服务器返回值,
                    mHanlder.postDelayed(boot, 1000);
                    Log.i(TAG, "服务器没有返回信息");
                }
                break;
            case SPGProtocol.ERR_ORDER_01H:
                //若无接受到服务器返回的信息，延迟 分钟，再次执行-校时-请求直到接收到服务器的返回值

                mHanlder.postDelayed(WhenTheSchool, 1000);
                break;
            case SPGProtocol.ERR_ORDER_02H:
                break;
            case SPGProtocol.ERR_ORDER_03H:
                break;
            case SPGProtocol.ERR_ORDER_04H:
                break;
            case SPGProtocol.ERR_ORDER_05H:
                //若无接受到服务器返回的信息，延迟 分钟，再次执行-心跳包-请求直到接收到服务器的返回值
                mHanlder.postDelayed(TheHeartbeatPackets, 1000);
                break;
            case SPGProtocol.ERR_ORDER_06H:
                break;
            case SPGProtocol.ERR_ORDER_07H:
                break;
            case SPGProtocol.ERR_ORDER_08H:

                boolean trReturn;
                trReturn = hikVisionUtils.terminalReduction(PTZCommand.GOTO_PRESET, 1);
                if (!trReturn) {
                    int LastError = hikVisionUtils.GetLastError();
                    Log.d("LastError:", String.valueOf(LastError));
                }
                break;
            case SPGProtocol.ERR_ORDER_09H:
                break;
            case SPGProtocol.ERR_ORDER_0AH:
                break;
            case SPGProtocol.ERR_ORDER_0BH:
                break;
            case SPGProtocol.ERR_ORDER_0CH:
                break;
            case SPGProtocol.ERR_ORDER_0DH:
                break;
            case SPGProtocol.ERR_ORDER_21H:
                break;
            case SPGProtocol.ERR_ORDER_30H:
                break;
            case SPGProtocol.ERR_ORDER_71H:
                break;
            case SPGProtocol.ERR_ORDER_72H:
                break;
            case SPGProtocol.ERR_ORDER_73H:
                break;
            case SPGProtocol.ERR_ORDER_74H:
                break;
            case SPGProtocol.ERR_ORDER_75H:
                break;
            case SPGProtocol.ERR_ORDER_76H:
                break;
            case SPGProtocol.ERR_ORDER_81H:
                break;
            case SPGProtocol.ERR_ORDER_82H:
                break;
            case SPGProtocol.ERR_ORDER_83H:
                break;
            case SPGProtocol.ERR_ORDER_84H:
                break;
            case SPGProtocol.ERR_ORDER_85H:
                break;
            case SPGProtocol.ERR_ORDER_86H:
                break;
            case SPGProtocol.ERR_ORDER_87H:
                break;
            case SPGProtocol.ERR_ORDER_88H:
                break;
            case SPGProtocol.ERR_ORDER_89H:
                break;
            case SPGProtocol.ERR_ORDER_8AH:
                break;
            case SPGProtocol.ERR_ORDER_8BH:
                break;
            case SPGProtocol.ERR_ORDER_93H:
                break;
            case SPGProtocol.ERR_ORDER_94H:
                break;
            case SPGProtocol.ERR_ORDER_95H:
                break;
            case SPGProtocol.ERR_ORDER_96H:
                break;
            case SPGProtocol.ERR_ORDER_97H:
                break;
        }
    }

    @Override
    public void remoteAdjustmentCamera(int channelNum, int order, int preposition) {
        switch (order) {
            case 1://打开摄像机电源
                break;
            case 2://摄像机调节到指定预置点
                boolean is = hikVisionUtils.terminalReduction(PTZPresetCmd.GOTO_PRESET, preposition);
                if (!is) {
                    Log.e(TAG, "remoteAdjustmentCamera: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                }
                break;
            case 3://向上调节1个单位(一个单位指角度值域的1%)
                hikVisionUtils.onPTZControl(PTZCommand.TILT_UP);
                break;
            case 4://向下调节1个单位
                hikVisionUtils.onPTZControl(PTZCommand.TILT_DOWN);
                break;
            case 5: //向左调节1个单位
                hikVisionUtils.onPTZControl(PTZCommand.PAN_LEFT);
                break;
            case 6://向右调节1个单位
                hikVisionUtils.onPTZControl(PTZCommand.PAN_RIGHT);
                break;
            case 7://焦距向远方调节1个单位
                hikVisionUtils.onPTZControl(PTZCommand.ZOOM_IN);
                break;
            case 8://焦距向近处调节1个单位
                hikVisionUtils.onPTZControl(PTZCommand.ZOOM_OUT);
                break;
            case 9://保存当前位置为谋预置点
                hikVisionUtils.terminalReduction(PTZPresetCmd.SET_PRESET, preposition);
                break;
            case 10:// 关闭摄像机电源
                break;
        }
    }

    @Override
    public void useChannelNumOne(int colorSelection, int imageSize, int brightness, int contrast, int saturation) {
        //上传图片
        String fileName = "picture.jpg";
        Boolean isSuccess = hikVisionUtils.onCaptureJPEGPicture(filePath, fileName, imageSize);
        if (!isSuccess) {
            HCNetSDK.getInstance().NET_DVR_GetLastError();
            Log.e(TAG, "receiveSuccess: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        spgProtocol.uploadFile(filePath, fileName);
    }

    @Override
    public void useChannelNumTwo(int colorSelection, int imageSize, int brightness, int contrast, int saturation) {

    }

    @Override
    public boolean setDvrTime(int dwYear, int dwMonth, int dwDay, int dwHour, int dwMinute, int dwSecond) {
        return hikVisionUtils.setDateTime(dwYear, dwMonth, dwDay, dwHour, dwMinute, dwSecond);
    }

    @Override
    public String getDvrTime() {
        return hikVisionUtils.getNetDvrTime().ToString();
    }


    @Override
    public void setPreset(int preset) {
        hikVisionUtils.terminalReduction(PTZPresetCmd.GOTO_PRESET, preset);
    }


    private RealPlayCallBack getRealPlayerCbf() {
        RealPlayCallBack cbf = new RealPlayCallBack() {
            public void fRealDataCallBack(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize) {
                // 播放通道1
//                processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_REALTIME);
                Player.getInstance().openStream(-1, pDataBuffer, iDataSize, 2 * 1024 * 1024);
                spgProtocol.setOrder(SPGProtocol.ORDER_89H);
                byte[] buf = new byte[400];
                ByteArrayInputStream inputStream = new ByteArrayInputStream(pDataBuffer);
                try {
                    inputStream.read(buf);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                SystemClock.sleep(1000);
                spgProtocol.dataDomain = buf;
                spgProtocol.sendPack();
            }
        };
        return cbf;
    }


//    private void initView() {
//        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
//                Log.i(TAG, "surface is created" + m_iPort);
//                if (-1 == m_iPort) {
//                    return;
//                }
//                Surface surface = holder.getSurface();
//                if (true == surface.isValid()) {
//                    if (false == Player.getInstance().setVideoWindow(m_iPort, 0, holder)) {
//                        Log.e(TAG, "播放器设置或销毁显示区域失败!");
//                    }
//                }
//            }

//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                Log.i(TAG, "Player setVideoWindow release!" + m_iPort);
//                if (-1 == m_iPort) {
//                    return;
//                }
//                if (true == holder.getSurface().isValid()) {
//                    if (false == Player.getInstance().setVideoWindow(m_iPort, 0, null)) {
//                        Log.e(TAG, "播放器设置或销毁显示区域失败!");
//                    }
//                }
//            }
//        });
//    }
//
//    public void processRealData(int iPlayViewNo, int iDataType, byte[] pDataBuffer, int iDataSize, int iStreamMode) {
//        if (HCNetSDK.NET_DVR_SYSHEAD == iDataType) {
//            if (m_iPort >= 0) {
//                return;
//            }
//            m_iPort = Player.getInstance().getPort();
//            if (m_iPort == -1) {
//                Log.e(TAG, "获取端口失败！: " + Player.getInstance().getLastError(m_iPort));
//                return;
//            }
//            Log.i(TAG, "获取端口成功！: " + m_iPort);
//            if (iDataSize > 0) {
//                if (!Player.getInstance().setStreamOpenMode(m_iPort, iStreamMode))  //set stream mode
//                {
//                    Log.e(TAG, "设置流播放模式失败！");
//                    return;
//                }
//                if (!Player.getInstance().openStream(m_iPort, pDataBuffer, iDataSize, 2 * 1024 * 1024)) //open stream
//                {
//                    Log.e(TAG, "打开流失败！");
//                    return;
//                }
//                if (!Player.getInstance().play(m_iPort, surfaceView.getHolder())) {
//                    Log.e(TAG, "播放失败！");
//                    return;
//                }
//                if (!Player.getInstance().playSound(m_iPort)) {
//                    Log.e(TAG, "以独占方式播放音频失败！失败码 :" + Player.getInstance().getLastError(m_iPort));
//                    return;
//                }
//            }
//        } else {
//            if (!Player.getInstance().inputData(m_iPort, pDataBuffer, iDataSize)) {
////		    		Log.e(TAG, "inputData failed with: " + Player.getInstance().getLastError(m_iPort));
//                for (int i = 0; i < 4000 && -1 >= 0; i++) {
//                    if (!Player.getInstance().inputData(m_iPort, pDataBuffer, iDataSize))
//                        Log.e(TAG, "输入流数据失败: " + Player.getInstance().getLastError(m_iPort));
//                    else
//                        break;
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//
//                    }
//                }
//            }
//
//        }
//
//    }
}
