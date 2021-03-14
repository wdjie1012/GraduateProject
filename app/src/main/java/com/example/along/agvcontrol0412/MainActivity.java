package com.example.along.agvcontrol0412;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import message.TcpMessage;
import socket.TcpClient;
import socket.TcpMultiRobots;
import socket.TcpQtClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String imagePath;//图片路径
    //***变量***//
    private static boolean receive_flag = false;
    private static boolean sendPicOrNot = false;
    private static boolean navigateOrNot = false;
    private static boolean stereoFlag = false;
    private static int mode = 0;
    //***连接ip、端口用***//
    private static Button bnConnect, bnQtConnect, bnSend,bnChangeMode,bnMultiRobotConnect,bnMultiRobots;     //连接、发送
    private boolean singleCarMode=true;
    private static EditText edIp, edPort, edIp2, edPort2, edData,edMultiRobotIp, edMultiRobotPort; //IP地址、端口号、输入数据 文本框
    //***跳转界面***//
    private static Button bnBuildPic, bnNavigation, bnStereo;           //开始遥控并显示图像
    //***多线程***//
    private Handler handler = new Handler(Looper.getMainLooper());
    //***单例模式***//
    private TcpClient client = TcpClient.getInstance();
    private TcpQtClient qtClient = TcpQtClient.getInstance();
    private TcpMultiRobots multiRobotsClient = TcpMultiRobots.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //***对接xml***//
        bnConnect = this.findViewById(R.id.bn_connect);
//        bnQtConnect = this.findViewById(R.id.bn_connect2);

        edIp = this.findViewById(R.id.ed_ip);
        edPort = this.findViewById(R.id.ed_port);
//        edIp2 = this.findViewById(R.id.ed_ip2);
//        edPort2 = this.findViewById(R.id.ed_port2);
        edMultiRobotIp = this.findViewById(R.id.ed_ip3);
        edMultiRobotPort = this.findViewById(R.id.ed_port3);
        bnMultiRobotConnect=this.findViewById(R.id.bn_connect3);
        bnMultiRobots=this.findViewById(R.id.bn_multiRobots);
        //edData=this.findViewById(R.id.ed_dat);
        bnBuildPic = this.findViewById(R.id.bn_buildPic);
        bnNavigation = this.findViewById(R.id.bn_navigation);
//        bnStereo = this.findViewById(R.id.bn_stereo);


        //***监听***//
        bnConnect.setOnClickListener(this);
//        bnQtConnect.setOnClickListener(this);
        bnBuildPic.setOnClickListener(this);
        bnNavigation.setOnClickListener(this);
