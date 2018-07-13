package hikvision.zhanyun.com.hikvision;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static android.support.constraint.Constraints.TAG;


/**
 * Created by ZY004Engineer on 2018/6/12.
 */

public class SPGProtocol {

    public static final byte ORDER_00H = (byte) 0x00;
    public static final byte ORDER_01H = (byte) 0x01;
    public static final byte ORDER_02H = (byte) 0x02;
    public static final byte ORDER_03H = (byte) 0x03;
    public static final byte ORDER_04H = (byte) 0x04;
    public static final byte ORDER_05H = (byte) 0x05;
    public static final byte ORDER_06H = (byte) 0x06;
    public static final byte ORDER_07H = (byte) 0x07;
    public static final byte ORDER_08H = (byte) 0x08;
    public static final byte ORDER_09H = (byte) 0x09;
    public static final byte ORDER_0AH = (byte) 0x0A;
    public static final byte ORDER_0BH = (byte) 0x0B;
    public static final byte ORDER_0CH = (byte) 0x0C;
    public static final byte ORDER_0DH = (byte) 0x0D;
    public static final byte ORDER_21H = (byte) 0x21;
    public static final byte ORDER_30H = (byte) 0x30;
    public static final byte ORDER_71H = (byte) 0x71;
    public static final byte ORDER_72H = (byte) 0x72;
    public static final byte ORDER_73H = (byte) 0x73;
    public static final byte ORDER_74H = (byte) 0x74;
    public static final byte ORDER_75H = (byte) 0x75;
    public static final byte ORDER_76H = (byte) 0x76;
    public static final byte ORDER_81H = (byte) 0x81;
    public static final byte ORDER_82H = (byte) 0x82;
    public static final byte ORDER_83H = (byte) 0x83;
    public static final byte ORDER_84H = (byte) 0x84;
    public static final byte ORDER_85H = (byte) 0x85;
    public static final byte ORDER_86H = (byte) 0x86;
    public static final byte ORDER_87H = (byte) 0x87;
    public static final byte ORDER_88H = (byte) 0x88;
    public static final byte ORDER_89H = (byte) 0x89;
    public static final byte ORDER_8AH = (byte) 0x8A;
    public static final byte ORDER_8BH = (byte) 0x8B;
    public static final byte ORDER_93H = (byte) 0x93;
    public static final byte ORDER_94H = (byte) 0x94;
    public static final byte ORDER_95H = (byte) 0x95;
    public static final byte ORDER_96H = (byte) 0x96;
    public static final byte ORDER_97H = (byte) 0x97;

    private byte[] startChar = {0x68};
    private byte[] endChar = {0x16};
    private byte[] controlChar = {0x00};
    private int maxPacketLength = 5000;
    private byte[] version = {0x01, 0x02};


    //开机联络信息 00H
    private final byte[] START_CHAR = {0x68};
    private final byte[] END_CHAR = {0x16};
    private final byte[] VERSION = {0x01, 0x03};

    //校时 01H
    private final byte[] WHEN_THE_SCHOOL_VERSION = {};


    //设置终端密码 02H
    private final byte[] TERMINAL_PWD_VERSION = {(byte) 0xFFFF};
    public byte[] oldPassword;
    public byte[] newPassword;
    public boolean judge;

    //终端心跳信息 05H
    private byte[] signalRecordingTime = new byte[5];
    private byte[] signalStrength = new byte[0];
    private byte[] batteryVoltage = new byte[0];
    //心跳包间隔
    private int TheHeartbeatPacketsTime = 60;//秒钟
    private int TheHeartbeatPacketsTimes = TheHeartbeatPacketsTime * 1000;
    //终端复位 08H
    private final byte[] TERMINAL_RESET = {(byte) 0xFFF};
    private static boolean RIGHT_OR_NOT;

    //终端休眠通知 0CH
    private final byte[] NOTIFICATIONS_DORMANCY = {};
    // ....

    private DatagramSocket socket = null;
    private String Server;
    private int Port;
    private String deviceID;
    private InetSocketAddress addr;

    private UdpListenerCallBack listenerCallBack;

    //判断发送数据与接收数据相同
    private byte[] mSendData;
    public byte[] mReceiveData;
    private byte[] mUpLocalReceiveData;

    public byte order;

    //记录发送时间，接收时间
    private String dateTime;
    private String dateTimes;
    private String simNumber;
    //主站下发参数配置
    public byte[] password;    //密码
    private long contactInterval;   //心跳间隔
    public long samplingInterval;   //采样间隔
    public long theSleepTime;  //休眠时长
    public long theOnlineTime; //在线时长时间点
    public String hardwareResetTime;    //硬件重启
    public String cipherCertification = "1234";    //密文认证

    //更改主站IP
    public byte[] Http;    //主站IP
    public byte[] Https; //主站IP
    public byte[] port; //端口号
    public byte[] ports;    //端口号
    public byte[] cardNumber;    //主站卡号
    public byte[] cardNumbers;    //主站卡号
    public boolean judges;
    //返回密码错误
    private static final byte[] PASSWORD_MISTAKE_VERSION = {(byte) 0xFFFF};
    //返回主站IP端口主站卡号错误
    private static final byte[] HTTP_OR_PORT_CAR_NUMBER = {0x0000};
    //储存接受数据
    public byte[] mReceiveDatas;
    //主站卡号

    //主站查询终端文件列表 71H
    //获取文件属性
    private int filesNumber;//需传输的文件个数N //
    private List<String> fileNames = new ArrayList<String>();// 文件名集合
    private List<Integer> fileLengths = new ArrayList<Integer>();// 文件大小集合
    private List<String> fileTimes = new ArrayList<String>();// 文件生成时间集合
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Calendar calendar = Calendar.getInstance();

    //装置请求上送文件 73H
    private String fileName;
    private int fileLength;
    private String fileTime;

    //文件存储路径
    public String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "HikVisionData/";

    //ErrMsg 信息 标识
    public static final int ERR_SEND_UDP = 1;
    public static final int ERR_SECEIVE_UDP = 2;
    public static final int ERR_ORDER_00H = 0;
    public static final int ERR_ORDER_01H = -1;
    public static final int ERR_ORDER_02H = -2;
    public static final int ERR_ORDER_03H = -3;
    public static final int ERR_ORDER_04H = -4;
    public static final int ERR_ORDER_05H = -5;
    public static final int ERR_ORDER_06H = -6;
    public static final int ERR_ORDER_07H = -7;
    public static final int ERR_ORDER_08H = -8;
    public static final int ERR_ORDER_09H = -9;
    public static final int ERR_ORDER_0AH = -10;
    public static final int ERR_ORDER_0BH = -11;
    public static final int ERR_ORDER_0CH = -12;
    public static final int ERR_ORDER_0DH = -13;
    public static final int ERR_ORDER_21H = -21;
    public static final int ERR_ORDER_30H = -30;
    public static final int ERR_ORDER_71H = -71;
    public static final int ERR_ORDER_72H = -72;
    public static final int ERR_ORDER_73H = -73;
    public static final int ERR_ORDER_74H = -74;
    public static final int ERR_ORDER_75H = -75;
    public static final int ERR_ORDER_76H = -76;
    public static final int ERR_ORDER_81H = -81;
    public static final int ERR_ORDER_82H = -82;
    public static final int ERR_ORDER_83H = -83;
    public static final int ERR_ORDER_84H = -84;
    public static final int ERR_ORDER_85H = -85;
    public static final int ERR_ORDER_86H = -86;
    public static final int ERR_ORDER_87H = -87;
    public static final int ERR_ORDER_88H = -88;
    public static final int ERR_ORDER_89H = -89;
    public static final int ERR_ORDER_8AH = -811;
    public static final int ERR_ORDER_8BH = -812;
    public static final int ERR_ORDER_93H = -93;
    public static final int ERR_ORDER_94H = -94;
    public static final int ERR_ORDER_95H = -95;
    public static final int ERR_ORDER_96H = -96;
    public static final int ERR_ORDER_97H = -97;


