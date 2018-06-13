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

public class SPGrotocol extends Thread {
    final byte[] START_CHAR = {0x68};
    final byte[] END_CHAR = {0x16};
    final byte[] POWERON_CHAR = {0x00};
    final int MAX_PACKET_LENGTH = 5000;
    final byte[] VERSION = {0x01, 0x02};
    // ....

    private DatagramSocket socket = null;
    private String Server;
    private int Port;
    private String DeviceID;
    private InetSocketAddress addr;

    public void Init(final String host,final int port, final String id)
    {
        Server = host;
        Port = port;
        DeviceID = id;
        addr = new InetSocketAddress(Server, Port);

        try {
            if (socket == null) socket = new DatagramSocket();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void PowerOn()
    {
        // 数据存储区
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream buf_stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        try {
            out.write(DeviceID.getBytes());
            out.write(POWERON_CHAR);

            short len = (short)VERSION.length;
            out.writeShort(len);
            out.write(VERSION);
            byte[] c = { Crc(bos.toByteArray()) };

            buf_stream.write(START_CHAR);
            buf_stream.write(bos.toByteArray());
            buf_stream.write(c);
            buf_stream.write(END_CHAR);

            byte[] buf = buf_stream.toByteArray();

            DatagramPacket outPacket = new DatagramPacket(buf, buf.length, addr);

            socket.send(outPacket);

//            while (true) {
//            	System.out.println("aaaaaa");
//	            byte[] recvbuf = new byte[512];
//	            DatagramPacket recvPacket = new DatagramPacket(recvbuf, recvbuf.length);
//	            try {
//	                socket.receive(recvPacket);
//
//	                System.out.println(recvPacket.getLength());
//	                System.out.println(recvPacket.getData());
//	                sleep(10);
//	            } catch (IOException e) {
//	                e.printStackTrace();
//	            } catch (InterruptedException e) {
//	                e.printStackTrace();
//	            }
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte Crc(byte[] data) {
        int r = 0;
        byte b = 0;
        for (int i = 0; i < data.length; i++) r += data[i];
        b = (byte) (r & 0x00FF);
        b = (byte) ~b;
        return b;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            if (socket==null) continue;

            byte[] buf = new byte[MAX_PACKET_LENGTH];
            DatagramPacket recvPacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(recvPacket);


                System.out.println(recvPacket.getLength());
                System.out.println(recvPacket.getData());
                sleep(10);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
