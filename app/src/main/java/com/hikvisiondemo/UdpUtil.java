package ziyouniao.zhanyun.com.hikvisiondemo;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by ZY004Engineer on 2018/6/5.
 * udp工具类
 */

public class UdpUtil {
    private String TAG = UdpUtil.class.getSimpleName();
    private String SEND_IP = "171.221.207.59";
    private int SEND_PORT = 17116;               //发送端口号
    private int RECEIVE_PORT = 8989;            //接收端口号
    private boolean listenReceiveStatus = true;
    public String BUNDLE_UDP_RECEIVE_MESSAGE = "updReceiveMessage";
    private DatagramSocket receiveSocket;
    private static UdpUtil instance = null;
    private String receiveMsg;
    private static UdpListenerCallBack listenerCallBack;

    public static UdpUtil getInstance(UdpListenerCallBack listenerCallBack) {
        UdpUtil.listenerCallBack = listenerCallBack;
        if (instance == null) {
            synchronized (UdpUtil.class) {
                if (instance == null) {
                    instance = new UdpUtil();
                }
            }
        }
        return instance;
    }

    public void setSendIp(String sendIp) {
        SEND_IP = sendIp;
    }

    public void setSendPort(int sendPort) {
        SEND_PORT = sendPort;
    }

    public void setReceivePort(int receivePort) {
        RECEIVE_PORT = receivePort;
    }

    /**
     * 开始udp 接收循环
     *
     * @param listenReceiveStatus false:不循环 true:循环 默认true
     */
    public void setListenReceiveStatus(boolean listenReceiveStatus) {
        this.listenReceiveStatus = listenReceiveStatus;
    }

    /**
     * udp 发送
     *
     * @param msg 需要发送的信息
     */
    public void sendMsg(String msg) {
        receiveMsg = msg;
        new UdpSendThread().start();
    }

    /**
     * udp 接收
     */
    public void receiveMsg() {
        new UpdReceiveThread().start();
    }

    private byte crc(byte[] data) {
        int r = 0;
        byte b = 0;
        for (int i = 0; i < data.length; i++) r += data[i];
        b = (byte) (r & 0x000F);
        b = (byte) ~b;
        return b;
    }

    /**
     * UDP数据发送线程
     */
    private class UdpSendThread extends Thread {

        @Override
        public void run() {

            try {
                // 数据存储区
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ByteArrayOutputStream buf_stream = new ByteArrayOutputStream();

                String device_id = "123456";
                byte[] control_char = {0x00};
                byte[] version = {0x02, 0x01};
                byte[] START_CHAR = {0x68};
                byte[] END_CHAR = {0x16};

                bos.write(device_id.getBytes());
                bos.write(control_char);
                bos.write(version.length);
                bos.write(version);
                byte[] c = {crc(bos.toByteArray())};

                buf_stream.write(START_CHAR);
                buf_stream.write(bos.toByteArray());
                buf_stream.write(c);
                buf_stream.write(END_CHAR);
                byte[] buf = buf_stream.toByteArray();
                // 创建DatagramSocket对象，使用随机端口
                DatagramSocket sendSocket = new DatagramSocket();
//                InetAddress serverAddress = Inet4Address.getByName(SEND_IP);
//                sendSocket.setBroadcast(true);
                DatagramPacket outPacket = new DatagramPacket(buf, buf.length, SEND_PORT);
                sendSocket.send(outPacket);

//                while (true)
//                {
//                    sendSocket.receive();
//                }
                sendSocket.close();
                listenerCallBack.sendSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "UdpSendThread: ", e);
                listenerCallBack.onErrMsg(e.toString());
            }
        }
    }

    /**
     * UDP数据接受线程
     */
    private class UpdReceiveThread extends Thread {

        @Override
        public void run() {
            try {
                receiveSocket = new DatagramSocket();
//                InetAddress serverAddress = Inet4Address.getByName(SEND_IP);

                while (listenReceiveStatus) {
                    byte[] inBuf = new byte[1024];
                    DatagramPacket inPacket = new DatagramPacket(inBuf, inBuf.length);
                    receiveSocket.receive(inPacket);
//                    if (!inPacket.getAddress().equals(serverAddress)) {
//                        throw new IOException("未知名的报文");
//                    }
                    String result = new String(inPacket.getData()).trim();

                    listenerCallBack.receiveSuccess(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "UpdReceiveThread: ", e);
                if (!receiveSocket.isClosed())
                    listenerCallBack.onErrMsg(e.toString());
            }

        }
    }


    /**
     * 关闭 udp 接收Socket
     */
    public void closeUpdReceive() {
        if (receiveSocket != null) {
            listenReceiveStatus = false;
            receiveSocket.close();
        }

    }
}
