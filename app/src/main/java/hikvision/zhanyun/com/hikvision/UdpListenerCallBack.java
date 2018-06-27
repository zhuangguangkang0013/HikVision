package hikvision.zhanyun.com.hikvision;

import java.util.List;

/**
 * Created by ZY004Engineer on 2018/6/12.
 */

public interface UdpListenerCallBack {

    /**
     * 发送udp成功
     */
    void sendSuccess(byte order);

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
     * 用于上传照片
     *
     * @return 取对应文件数据，以指定的字节放入list数组
     */
    List<byte[]> getFileData();

    /**
     * 用于上传照片
     *
     * @return 获取图片一共包数，以4000字节切割的
     */
    int getPackIndex();

}