//        bnStereo.setOnClickListener(this);
        bnChangeMode=this.findViewById(R.id.bn_changeMode);
        bnChangeMode.setOnClickListener(this);
        bnMultiRobotConnect.setOnClickListener(this);
        bnMultiRobots.setOnClickListener(this);

        refreshUI(false);  //刷新界面
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (receive_flag || navigateOrNot) {  //满足其中一种情况
            //if(sendPicOrNot || navigateOrNot){
            client.endPic();//(发送 停止发图的指令)现在的目的是让小车端退出相应的模式
        }
        receive_flag = false;
        sendPicOrNot = false; //建图模式结束
        navigateOrNot = false;//导航模式结束
        stereoFlag = false;  //视觉模式结束
        client.setMybitmap(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_connect:
                connect();
                break;
//            case R.id.bn_connect2:
//                qtConnect();
//                break;
            case R.id.bn_buildPic:
                Intent BuildPic = new Intent(MainActivity.this, Control_getPic.class);
                receive_flag = true;  //允许接受标志位
                sendPicOrNot = true;  //允许接受图像 进入Control_getpic时发送指令，退出时，标为false
                client.sendSpeed(); //发送速度线程
                startActivity(BuildPic);
                break;
            case R.id.bn_navigation:
                chooseMode();
                break;
//            case R.id.bn_stereo:
//                Intent stereo = new Intent(MainActivity.this, StereoDisplay.class);
//                startActivity(stereo);
//                stereoFlag = true; // 进入视觉模式
//                break;
            case R.id.bn_changeMode:
                changeMode();
                break;
            case R.id.bn_connect3:
                multiRobotConnect();
                break;
            case R.id.bn_multiRobots:
                Intent intent=new Intent(MainActivity.this,MultiRobotsInfoConfirm.class);
                startActivity(intent);
//                ToolKit.saveStateInPhone(MainActivity.this);
                break;
        }
    }

    private void changeMode(){
        if(singleCarMode){
            this.findViewById(R.id.singleCarControl).setVisibility(View.GONE);
            this.findViewById(R.id.multiCarControl).setVisibility(View.VISIBLE);
            bnChangeMode.setText("-->单车控制");
            singleCarMode=false;
        }else if(!singleCarMode){
            this.findViewById(R.id.singleCarControl).setVisibility(View.VISIBLE);
            this.findViewById(R.id.multiCarControl).setVisibility(View.GONE);
            bnChangeMode.setText("-->多车导航");
            singleCarMode=true;
        }
    }

    private void multiRobotConnect() {
        if (multiRobotsClient.isConnected()) {
            multiRobotsClient.stop();
        } else {
            try {
                String hostIp = edMultiRobotIp.getText().toString();
                int port = Integer.parseInt(edMultiRobotPort.getText().toString());
                System.out.println(hostIp+","+port);
                multiRobotsClient.connect(hostIp, port);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "端口错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void connect() {
        if (client.isConnected()) {
            client.stop();
        } else {
            try {
                String hostIP = edIp.getText().toString();               //获取App里写的IP地址
                int port = Integer.parseInt(edPort.getText().toString());//获取APP里写的端口号，string->int
                client.connect(hostIP, port);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "端口错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void qtConnect() {
        if (qtClient.isConnected()) {
            qtClient.stop();
        } else {
            try {
                String hostIp = edIp2.getText().toString();
                int port = Integer.parseInt(edPort2.getText().toString());
                qtClient.connect(hostIp, port);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "端口错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void chooseMode() {
//        final int[] chooseItem = new int[1];
//        final String radioItems[] = new String[]{"激光模式", "视觉模式"};
//        AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
//        radioDialog.setTitle("选择模式");
//        radioDialog.setSingleChoiceItems(radioItems, 0, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                chooseItem[0] = which;
//            }
//        });
//        radioDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (chooseItem[0] == 0) {
//                    client.sendMessageToCar(TcpMessage.CMD_MAIN_FUNCTION, TcpMessage.DATA_REQUEST_LASER_SLAM);
//                    mode = 1;
//                } else {
//                    client.sendMessageToCar(TcpMessage.CMD_MAIN_FUNCTION, TcpMessage.DATA_REQUEST_STEREO_SLAM);
//                    mode = 2;
//                }
//
//                Intent navigation = new Intent(MainActivity.this, Navigation.class);
//                navigateOrNot = true;
//                dialog.dismiss();
//                startActivity(navigation);
//            }
//        });
//        radioDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        radioDialog.show();


        client.sendMessageToCar(TcpMessage.CMD_MAIN_FUNCTION, TcpMessage.DATA_REQUEST_LASER_SLAM);
        mode = 1;
        Intent navigation = new Intent(MainActivity.this, Navigation.class);
        startActivity(navigation);
        navigateOrNot = true;
    }

    private void refreshUI(final boolean isConnected) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                edPort.setEnabled(!isConnected);  //端口
                edIp.setEnabled(!isConnected);
                bnConnect.setText(isConnected ? "断开" : "连接AGV");
            }
        });
    }

    public static Button getBnConnect() {
        return bnConnect;
    }

    public static EditText getEdIp() {
        return edIp;
    }

    public static EditText getEdPort() {
        return edPort;
    }

    public static Button getBnQtConnect() {
        return bnQtConnect;
    }

    public static EditText getEdIp2() {
        return edIp2;
    }

    public static EditText getEdPort2() {
        return edPort2;
    }

    public static boolean isReceive_flag() {
        return receive_flag;
    }

    public static void setReceive_flag(boolean receive_flag) {
        MainActivity.receive_flag = receive_flag;
    }

    public static boolean isNavigateOrNot() {
        return navigateOrNot;
    }

    public static boolean isStereoFlag() {
        return stereoFlag;
    }

    public static int getMode() {
        return mode;
    }

    public static EditText getEdMultiRobotIp() {
        return edMultiRobotIp;
    }

    public static EditText getEdMultiRobotPort() {
        return edMultiRobotPort;
    }

    public static Button getBnMultiRobotConnect() {
        return bnMultiRobotConnect;
    }
}
