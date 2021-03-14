package message;

import sendMesToCar.EndMapSend;
import sendMesToCar.LaunchLaserMode;
import sendMesToCar.LaunchStereoMode;
import sendMesToCar.RequestPbstream;
import sendMesToCar.RequestSlam;
import sendMesToCar.SaveLaserTemplate;
import sendMesToCar.SaveSlamMap;
import sendMesToCar.SaveStereoTemplate;
import sendMesToCar.StereoBack;
import sendMesToCar.Stop;

public class TcpSend {


    public static TcpipFunc sendMessage(byte cmd, byte data) {
        switch (cmd) {
            case TcpMessage.CMD_MAIN_FUNCTION: //01
            {
                switch (data) {
                    case TcpMessage.DATA_REQUEST_SLAM://请求slam建图模式
                        return new RequestSlam();
                    case TcpMessage.DATA_END_MAP_SEND://请求停止建图
                        return new EndMapSend();
                    case TcpMessage.DATA_REQUEST_LASER_SLAM://激光导航
                        return new LaunchLaserMode();
                    case TcpMessage.DATA_REQUEST_STEREO_SLAM://视觉导航
                        return new LaunchStereoMode();
                }
            }
            case TcpMessage.CMD_FUNCTION:  //05
            {
                switch (data) {
                    case TcpMessage.DATA_REQUEST_PBSTREAM://请求pbstream文件
                        return new RequestPbstream();
                    case TcpMessage.DATA_SAVE_MAP://请求保存当前地图
                        return new SaveSlamMap();
                    case TcpMessage.DATA_STOP:    //停止动作
                        return new Stop();
                    case TcpMessage.DATA_STEREO_BACK://视觉模式倒车
                        return new StereoBack();
                    case TcpMessage.DATA_SAVE_LASER_TEMPLATE://保存激光建图模版
                        return new SaveLaserTemplate();
                    case TcpMessage.DATA_SAVE_STEREO_TEMPLATE://保存视觉建图模版
                        return new SaveStereoTemplate();
                }
            }
            return new RequestPbstream();
            default:
                return null;
        }
    }
}
