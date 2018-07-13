package hikvision.zhanyun.com.hikvision;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.PTZCommand;
import com.hikvision.netsdk.PTZCruiseCmd;
import com.hikvision.netsdk.PTZPresetCmd;
import com.hikvision.netsdk.RealPlayCallBack;

import org.MediaPlayer.PlayM4.Player;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class MainActivity extends AppCompatActivity implements UdpListenerCallBack {
    private HikVisionUtils hikVisionUtils;
    private static String TAG = MainActivity.class.getSimpleName();
    private int m_iLogId;
    private SPGProtocol spgProtocol;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int CAMERA_REQUEST_CODE = 100;
    private byte[] simNumber = {(byte) 0xF1, 0x39, 0x12, 0x34, 0x56, 0x78};

    private String cardNumber;
    private String http;
    private int httpPort;
    //    private String http = "10.18.67.225";
    //    private String http = "171.221.207.59";
//    private int httpPort = 17116;
    //private String http = "192.168.200.11";
    int acb;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    public static String filePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "HikVisionData/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keepScreenLongLight(this);
        verifyStoragePermissions(this);

        String apkRoot = "chmod 777 " + getPackageCodePath();
        RootCommand(apkRoot);
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            Map<String, String> readFile = readFile();

            hikVisionUtils = HikVisionUtils.getInstance();
            hikVisionUtils.initSDK();
            cardNumber = readFile.get("card_number");
            http = readFile.get("server_ip");
            httpPort = Integer.parseInt(readFile.get("server_port"));
            String address = readFile.get("camera_ip");
            int port = Integer.parseInt(readFile.get("camera_port"));
            String user = readFile.get("camera_user");
            String password = readFile.get("camera_password");
            Log.e(TAG, "server_ip: " + http);
            m_iLogId = hikVisionUtils.loginNormalDevice(address, port, user, password);
            int count = 0;
            if (m_iLogId < 0) {
                while (true) {
                    SystemClock.sleep(1000);
                    count++;
                    if (count == 5) {
                        Toast.makeText(MainActivity.this, "摄像头设备登录失败", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (m_iLogId >= 0) {
                        initData();
                        break;
                    }
                    m_iLogId = hikVisionUtils.loginNormalDevice(address, port, user, password);
                }
            } else {
                initData();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Err:配置文件错误", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


//
//        int port = 8000;
//        String user = "admin";
//        m_iLogId = hikVisionUtils.loginNormalDevice(address, port, user, password);

//        if (m_iLogId < 0) {
//            return;
////        }
//        spgProtocol = new SPGProtocol(this, this);
//        spgProtocol.InitUdp(http, httpPort, cardNumber, simNumber);
//        initView();
    }

    private void initData() {
        if (http != null && httpPort != 0) {
            spgProtocol = new SPGProtocol(MainActivity.this, MainActivity.this);
            spgProtocol.InitUdp(http, httpPort, cardNumber, simNumber);
        }

    }

    public Map<String, String> readFile() throws Exception {
        //读取指定目录下的指定文件
        InputStream in = new FileInputStream(new File(filePath + "config.ini"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        Properties props = new Properties();
        props.load(br);
        Map<String, String> map = new HashMap<>();
        for (Object s : props.keySet()) {
            map.put(s.toString(), props.getProperty(s.toString()));
        }
        return map;
    }

    /**
     * 使屏幕常亮
     *
     * @param activity
     */
    public static void keepScreenLongLight(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 动态加载读写权限
     *
     * @param activity
     */
    public void verifyStoragePermissions(Activity activity) {

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
            int cameraPermission = ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.CAMERA);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
    public void onErrMsg(int message) {
        switch (message) {
            case SPGProtocol.ERR_ORDER_00H:
                break;
            case SPGProtocol.ERR_ORDER_01H:
                break;
            case SPGProtocol.ERR_ORDER_02H:
                break;
            case SPGProtocol.ERR_ORDER_03H:
                break;
            case SPGProtocol.ERR_ORDER_04H:
                break;
            case SPGProtocol.ERR_ORDER_05H:
                break;
            case SPGProtocol.ERR_ORDER_06H:
                break;
            case SPGProtocol.ERR_ORDER_07H:
                break;
            case SPGProtocol.ERR_ORDER_08H:
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
                hikVisionUtils.onPTZControl(PTZCommand.ZOOM_IN);
                break;
            case 7://焦距向远方调节1个单位
                hikVisionUtils.onPTZControl(PTZCommand.ZOOM_OUT);
                break;
            case 8://焦距向近处调节1个单位
                hikVisionUtils.onPTZControl(PTZCommand.FOCUS_FAR);
                break;
            case 9://保存当前位置为谋预置点
                hikVisionUtils.terminalReduction(PTZPresetCmd.SET_PRESET, preposition);
                break;
            case 10:// 关闭摄像机电源
                break;
            case 11://光圈放大1个单位
                hikVisionUtils.onPTZControl(PTZCommand.IRIS_OPEN);
                break;
            case 12://光圈缩小1个单位
                hikVisionUtils.onPTZControl(PTZCommand.IRIS_CLOSE);
                break;
            case 13://镜头变陪放大1倍
                break;
            case 14://镜头变倍缩小1倍
                break;
            case 15://开始巡航
                hikVisionUtils.onPZTCruise(PTZCruiseCmd.STOP_SEQ, (byte) 1, (byte) preposition, (short) 1);
                break;
            case 16://停止巡航
                hikVisionUtils.onPZTCruise(PTZCruiseCmd.STOP_SEQ, (byte) 1, (byte) preposition, (short) 1);
                break;
            case 17://打开辅助开关
                break;
            case 18://关闭辅助开关
                break;
            case 19://开始自动扫描
                break;
            case 20://停止自动扫描
                break;
            case 21://开始随机扫描
                break;
            case 22://停止随机扫描
                break;
            case 23://红外线灯全开
                break;
            case 24://红外线半开
                break;
            case 25://红外线关闭
                break;
        }
    }

    @Override
    public void useChannelNumOne(int colorSelection, int imageSize, int brightness, int contrast, int saturation) {
        //上传图片;
        String fileName = getFileNameCriterion(1, "jpg");
        Boolean isSuccess = hikVisionUtils.onCaptureJPEGPicture(filePath, fileName, imageSize);
        if (!isSuccess) {
            HCNetSDK.getInstance().NET_DVR_GetLastError();
            Log.e(TAG, "receiveSuccess: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        spgProtocol.uploadFile(filePath, fileName);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void useChannelNumTwo(int colorSelection, int imageSize, int brightness, int contrast, int saturation) {
        Intent intent = new Intent();
        intent.putExtra("Picture", 1);
        intent.setClass(MainActivity.this, PhonePitureActivity.class);
        startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);

//        spgProtocol.uploadFile(filePath, fileName);
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

    @Override
    public void modifyTheHostIPPortNumbers(String http, int port, byte[] simNumber) {
        this.http = http;
        this.httpPort = port;
        this.simNumber = simNumber;
        //更改后重新初始化地址端口卡号
        spgProtocol.InitUdp(http, port, cardNumber, simNumber);

        Log.i(TAG, "ModifyTheHostIPPortNumbers: " + this.http);
        Log.i(TAG, "ModifyTheHostIPPortNumbers: " + this.httpPort);
        Log.i(TAG, "ModifyTheHostIPPortNumbers: " + this.simNumber);
    }

    @Override
    public void startVideo() {
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

    }

    @Override
    public void stopVideo() {
        HCNetSDK.getInstance().NET_DVR_StopSaveRealData(acb);
        HCNetSDK.getInstance().NET_DVR_StopRealPlay(acb);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void startShortVideo(int channel, int type, int time) {
        spgProtocol.setOrder(SPGProtocol.ORDER_93H);
        spgProtocol.sendPack();

        if (channel == 1) {
            String fileNames = getFileNameCriterion(1, "mp4");
            //TODO 下发数据参数应用到配置参数，可能需要转换类型
            hikVisionUtils.onCaptureVideo(4, channel, type, 0,
                    0, 1, 0, filePath, fileNames,
                    time * 1000);
            spgProtocol.uploadFile(filePath, fileNames);
        } else if (channel == 2) {
            Intent intent = new Intent();
            intent.putExtra("Video", 2);
            intent.putExtra("Time", time);
            intent.setClass(MainActivity.this, PhonePitureActivity.class);
            startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
        }
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


    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @param command 命令：String apkRoot="chmod 777 "+getPackageCodePath(); RootCommand(apkRoot);
     * @return 应用程序是/否获取Root权限
     */

    private void RootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
            return;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        Log.d("*** DEBUG ***", "Root SUC ");
    }

    /**
     * 文件命名规范
     *
     * @param channelNum 通道号
     * @param type       文件类型
     * @return
     */
    private String getFileNameCriterion(int channelNum, String type) {
        String different = null;
        if (channelNum == 1) {
            different = "A";
        } else if (channelNum == 2) {
            different = "B";
        }
        Date date = new Date(hikVisionUtils.getNetDvrTime().ToString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = simpleDateFormat.format(date);

        return cardNumber + "_" + different + "_" + "01" + "_" + time + "." + type;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 5) {
            spgProtocol.uploadFile(filePath, PhonePitureActivity.fileName);
        } else if (requestCode == 4) {
            spgProtocol.uploadFile(filePath, PhonePitureActivity.fileNames);
        }
    }
}
