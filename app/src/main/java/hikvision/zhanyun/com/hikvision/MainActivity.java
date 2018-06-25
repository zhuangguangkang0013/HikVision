package hikvision.zhanyun.com.hikvision;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hikvision.netsdk.HCNetSDK;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements UdpListenerCallBack {
    private HikVisionUtils hikVisionUtils;
    private static String TAG = MainActivity.class.getSimpleName();
    private int m_iLogId;
    private SPGProtocol spgProtocol;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String password = "admin12345";
    private String http = "171.221.207.59";
    private int httpPort = 17116;
    private String cardNumber = "123456";
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
        b = Integer.parseInt(http.substring(position1+1, position2));
        c = Integer.parseInt(http.substring(position2+1, position3));
        d = Integer.parseInt(http.substring(position3+1));

        Log.i(TAG, "onCreate: "+a);
        Log.i(TAG, "onCreate: "+b);
        Log.i(TAG, "onCreate: "+c);
        Log.i(TAG, "onCreate: "+d);

        mHanlder.postDelayed(boot, 0);


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
            spgProtocol.setOrder(SPGProtocol.ORDER_00H);
            spgProtocol.PowerOn();
            spgProtocol.receive();


        }
    };


    @Override
    public void sendSuccess(byte order) {
        if (order==SPGProtocol.ORDER_08H){
            boolean trReturn;
            trReturn=hikVisionUtils.TerminalReduction(m_iLogId);
            if(!trReturn){
                int LastError=hikVisionUtils.GetLastError();
                Log.d("LastError:", String.valueOf(LastError));
            }
        }
    }

    @Override
    public void receiveSuccess(byte order) {
        if (order == SPGProtocol.ORDER_00H) {
            //成功后停止定时循环发送开机请求
            mHanlder.removeCallbacks(boot);
            Log.i(TAG, "服务器返回了信息停止向服务器发送开机请求");
            //开启校时功能
            mHanlder.postDelayed(WhenTheSchool, 1000);
        } else if (order == SPGProtocol.ORDER_01H) {
            //校时成功后停止校时功能
            mHanlder.removeCallbacks(WhenTheSchool);
            //开启心跳包
            mHanlder.postDelayed(TheHeartbeatPackets, 1000);
        } else if (order == SPGProtocol.ORDER_05H) {
            //判断心跳包返回的指令  如果是原指令择每隔两分钟重复发送心跳
            if (spgProtocol.mReceiveData != null && spgProtocol.mReceiveData[7] == SPGProtocol.ORDER_05H) {
                Log.i(TAG, "run: " + spgProtocol.mReceiveData[7]);
                mHanlder.postDelayed(TheHeartbeatPackets, TheHeartbeatPacketsTimes);
            } else {
                //否则关闭心跳包,两分钟后再重新开启定时心跳线程
                mHanlder.removeCallbacks(TheHeartbeatPackets);
                //执行指令
                spgProtocol.setOrder(spgProtocol.mReceiveData[7]);
                spgProtocol.PowerOn();
                mHanlder.postDelayed(TheHeartbeatPackets, TheHeartbeatPacketsTimes);
            }


            //测试拍照
            Boolean is = hikVisionUtils.onCaptureJPEGPicture();
            if (!is) {
                HCNetSDK.getInstance().NET_DVR_GetLastError();
                Log.e(TAG, "receiveSuccess: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
            }
            spgProtocol.setOrder(SPGProtocol.ORDER_85H);
            spgProtocol.PowerOn();



        } else if (order==SPGProtocol.ORDER_08H){
            spgProtocol.PowerOn();
        } else if (order == SPGProtocol.ORDER_86H) {

        } else if (order == SPGProtocol.ORDER_02H) {
            if (spgProtocol.oldPassword.equals(password)) {
                password = spgProtocol.newPassword;
                spgProtocol.setOrder(SPGProtocol.ORDER_02H);
                spgProtocol.PowerOn();
                spgProtocol.judge = true;
            } else {
                spgProtocol.setOrder(SPGProtocol.ORDER_02H);
                spgProtocol.PowerOn();
                spgProtocol.judge = false;
            }

        } else if (order == SPGProtocol.ORDER_03H) {
            if (password.equals(spgProtocol.password)) {
                TheHeartbeatPacketsTime = spgProtocol.HeartbeatInterval;//心跳间隔
                SamplingIntervals = spgProtocol.SamplingInterval;//采样间隔
                TheSleepTime = spgProtocol.TheSleepTime;//休眠时长
                TheOnlineTime = spgProtocol.TheOnlineTime;//在线时长
                HardwareResetTime = spgProtocol.HardwareResetTime;//硬件重启时间点   //重启功能未实现
                CipherCertification = spgProtocol.CipherCertification;//密文认证
                //TODO 处理休眠

            }
        } else if (order == SPGProtocol.ORDER_06H) {
//            int a = 8000;
//            short high_a = (short) ((a & 0xffff0000) >> 16);
//            short low_a = (short) (a & 0xffff);
            if (password.equals(spgProtocol.password)) {
                spgProtocol.judges = true;
                if (password.equals(spgProtocol.password) && spgProtocol.Http.equals(spgProtocol.Https) && Arrays.equals(spgProtocol.port, spgProtocol.ports) && Arrays.equals(spgProtocol.cardNumber, spgProtocol.cardNumbers)) {
                    spgProtocol.setOrder(SPGProtocol.ORDER_06H);
                    spgProtocol.PowerOn();
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
                    httpPort = c * 256 +d;
                    //更改卡号 暂不知道服务器发送的卡号是纯数字还是 F?H 格式   需格式判断或询问清格式
                    int  ChangeTheNumber_a = spgProtocol.mReceiveDatas[26];
                    int  ChangeTheNumber_b = spgProtocol.mReceiveDatas[27];
                    int  ChangeTheNumber_c = spgProtocol.mReceiveDatas[28];
                    int  ChangeTheNumber_d = spgProtocol.mReceiveDatas[29];
                    int  ChangeTheNumber_e = spgProtocol.mReceiveDatas[30];
                    int  ChangeTheNumber_f = spgProtocol.mReceiveDatas[31];
                    cardNumber = String.valueOf(ChangeTheNumber_a +ChangeTheNumber_b+ChangeTheNumber_c+ChangeTheNumber_d+ChangeTheNumber_e+ChangeTheNumber_f);


                } else {
                    spgProtocol.setOrder(SPGProtocol.ORDER_06H);
                    spgProtocol.PowerOn();
                    spgProtocol.judge = false;
                }

            } else {
                spgProtocol.setOrder(SPGProtocol.ORDER_06H);
                spgProtocol.PowerOn();
                spgProtocol.judges = false;
            }
        }else if(order == SPGProtocol.ORDER_07H){
            //端口
            int mainVersion = (httpPort & 0xFF00) >> 8;
             int minorVersion = httpPort & 0xFF;
             byte[] portNumber = {(byte) (mainVersion*256+minorVersion)};
             spgProtocol.queryPort = portNumber;
            //IP
            int a,b,c,d;
            //先找到IP地址字符串中.的位置
            int position1 = http.indexOf(".");
            int position2 = http.indexOf(".", position1 + 1);
            int position3 = http.indexOf(".", position2 + 1);
            //将每个.之间的字符串转换成整型
            a = Integer.parseInt(http.substring(0, position1));
            b = Integer.parseInt(http.substring(position1+1, position2));
            c = Integer.parseInt(http.substring(position2+1, position3));
            d = Integer.parseInt(http.substring(position3+1));
            spgProtocol.ip = new byte[]{(byte) a, (byte) b, (byte) c, (byte) d};
            //卡号
            spgProtocol.cardNumber = new byte[]{Byte.parseByte(cardNumber)};
            //启动
            spgProtocol.setOrder(SPGProtocol.ORDER_07H);
            spgProtocol.PowerOn();
        }

    }

    //定时发送心跳包
    public Runnable TheHeartbeatPackets = new Runnable() {
        @Override
        public void run() {
            spgProtocol.setOrder(SPGProtocol.ORDER_05H);
            spgProtocol.PowerOn();
            spgProtocol.receive();
        }
    };


    //主动校时线程
    public Runnable WhenTheSchool = new Runnable() {
        public void run() {
            spgProtocol.setOrder(SPGProtocol.ORDER_01H);
            spgProtocol.PowerOn();
            spgProtocol.receive();
        }
    };

    //终端休眠通知
    public Runnable NotificationsDormancy =new Runnable() {
        @Override
        public void run() {
            spgProtocol.setOrder(SPGProtocol.ORDER_0CH);
            spgProtocol.PowerOn();
            spgProtocol.receive();
        }
    };
    @Override
    public void onErrMsg(int message) {
        if (message == SPGProtocol.ERR_ORDER_00H && spgProtocol.mReceiveData == null) {
            //若无接收到服务器返回的信息。延迟2分钟,再次执行发送-开机-请求直到接收到服务器返回值,
            mHanlder.postDelayed(boot, 1000);
            Log.i(TAG, "服务器没有返回信息");
        } else if (message == SPGProtocol.ERR_ORDER_01H) {
            //若无接受到服务器返回的信息，延迟 分钟，再次执行-校时-请求直到接收到服务器的返回值
            mHanlder.postDelayed(WhenTheSchool, 1000);
        } else if (message == SPGProtocol.ERR_ORDER_05H) {
            //若无接受到服务器返回的信息，延迟 分钟，再次执行-心跳包-请求直到接收到服务器的返回值
            mHanlder.postDelayed(TheHeartbeatPackets, 1000);
        }
    }



    @Override
    public byte getSignalStrength() {
        return 0x64;
    }

    @Override
    public byte getBatterVoltage() {
        return 0x44;
    }

}
