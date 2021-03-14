package sendMesToCar;

import message.TcpMessage;
import message.TcpipFunc;

public class LaunchStereoMode implements TcpipFunc {
    private byte cmd;
    private byte data;

    public LaunchStereoMode() {
        this.cmd = TcpMessage.CMD_MAIN_FUNCTION;
        this.data = TcpMessage.DATA_REQUEST_STEREO_SLAM;
    }

    @Override
    public byte[] send() {
        return TcpMessage.send(this.cmd, this.data);
    }
}
