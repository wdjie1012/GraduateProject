package sendMesToCar;

import message.TcpMessage;
import message.TcpipFunc;

public class RequestPbstream implements TcpipFunc {

    private byte cmd;
    private byte data;

    public RequestPbstream() {
        this.cmd = TcpMessage.CMD_FUNCTION;
        this.data = TcpMessage.DATA_REQUEST_PBSTREAM;
    }

    @Override
    public byte[] send() {
        return TcpMessage.send(this.cmd, this.data);
    }
}
