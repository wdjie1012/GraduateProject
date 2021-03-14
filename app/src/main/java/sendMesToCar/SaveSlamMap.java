package sendMesToCar;

import message.TcpMessage;
import message.TcpipFunc;

public class SaveSlamMap implements TcpipFunc {

    private byte cmd;
    private byte data;

    public SaveSlamMap() {
        this.cmd = TcpMessage.CMD_FUNCTION;
        this.data = TcpMessage.DATA_SAVE_MAP;
    }

    @Override
    public byte[] send() {
        return TcpMessage.send(this.cmd, this.data);
    }
}
