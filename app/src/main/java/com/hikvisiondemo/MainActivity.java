package ziyouniao.zhanyun.com.hikvisiondemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements UdpListenerCallBack {
    private HikVisionUtils hikVisionUtils;
    private String TAG = MainActivity.class.getSimpleName();
    private int m_iLogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        if (!hikVisionUtils.getExceptionCbf()) {
            Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
            return;
        }
//        UdpUtil.getInstance(this).sendMsg(String.valueOf(m_iLogId));
//        UdpUtil.getInstance(this).setListenReceiveStatus(true);

//        handler.sendEmptyMessage(1);
        SPGProtocol spgProtocol = new SPGProtocol(this);
        spgProtocol.InitUdp("171.221.207.59", 17116,"123456");
        spgProtocol.initFormat(spgProtocol.START_CHAR, spgProtocol.POWERON_CHAR,
                spgProtocol.VERSION, spgProtocol.END_CHAR);
//        spgProtocol.InitUdp("10.18.67.225", 8989, "123456");
        spgProtocol.PowerOn();
        spgProtocol.receive();
    }



    public void btnTest(View view) {

//        spgProtocol.startRunnable();
    }

    @Override
    public void sendSuccess() {
        UdpUtil.getInstance(this).receiveMsg();
    }

    @Override
    public void receiveSuccess(String message) {
        hikVisionUtils.onPTZControl(1, m_iLogId, Integer.parseInt(message));
        UdpUtil.getInstance(this).sendMsg(message);
    }

    @Override
    public void onErrMsg(String message) {
        Log.e(TAG, "onErrMsg: " + message);
    }

    @Override
    protected void onDestroy() {
//        UdpUtil.getInstance(this).closeUpdReceive();
        super.onDestroy();
    }
}