    private boolean isUpLocal = false;//保证单一上传图片
    private final int UPLOAD_IMAGE_PACK_DIVISOR = 256;
    private final int MAX_UPLOAD_IMAGE_SIZE = 400;
    private final int MAX_UPLOAD_FILE_SIZE = 850;
    private byte[] originalCommandData;
    private byte channelNum = 0;//通道号
    private byte preset = 0;//预置点
    private int colorSelectionOne = -1;
    private int imageSizeOne = 0;
    private int brightnessOne = 0;
    private int contrastOne = 0;
    private int saturationOne = 0;
    private int colorSelectionTwo = -1;
    private int imageSizeTwo = 0;
    private int brightnessTwo = 0;
    private int contrastTwo = 0;
    private int saturationTwo = 0;
    private Thread sendThread;
    public byte[] dataDomain;//数据域

    private File pictureFile;
    private final static long ONE_MINUTE = 60 * 1000;
    private final static long TWO_MINUTE = 2 * 60 * 1000;
    private String terminalPassword;
    private int[] timeArray;
    private int countLoop;
    private int[] presetGroup;

    private Context context;
    private SharedPreferences sharedPreferences;
    //更改主站IP地址、端口号和卡号
    private static final String SP_PACK_NAME = "StatutePack";
    private static final String SP_SIM_NUMB = "simNumber";
    private static final String SP_PASSWORD = "password";
    private static final String SP_SERVER = "http";
    private static final String SP_PORT = "port";
    //参数配置
    private static final String SP_CONTACT_INTERVAL = "contactInterval";
    private static final String SP_SAMPLING_INTERVAL = "samplingInterval";
    private static final String SP_SLEEP_TIME = "sleepTime";
    private static final String SP_ONLINE_TIME = "onlineTime";
    private static final String SP_HARDWARE_RESTART_TIME_POINT = "hardwareRestartTimePoint";
    private static final String SP_CIPHER_VERIFICATION_CODE = "cipherVerificationCode";
    //图像采集参数配置
    //通道号1
    private static final String SP_COLOR_SELECT_ONE = "colorSelectionOne";
    private static final String SP_IMAGE_SIZE_ONE = "imageSizeOne";
    private static final String SP_BRIGHTNESS_ONE = "brightnessOne";
    private static final String SP_CONTRAST_ONE = "contrastOne";
    private static final String SP_SATURATION_ONE = "saturationOne";
    //通道号2
    private static final String SP_COLOR_SELECT_TWO = "colorSelectionTwo";
    private static final String SP_IMAGE_SIZE_TWO = "imageSizeTwo";
    private static final String SP_BRIGHTNESS_TWO = "brightnessTwo";
    private static final String SP_CONTRAST_TWO = "contrastTwo";
    private static final String SP_SATURATION_TWO = "saturationTwo";
    //拍照时间表
    private static final String SP_PHOTO_TIME_TABLE = "photoTimeTable";

    private Timer upLoadFileTimer;

    private Handler mHandler = new Handler();
    private String requestTime;
    private String responseTime;
    private int repeatCountLoop;
    private String scheduleData;
    private byte[] fileNameByteData;


    public void setOrder(byte order) {
        this.order = order;
        controlChar = new byte[]{order};
    }

