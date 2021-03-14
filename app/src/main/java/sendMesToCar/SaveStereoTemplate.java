package sendMesToCar;

import message.TcpMessage;
import message.TcpipFunc;

public class SaveStereoTemplate implements TcpipFunc {
    private byte cmd;
    private byte data;

    public SaveStereoTemplate() {
        this.cmd = TcpMessage.CMD_FUNCTION;
        this.data = TcpMessage.DATA_SAVE_STEREO_TEMPLATE;
    }

    @Override
    public byte[] send() {
        return TcpMessage.send(this.cmd, this.data);
    }

}
