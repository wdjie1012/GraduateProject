package com.example.along.agvcontrol0412;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import common.Constant;
import socket.TcpMultiRobots;

public class MultiRobotsInfoConfirm extends Activity implements View.OnClickListener{

    private static Button btLoadInfo,btConfirmInfo;
    private static TextView txCarInfo;
    private static ImageView ivMapInfo;
    private TcpMultiRobots tcpMultiRobots=TcpMultiRobots.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_robots_info_confirm);
        btLoadInfo=this.findViewById(R.id.bn_loadInformation);
        txCarInfo=this.findViewById(R.id.tx_carInfo);
        ivMapInfo=this.findViewById(R.id.iv_mapInfo);
        btConfirmInfo=this.findViewById(R.id.bn_confirm);

        btLoadInfo.setOnClickListener(this);
        btConfirmInfo.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bn_loadInformation:
                tcpMultiRobots.sendMessage(Constant.CMD_COMMON_FUNC,Constant.LOAD_CAR_INFO);
                tcpMultiRobots.sendMessage(Constant.CMD_COMMON_FUNC,Constant.LOAD_MAP_INFO);
                break;
            case R.id.bn_confirm:

//                tcpMultiRobots.sendMessage(Constant.CMD_COMMON_FUNC,Constant.LOAD_CAR_INFO);
//                if(HandleRecvMessage.isInfoConfirm()) {
                Intent intent = new Intent(MultiRobotsInfoConfirm.this, MultiRobots.class);
                startActivity(intent);
//                }
                break;
        }
    }

    public static TextView getTxCarInfo() {
        return txCarInfo;
    }

    public static ImageView getImageViewMap(){
        return ivMapInfo;
    }
}