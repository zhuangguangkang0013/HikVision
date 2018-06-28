package hikvision.zhanyun.com.hikvision;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.PTZCommand;

import java.io.File;
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
    private String http = "171.221.207.59";
    private int httpPort = 17116;
    //    private String http = "10.18.67.225";
//    private int httpPort = 8989;
    private String cardNumber = "ZJ0001";
    public Handler mHanlder = new Handler();
    //心跳包间隔
    private int TheHeartbeatPacketsTime = 60;//秒钟
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
    private String filePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "HikVisionPicture/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
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
        spgProtocol = new SPGProtocol(this);
        spgProtocol.InitUdp(http, httpPort, cardNumber);

        int a;
        int b;
        int c;
        int d;
        //先找到IP地址字符串中.的位置
        int position1 = http.indexOf(".");
        int position2 = http.indexOf(".", position1 + 1);
        int position3 = http.indexOf(".", position2 + 1);
        //将每个.之间的字符串转换成整型
        a = Integer.parseInt(http.substring(0, position1));
        b = Integer.parseInt(http.substring(position1 + 1, position2));
        c = Integer.parseInt(http.substring(position2 + 1, position3));
        d = Integer.parseInt(http.substring(position3 + 1));

        Log.i(TAG, "onCreate: " + a);
        Log.i(TAG, "onCreate: " + b);
        Log.i(TAG, "onCreate: " + c);
        Log.i(TAG, "onCreate: " + d);
        mHanlder.postDelayed(boot, 0);
//        spgProtocol.uploadFile(filePath+"picture.jpg");
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
                trReturn = hikVisionUtils.terminalReduction(3, PTZCommand.GOTO_PRESET, 1);
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
                //成功后停止定时循环发送开机请求
//                mHanlder.removeCallbacks(boot);
//                Log.i(TAG, "服务器返回了信息停止向服务器发送开机请求");
//                //开启校时功能
//                mHanlder.postDelayed(WhenTheSchool, 5000);
                spgProtocol.schoolTime();
                break;
            case SPGProtocol.ORDER_01H:
//                //校时成功后停止校时功能
//                mHanlder.removeCallbacks(WhenTheSchool);
//                //开启心跳包
//                mHanlder.postDelayed(TheHeartbeatPackets, 1000);
                break;
            case SPGProtocol.ORDER_02H:
                if (spgProtocol.oldPassword.equals(password)) {
                    password = spgProtocol.newPassword;
                    spgProtocol.judge = true;
                    spgProtocol.setTerminalPassword(true);

                } else {
                    spgProtocol.judge = false;
                    spgProtocol.setTerminalPassword(false);
                }
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
                        httpPort = c * 256 + d;
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
                //端口
                int mainVersion = (httpPort & 0xFF00) >> 8;
                int minorVersion = httpPort & 0xFF;
                byte[] portNumber = {(byte) (mainVersion * 256 + minorVersion)};
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
                        , portNumber, new byte[]{Byte.parseByte(cardNumber)});
                break;
            case SPGProtocol.ORDER_08H:
//                spgProtocol.PowerOn();
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
                hikVisionUtils.terminalReduction(spgProtocol.getChannelNum(), PTZCommand.GOTO_PRESET, spgProtocol.getPreset());

                //上传图片
                Boolean isSuccess = hikVisionUtils.onCaptureJPEGPicture(filePath, spgProtocol.getImageSizeOne());
                if (!isSuccess) {
                    HCNetSDK.getInstance().NET_DVR_GetLastError();
                    Log.e(TAG, "receiveSuccess: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                    return;
                }


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
                trReturn = hikVisionUtils.terminalReduction(3, PTZCommand.GOTO_PRESET, 1);
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
    public List<byte[]> getFileData() {
        return fileData;
    }

    @Override
    public int getPackIndex() {
        return packIndex;
    }
}
