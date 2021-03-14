package message;

/**
 * head   cmd   len   data  back
 * AA      ..                77
 * <p>
 * <p>
 * cmd   data
 * 01 -  00   遥控模式下停止发送图片
 * 01   请求遥控建图
 * 02   请求导航全图--激光
 * 03   双目视觉
 * 04   双目视觉+IMU
 * 05   请求导航全图--视觉
 * 02         摇杆
 * 03         设定速度
 * 04         手指点的坐标
 * 05    01   保存图片         //功能
 * 02   保存充电桩坐标
 * 03   停止动作
 * 04   请求发送pbstream文件
 * 05   记录充电桩点云模版
 * 06   保存双目地图
 * 40         小车用来导航的图
 * <p>
 * <p>
 * <p>
 * 80         获取当前地图点
 * 81         保存地图成功反馈(已不用)
 * 82    01   到达目的点
 * 83         电量
 * 84         vslam状态
 */
public class TcpMessage {
    public static byte HEAD = (byte) 0xAA;
    public static byte TAIL = (byte) 0x77;


    //----------------------cmd-------------------------------
    public static final byte CMD_MAIN_FUNCTION = (byte) 0x01;
    public static final byte DATA_END_MAP_SEND = 0;
    public static final byte DATA_REQUEST_SLAM = 1;
    public static final byte DATA_REQUEST_LASER_SLAM = 2;
    public static final byte DATA_REQUEST_STEREO_SLAM = 5;
    //----------------------cmd-------------------------------
    public static final byte CMD_FUNCTION = (byte) 0x05;
    public static final byte DATA_SAVE_MAP = 1;
    public static final byte DATA_STOP = 3;
    public static final byte DATA_REQUEST_PBSTREAM = 4;
    public static final byte DATA_SAVE_LASER_TEMPLATE = 5;
    public static final byte DATA_STEREO_BACK = 7;
    public static final byte DATA_SAVE_STEREO_TEMPLATE = 8;


    //-----------------------send function--------------------

    /**
     * 只有一位byte
     *
     * @param cmd
     * @param data
     */
    public static byte[] send(byte cmd, byte data) {
        byte send[] = new byte[5];
        send[0] = HEAD;
        send[1] = cmd;
        send[2] = 1;
        send[3] = data;
        send[4] = TAIL;
        return send;
    }

}
