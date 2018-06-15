package ziyouniao.zhanyun.com.hikvisiondemo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Created by ZY004Engineer on 2018/6/12.
 */

public class SPGProtocol extends Thread {
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
    public final byte[] CAMERA_START_CHAR = {0x68};
    public final byte[] CAMERA_END_CHAR = {(byte) 0x88};
    public final byte[] CAMERA_POWERON_CHAR = {0x00};
    public final byte[] CAMERA_POWERON_VERSION = {0x01, 0x02};
//    String password = "1234";
//    public final byte[] CAMERA_DATA = {Byte.parseByte(password), 0x01, 0x02};

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
     * @param startChar   起始码 {0x68}
     * @param controlChar 控制字 {0x00}
     * @param version     数据域（规约版本号）  {0x01, 0x02}
     * @param endChar     结束码 {0x16}
     */
    public void initFormat(byte[] startChar, byte[] controlChar, byte[] version, byte[] endChar) {
        this.startChar = startChar;
        this.controlChar = controlChar;

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
                // 数据存储区
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ByteArrayOutputStream buf_stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(bos);

                try {
                    out.write(deviceID.getBytes());
                    out.write(controlChar);

                    short len = (short) version.length;

                    out.writeShort(len);
                    out.write(version);
                    byte[] c = {Crc(bos.toByteArray())};

                    buf_stream.write(startChar);
                    buf_stream.write(bos.toByteArray());
                    buf_stream.write(c);
                    buf_stream.write(endChar);

                    byte[] buf = buf_stream.toByteArray();

                    if (socket == null) socket = new DatagramSocket();

                    DatagramPacket outPacket = new DatagramPacket(buf, buf.length, addr);

                    socket.send(outPacket);

                    listenerCallBack.sendSuccess();

                    mSendData = buf;
                    bos.close();
                    buf_stream.close();
                    out.close();
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
        }
        ).start();

    }

    /**
     * 处理命令
     *
     * @param order 命令
     */
    private void handlerOrder(byte order) {

        switch (order) {
            case 0x00:
                int count = 0;
                for (int i = 0; i < 8; i++) {
                    if (mReceiveData[i] == mSendData[i]) {
                        count++;
                    }
                }
                if (count == 8) listenerCallBack.receiveSuccess(null);
                else listenerCallBack.onErrMsg("-1");
                break;
            case 0x01:
                //TODO 操作。。。
                break;
            //...
        }

    }

}
