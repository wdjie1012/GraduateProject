package sendMesToCar;

import message.TcpMessage;
import message.TcpipFunc;

public class EndMapSend implements TcpipFunc {

    private byte cmd;
    private byte data;

    public EndMapSend() {
        this.cmd = TcpMessage.CMD_MAIN_FUNCTION;
        this.data = TcpMessage.DATA_END_MAP_SEND;
    }

    @Override
    public byte[] send() {
        return TcpMessage.send(this.cmd, this.data);
    }
}
