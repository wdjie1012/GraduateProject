package socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.example.along.agvcontrol0412.MainActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Arrays;

import common.Constant;
import common.HandleMessage;
import common.Message;
import common.ToolKit;

/**
 * head   cmd   len   data  back
 * AA      ..                77
 *
 *
 * cmd    data
 * 20  -   01    载入车辆信息
 *     -   02    载入地图信息
 * 21  -   01    让所有车开始启动
 *
 *
 * 50  -         接受车辆初始化信息
 * 51  -         接受地图初始化信息
 * 52  -         接受车辆坐标信息
 *
 *
 */

public class TcpMultiRobots implements Runnable{

    private int port;
    private String hostIP;
    private Socket socket;
    protected DataInputStream in;
    protected DataOutputStream out;
    private boolean connect = false;
    private boolean runFlag;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayDeque<Message>msgQueue=new ArrayDeque<>();

    private static TcpMultiRobots mTcpMultiRobots = null;
    public static TcpMultiRobots getInstance() {
        if (mTcpMultiRobots == null) {
            synchronized (TcpMultiRobots.class) {
                if (mTcpMultiRobots == null) {
                    mTcpMultiRobots = new TcpMultiRobots();
                }
            }
        }
        return mTcpMultiRobots;
    }

    @Override
    public void run() {
        try{
            socket=new Socket(hostIP,port);
            runFlag=true;
            //消费者
            new Thread(this::consumeMsg).start();//消费者

            refreshUI(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        in = new DataInputStream(socket.getInputStream());
                        out = new DataOutputStream(socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                        runFlag = false;
                    }
                    while (runFlag) {

                        Message recv_msg=handleMessage();
                        if(recv_msg!=null) {
                            msgQueue.add(recv_msg);
                        }
                        if(msgQueue.size()>30){
                            msgQueue.clear();
                        }
                    }
                    try {
                        in.close();
                        out.close();
                        socket.close();

                        in = null;
                        out = null;
                        socket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connect = false;
                    refreshUI(false);
                }
            }).start();
            connect=true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Message handleMessage(){
        boolean returnFlag=false;
        Message message=new Message();
        try {
            byte[] headBuff = new byte[3];
            in.read(headBuff, 0, 3);
            message.head=headBuff[0];
            message.cmd=headBuff[1];
            message.len=headBuff[2];
            if(message.head== Constant.HEAD) {
                if (message.cmd == Constant.RECV_CAR_INFO) {
                    byte[] carCounts = new byte[1];
                    byte[] carInfoSize = new byte[1];
                    byte[] tail = new byte[1];
                    in.read(carCounts, 0, 1);
                    in.read(carInfoSize, 0, 1);
                    int size = carCounts[0] * carInfoSize[0];
                    byte[] recvBuff = new byte[size + 2];
                    recvBuff[0] = carCounts[0];
                    recvBuff[1] = carInfoSize[0];
                    in.read(recvBuff, 2, size);
                    in.read(tail, 0, 1);
                    message.tail = tail[0];
                    if (message.tail == Constant.TAIL) {
                        message.datas = new byte[size + 2];
                        message.datas = recvBuff;
                        returnFlag = true;
                    } else {
                        Log.w("RECV_CAR_INFO", "tail fail");
                    }
                } else if (message.cmd == Constant.RECV_MAP_INFO) {
                    byte[] widthBytes = new byte[4];
                    byte[] heightBytes = new byte[4];
                    byte[] tail = new byte[1];
                    in.read(widthBytes, 0, 4);
                    in.read(heightBytes, 0, 4);
                    int width = ToolKit.byteToInt(widthBytes);
                    int height = ToolKit.byteToInt(heightBytes);
                    int size = width * height;
                    byte[] recvDataBuff = new byte[size];
                    int offset = 0;
                    while (offset < size) {
                        int picL = in.read(recvDataBuff, offset, size - offset);
                        offset += picL;
                    }
                    in.read(tail, 0, 1);
                    message.tail = tail[0];
                    if (message.tail == Constant.TAIL) {
                        byte[] recvBuff=new byte[size+8];
                        for(int i=0;i<4;i++) {
                            recvBuff[i] = widthBytes[i];
                            recvBuff[4+i]= heightBytes[i];
                        }
                        for(int i=0;i<size;i++)
                            recvBuff[8+i]=recvDataBuff[i];

                        message.datas = new byte[size + 8];
                        message.datas = recvBuff;
                        returnFlag = true;
                    } else {
                        Log.w("RECV_MAP_INFO", "tail fail");
                    }
                }else if(message.cmd== Constant.RECV_CAR_POS){
                    byte[] datas=new byte[16];
                    byte[] tail=new byte[1];
                    in.read(datas,0,16);
                    in.read(tail,0,1);
                    message.tail=tail[0];
                    if(message.tail== Constant.TAIL) {
                        message.datas=new byte[16];
                        message.datas=datas;
                        returnFlag=true;
                    }else
                        Log.w("RECV_CAR_POS","tail fail");
                }else if(message.cmd== Constant.RECV_CAR_ELEC_MISSIONCNT){
                    byte[] datas=new byte[12];
                    byte[] tail=new byte[1];
                    in.read(datas,0,12);
                    in.read(tail,0,1);
                    message.tail=tail[0];
                    if(message.tail== Constant.TAIL){
                        message.datas=new byte[12];
                        message.datas=datas;
                        returnFlag=true;
                    }else
                        Log.w("RECV_CAR_ELEC_MISSIONCNT","tail fail");
                }
            }else
                Log.w("RECV_INFO", "head fail");
        }catch (IOException e){
            e.printStackTrace();
        }

        if(returnFlag) {
            Log.d(this.getClass().getName(),"message-->"+message);
            return message;
        }
        else
            return null;
    }

    public void sendMessage(byte cmd,byte data){
        new Thread(() -> {
            if (out != null) {
                try {
                    byte[] send = Constant.getContent(cmd,data);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(send);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(byte cmd,byte[] datas){
        new Thread(() -> {
            if (out != null) {
                try {
                    byte[] send = Constant.getContent(cmd,datas);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(send);
                    System.out.println(Arrays.toString(send));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




    public boolean isConnected() {
        return connect;
    }

    public void stop() {
        System.out.println("stop");
        runFlag = false;
        try {
            socket.shutdownInput();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect(String hostIP, int port) {
        this.hostIP = hostIP;
        this.port = port;
        new Thread(this).start();
    }


    private void consumeMsg(){
        while(runFlag) {
            if (msgQueue.size()!=0) {
                Message message = msgQueue.pollFirst();
//                Log.i("consumeMsg",Thread.currentThread().getName()+"||messageQueueLength="+msgQueue.size()+"\n"+message);
                if(message!=null)
                    HandleMessage.processMsg(message);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void refreshUI(final boolean isConnected) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                System.out.println("refreshUI");
                MainActivity.getEdMultiRobotPort().setEnabled(!isConnected);  //端口
                MainActivity.getEdMultiRobotIp().setEnabled(!isConnected);
                MainActivity.getBnMultiRobotConnect().setText(isConnected ? "断开" : "连接多车调度");
            }
        });
    }

    public boolean getRunflag(){
        return this.runFlag;
    }
}
