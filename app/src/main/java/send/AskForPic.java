package send;

public class AskForPic {
    private byte head;    //帧头
    private byte cmd;     //辨识符
    private byte length;   //长度
    private byte data;    //坐标
    private byte back;    //帧尾


    public AskForPic() {
        this.head = (byte) 0xAA;
        this.cmd = (byte) 0x01;
        this.length = 1;
        this.data = 1;
        this.back = (byte) 0x77;
    }

    public byte getHead() {
        return head;
    }

    public byte getCmd() {
        return cmd;
    }

    public byte getLength() {
        return length;
    }

    public byte getData() {
        return data;
    }

    public byte getBack() {
        return back;
    }
}
