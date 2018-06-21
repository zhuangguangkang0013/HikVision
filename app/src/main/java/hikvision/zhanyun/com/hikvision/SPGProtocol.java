package hikvision.zhanyun.com.hikvision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;

import com.hikvision.netsdk.NET_DVR_TIME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.text.TextUtils.isEmpty;

/**
 * Created by ZY004Engineer on 2018/6/12.
 */

public class SPGProtocol {

    public static final byte ORDER_00H =0x00;
    public static final byte ORDER_01H =0x01;
    public static final byte ORDER_02H =0x02;
    public static final byte ORDER_03H =0x03;
    public static final byte ORDER_04H =0x04;
    public static final byte ORDER_05H =0x05;
    public static final byte ORDER_06H =0x06;
    public static final byte ORDER_86H = (byte) 0x86;


    private HikVisionUtils hikVisionUtils = new HikVisionUtils();
    private String datetime;
    public String datetimes;
    private byte[] startChar = {0x68};
    private byte[] endChar = {0x16};
    private byte[] controlChar = {0x00};
    private int maxPacketLength = 5000;
    private byte[] version = {0x01, 0x02};

    //
    public final byte[] START_CHAR = {0x68};
    public final byte[] END_CHAR = {0x16};
    public final byte[] POWERON_CHAR = {0x00};
    public final byte[] VERSION = {0x01, 0x02};


    //摄像机远程调节
    //    public final byte[] CAMERA_START_CHAR = {0x68};
    //    public final byte[] CAMERA_END_CHAR = {(byte) 0x88};
    public final byte[] CAMERA_POWERON_CHAR = {0x00};
    public final byte[] CAMERA_POWERON_VERSION = {0x01, 0x02};


    //校时功能
    public  byte[] WHEN_THE_SCHOOL_VERSION = {};


    //设置终端密码 02H
    private final byte[] TERMINAL_PWD_ORDER = {0x02};

    //终端心跳信息 05H
    private byte[] signalRecordingTime = new byte[5];
    private byte[] signalStrength = new byte[0];
    private byte[] batteryVoltage = new byte[0];


    // 图像采集参数配置
    public final byte[] IMAGE_CONFIG_CONTROL_CHAR = {0x68};
    public final byte[] IMAGE_CONFIG_DATA_FIELD = new byte[]{};
    private final byte[] IMAGE_CONFIG_CHECK_CODE = new byte[]{};

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


    public byte[] order=new byte[]{};
    public void setOrder( byte[] order) {
        this.order = order;
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
            out.write(order);
            baleDataChar(out);
            byte[] checkCode = {Crc(bos.toByteArray())};
            buf_stream.write(startChar);
            buf_stream.write(bos.toByteArray());
            buf_stream.write(checkCode);
            buf_stream.write(endChar);
            buf_stream.close();
            return buf_stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

	private void test2(String s)
	{
	}
	
    /**
     * 打包 数据域
     *
     * @param outputStream
     */
    private void baleDataChar(DataOutputStream outputStream) {
        try {
            switch (order[0]) {
                case 0x00:
                    outputStream.writeShort(version.length);
                    outputStream.write(version);
                    break;
                case 0x01:
                    datetime = hikVisionUtils.getNetDvrTime().ToString();
                    outputStream.writeShort(WHEN_THE_SCHOOL_VERSION.length);
                    outputStream.write(WHEN_THE_SCHOOL_VERSION);
                    break;
                case 0x05:
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
                    listenerCallBack.sendSuccess();
                    mSendData = buf;


                    Log.i(TAG, "发送时间: "+datetime);
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
        b = (byte) (r & 0xFF);
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
                        if (order[0] == 0x00) {
                            if (receivePacket.getData() == null) {
                                listenerCallBack.onErrMsg("-1");
                            }
                        } else if (order[0] == 0x01) {
                            if (receivePacket.getData() == null) {
                                listenerCallBack.onErrMsg("-2");
                            }
                        }
                        mReceiveData = receivePacket.getData();

                        if(order[0] == 0x01||order[0] ==0x05)
                        datetimes = hikVisionUtils.getNetDvrTime().ToString();
                        handlerOrder(mReceiveData[7]);

                        Log.i(TAG, "接收时间: "+datetimes);
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
            case 0x00:
                if (proofOrder()) listenerCallBack.receiveSuccess(order);
                else listenerCallBack.onErrMsg("-1");
                break;
            case 0x01:
                if (proofOrder()) listenerCallBack.receiveSuccess(order);
                if(datetime!=null&&datetimes!=null){
                Date date = new Date(datetime);//发送请求的时间
                Date dates = new Date(datetimes);//接受到返回值的时间
                int handlerOrder = getTimeDelta(date,dates);
                Log.i(TAG, "handlerOrder: "+getTimeDelta(date,dates));
                Log.i(TAG, "handlerOrder: "+date);
                Log.i(TAG, "handlerOrder: "+dates);
               if(handlerOrder < 20){
                Boolean setTimer = hikVisionUtils.setDateTime(mReceiveData[10]+2000,
                                                                        mReceiveData[11],
                                                                        mReceiveData[12],
                                                                        mReceiveData[13],
                                                                        mReceiveData[14],
                                                                        mReceiveData[15]);
                Log.i(TAG, "run: "+hikVisionUtils.getNetDvrTime().ToString());
                if(!setTimer){
                    listenerCallBack.onErrMsg("-2");
                }
        }else {
                   listenerCallBack.onErrMsg("-2");

               }}
                break;
            case 0x02:
                listenerCallBack.receiveSuccess(order);
                String oldPassword = String.valueOf(mReceiveData[10] + mReceiveData[11] + mReceiveData[12] + mReceiveData[13]);

                if (oldPassword.equals("1234")) {
                    listenerCallBack.receiveSuccess(order);
                }
                break;
            case 0x03:

                break;
            case 0x05:
                listenerCallBack.receiveSuccess(order);
                    Boolean setTimer = hikVisionUtils.setDateTime(mReceiveData[10]+2000,
                            mReceiveData[11],
                            mReceiveData[12],
                            mReceiveData[13],
                            mReceiveData[14],
                            mReceiveData[15]);
                Log.i(TAG, "handlerOrder: "+setTimer);
                Log.i(TAG, "run: "+hikVisionUtils.getNetDvrTime().ToString());
                    break;
                //...
        }

    }
    //计算两个日期时间差
    public static int getTimeDelta(Date date1,Date date2){
        long timeDelta=(date1.getTime()-date2.getTime())/1000;//单位是秒
        int secondsDelta=timeDelta>0?(int)timeDelta:(int)Math.abs(timeDelta);
        return secondsDelta;
    }

	
	private Boolean Test() {
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
