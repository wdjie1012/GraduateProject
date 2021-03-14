package sendMesToCar;

import message.TcpMessage;
import message.TcpipFunc;

public class Stop implements TcpipFunc {
    private byte cmd;
    private byte data;

    public Stop() {
        this.cmd = TcpMessage.CMD_FUNCTION;
        this.data = TcpMessage.DATA_STOP;
    }

    @Override
    public byte[] send() {
        return TcpMessage.send(this.cmd, this.data);
    }
}
