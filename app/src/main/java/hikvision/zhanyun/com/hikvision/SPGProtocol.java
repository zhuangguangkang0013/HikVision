package hikvision.zhanyun.com.hikvision;

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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;


/**
 * Created by ZY004Engineer on 2018/6/12.
 */

public class SPGProtocol {

    public static final byte ORDER_00H = 0x00;
    public static final byte ORDER_01H = 0x01;
    public static final byte ORDER_02H = 0x02;
    public static final byte ORDER_03H = 0x03;
    public static final byte ORDER_04H = 0x04;
    public static final byte ORDER_05H = 0x05;
    public static final byte ORDER_06H = 0x06;
    public static final byte ORDER_07H = 0x07;
    public static final byte ORDER_08H = 0x08;
    public static final byte ORDER_09H = 0x09;
    public static final byte ORDER_0AH = 0x0A;
    public static final byte ORDER_0BH = 0x0B;
    public static final byte ORDER_0CH = 0x0C;
    public static final byte ORDER_0DH = 0x0D;
    public static final byte ORDER_21H = 0x21;
    public static final byte ORDER_30H = 0x30;
    public static final byte ORDER_71H = 0x71;
    public static final byte ORDER_72H = 0x72;
    public static final byte ORDER_73H = 0x73;
    public static final byte ORDER_74H = 0x74;
    public static final byte ORDER_75H = 0x75;
    public static final byte ORDER_76H = 0x76;
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
    private final byte[] VERSION = {0x01, 0x02};

    //校时 01H
    private final byte[] WHEN_THE_SCHOOL_VERSION = {};

    //设置终端密码 02H
    private final byte[] TERMINAL_PWD_ORDER = {0x02};
    private final byte[] TERMINAL_PWD_VERSION = {(byte) 0xFFFF};
    public String oldPassword;
    public String newPassword;
    public boolean judge;

    //终端心跳信息 05H
    private byte[] signalRecordingTime = new byte[5];
    private byte[] signalStrength = new byte[0];
    private byte[] batteryVoltage = new byte[0];

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

    //主站下发参数配置
    public String password;    //密码
    public int HeartbeatInterval;    //心跳间隔
    public int SamplingInterval;   //采样间隔
    public int TheSleepTime;  //休眠时长
    public int TheOnlineTime; //在线时长时间点
    public byte[] HardwareResetTime;    //硬件重启
    public byte[] CipherCertification;    //密文认证

    //更改主站IP
    public String Http;    //主站IP
    public String Https; //主站IP
    public byte[] port; //端口号
    public byte[] ports;    //端口号
    public byte[] cardNumber;    //主站卡号
    public byte[] cardNumbers;    //主站卡号
    public boolean judges;
    //返回密码错误
    private final byte[] Password_Mistake_VERSION = {(byte) 0xFFFF};
    //返回主站IP端口主站卡号错误
    private final byte[] HttpOrPortCarNumber = {0x0000};
    //储存接受数据
    public byte[] mReceiveDatas;

    //查询主站IP，端口，卡号
    //主站IP
    public byte[] ip;
    //端口号
    public byte[] queryPort;
    //主站卡号
    public byte[] queryCardNumber;


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
    private byte[] dataDomain;//数据域

    private File pictureFile;
    private Timer timer;
    private final static long ONE_MINUTE = 60 * 1000;
    private final static long TWO_MINUTE = 2 * 1000;
    private String terminalPassword = "1234";
    private int[] timeArray;
    private Handler handler = new Handler();
    private int countLoop;
    private int[] presetGroup;
    private ByteArrayOutputStream scheduleBos;

    public void setOrder(byte order) {
        this.order = order;
        controlChar = new byte[]{order};
    }

    public int getChannelNum() {
        return channelNum;
    }

    public int getPreset() {
        return preset;
    }

    public int getColorSelectionOne() {
        return colorSelectionOne;
    }

    public int getImageSizeOne() {
        return imageSizeOne;
    }

    public int getBrightnessOne() {
        return brightnessOne;
    }

    public int getContrastOne() {
        return contrastOne;
    }

    public int getSaturationOne() {
        return saturationOne;
    }

    public int getColorSelectionTwo() {
        return colorSelectionTwo;
    }

    public int getImageSizeTwo() {
        return imageSizeTwo;
    }

    public int getBrightnessTwo() {
        return brightnessTwo;
    }

