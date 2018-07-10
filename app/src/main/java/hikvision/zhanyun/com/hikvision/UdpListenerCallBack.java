package hikvision.zhanyun.com.hikvision;

import java.security.PublicKey;

/**
 * Created by ZY004Engineer on 2018/6/12.
 */

public interface UdpListenerCallBack {

    /**
     * 发送udp成功
     */
    public void sendSuccess(byte order);

    /**
     * 接受udp成功
     *
     * @param order 服务器发送的信息
     */
    public void receiveSuccess(byte order);

    /**
     * @param message 错误信息
     */
    public void onErrMsg(int message);

    /**
     * 摄像机远程调节
     *
     * @param channelNum  通道号
     * @param order       指令
     * @param preposition 预置位
     */
    public void remoteAdjustmentCamera(int channelNum, int order, int preposition);

    /**
     * 使用通道号1
     *
     * @param colorSelection 色彩选择
     * @param imageSize      图像大小
     * @param brightness     亮度
     * @param contrast       对比度
     * @param saturation     饱和度
     */
    public void useChannelNumOne(int colorSelection, int imageSize, int brightness, int contrast, int saturation);

    /**
     * 使用通道号2
     *
     * @param colorSelection 色彩选择
     * @param imageSize      图像大小
     * @param brightness     亮度
     * @param contrast       对比度
     * @param saturation     饱和度
     */
    public void useChannelNumTwo(int colorSelection, int imageSize, int brightness, int contrast, int saturation);

    /**
     * 设置设备时间
     *
     * @return
     */
    public boolean setDvrTime(int dwYear, int dwMonth, int dwDay, int dwHour, int dwMinute, int dwSecond);

    /**
     * 获取设备时间
     *
     * @return
     */
    public String getDvrTime();

    /**
     * 设置预置点(只对通道号1有效)
     *
     * @param preset 预置点
     */
    public void setPreset(int preset);

    /**
     * 修改主站IP，端口，卡号
     *
     * @param port      端口
     * @param http      IP
     * @param simNumber 卡 号
     */
    public void modifyTheHostIPPortNumbers(String http, int port, byte[] simNumber);

    /**
     * 启动拍摄视频
     */
    public void startVideo();

    /**
     * 停止拍摄视频
     */
    public void stopVideo();

    /**
     * 开始拍摄小视频
     *
     * @param channel 通道号
     * @param type    通道类型
     * @param time    拍摄时长
     */
    public void startShortVideo(int channel, int type, int time);


}
