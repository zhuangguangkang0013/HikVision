package hikvision.zhanyun.com.hikvision;

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
     * @param order 服务器发送的信息
     */
    void receiveSuccess(byte order);

    /**
     * @param message 错误信息
     */
    void onErrMsg(int message);

    /**
     * 信号强度
     *
     * @return 信号强度值
     */
    byte getSignalStrength();

    /**
     * 蓄电池电压
     * @return 蓄电池电压值
     */
    byte getBatterVoltage();
}
