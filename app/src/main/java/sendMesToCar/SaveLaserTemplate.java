package sendMesToCar;

import message.TcpMessage;
import message.TcpipFunc;

public class SaveLaserTemplate implements TcpipFunc {
    private byte cmd;
    private byte data;

    public SaveLaserTemplate() {
        this.cmd = TcpMessage.CMD_FUNCTION;
        this.data = TcpMessage.DATA_SAVE_LASER_TEMPLATE;
    }

    @Override
    public byte[] send() {
        return TcpMessage.send(this.cmd, this.data);
    }
}
