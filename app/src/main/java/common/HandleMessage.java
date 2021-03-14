package common;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;


import com.example.along.agvcontrol0412.MultiRobotsInfoConfirm;

import java.text.DecimalFormat;
import java.util.ArrayList;

import model.Car;
import model.CarMessage;
import model.Map;
import socket.TcpMultiRobots;

public class HandleMessage {
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static boolean carInfoConfirm=false;
    private static boolean mapInfoConfirm =false;
    private static Bitmap bitmap;
    private static final Car cars= Car.getInstance();
    private static final Map map= Map.getInstance();
    private static final TcpMultiRobots tcpMultiRobots= TcpMultiRobots.getInstance();

    public static void processMsg(Message message){
        switch (message.cmd){
            case Constant.RECV_CAR_INFO:
                handleCarInfo(message.datas);
                carInfoConfirm=true;
                break;
            case Constant.RECV_MAP_INFO:
                handleMapInfo(message.datas);
                mapInfoConfirm =true;
                break;
            case Constant.RECV_CAR_POS:
                handleCarPosition(message.datas);
                break;
            case Constant.RECV_CAR_ELEC_MISSIONCNT:
                handleCarElecMissionCnt(message.datas);
                break;
            default:
                break;
        }
    }

    private static void handleCarElecMissionCnt(byte[] datas){
        byte[] carNumber=new byte[4];
        byte[] electricityTmp=new byte[4];
        byte[] missionCountTmp=new byte[4];
        for(int i=0;i<4;i++){
            carNumber[i]=datas[i];
            electricityTmp[i]=datas[4+i];
            missionCountTmp[i]=datas[8+i];
        }
        int number= ToolKit.byteToInt(carNumber);
        float electricity= ToolKit.byteToFloat(electricityTmp);
        int missionCount= ToolKit.byteToInt(missionCountTmp);
        cars.getCarInfo().get(number).setElectricty(electricity);
        cars.getCarInfo().get(number).setMissionSuccessCount(missionCount);
//        System.out.println(cars.getCarInfo().get(number));
    }

    private static void handleCarPosition(byte[] datas){
        byte[] carNumber=new byte[4];
        byte[] posX=new byte[4];
        byte[] posY=new byte[4];
        byte[] posAngle=new byte[4];
        for(int i=0;i<4;i++){
            carNumber[i]=datas[i];
            posX[i]=datas[4+i];
            posY[i]=datas[8+i];
            posAngle[i]=datas[12+i];
        }
        int number= ToolKit.byteToInt(carNumber);
        float x= ToolKit.byteToFloat(posX);
        float y= ToolKit.byteToFloat(posY);
        float angle= ToolKit.byteToFloat(posAngle)*180/(float)3.14;

        DecimalFormat df=new DecimalFormat("##0.00");
        String xTmp=df.format(x);
        String yTmp=df.format(y);
        String angleTmp=df.format(angle);

        x=Float.parseFloat(xTmp);
        y=Float.parseFloat(yTmp);
        angle= Float.parseFloat(angleTmp);

        cars.getCarInfo().get(number).setX(x);
        cars.getCarInfo().get(number).setY(y);
        cars.getCarInfo().get(number).setAngle(angle);

    }

    private static void handleCarInfo(byte[] datas){
        int carCount=datas[0];
        cars.setCarCounts(carCount);
        float[][] carInfo=new float[carCount][2];
        int size=9;
        for(int i=0;i<carCount;i++){
            byte[] xBytes=new byte[4];
            byte[] yBytes=new byte[4];
            for(int j=0;j<4;j++){
                xBytes[j]=datas[i*size+3+j];
                yBytes[j]=datas[i*size+7+j];
            }
            float x= ToolKit.byteToFloat(xBytes);
            float y= ToolKit.byteToFloat(yBytes);

            DecimalFormat df=new DecimalFormat("##0.00");
            String xTmp=df.format(x);
            String yTmp=df.format(y);

            x=Float.parseFloat(xTmp);
            y=Float.parseFloat(yTmp);

            carInfo[i][0]=x;
            carInfo[i][1]=y;
            cars.addCar(i+1,new CarMessage(x,y,0));
        }

        handler.post(() -> {
            MultiRobotsInfoConfirm.getTxCarInfo().setText(null);
            for (int i = 0; i < carCount; i++)
                MultiRobotsInfoConfirm.getTxCarInfo().append("Car" + (i + 1) + ":x=" + carInfo[i][0] + ",y=" + carInfo[i][1] + "\n");
        });
    }

    public static void handleMapInfo(byte[] datas){
        byte[] widthBytes=new byte[4];
        byte[] heightBytes=new byte[4];
        for(int i=0;i<4;i++){
            widthBytes[i]=datas[i];
            heightBytes[i]=datas[4+i];
        }
        int width= ToolKit.byteToInt(widthBytes);
        int height= ToolKit.byteToInt(heightBytes);
        byte[] mapBuff=new byte[datas.length-8];
        System.arraycopy(datas,8,mapBuff,0,datas.length-8);

        map.setHeight(height);
        map.setWidth(width);
        map.setMapDatas(mapBuff);

        bitmap= ToolKit.createBitmap(mapBuff,width,height);
        ToolKit.drawPointOnBitmap(cars.getCarInfo(),bitmap,height,MultiRobotsInfoConfirm.getImageViewMap(),-1,new ArrayList<>());
    }

    public static void sendChangeTaskMessage(int carNumber,int taskNumber){

        byte number1=(byte)carNumber;
        byte number2=(byte)taskNumber;
        byte[] send=new byte[2];
        send[0]=number1;
        send[1]=number2;
//        if( carNumber!=-1 && taskNumber!=-1  )
            tcpMultiRobots.sendMessage(Constant.CMD_CHANGE_TASK,send);
    }


    public static boolean isInfoConfirm(){
        return carInfoConfirm && mapInfoConfirm;
    }

    public static Bitmap getBitmap(){
        return bitmap;
    }
}
