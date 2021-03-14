package com.example.along.agvcontrol0412;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import common.Constant;
import common.HandleMessage;
import common.ToolKit;
import model.Car;
import model.Map;
import model.State;
import socket.TcpMultiRobots;

public class MultiRobots extends Activity implements View.OnClickListener{

    private static ImageView ivMap;
    private static Button btRunCar,btLoadStates,btChangeTask;
    private static Spinner spCarLists,spChooseTask;
    private static TextView tvCarListX,tvCarListY,tvCarListAngle,tvElectricity,tvMissionSuccess;

    private int carNumChose=-1;
    private int stateNumChose=-1;
    private boolean refreshStateListFlag=false;
    private boolean refreshCarListFlag=true;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private final TcpMultiRobots tcpMultiRobots= TcpMultiRobots.getInstance();
    private final Map map= Map.getInstance();
    private final Car cars= Car.getInstance();

    private ArrayAdapter<String> carListAdapter;
    private ArrayAdapter<String> stateListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_robots);

        ivMap=this.findViewById(R.id.iv_map);
        btRunCar=this.findViewById(R.id.bn_runCar);
        btLoadStates=this.findViewById(R.id.bn_loadStatesFromPhone);
        btChangeTask=this.findViewById(R.id.bn_changeTask);
        spCarLists=this.findViewById(R.id.sp_carLists);
        spChooseTask=this.findViewById(R.id.sp_chooseTask);
        tvCarListX=this.findViewById(R.id.tv_carList_x);
        tvCarListY=this.findViewById(R.id.tv_carList_y);
        tvCarListAngle=this.findViewById(R.id.tv_carList_angle);
        tvElectricity=this.findViewById(R.id.tv_electricity);
        tvMissionSuccess=this.findViewById(R.id.tv_missionSuccess);

        btRunCar.setOnClickListener(this);
        btLoadStates.setOnClickListener(this);
        btChangeTask.setOnClickListener(this);

        //填充车辆列表
        setCarSpinner();
        if(tcpMultiRobots.getRunflag()) {
            refreshCarList();
            refreshStateList();
            init();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        refreshCarListFlag=false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bn_runCar:
                tcpMultiRobots.sendMessage(Constant.CMD_CAR_COMMAND,Constant.RUN_ALL_CAR);
                break;
            case R.id.bn_loadStatesFromPhone:
                ToolKit.getStateInPhone(map.getList(),MultiRobots.this);
                break;
            case R.id.bn_changeTask:
                HandleMessage.sendChangeTaskMessage(carNumChose,stateNumChose);
                break;
        }
    }

    private void init(){
        //画图、车辆
        new Thread(() -> {
            while(refreshCarListFlag) {
                ToolKit.drawPointOnBitmap(cars.getCarInfo(),
                        ToolKit.createBitmap(map.getMapDatas(), map.getWidth(), map.getHeight()),
                        map.getHeight(),
                        ivMap,
                        carNumChose,
                        map.getList());
            }
        }).start();

    }

    public void setCarSpinner(){
        List<String> list=new ArrayList<>();
        list.add("null");
        for(int i=1;i<=cars.getCarCounts();i++)
            list.add("car"+i);
        carListAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,list);
        spCarLists.setAdapter(carListAdapter);
        spCarLists.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str=carListAdapter.getItem(i);
                if(str.equals("null")){
                    carNumChose=-1;
                    setCarListsNull();
                }else {
                    carNumChose=i;
                    setCarLists(carNumChose);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setStatesSpinner(){
        List<String>list=new ArrayList<>();
        list.add("null");
        for(State state:map.getList())
            list.add(state.getName());
        stateListAdapter=new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,list);
        spChooseTask.setAdapter(stateListAdapter);
        spChooseTask.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str=stateListAdapter.getItem(i);
                if(str.equals("null")) {
                    stateNumChose = -1;
                    System.out.println("null");
                }
                else {
                    stateNumChose = i;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }


    private void refreshCarList(){
        new Thread(() -> {
            while (refreshCarListFlag) {
                if (carNumChose == -1)
                    setCarListsNull();
                else
                    setCarLists(carNumChose);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void refreshStateList(){
        new Thread(()->{
            while(true){
                handler.post(this::setStatesSpinner);
                if(!map.getList().isEmpty())
                    break;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void setCarListsNull(){
        handler.post(() -> {
            tvCarListX.setText(null);
            tvCarListY.setText(null);
            tvCarListAngle.setText(null);
            tvElectricity.setText(null);
            tvMissionSuccess.setText(null);
        });

    }

    private void setCarLists(int number){
        float x=cars.getCarInfo().get(number).getX();
        float y=cars.getCarInfo().get(number).getY();
        float angle=cars.getCarInfo().get(number).getAngle();
        int missionCnt=cars.getCarInfo().get(number).getMissionSuccessCount();
        float electricity=cars.getCarInfo().get(number).getElectricty();
        handler.post(() -> {
            tvCarListX.setText(String.valueOf(x));
            tvCarListY.setText(String.valueOf(y));
            tvCarListAngle.setText(String.valueOf(angle));
            tvMissionSuccess.setText(String.valueOf(missionCnt));
            tvElectricity.setText(String.valueOf(electricity));
            tvElectricity.setText("34.2");
        });
    }


}