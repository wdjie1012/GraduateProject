package sendMesToCar;

import message.TcpMessage;
import message.TcpipFunc;

public class StereoBack implements TcpipFunc {
    private byte cmd;
    private byte data;

    public StereoBack() {
        this.cmd = TcpMessage.CMD_FUNCTION;
        this.data = TcpMessage.DATA_STEREO_BACK;
    }

    @Override
    public byte[] send() {
        return TcpMessage.send(this.cmd, this.data);
    }
}
