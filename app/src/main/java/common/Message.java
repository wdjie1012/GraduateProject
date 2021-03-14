package common;

import java.util.Arrays;

public class Message {


    public byte head;
    public byte cmd;
    public byte len;
    public byte[] data;
    public byte tail;
    public byte[] datas;

    public Message(){

    }

    @Override
    public String toString() {
        return "Message{" +
                "head=" + head +
                ",\n cmd=" + cmd +
                ",\n len=" + len +
                ",\n data=" + Arrays.toString(data) +
                ",\n datas=" + Arrays.toString(datas) +
                ",\n tail=" + tail +
                '}';
    }
}