    public int getContrastTwo() {
        return contrastTwo;
    }

    public int getSaturationTwo() {
        return saturationTwo;
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
    public SPGProtocol(UdpListenerCallBack listenerCallBack) {
        this.listenerCallBack = listenerCallBack;
    }

    /**
     * 初始化udp
     *
     * @param id   终端号码 123456
     * @param host 服务器地址
     * @param port 端口
     */
    public void InitUdp(final String host, final int port, String id) {
        Server = host;
        Port = port;
        this.deviceID = id;
        addr = new InetSocketAddress(Server, Port);
        try {
            socket = new DatagramSocket();
            receive();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

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
    }

    /**
     * 设置终端密码
     *
     * @param judge true 修改密码，false密码错误
     */
    public void setTerminalPassword(boolean judge) {
        if (judge) {
            originalCommandData = mReceiveDatas;
        } else {
            originalCommandData = null;
            dataDomain = TERMINAL_PWD_VERSION;
        }
        setOrder(ORDER_02H);
        sendPack();
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
        originalCommandData = null;
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
     * 终端休眠通知 0CH
     */
    public void terminalSleepNotification() {
        originalCommandData = null;
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
        originalCommandData = null;
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
            sendPack();
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
    private void sendPack() {

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
                    dateTime = HikVisionUtils.getInstance().getNetDvrTime().ToString();
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
                        dateTimes = HikVisionUtils.getInstance().getNetDvrTime().ToString();
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
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().remove(sendThread);
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
                if (proofOrder()) listenerCallBack.receiveSuccess(order);
                else listenerCallBack.onErrMsg(ERR_ORDER_05H);
                break;
            case ORDER_06H:
                changeMasterStationInfo(mReceiveData);
                break;
            case ORDER_07H:
                //查询主站IP,端口号，卡号
                if (proofOrder()) listenerCallBack.receiveSuccess(order);
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
                break;
            case ORDER_21H:
                break;
            case ORDER_30H:
                break;
            case ORDER_71H:
                break;
            case ORDER_72H:
                break;
            case ORDER_73H:
                break;
            case ORDER_74H:
                break;
            case ORDER_75H:
                break;
            case ORDER_76H:
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
                handlerTonicPack(mReceiveData, ORDER_85H, ORDER_86H);
                break;
            case ORDER_88H:
                handlerRemoteAdjustment();
                break;
            case ORDER_89H:
                break;
            case ORDER_8AH:
                break;
            case ORDER_8BH:
                queryPhotoSchedule();
                break;
            case ORDER_93H:
                break;
            case ORDER_94H:
                break;
            case ORDER_95H:
                break;
            case ORDER_96H:
                break;
            case ORDER_97H:
                break;
        }
        mReceiveData = null;
        mSendData = null;
    }

    /**
     * 开机联络信息
     */
    protected void handlerBootContactInfo(byte[] mReceiveData) {
        if (proofOrder()) {
            listenerCallBack.receiveSuccess(order);
        } else if (!proofOrder()) {
            listenerCallBack.onErrMsg(ERR_ORDER_00H);
        } else if (mReceiveData != null) {
            originalCommandData = mReceiveData;
            setOrder(ORDER_00H);
            sendPack();
        }
    }

    /**
     * 主机主动发下 校时 01H
     *
     * @param mReceiveData
     */
    protected void handlerSchoolTime(byte[] mReceiveData) {
//        setTimer(false, TWO_MINUTE);
        if (dateTime != null && dateTimes != null) {
            Date date = new Date(dateTime);//发送请求的时间
            Date dates = new Date(dateTimes);//接受到返回值的时间
            int handlerOrder = getTimeDelta(date, dates);
            if (handlerOrder < 20) {

                boolean setTimer = listenerCallBack.setDvrTime(mReceiveData[10] + 2000,
                        mReceiveData[11],
                        mReceiveData[12],
                        mReceiveData[13],
                        mReceiveData[14],
                        mReceiveData[15]);

                if (!setTimer) {
                    listenerCallBack.onErrMsg(ERR_ORDER_01H);
                } else if (proofOrder()) listenerCallBack.receiveSuccess(order);
            } else {
                listenerCallBack.onErrMsg(ERR_ORDER_01H);
            }
            dateTime = null;
            dateTimes = null;
        } else if (mReceiveData != null) {//被动校时
            listenerCallBack.setDvrTime(mReceiveData[10] + 2000,
                    mReceiveData[11],
                    mReceiveData[12],
                    mReceiveData[13],
                    mReceiveData[14],
                    mReceiveData[15]);

            //被动校时
            originalCommandData = mReceiveData;
            setOrder(ORDER_01H);
            sendPack();
//            listenerCallBack.receiveSuccess(order);
        }
    }

    /**
     * 设置终端密码
     */
    protected void setTerminalPassword(byte[] mReceiveData) {
        oldPassword = Arrays.toString(new byte[]{mReceiveData[10],
                mReceiveData[11], mReceiveData[12], mReceiveData[13]});
        newPassword = Arrays.toString(new byte[]{mReceiveData[14],
                mReceiveData[15], mReceiveData[16], mReceiveData[17]});
        mReceiveDatas = mReceiveData;
        listenerCallBack.receiveSuccess(order);
    }

    /**
     * 主站下发参数配置 03H
     */
    protected void handlerMasterStationParamConfig(byte[] mReceiveData) {
        mReceiveDatas = mReceiveData;
        password = Arrays.toString(new byte[]{mReceiveData[10], mReceiveData[11], mReceiveData[12], mReceiveData[13]});//密码
        HeartbeatInterval = mReceiveData[14];//心跳间隔
        SamplingInterval = mReceiveData[15] + mReceiveData[16];//采样间隔
        TheSleepTime = mReceiveData[17] + mReceiveData[18];//休眠间隔
        TheOnlineTime = mReceiveData[19] + mReceiveData[20];//在线时长
        HardwareResetTime = new byte[]{mReceiveData[21], mReceiveData[22], mReceiveData[23]};//硬件重启时间点
        CipherCertification = new byte[]{mReceiveData[24], mReceiveData[25], mReceiveData[26], mReceiveData[27]};//密文认证
        listenerCallBack.receiveSuccess(order);
    }

    /**
     * 更改主站IP地址、端口号和卡号 06H
     */
    protected void changeMasterStationInfo(byte[] mReceiveData) {
        originalCommandData = mReceiveData;
        setOrder(ORDER_06H);
        sendPack();

        if (judges) {
            if (judge) {
                dataDomain = new byte[]{mReceiveData[10], mReceiveData[11],
                        mReceiveData[12], mReceiveData[13], mReceiveData[14],
                        mReceiveData[15], mReceiveData[16], mReceiveData[17], mReceiveData[18],
                        mReceiveData[19], mReceiveData[20], mReceiveData[21], mReceiveData[22],
                        mReceiveData[23], mReceiveData[24], mReceiveData[25], mReceiveData[26],
                        mReceiveData[27], mReceiveData[28], mReceiveData[29], mReceiveData[30],
                        mReceiveData[31], mReceiveData[32], mReceiveData[33], mReceiveData[34],
                        mReceiveData[35], mReceiveData[36], mReceiveData[37]};
            } else {
                dataDomain = HttpOrPortCarNumber;
            }
        } else {
            dataDomain = Password_Mistake_VERSION;
        }
        if (proofOrder()) listenerCallBack.receiveSuccess(order);
        else listenerCallBack.onErrMsg(ERR_ORDER_05H);
    }

    /**
     * 终端复位
     */
    protected void handlerTerminalReset(byte[] mReceiveData) {
        String NowPassword = String.valueOf(mReceiveData[10] + mReceiveData[11] + mReceiveData[12] + mReceiveData[13]);
        if (NowPassword.equals("1234")) {
            dataDomain = new byte[]{mReceiveData[10], mReceiveData[11],
                    mReceiveData[12], mReceiveData[13]};
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

                originalCommandData = mReceiveData;
            } else {
                originalCommandData = null;
                dataDomain = Password_Mistake_VERSION;
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
        String password = getReceivePassword(mReceiveData);
        if (password == null) {
            listenerCallBack.onErrMsg(ORDER_82H);
            return;
        }
        scheduleBos = new ByteArrayOutputStream();
        if (password.equals(terminalPassword)) {
            handler.removeCallbacks(runnable);
            channelNum = receiveData[14];
            timeArray = new int[receiveData[15]];
            presetGroup = new int[receiveData[15]];
            scheduleBos.write(channelNum);
            scheduleBos.write(receiveData[15]);
            for (int i = 0; i < receiveData[15]; i++) {
                presetGroup[i] = receiveData[18 + i * 3];
                scheduleBos.write(receiveData[16 + i * 3]);
                scheduleBos.write(receiveData[17 + i * 3]);
                scheduleBos.write(receiveData[18 + i * 3]);
                int hour = receiveData[16 + i * 3];
                if (hour == 0) hour = 24;
                timeArray[i] = (hour * 60 + receiveData[17 + i * 3]) * 60000;
                for (int j = 0; j < timeArray.length; j++) {
                    if (timeArray[i] < timeArray[j]) {
                        int replaceNumb = timeArray[i];
                        timeArray[i] = timeArray[j];
                        timeArray[j] = replaceNumb;
                    }
                }
            }


            int delayTime = -1;
            for (int i = 0; i < receiveData[15]; i++) {
                delayTime = timeArray[i] - getDelayTime();
                if (delayTime >= 0) {
                    countLoop = i;
                    break;
                }
            }
            if (delayTime == 0 && channelNum == 1)
                listenerCallBack.setPreset(presetGroup[countLoop]);
            if (delayTime >= 0) handler.postDelayed(runnable, delayTime);
            else {
                countLoop = 0;
                handler.postDelayed(runnable, 24 * 3600000 - (getDelayTime() - timeArray[countLoop]));
            }
        }

    }


    /**
     * 拍照时间表定时器
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            countLoop++;
            if (countLoop == timeArray.length) {
                countLoop = -1;
                handler.postDelayed(this, 24 * 3600000);
                return;
            }
            int delayTime = timeArray[countLoop] - getDelayTime();
            handler.postDelayed(this, delayTime);
            if (channelNum == 1)
                listenerCallBack.setPreset(presetGroup[countLoop]);
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
        }
    }

    /**
     * 上传文件数据,上传完后,2秒后发送上传结束标记
     *
     * @param order1 上传指令
     * @param order2 结束指令
     */
    protected void handlerUploadPicture(byte order1, byte order2) {
        try {
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
                int pack_count = tonicPackData[12];
                Log.e("需要补的包", "handlerTonicPack: " + pack_count);
                if (pack_count > 0) {
                    isUpLocal = true;
                    int count = pack_count;
                    int len = 0;
                    byte[] buf = new byte[MAX_UPLOAD_IMAGE_SIZE];

                    FileInputStream fis = new FileInputStream(pictureFile);
                    int readCount = 0;
                    while (count-- > 0) {
                        byte bytePackLow = tonicPackData[14 + len * 2];
                        int pack_high = tonicPackData[13 + len * 2];
                        int pack_low = (bytePackLow < 0) ? (bytePackLow & 0xFF) : bytePackLow;

                        int packIndex = pack_high * UPLOAD_IMAGE_PACK_DIVISOR + pack_low;

                        if (packIndex > 0) {
                            int read;
                            while ((read = fis.read(buf)) != -1) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                SystemClock.sleep(100);
                                readCount++;
                                if (packIndex == readCount) {
                                    baos.write(channelNum);
                                    baos.write(preset);
                                    baos.write((byte) pack_high);
                                    baos.write((byte) pack_low);
                                    baos.write(buf, 0, read);
                                    dataDomain = baos.toByteArray();
                                    setOrder(order1);
                                    sendPack();
                                    Log.e("补包packIndex", "tonicPack: " + packIndex + "," + buf.length);
                                    baos.close();
                                    break;
                                }
                            }
                        }
                        len++;
                    }

                    fis.close();
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
            originalCommandData = null;
            dataDomain = Password_Mistake_VERSION;
        }
        setOrder(ORDER_88H);
        sendPack();
    }

    /**
     * 查询拍照时间表 8BH
     */
    protected void queryPhotoSchedule() {

        try {
            originalCommandData = null;
            if (channelNum == mReceiveData[10]) {
                dataDomain = scheduleBos.toByteArray();
            } else {
                dataDomain = new byte[]{};
            }
            setOrder(ORDER_8BH);
            sendPack();
            scheduleBos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //计算两个日期时间差
    private static int getTimeDelta(Date date1, Date date2) {
        long timeDelta = (date1.getTime() - date2.getTime()) / 1000;//单位是秒
        int secondsDelta = timeDelta > 0 ? (int) timeDelta : (int) Math.abs(timeDelta);
        return secondsDelta;
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
}
