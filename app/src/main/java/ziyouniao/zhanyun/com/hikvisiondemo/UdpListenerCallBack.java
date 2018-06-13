package ziyouniao.zhanyun.com.hikvisiondemo;

/**
 * Created by ZY004Engineer on 2018/6/12.
 */

public interface UdpListenerCallBack {

    /**
     * 发送udp成功
     */
    void sendSuccess();

    /**
     * 接受udp成功
     *
     * @param message 服务器发送的信息
     */
    void receiveSuccess(String message);

    /**
     * @param message 错误信息
     */
    void onErrMsg(String message);
}
