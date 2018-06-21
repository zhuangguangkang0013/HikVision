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

public class MainActivity extends AppCompatActivity implements UdpListenerCallBack {
    private HikVisionUtils hikVisionUtils;
    private String TAG = MainActivity.class.getSimpleName();
    private int m_iLogId;
    private SPGProtocol spgProtocol;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
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
        String password = "admin12345";
        m_iLogId = hikVisionUtils.loginNormalDevice(address, port, user, password);

        if (m_iLogId < 0) {
            Log.e(TAG, "This device login failed!");
            return;
        } else {
            Log.i(TAG, "m_iLogID=" + m_iLogId);
        }

        spgProtocol = new SPGProtocol(this);
        spgProtocol.InitUdp("171.221.207.59", 17116, "123456");
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

    public Handler mHanlder = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            spgProtocol.setOrder(SPGProtocol.ORDER_00H);
            spgProtocol.PowerOn();
            spgProtocol.receive();
            //若无接收到服务器返回的信息。延迟5秒,再次执行发送开机请求直到接受到服务器返回值,
            mHanlder.postDelayed((Runnable) this, 120 * 1000);
            Log.i(TAG, "服务器没有返回信息");
            mHanlder.postDelayed(task, 2000);  //启动。后面的数字是延时多久执行
        }
    };

    @Override
    public void sendSuccess() {
    }

    @Override
    public void receiveSuccess(byte order) {

        //成功后停止定时循环发送开机请求
        if (order == SPGProtocol.ORDER_00H){
            mHanlder.removeCallbacks(boot);
            Log.i(TAG, "服务器返回了信息停止向服务器发送开机请求");
            mHanlder.postDelayed(WhenTheSchool,1000);
        }else if (order == SPGProtocol.ORDER_01H){
            mHanlder.removeCallbacks(WhenTheSchool);
            mHanlder.postDelayed(TheHeartbeatPackets,2*60000);
        }else if(order == SPGProtocol.ORDER_05H){
            mHanlder.removeCallbacks(TheHeartbeatPackets);
            mHanlder.postDelayed(TheHeartbeatPacketss,0);
            Boolean is = hikVisionUtils.onCaptureJPEGPicture();
            if (!is) {
                HCNetSDK.getInstance().NET_DVR_GetLastError();
                Log.e(TAG, "receiveSuccess: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
            }

            spgProtocol.setOrder(SPGProtocol.ORDER_85H);
            spgProtocol.PowerOn();
        }else if (order == SPGProtocol.ORDER_86H) {

        }
    }

    //心跳包线程
    public Runnable TheHeartbeatPackets = new Runnable() {
        @Override
        public void run() {
            hikVisionUtils.getNetDvrTime();
            spgProtocol.setOrder(SPGProtocol.ORDER_05H);
            spgProtocol.PowerOn();
            spgProtocol.receive();
        }
    };

    //定时发送心跳包
    public Runnable TheHeartbeatPacketss = new Runnable() {
        @Override
        public void run() {
            spgProtocol.setOrder(SPGProtocol.ORDER_05H);
            spgProtocol.PowerOn();
            mHanlder.postDelayed(TheHeartbeatPacketss,1000);
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

    @Override
    public void onErrMsg(int message) {
        if(message==SPGProtocol.ERR_ORDER_00H){
            //若无接收到服务器返回的信息。延迟2分钟,再次执行发送开机请求直到接收到服务器返回值,
            mHanlder.postDelayed((Runnable) this, 1000);
            Log.i(TAG, "服务器没有返回信息");
        }else if(message==SPGProtocol.ERR_ORDER_01H){
            //若无接受到服务器返回的信息，延迟 分钟，再次执行校时请求直到接收到服务器的返回值
            mHanlder.postDelayed(WhenTheSchool, 1000);
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