    /**
     * @return 返回所需要的数据
     */
    private byte[] result() {
        if (originalCommandData != null) return originalCommandData;

        // 数据存储区
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream buf_stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        try {

            out.write(deviceID.getBytes());
            out.write(controlChar);
            out.writeShort(dataDomain.length);
            out.write(dataDomain);
            byte[] checkCode = {Crc(bos.toByteArray())};

            buf_stream.write(START_CHAR);
            buf_stream.write(bos.toByteArray());
            buf_stream.write(checkCode);
            buf_stream.write(END_CHAR);

            buf_stream.close();
            out.close();
            bos.close();

            return buf_stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置回调
     *
     * @param listenerCallBack 回调接口
     */
    public SPGProtocol(UdpListenerCallBack listenerCallBack, Context context) {
        this.listenerCallBack = listenerCallBack;
        this.context = context;
    }

    /**
     * 初始化udp
     *
     * @param id        终端号码
     * @param host      服务器地址
     * @param port      端口
     * @param simNumber 卡号
     */
    public void InitUdp(final String host, final int port, String id, byte[] simNumber) {
        this.deviceID = id;

        sharedPreferences = context.getSharedPreferences(SP_PACK_NAME, MODE_PRIVATE);

        //初始化端口

        this.Server = sharedPreferences.getString(SP_SERVER, host);
        this.Port = sharedPreferences.getInt(SP_PORT, port);
        this.simNumber = simNumber != null ? sharedPreferences.getString(SP_SIM_NUMB, byteArrayToHexStr(simNumber)) : "";
        initConfigParam();
        Log.e(TAG, "InitUdp: " + Arrays.toString(hexStrToByteArray(this.simNumber)));

        try {
            if (socket != null) socket.close();
            socket = new DatagramSocket();
            addr = new InetSocketAddress(Server, Port);
            receive();

            mHandler.postDelayed(boot, 0);
            mHandler.postDelayed(TheHeartbeatPackets, 10);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化配置参数
     */
    private void initConfigParam() {
        //更改密码
        this.terminalPassword = sharedPreferences.getString(SP_PASSWORD, "1234");
        //初始参数配置
        this.contactInterval = sharedPreferences.getLong(SP_CONTACT_INTERVAL, ONE_MINUTE);
        this.samplingInterval = sharedPreferences.getLong(SP_SAMPLING_INTERVAL, 20 * ONE_MINUTE);
        this.theSleepTime = sharedPreferences.getLong(SP_SLEEP_TIME, 0);
        this.theOnlineTime = sharedPreferences.getLong(SP_ONLINE_TIME, 24 * ONE_MINUTE);
        this.hardwareResetTime = sharedPreferences.getString(SP_HARDWARE_RESTART_TIME_POINT, "0");
        this.cipherCertification = sharedPreferences.getString(SP_CIPHER_VERIFICATION_CODE, "1234");
        //图像采集参数配置
        this.colorSelectionOne = sharedPreferences.getInt(SP_COLOR_SELECT_ONE, 0);
        this.imageSizeOne = sharedPreferences.getInt(SP_IMAGE_SIZE_ONE, 1);
        this.brightnessOne = sharedPreferences.getInt(SP_BRIGHTNESS_ONE, 50);
        this.contrastOne = sharedPreferences.getInt(SP_CONTRAST_ONE, 50);
        this.saturationOne = sharedPreferences.getInt(SP_SATURATION_ONE, 50);
        this.colorSelectionTwo = sharedPreferences.getInt(SP_COLOR_SELECT_TWO, 0);
        this.imageSizeTwo = sharedPreferences.getInt(SP_IMAGE_SIZE_TWO, 1);
        this.brightnessTwo = sharedPreferences.getInt(SP_BRIGHTNESS_TWO, 50);
        this.contrastTwo = sharedPreferences.getInt(SP_CONTRAST_TWO, 50);
        this.saturationTwo = sharedPreferences.getInt(SP_SATURATION_TWO, 50);
        //拍照时间表
        this.scheduleData = sharedPreferences.getString(SP_PHOTO_TIME_TABLE, "");
        if (!scheduleData.equals("")) setPhotoTimeTable(hexStrToByteArray(scheduleData));
    }

    /**
     * byte[]转十六进制String
     *
     * @param byteArray
     * @return
     */
    private static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 十六进制String转byte[]
     *
     * @param str
     * @return
     */
    private static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    //主动开机线程
    private Runnable boot = new Runnable() {
        @Override
        public void run() {
            bootContactInfo();
            mHandler.postDelayed(boot, ONE_MINUTE);
        }
    };

    /**
     * 开机联络信息
     */
    public void bootContactInfo() {
        //主动开机
        originalCommandData = null;
        dataDomain = VERSION;
        setOrder(ORDER_00H);
        sendPack();
    }

    /**
     * 主动校时
     */
    public void schoolTime() {
        //主动校时
        originalCommandData = null;
        dataDomain = new byte[]{};
        setOrder(ORDER_01H);
        sendPack();
        requestTime = listenerCallBack.getDvrTime();
    }

    /**
     * 终端心跳信息
     *
     * @param PZTTime        信号记录时间(6个字节，年份需要-2000)
     * @param signalStrength 信号强度
     * @param batterVoltage  蓄电池电压
     */
    public void terminalHeartBeatInfo(byte[] PZTTime, byte signalStrength, byte batterVoltage) {
        if (listenerCallBack != null) {
            signalRecordingTime = PZTTime;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                originalCommandData = null;
                baos.write(signalRecordingTime);
                baos.write(signalStrength);
                baos.write(batterVoltage);
                dataDomain = baos.toByteArray();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setOrder(ORDER_05H);
        sendPack();
    }

    /**
     * 查询主站IP地址、端口号和卡号 07H
     *
     * @param ip            主站IP
     * @param queryPort     端口号
     * @param queryCardNumb 主站卡号
     */
    public void queryMasterStation(byte[] ip, byte[] queryPort, byte[] queryCardNumb) {
        //查询端口
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(ip);
            baos.write(queryPort);
            baos.write(queryCardNumb);
            dataDomain = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setOrder(ORDER_07H);
        sendPack();
    }

    /**
     * 查询终端设备时间 0DH
     *
     * @param queryTime 时间
     */
    public void theQueryTime(byte[] queryTime) {
        //查询端口
        dataDomain = queryTime;
        setOrder(ORDER_0DH);
        sendPack();
    }

    /**
     * 终端休眠通知 0CH
     */
    public void terminalSleepNotification() {
        dataDomain = NOTIFICATIONS_DORMANCY;
        setOrder(ORDER_0CH);
        sendPack();
    }


    /**
     * 上传图片
     *
     * @param filePath 路径
     */
    public void uploadFile(String filePath, String fileName) {
        mHandler.removeCallbacks(TheHeartbeatPackets);
        originalCommandData = null;
        countLoop = 0;
        if (isUpLocal) return;
        isUpLocal = true;
        byte order = -1;
        if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            order = ORDER_84H;
        } else if (fileName.endsWith(".mp4")) {
            order = ORDER_94H;
        } else {
            return;
        }
        pictureFile = new File(filePath + fileName);
        if (!pictureFile.exists()) {
            listenerCallBack.onErrMsg(order);
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(pictureFile);
            int pack_count;
            if (fis.available() % MAX_UPLOAD_IMAGE_SIZE == 0)
                pack_count = fis.available() / MAX_UPLOAD_IMAGE_SIZE;
            else
                pack_count = fis.available() / MAX_UPLOAD_IMAGE_SIZE + 1;
            int pack_high = pack_count / UPLOAD_IMAGE_PACK_DIVISOR;
            int pack_low = pack_count % UPLOAD_IMAGE_PACK_DIVISOR;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(getLastModifiedTime());
            Log.e("", "uploadFile: " + getLastModifiedTime().length);
            baos.write(channelNum);
            baos.write(preset);
            baos.write((byte) pack_high);
            baos.write((byte) pack_low);
            dataDomain = baos.toByteArray();
            baos.close();
            setOrder(order);
            if (upLoadFileTimer != null) {
                upLoadFileTimer.cancel();
                upLoadFileTimer.purge();
            }

            upLoadFileTimer = new Timer();
            upLoadFileTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendPack();
                    countLoop++;
                    if (countLoop == 5) {
                        isUpLocal = false;
                        cancel();
                    }
                }
            }, 0, 3000);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 文件最后修改的时间
     */
    private byte[] getLastModifiedTime() {
        if (pictureFile == null) return new byte[]{0};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pictureFile.lastModified());
        String info = sdf.format(cal.getTime());
        String[] date = info.split("-");
        String[] time = date[3].split(":");
        int date_yyyy = Integer.valueOf(date[0]) - 2000;
        return new byte[]{(byte) date_yyyy, Byte.parseByte(date[1]), Byte.parseByte(date[2]),
                Byte.parseByte(time[0]), Byte.parseByte(time[1]), Byte.parseByte(time[2])};
    }

    /**
     * upd发送
     */

    public void sendPack() {
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    byte[] buf = result();

                    if (socket == null) socket = new DatagramSocket();

                    DatagramPacket outPacket = new DatagramPacket(buf, buf.length, addr);

                    socket.send(outPacket);
                    SystemClock.sleep(10);
                    listenerCallBack.sendSuccess(order);
                    mSendData = buf;
                    if (order == ORDER_01H) {
                        dateTime = HikVisionUtils.getInstance().getNetDvrTime().ToString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    listenerCallBack.onErrMsg(ERR_SEND_UDP);
                }
            }
        });
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(sendThread);
    }

    /**
     * 采用累加和取反的校验方式，
     * 发送方将终端号码、控制字、数据长度和数据区的所有字节进行算术累加，
     * 抛弃高位，只保留最后单字节，将单字节取反；
     *
     * @param data 需要计算的数据
     * @return 结果
     */
    private byte Crc(byte[] data) {
        int r = 0;
        byte b = 0;
        for (int i = 0; i < data.length; i++) r += data[i];
        b = (byte) (r & 0x00FF);
        b = (byte) ~b;
        return b;
    }

    /**
     * udp 接收
     */
    private void receive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (socket == null) continue;
                    byte[] buf = new byte[maxPacketLength];
                    DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(receivePacket);
                        mReceiveData = receivePacket.getData();
                        Log.e("receive的数据", "run: " + Arrays.toString(mReceiveData));
                        mReceiveDatas = mReceiveData;
                        if (mReceiveData != null) handlerOrder(mReceiveData[7]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }


    /**
     * 处理命令
     *
     * @param order 命令
     */
    private void handlerOrder(byte order) {
        Log.e("接收到的命令：", String.valueOf(order));
        originalCommandData = null;
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().remove(sendThread);
        mHandler.removeCallbacks(TheHeartbeatPackets);
        mHandler.postDelayed(TheHeartbeatPackets, contactInterval);
        switch (order) {
            case ORDER_00H:
                handlerBootContactInfo(mReceiveData);
                break;
            case ORDER_01H:
                handlerSchoolTime(mReceiveData);
                break;
            case ORDER_02H:
                setTerminalPassword(mReceiveData);
                break;
            case ORDER_03H:
                handlerMasterStationParamConfig(mReceiveData);
                break;
            case ORDER_04H:
                break;
            case ORDER_05H:
//                cardiacPacingInterval();
                break;
            case ORDER_06H:
                //更改主站IP，端口，卡号
                ModifyTheHostIPPortNumbers(mReceiveData);
                break;
            case ORDER_07H:
                //查询主站IP,端口号，卡号
                selectIpAndPortAndSimNumber();
                break;
            case ORDER_08H:
                handlerTerminalReset(mReceiveData);
                break;
            case ORDER_09H:
                break;
            case ORDER_0AH:
                break;
            case ORDER_0BH:
                break;
            case ORDER_0CH:
                break;
            case ORDER_0DH:
                String time = listenerCallBack.getDvrTime();
                byte[] timeByte = getByteTime(time);
                theQueryTime(timeByte);
                break;
            case ORDER_21H:
                break;
            case ORDER_30H:
                break;
            case ORDER_71H:
                filesNumber = mReceiveData[10];
                setFileNameList();
                break;
            case ORDER_72H:
                originalCommandData = mReceiveData;
                setOrder(ORDER_72H);
                sendPack();
                repeatCountLoop = 0;
                uploadingFileRequests();
                break;
            case ORDER_73H:
                repeatCountLoop = 0;
                mHandler.removeCallbacks(repeatTiming);
                uploadingFile();
                break;
            case ORDER_74H:
                break;
            case ORDER_75H:
                break;
            case ORDER_76H:
                mHandler.removeCallbacks(repeatTiming);
                handlerTonicPacks(mReceiveData);
                break;
            case ORDER_81H:
                handlerPictureParamConfig(mReceiveData);
                break;
            case ORDER_82H:
                setTakePhotoTimetable(mReceiveData);
                break;
            case ORDER_83H:
                handlerQuestTakingPictures(mReceiveData);
                break;
            case ORDER_84H:
                handlerUploadPicture(ORDER_85H, ORDER_86H);
                break;
            case ORDER_85H:
                break;
            case ORDER_86H:
                break;
            case ORDER_87H:
                mHandler.removeCallbacks(repeatTiming);
                handlerTonicPack(mReceiveData, ORDER_85H, ORDER_86H);
                break;
            case ORDER_88H:
                handlerRemoteAdjustment();
                break;
            case ORDER_89H:
                listenerCallBack.startVideo();
                break;
            case ORDER_8AH:
                listenerCallBack.stopVideo();
                break;
            case ORDER_8BH:
                queryPhotoSchedule();
                break;
            case ORDER_93H:
                theMainRequestFilmingShortVideo(mReceiveData);
                break;
            case ORDER_94H:
                handlerUploadPicture(ORDER_95H, ORDER_96H);
                break;
            case ORDER_95H:
                break;
            case ORDER_96H:
                break;
            case ORDER_97H:
                handlerTonicPack(mReceiveData, ORDER_95H, ORDER_96H);
                break;
        }
        mReceiveData = null;
        mSendData = null;
    }

    /**
     * 字符串中提取年/月/日/时/分/秒
     *
     * @param time 传入String类型 YYYY/MM/DD HH:MM:ss 格式时间
     * @return 6字节byte类型参数
     */
    private byte[] getByteTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        byte[] timeByte;
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR) - 2000;
        int moth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        timeByte = new byte[]{(byte) year, (byte) moth, (byte) day, (byte) hour, (byte) minute, (byte) second};
        return timeByte;
    }

    /**
     * 修改主站端口IP卡号
     */
    private void ModifyTheHostIPPortNumbers(byte[] mReceiveData) {
        String password = getReceivePassword(mReceiveData);
        Http = new byte[]{mReceiveData[14], mReceiveData[15], mReceiveData[16], mReceiveData[17]};
        port = new byte[]{mReceiveData[18], mReceiveData[19]};
        Https = new byte[]{mReceiveData[20], mReceiveData[21], mReceiveData[22], mReceiveData[23]};
        ports = new byte[]{mReceiveData[24], mReceiveData[25]};
        cardNumber = new byte[]{mReceiveData[26], mReceiveData[27], mReceiveData[28], mReceiveData[29], mReceiveData[30], mReceiveData[31]};
        cardNumbers = new byte[]{mReceiveData[32], mReceiveData[33], mReceiveData[34], mReceiveData[35], mReceiveData[36], mReceiveData[37]};
        if (terminalPassword.equals(password)) {

            if (terminalPassword.equals(password) && Arrays.equals(Http, Https) && Arrays.equals(port, ports) && Arrays.equals(cardNumber, cardNumbers)) {
                dataDomain = mReceiveData;
                //更改IP
                Server = String.valueOf(toInt(mReceiveData[14]) + "." + toInt(mReceiveData[15]) + "." + toInt(mReceiveData[16]) + "." + toInt(mReceiveData[17]));
                Log.i("更改后的IP", "receiveSuccess: " + Server);
                //更改端口
                int a = toInt(mReceiveData[18]);
                int b = toInt(mReceiveData[19]);

                Port = a * UPLOAD_IMAGE_PACK_DIVISOR + b;
                Log.i("更改后的IP", "receiveSuccess: " + Port);
                //更改卡号 暂不知道服务器发送的卡号是纯数字还是 F?H 格式   需格式判断或询问清格式
                simNumber = byteArrayToHexStr(cardNumber);
                Log.i("更改后的卡号", "receiveSuccess: " + simNumber);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SP_SERVER, Server);
                editor.putInt(SP_PORT, Port);
                editor.putString(SP_SIM_NUMB, simNumber);
                editor.apply();
                InitUdp(Server, Port, deviceID, cardNumber);
            } else {
                dataDomain = HTTP_OR_PORT_CAR_NUMBER;
            }
        } else {
            dataDomain = PASSWORD_MISTAKE_VERSION;
        }
        setOrder(ORDER_06H);
        sendPack();
    }


    /**
     * 查询IP，端口，http
     */
    private void selectIpAndPortAndSimNumber() {
        //端口取高字节
        byte mainVersion = (byte) (Port / UPLOAD_IMAGE_PACK_DIVISOR);
        //端口取低字节
        byte minorVersion = (byte) (Port % UPLOAD_IMAGE_PACK_DIVISOR);
        //高字节*256+低字节
        byte[] portNumber = new byte[]{mainVersion, minorVersion};
        Log.i(TAG, "receiveSuccess: " + Arrays.toString(portNumber));
        //IP
        int a, b, c, d;
        //先找到IP地址字符串中.的位置
        int position1 = Server.indexOf(".");
        int position2 = Server.indexOf(".", position1 + 1);
        int position3 = Server.indexOf(".", position2 + 1);
        //将每个.之间的字符串转换成整型
        a = Integer.parseInt(Server.substring(0, position1));
        b = Integer.parseInt(Server.substring(position1 + 1, position2));
        c = Integer.parseInt(Server.substring(position2 + 1, position3));
        d = Integer.parseInt(Server.substring(position3 + 1));
        //启动
        queryMasterStation(new byte[]{(byte) a, (byte) b, (byte) c, (byte) d}, portNumber, hexStrToByteArray(simNumber));
    }

    /**
     * 循环发送心跳包处理
     */
    private void cardiacPacingInterval() {
        mHandler.removeCallbacks(TheHeartbeatPackets);
        //判断心跳包返回的指令  如果是原指令择每隔两分钟重复发送心跳
        if (mReceiveData != null && mReceiveData[7] == SPGProtocol.ORDER_05H) {
            Log.i(TAG, "run: " + mReceiveData[7]);
            mHandler.postDelayed(TheHeartbeatPackets, TheHeartbeatPacketsTimes);
        } else {
            //否则关闭心跳包,两分钟后再重新开启定时心跳线程
            mHandler.removeCallbacks(TheHeartbeatPackets);
            //执行指令
            byte[] time = HikVisionUtils.getInstance().getNetDvrTimeByte();
            terminalHeartBeatInfo(time, (byte) 0x64, (byte) 0x44);
            mHandler.postDelayed(TheHeartbeatPackets, TheHeartbeatPacketsTimes);
        }
    }

    //主动校时线程
    public Runnable WhenTheSchool = new Runnable() {
        public void run() {
            schoolTime();
            mHandler.postDelayed(this, TWO_MINUTE);
        }
    };

    /**
     * 开机联络信息
     */
    protected void handlerBootContactInfo(byte[] mReceiveData) {

        if ((toInt(mReceiveData[8]) + toInt(mReceiveData[9])) > 0) {
            mHandler.removeCallbacks(boot);
            mHandler.postDelayed(WhenTheSchool, 0);
        } else {
            mHandler.postDelayed(boot, 0);
        }
    }

    /**
     * 主机主动发下 校时 01H
     *
     * @param mReceiveData
     */
    protected void handlerSchoolTime(byte[] mReceiveData) {
        responseTime = listenerCallBack.getDvrTime();
        mHandler.removeCallbacks(WhenTheSchool);

        if (requestTime != null) {
            long requestTime1 = new Date(requestTime).getTime();
            long responseTime1 = new Date(responseTime).getTime();
            long result = (responseTime1 - requestTime1) / 1000;
            if (result <= 20) {
                boolean setTimer = listenerCallBack.setDvrTime(mReceiveData[10] + 2000,
                        mReceiveData[11],
                        mReceiveData[12],
                        mReceiveData[13],
                        mReceiveData[14],
                        mReceiveData[15]);
                if (!setTimer) {
                    listenerCallBack.onErrMsg(ERR_ORDER_01H);
                }
            } else {
                listenerCallBack.onErrMsg(ERR_ORDER_01H);
            }
            requestTime = null;
        } else if (mReceiveData != null) {//被动校时
            boolean isSuccess = listenerCallBack.setDvrTime(mReceiveData[10] + 2000,
                    mReceiveData[11],
                    mReceiveData[12],
                    mReceiveData[13],
                    mReceiveData[14],
                    mReceiveData[15]);
            if (!isSuccess) return;
            //被动校时
            originalCommandData = mReceiveData;
            setOrder(ORDER_01H);
            sendPack();
        }
    }

    //定时发送心跳包
    public Runnable TheHeartbeatPackets = new Runnable() {
        @Override
        public void run() {
            byte[] time = getByteTime(listenerCallBack.getDvrTime());
            terminalHeartBeatInfo(time, (byte) 0x64, (byte) 0x44);
            mHandler.postDelayed(this, contactInterval);
        }
    };

    /**
     * 设置终端密码
     */
    protected void setTerminalPassword(byte[] mReceiveData) {
        String pass = getReceivePassword(mReceiveData);
        if (pass.equals(terminalPassword)) {

            newPassword = new byte[]{mReceiveData[14], mReceiveData[15], mReceiveData[16], mReceiveData[17]};
            terminalPassword = new String(newPassword);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SP_PASSWORD, terminalPassword);
            editor.apply();
            originalCommandData = mReceiveData;
        } else {
            dataDomain = TERMINAL_PWD_VERSION;
        }
        setOrder(ORDER_02H);
        sendPack();
    }

    /**
     * 主站下发参数配置 03H
     */
    protected void handlerMasterStationParamConfig(byte[] mReceiveData) {
        String password = getReceivePassword(mReceiveData);
        if (password.equals(terminalPassword)) {
            mReceiveDatas = mReceiveData;
            contactInterval = getSampleTime(mReceiveData, 14, 1); //心跳间隔
//            toInt(mReceiveData[14]) * ONE_MINUTE;
            samplingInterval = getSampleTime(mReceiveData, 15, 2);//采样间隔
//                    (toInt(mReceiveData[15]) + toInt(mReceiveData[16])) * ONE_MINUTE;
            theSleepTime = getSampleTime(mReceiveData, 17, 2);//休眠间隔
//                    mReceiveData[17] + mReceiveData[18];
            theOnlineTime = getSampleTime(mReceiveData, 19, 2);//在线时长
//                    mReceiveData[19] + mReceiveData[20];
            String time = listenerCallBack.getDvrTime();
            String[] times = time.split("/");
            if (mReceiveData[21] != 0)
                hardwareResetTime = times[0] + "/" + times[1] + "/" + mReceiveData[21] + " " + mReceiveData[22] + ":" + mReceiveData[23];//硬件重启时间点
            else hardwareResetTime = "0";
// new byte[]{mReceiveData[21], mReceiveData[22], mReceiveData[23]};
            cipherCertification = new String(new byte[]{mReceiveData[24], mReceiveData[25], mReceiveData[26], mReceiveData[27]});//密文认证

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(SP_CONTACT_INTERVAL, contactInterval);
            editor.putLong(SP_SAMPLING_INTERVAL, samplingInterval);
            editor.putLong(SP_SLEEP_TIME, theSleepTime);
            editor.putLong(SP_ONLINE_TIME, theOnlineTime);
            editor.putString(SP_HARDWARE_RESTART_TIME_POINT, hardwareResetTime);
            editor.putString(SP_CIPHER_VERIFICATION_CODE, cipherCertification);
            editor.apply();

            originalCommandData = mReceiveData;
        } else {
            dataDomain = PASSWORD_MISTAKE_VERSION;
        }
        setOrder(ORDER_03H);
        sendPack();
    }

    /**
     * 获取参数配置的时间
     *
     * @param data  数据
     * @param index 指数
     * @param numb  次数
     * @return
     */
    private long getSampleTime(byte[] data, int index, int numb) {
        long time = 0;
        for (int i = 0; i < numb; i++) {
            time = time + toInt(data[index + i]);
        }
        return time * ONE_MINUTE;
    }

    /**
     * 终端复位
     */
    protected void handlerTerminalReset(byte[] mReceiveData) {

        byte[] receive = new byte[]{(mReceiveData[10]), mReceiveData[11]
                , mReceiveData[12], mReceiveData[13]};
        String NowPassword = new String(receive);

        if (NowPassword.equals(terminalPassword)) {
            dataDomain = new byte[]{mReceiveData[10], mReceiveData[11],
                    mReceiveData[12], mReceiveData[13]};
            setOrder(mReceiveData[7]);
            listenerCallBack.receiveSuccess(order);
        } else {
            dataDomain = TERMINAL_RESET;
        }
    }

    /**
     * 图像采集参数配置
     *
     * @param mReceiveData 接收的数据的
     */
    protected void handlerPictureParamConfig(byte[] mReceiveData) {
        if (mReceiveData != null) {
            String password = getReceivePassword(mReceiveData);
            if (password == null) {
                listenerCallBack.onErrMsg(ERR_ORDER_81H);
                return;
            }
            Log.e("图像采集参数配置", "handlerPictureParamConfig: " + password);
            if (password.equals(terminalPassword)) {
                //通道1
                colorSelectionOne = mReceiveData[14];
                imageSizeOne = mReceiveData[15];
                brightnessOne = mReceiveData[16];
                contrastOne = mReceiveData[17];
                saturationOne = mReceiveData[18];
                //通道2
                colorSelectionTwo = mReceiveData[19];
                imageSizeTwo = mReceiveData[20];
                brightnessTwo = mReceiveData[21];
                contrastTwo = mReceiveData[22];
                saturationTwo = mReceiveData[23];

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(SP_COLOR_SELECT_ONE, colorSelectionOne);
                editor.putInt(SP_IMAGE_SIZE_ONE, imageSizeOne);
                editor.putInt(SP_BRIGHTNESS_ONE, brightnessOne);
                editor.putInt(SP_CONTRAST_ONE, contrastOne);
                editor.putInt(SP_SATURATION_ONE, saturationOne);

                editor.putInt(SP_COLOR_SELECT_TWO, colorSelectionTwo);
                editor.putInt(SP_IMAGE_SIZE_TWO, imageSizeTwo);
                editor.putInt(SP_BRIGHTNESS_TWO, brightnessTwo);
                editor.putInt(SP_CONTRAST_TWO, contrastTwo);
                editor.putInt(SP_SATURATION_TWO, saturationTwo);
                editor.apply();

                originalCommandData = mReceiveData;
            } else {
                dataDomain = PASSWORD_MISTAKE_VERSION;
            }
            setOrder(ORDER_81H);
            sendPack();
        }
        listenerCallBack.receiveSuccess(order);
    }

    /**
     * 拍照时间表设置 82H
     *
     * @param receiveData 数据
     */
    protected void setTakePhotoTimetable(final byte[] receiveData) {
        countLoop = 0;
        String password = getReceivePassword(mReceiveData);
        if (password == null) {
            listenerCallBack.onErrMsg(ORDER_82H);
            return;
        }
        ByteArrayOutputStream scheduleBos = new ByteArrayOutputStream();
        if (password.equals(terminalPassword)) {
            originalCommandData = receiveData;
            channelNum = receiveData[14];

            scheduleBos.write(channelNum);
            scheduleBos.write(receiveData[15]);
            for (int i = 0; i < receiveData[15]; i++) {
                scheduleBos.write(receiveData[16 + i * 3]);
                scheduleBos.write(receiveData[17 + i * 3]);
                scheduleBos.write(receiveData[18 + i * 3]);

            }
            try {
                if (channelNum == 1) {
                    setPhotoTimeTable(scheduleBos.toByteArray());
                }
                scheduleBos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            dataDomain = PASSWORD_MISTAKE_VERSION;
        }
        setOrder(ORDER_82H);
        sendPack();
    }

    private void setPhotoTimeTable(byte[] data) {
        mHandler.removeCallbacks(runnable);
        int delayTime = -1;
        timeArray = new int[data[1]];
        presetGroup = new int[data[1]];

        for (int i = 0; i < data[1]; i++) {
            presetGroup[i] = data[4 + i * 3];

            int hour = data[2 + i * 3];
            if (hour == 0) hour = 24;
            timeArray[i] = (hour * 60 + data[3 + i * 3]) * 60000;

            for (int j = 0; j < timeArray.length; j++) {
                if (timeArray[i] < timeArray[j]) {
                    int replaceNumb = timeArray[i];
                    timeArray[i] = timeArray[j];
                    timeArray[j] = replaceNumb;
                }
            }

        }

        for (int i = 0; i < data[1]; i++) {
            delayTime = timeArray[i] - getDelayTime();
            if (delayTime >= 0) {
                countLoop = i;
                break;
            }
        }

        if (delayTime >= 0) mHandler.postDelayed(runnable, delayTime);
        else {
            countLoop = 0;
            mHandler.postDelayed(runnable, 24 * 3600000 - (getDelayTime() - timeArray[countLoop]));
        }

        scheduleData = byteArrayToHexStr(data);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SP_PHOTO_TIME_TABLE, scheduleData);
        editor.apply();
    }

    /**
     * 拍照时间表定时器
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            listenerCallBack.setPreset(presetGroup[countLoop]);
            countLoop++;
            if (countLoop == timeArray.length) {
                countLoop = -1;
                mHandler.postDelayed(this, 24 * 3600000);
                return;
            }
            mHandler.removeCallbacks(this);
            int delayTime = timeArray[countLoop] - getDelayTime();
            mHandler.postDelayed(this, delayTime);
            Log.e("delayTime", "runnable: " + delayTime);
        }
    };

    /**
     * 计算定时器所需的某段时间
     *
     * @return
     */
    private int getDelayTime() {
        String[] dvrTime = listenerCallBack.getDvrTime().split("/");
        dvrTime = dvrTime[2].split(" ");
        dvrTime = dvrTime[1].split(":");
        int dvrHour = Integer.parseInt(dvrTime[0]);
        int dvrMinute = Integer.parseInt(dvrTime[1]);
        if (dvrHour == 0) dvrHour = 24;
        return (dvrHour * 60 + dvrMinute) * 60000;
    }

    /**
     * 主站请求拍摄照片
     *
     * @param mReceiveData 主站下发的数据
     */
    protected void handlerQuestTakingPictures(byte[] mReceiveData) {
        originalCommandData = mReceiveData;
        channelNum = mReceiveData[10];
        preset = mReceiveData[11];
        setOrder(ORDER_83H);
        sendPack();
        if (channelNum == 1) {
            if (preset != 0) {
                listenerCallBack.setPreset(preset);
                SystemClock.sleep(10000);
            }
            useChannelNum(channelNum);
        } else if (channelNum == 2) {
            useChannelNum(channelNum);
        }
    }

    /**
     * 主站请求拍摄短视频
     *
     * @param mReceiveData
     */
    protected void theMainRequestFilmingShortVideo(byte[] mReceiveData) {
        channelNum = mReceiveData[10];
        preset = mReceiveData[11];
        originalCommandData = mReceiveData;
        setOrder(ORDER_93H);
        sendPack();
        Log.i(TAG, "receiveSuccess: " + mReceiveDatas[11]);
        listenerCallBack.startShortVideo(mReceiveDatas[10], mReceiveDatas[11], mReceiveDatas[12]);
    }


    /**
     * 上传文件数据,上传完后,2秒后发送上传结束标记
     *
     * @param order1 上传指令
     * @param order2 结束指令
     */
    protected void handlerUploadPicture(byte order1, byte order2) {
        try {
            mHandler.removeCallbacks(TheHeartbeatPackets);
            upLoadFileTimer.cancel();
            int len = 0;
            int packIndex = 0;
            byte[] buf = new byte[MAX_UPLOAD_IMAGE_SIZE];
            FileInputStream fis = new FileInputStream(pictureFile);
            while ((len = fis.read(buf)) != -1) {
                SystemClock.sleep(100);
                packIndex++;
                int pack_high = packIndex / UPLOAD_IMAGE_PACK_DIVISOR;
                int pack_low = packIndex % UPLOAD_IMAGE_PACK_DIVISOR;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(channelNum);
                baos.write(preset);
                baos.write((byte) pack_high);
                baos.write((byte) pack_low);
                baos.write(buf, 0, len);
                dataDomain = baos.toByteArray();
                setOrder(order1);
                sendPack();
                baos.close();
                Log.e("上传图片packIndex", "baleDataChar: " + packIndex + "," + buf.length);
            }
            fis.close();
            repeatCountLoop = 0;
            //上传图片数据2秒后再发送结束标记
            stopUploadPicture(order2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件数据上传结束标记
     *
     * @param order 结束指令
     * @throws IOException
     */
    protected void stopUploadPicture(byte order) throws IOException {
        isUpLocal = false;
        SystemClock.sleep(2000);
        Log.e("H6", "stopUploadPicture: " + "结束");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(channelNum);
        baos.write(preset);
        dataDomain = baos.toByteArray();
        setOrder(order);
        sendPack();
        baos.close();
        mHandler.postDelayed(repeatTiming, 28 * 1000);
    }

    /**
     * 补包处理
     *
     * @param tonicPackData 补包数据
     * @param order1        上传图片指令
     * @param oder2         结束指令
     */
    protected void handlerTonicPack(byte[] tonicPackData, byte order1, byte oder2) {
        try {
            if (tonicPackData != null) {
                int pack_count = (tonicPackData[12] < 0) ? (tonicPackData[12] & 0xFF) : tonicPackData[12];
                Log.e("需要补的包", "handlerTonicPack: " + pack_count);
                if (pack_count > 0) {
                    mHandler.removeCallbacks(TheHeartbeatPackets);
                    isUpLocal = true;
                    int count = pack_count;
                    int len = 0;
                    byte[] buf = new byte[MAX_UPLOAD_IMAGE_SIZE];

                    while (count-- > 0) {
                        byte bytePackLow = tonicPackData[14 + len * 2];
                        int pack_high = tonicPackData[13 + len * 2];
                        int pack_low = (bytePackLow < 0) ? (bytePackLow & 0xFF) : bytePackLow;
                        int packIndex = pack_high * UPLOAD_IMAGE_PACK_DIVISOR + pack_low;

                        if (packIndex > 0) {
                            int read;
                            FileInputStream fis = new FileInputStream(pictureFile);
                            int readCount = 0;
                            while ((read = fis.read(buf)) != -1) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                readCount++;
                                if (packIndex == readCount) {
                                    baos.write(channelNum);
                                    baos.write(preset);
                                    baos.write((byte) pack_high);
                                    baos.write((byte) pack_low);
                                    baos.write(buf, 0, read);
                                    dataDomain = baos.toByteArray();
                                    SystemClock.sleep(100);
                                    setOrder(order1);
                                    sendPack();
                                    Log.e("补包packIndex", "tonicPack: " + packIndex + "," + buf.length);
                                    baos.close();
                                    fis.close();
                                    break;
                                }
                            }
                        }
                        len++;
                    }

                    repeatCountLoop = 0;
                    stopUploadPicture(oder2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 摄像机远程调节 88H
     */
    private void handlerRemoteAdjustment() {
        String mPassword = getReceivePassword(mReceiveData);
        if (mPassword.equals(terminalPassword)) {
            originalCommandData = mReceiveData;
            listenerCallBack.remoteAdjustmentCamera(mReceiveData[14], mReceiveData[15], mReceiveData[16]);
        } else {
            dataDomain = PASSWORD_MISTAKE_VERSION;
        }
        setOrder(ORDER_88H);
        sendPack();
    }

    /**
     * 查询拍照时间表 8BH
     */
    protected void queryPhotoSchedule() {
        if (mReceiveData[10] == 1) {
            dataDomain = hexStrToByteArray(scheduleData);
        } else {
            dataDomain = new byte[]{2, 0};
        }
        setOrder(ORDER_8BH);
        sendPack();
    }

    /**
     * 验证发送数据与接收数据是否一致
     *
     * @return true:一致 否则false
     */
    private Boolean proofOrder() {
        if (mReceiveData == null) return false;
        if (mSendData == null) return false;
        int count = 0;
        for (int i = 0; i < 8; i++) {
            if (mReceiveData[i] == mSendData[i]) {
                count++;
            }
        }
        return count == 8;
    }

    /**
     * 把主站下发的密码转换成String
     *
     * @param password 下发byte[]数据
     * @return 结果
     */
    private String getReceivePassword(byte[] password) {
        if (password == null) {
            return null;
        }
        byte[] mPassword = new byte[]{password[10], password[11], password[12], password[13]};
        return new String(mPassword);
    }

    /**
     * 使用指定通道号
     *
     * @param channelNum 通道号
     */
    private void useChannelNum(byte channelNum) {
        if (channelNum == 1) {
            listenerCallBack.useChannelNumOne(colorSelectionOne, imageSizeOne,
                    brightnessOne, contrastOne, saturationOne);
        } else if (channelNum == 2) {
            listenerCallBack.useChannelNumTwo(colorSelectionTwo, imageSizeTwo,
                    brightnessTwo, contrastTwo, saturationTwo);
        }
    }


    /**
     * 71H上传文件列表
     */
    protected void setFileNameList() {
        getFileNameList();
        int num = 0;
        if (filesNumber == 0) {
            num = fileTimes.size() / 9 + 1;
        } else if (filesNumber > 0) {
            num = filesNumber / 9 + 1;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int j = 0; j < num; j++) {
                baos.write((byte) filesNumber);
                for (int i = 0; i <= 9; i++) {
                    if ((i + j * 9) < filesNumber)
                        baos = getFileList(baos, i + j * 9);
                }
            }
            dataDomain = baos.toByteArray();
            baos.close();
            setOrder(ORDER_71H);
            sendPack();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单个文件信息的封包
     *
     * @param i 第i个文件
     * @return 第i个文件信息
     */

    public ByteArrayOutputStream getFileList(ByteArrayOutputStream baoss, int i) {
        byte[] filesList = new byte[100];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos = baoss;
        try {
            //文件名
            byte[] name = fileNames.get(i).getBytes();
            //文件生成时间
            Date date = null;
            try {
                date = format.parse(String.valueOf(fileTimes.get(i)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR) - 2000;
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            //文件大小
            int length = fileLengths.get(i);

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bao);
            byte[] lengthList;
            out.writeShort(length);
            lengthList = bao.toByteArray();
            out.close();
            bao.close();
            for (int j = 0; j < filesList.length; j++) {
                if (j < name.length) {
                    filesList[j] = name[j];
                }
            }
            baos.write(filesList);
            baos.write(year);
            baos.write(month);
            baos.write(day);
            baos.write(hour);
            baos.write(minute);
            baos.write(second);
            baos.write(lengthList);
            baos.close();
//            else if (j < 100) {
//                filesList[j] = 0x00;
//            } else if (j < 106) {
//                filesList[100] = (byte) (year - 2000);
//                filesList[101] = (byte) month;
//                filesList[102] = (byte) day;
//                filesList[103] = (byte) hour;
//                filesList[104] = (byte) minute;
//                filesList[105] = (byte) second;
//            } else if (j < 108) {
//                filesList[106] = lengthList[0];
//                filesList[107] = lengthList[1];
//            }
//        }
//        bos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos;
    }

    /**
     * 获取文件列表信息
     */
    protected void getFileNameList() {
        File file = new File(filePath);
        if (!file.exists())
            return;
        File[] files = new File(filePath).listFiles();
        File x = new File(filePath);
        for (File f : files) {
            if (x.isDirectory()) {
                fileNames.add(f.getName());
                fileLengths.add((int) f.length());
                @SuppressLint("SimpleDateFormat") String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(f.lastModified()));
                fileTimes.add(String.valueOf(ctime));
            }
        }
        //将生成的文件列表按创建时间顺序，由new到old重新排列
        for (int i = 0; i < fileTimes.size(); i++) {
            for (int j = i + 1; j < fileTimes.size(); j++) {
                int va = fileTimes.get(i).compareTo(fileTimes.get(j));
                String oldFileName = fileNames.get(i);
                Integer oldFileLengt = fileLengths.get(i);
                String oldFileTime = fileTimes.get(i);
                if (va < 0) {
                    fileNames.set(i, fileNames.get(j));
                    fileLengths.set(i, fileLengths.get(j));
                    fileTimes.set(i, fileTimes.get(j));
                    fileNames.set(j, oldFileName);
                    fileLengths.set(j, oldFileLengt);
                    fileTimes.set(j, oldFileTime);
                }
            }
        }
    }

    /**
     * 通过文件名获取文件信息
     */
    protected void getFileNameList(String fileName) {
        File file = new File(filePath);
        if (!file.exists())
            return;
        File[] files = new File(filePath).listFiles();
        File x = new File(filePath);
        for (File f : files) {

            if (x.isDirectory()) {
                if (f.getName().equals(fileName)) {
                    fileLength = (int) f.length();
                    @SuppressLint("SimpleDateFormat") String ctime = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date(f.lastModified()));
                    fileTime = ctime;
                }
            }
        }
    }

    /**
     * 上传文件前请求
     */
    private void uploadingFileRequests() {
        originalCommandData = null;
        try {

            ByteArrayOutputStream baos = getFileName();
            DataOutputStream dos = new DataOutputStream(baos);
            fileNameByteData = getFileName().toByteArray();
            fileName = baos.toString().trim();
            getFileNameList(fileName);
            dos.write(getByteTime(fileTime));
            dos.writeShort(fileLength);
            int pack_count;
            if (fileLength % MAX_UPLOAD_FILE_SIZE == 0) {
                pack_count = (fileLength / MAX_UPLOAD_FILE_SIZE);
            } else {
                pack_count = (fileLength / MAX_UPLOAD_FILE_SIZE) + 1;
            }
            int pack_high = pack_count / 256;
            int pack_low = pack_count % 256;
            baos.write((byte) pack_high);
            baos.write((byte) pack_low);
            dataDomain = baos.toByteArray();
            setOrder(ORDER_73H);
            sendPack();
            baos.close();
            Log.e(TAG, "pack_high,pack_low: " + pack_high + "," + pack_low + "，包的大小：" + pack_count);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHandler.postDelayed(repeatTiming, 3000);
    }

    /**
     * 循环发送5次
     */
    private Runnable repeatTiming = new Runnable() {
        @Override
        public void run() {
            originalCommandData = null;
            repeatCountLoop++;
            switch (order) {
                case ORDER_73H:
                    uploadingFileRequests();
                    break;
                case ORDER_75H:
                    upLocalFileEnd(fileNameByteData);
                    break;
                case ORDER_86H:
                    try {
                        stopUploadPicture(ORDER_86H);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            if (repeatCountLoop == 5) mHandler.removeCallbacks(repeatTiming);
        }
    };

    /**
     * 上传文件
     */
    private void uploadingFile() {
        mHandler.removeCallbacks(TheHeartbeatPackets);
        try {
            int len = 0;
            int packIndex = 0;
            byte[] buf = new byte[MAX_UPLOAD_FILE_SIZE];

            fileName = new String(fileNameByteData).trim();
            pictureFile = new File(filePath + fileName);
            FileInputStream fis = new FileInputStream(pictureFile);

            while ((len = fis.read(buf)) != -1) {
                SystemClock.sleep(100);
                packIndex++;
                int pack_high = packIndex / UPLOAD_IMAGE_PACK_DIVISOR;
                int pack_low = packIndex % UPLOAD_IMAGE_PACK_DIVISOR;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(fileNameByteData);
                baos.write((byte) pack_high);
                baos.write((byte) pack_low);
                baos.write(buf, 0, len);
                dataDomain = baos.toByteArray();
                Log.e(TAG, "上传pack_high,pack_low: " + pack_high + "," + pack_low + ",包的大小：" + buf.length);
                setOrder(ORDER_74H);
                sendPack();
                baos.close();
            }
            fis.close();
            upLocalFileEnd(fileNameByteData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上送结束标记  75H
     *
     * @param data
     */
    private void upLocalFileEnd(byte[] data) {
        //2秒后再发送结束标记
        SystemClock.sleep(2000);
        dataDomain = data;
        setOrder(ORDER_75H);
        sendPack();
        mHandler.postDelayed(repeatTiming, 28000);
        Log.e("接收到的命令：", "结束" + String.valueOf(order));
    }

    /**
     * 获取文件名
     *
     * @return
     */
    private ByteArrayOutputStream getFileName() {
        ByteArrayOutputStream names = new ByteArrayOutputStream();
        for (int i = 0; i < 100; i++) {
            names.write(mReceiveData[i + 10]);
        }
        return names;
    }

    /**
     * 补包处理
     *
     * @param tonicPackData 补包数据
     */

    protected void handlerTonicPacks(byte[] tonicPackData) {
        try {
            if (tonicPackData != null) {
                int pack_count = tonicPackData[110];
                Log.e("需要补的包", "handlerTonicPack: " + pack_count);
                if (pack_count > 0) {
                    isUpLocal = true;
                    int count = pack_count;
                    int len = 0;
                    byte[] buf = new byte[MAX_UPLOAD_FILE_SIZE];
                    ByteArrayOutputStream names = getFileName();
                    fileName = names.toString().trim();
                    pictureFile = new File(filePath + fileName);
                    FileInputStream fis = new FileInputStream(pictureFile);
                    int readCount = 0;
                    while (count-- > 0) {
                        byte bytePackLow = tonicPackData[112 + len * 2];
                        int pack_high = tonicPackData[111 + len * 2];
                        int pack_low = (bytePackLow < 0) ? (bytePackLow & 0xFF) : bytePackLow;
                        int packIndex = pack_high * UPLOAD_IMAGE_PACK_DIVISOR + pack_low;
                        if (packIndex > 0) {
                            int read;
                            while ((read = fis.read(buf)) != -1) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                SystemClock.sleep(100);
                                readCount++;
                                if (packIndex == readCount) {
                                    baos.write(names.toByteArray());
                                    baos.write((byte) pack_high);
                                    baos.write((byte) pack_low);
                                    baos.write(buf, 0, read);
                                    dataDomain = baos.toByteArray();
                                    setOrder(ORDER_74H);
                                    sendPack();
                                    Log.e("补包packIndex", "tonicPack: " + packIndex + "," + buf.length);
                                    baos.close();
                                    break;
                                }
                            }
                        }
                        len++;
                    }
                    upLocalFileEnd(getFileName().toByteArray());
                    fis.close();
                    names.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * byte转int
     */
    private int toInt(byte value) {
        return value & 0xFF;
    }

}
