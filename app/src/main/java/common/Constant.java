package common;

public class Constant {
    public static final int REGION_RADIUS=20;
    public static final int DOCK_X_GAIN=8;
    public static final int DOCK_Y_GAIN=5;

    public static final byte HEAD = (byte) 0xAA ;
    public static final byte TAIL = (byte) 0x77 ;


    //==========================send==========================//

    //-----------------CMD_COMMON_FUNCTION 0x20 --------------------//
    public static final byte CMD_COMMON_FUNC = 0x20;
    public static final byte LOAD_CAR_INFO = 0x01;
    public static final byte LOAD_MAP_INFO = 0x02;

    //-----------------CMD_COMMON_FUNCTION 0x21 --------------------//
    public static final byte CMD_CAR_COMMAND = 0x21;
    public static final byte RUN_ALL_CAR=0x01;


    //-----------------CMD_CHANGE_TASK 0x22 --------------------//
    public static final byte CMD_CHANGE_TASK = 0x22;


    //==========================receive==========================//
    public static final byte RECV_CAR_INFO = 0x50;
    public static final byte RECV_MAP_INFO = 0x51;
    public static final byte RECV_CAR_POS  = 0x52;
    public static final byte RECV_CAR_ELEC_MISSIONCNT = 0x53;


    public static byte[] getContent(byte cmd,byte data){

        //head*1|cmd*1|length*1|data*1|tail*1--->5

        byte[] send=new byte[5];
        send[0]=HEAD;
        send[1]=cmd;
        send[2]=(byte)1;
        send[3]=data;
        send[4]=TAIL;
        return send;
    }

    public static byte[] getContent(byte cmd,byte[] datas){
        //head*1|cmd*1|length*1|data*len|tail*1--->4+len
        int length=datas.length+4;
        byte[] send=new byte[length];
        send[0]=HEAD;
        send[1]=cmd;
        send[2]=(byte)datas.length;
        for(int i=0;i<datas.length;i++)
            send[3+i]=datas[i];
        send[length-1]=TAIL;

        return send;
    }
}
