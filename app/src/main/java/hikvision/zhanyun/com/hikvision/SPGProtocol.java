package hikvision.zhanyun.com.hikvision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import com.hikvision.netsdk.NET_DVR_TIME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

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
    public static final byte ORDER_86H = (byte) 0x86;
    public static final byte ORDER_85H = (byte) 0x85;

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

    //终端心跳信息 05H
    private byte[] signalRecordingTime = new byte[5];
    private byte[] signalStrength = new byte[0];
    private byte[] batteryVoltage = new byte[0];

    //上传图像数据 85H


    //主站下发参数配置 03H

    // 图像采集参数配置
    public final byte[] IMAGE_CONFIG_CONTROL_CHAR = {0x68};


    public final byte[] IMAGE_CONFIG_DATA_FIELD = new byte[]{};
    private final byte[] IMAGE_CONFIG_CHECK_CODE = new byte[]{};

    //    private final byte[]
//校时功能
//摄像机远程调节
//    public final byte[] CAMERA_START_CHAR = {0x68};
//    public final byte[] CAMERA_END_CHAR = {(byte) 0x88};
    public final byte[] CAMERA_POWERON_CHAR = {0x00};
    public final byte[] CAMERA_POWERON_VERSION = {0x01, 0x02};
    //    String password = "1234";
//    public final byte[] CAMERA_DATA = {Byte.parseByte(password), 0x01, 0x02};
    private final byte[] WHEN_THE_SCHOOL_START_CHAR = {0x68};

    // ....

    private DatagramSocket socket = null;
    private String Server;
    private int Port;
    private String deviceID;
    private InetSocketAddress addr;

    private UdpListenerCallBack listenerCallBack;

    //判断发送数据与接收数据相同
    private byte[] mSendData;
    private byte[] mReceiveData;

    public byte order;

    public void setOrder(byte order) {
        this.order = order;
        controlChar = new byte[]{order};
    }

    /**
     * @return 返回所需要的数据
     */
    private byte[] result() {
        // 数据存储区
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream buf_stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        try {

            out.write(deviceID.getBytes());
            out.write(controlChar);
            baleDataChar(out);
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
     * 打包 数据域
     *
     * @param outputStream
     */
    private void baleDataChar(DataOutputStream outputStream) {
        try {
            switch (order) {
                case ORDER_00H:
                    outputStream.writeShort(VERSION.length);
                    outputStream.write(VERSION);
                    break;
                case ORDER_01H:
                    outputStream.writeShort(WHEN_THE_SCHOOL_VERSION.length);
                    outputStream.write(WHEN_THE_SCHOOL_VERSION);
                    break;
                case ORDER_05H:
                    if (HikVisionUtils.getInstance().getNetDvrTime() != null) {
                        NET_DVR_TIME netDvrTime = HikVisionUtils.getInstance().getNetDvrTime();

                        signalRecordingTime = new byte[]{(byte) (netDvrTime.dwYear - 2000)
                                , (byte) netDvrTime.dwMonth, (byte) netDvrTime.dwDay
                                , (byte) netDvrTime.dwHour, (byte) netDvrTime.dwMinute
                                , (byte) netDvrTime.dwSecond};
                    }
                    short signalLength = 8;
                    outputStream.writeShort(signalLength);
                    outputStream.write(signalRecordingTime);

                    if (listenerCallBack != null) {

                        outputStream.write(listenerCallBack.getSignalStrength());
                        outputStream.write(listenerCallBack.getBatterVoltage());
                    }
                    break;
                case ORDER_85H:

                    Bitmap bitmap = BitmapFactory.decodeFile(HikVisionUtils.FILE_PATH);

                    short pictureLength = 18;
                    outputStream.writeShort(pictureLength);
                    outputStream.write(new byte[]{1});
                    outputStream.write(new byte[]{(byte) 255});
                    outputStream.write(new byte[]{1});

//                    FileReader fr = new FileReader(HikVisionUtils.FILE_PATH);

                    ByteArrayInputStream inputStream = new ByteArrayInputStream(Bitmap2Bytes(bitmap));

                    while (inputStream.read() != -1) {
//                        DataOutputStream outputStream1 = new DataOutputStream(byteArrayOutputStream);
//                        fr.read(outputStream1., 0, Bitmap2Bytes(bitmap).length);
                        Log.e("1233", "baleDataChar: " + inputStream.read(Bitmap2Bytes(bitmap),0,4000));
                    }

                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
//    private void

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
    }

    /**
     * 初始参数的格式
     *
     * @param startChar 起始码 {0x68}
     * @param version   数据域（规约版本号）  {0x01, 0x02}
     * @param endChar   结束码 {0x16}
     */
    public void initFormat(byte[] startChar, byte[] version, byte[] endChar) {
        this.startChar = startChar;
        this.version = version;
        this.endChar = endChar;
    }


    /**
     * upd发送
     */
    public void PowerOn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    byte[] buf = result();

                    if (socket == null) socket = new DatagramSocket();

                    DatagramPacket outPacket = new DatagramPacket(buf, buf.length, addr);

                    socket.send(outPacket);

                    SystemClock.sleep(10);
                    listenerCallBack.sendSuccess();
                    mSendData = buf;
                } catch (IOException e) {
                    e.printStackTrace();
                    listenerCallBack.onErrMsg(e.toString());
                }
            }
        }).start();

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
    public void receive() {
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
                        handlerOrder(mReceiveData[7]);
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

        switch (order) {
            case ORDER_00H:
                if (proofOrder()) listenerCallBack.receiveSuccess(order);
                else listenerCallBack.onErrMsg("-1");
                break;
            case ORDER_01H:
                if (proofOrder()) listenerCallBack.receiveSuccess(order);
                else listenerCallBack.onErrMsg("-1");
                //TODO 操作。。。
                break;
            case ORDER_02H:
                listenerCallBack.receiveSuccess(order);
                String oldPassword = String.valueOf(mReceiveData[10] + mReceiveData[11] + mReceiveData[12] + mReceiveData[13]);

                if (oldPassword.equals("1234")) {
                    listenerCallBack.receiveSuccess(order);
                }
                break;
            case ORDER_03H:
                break;
            case ORDER_05H:
                if (proofOrder()) listenerCallBack.receiveSuccess(order);
                break;
            //
        }

    }

    /**
     * 验证发送数据与接收数据是否一致
     *
     * @return true:一致 否则false
     */
    private Boolean proofOrder() {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            if (mReceiveData[i] == mSendData[i]) {
                count++;
            }
        }
        return count == 8;
    }
}
