package com.example.along.agvcontrol0412;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import message.TcpMessage;
import send.SendSetSpeed;
import socket.RockerView;
import socket.TcpClient;

public class StereoDisplay extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    public static TextView tv_voltage;
    private RockerView mRockerView;
    private static ImageView s_imageView;
    private TextView edsetLineSpeed, edsetAngleSpeed, txLineSpeed, txAngleSpeed, dispLineSp, dispAngleSp;
    private Button bnConfirmSpeed;
    private static int Lx = 0, Ly = 0;
    public static float LineSpeed = (float) 0.6, AngleSpeed = (float) 0.4;
    private static boolean sendSpeedOrNot = false;
    private TcpClient client = TcpClient.getInstance();
    private SendSetSpeed sendSetSpeed = SendSetSpeed.getInstance_SendSetSpeed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stereo_display);

//        if(MainActivity.getMode()==0){//激光
//            this.findViewById(R.id.item_ms_stereoBack).setVisibility(View.GONE);
//        }
//        else{//视觉
//            this.findViewById(R.id.item_ms_stereoBack).setVisibility(View.VISIBLE);
//        }


//        findViewById(R.id.item_ms_stereoBack).setVisibility(View.INVISIBLE);

        tv_voltage = this.findViewById(R.id.stereo_batteryVoltage);
        mRockerView = findViewById(R.id.st_my_rocker);
        edsetLineSpeed = findViewById(R.id.ed_st_setLineSpeed);
        edsetAngleSpeed = findViewById(R.id.ed_st_setAngleSpeed);
        txLineSpeed = findViewById(R.id.tx_st_lineSpeed);
        txAngleSpeed = findViewById(R.id.tx_st_angleSpeed);
        dispLineSp = findViewById(R.id.tx_st_lineSpeed);
        dispAngleSp = findViewById(R.id.tx_st_maxAngleSpeed);
        bnConfirmSpeed = findViewById(R.id.bn_st_ConfirmSpeed);
        s_imageView = this.findViewById(R.id.s_image_View);

        bnConfirmSpeed.setOnClickListener(this);

        ToolBarInit();//工具栏初始化
        mRockerView.setOnLocation(new RockerView.OnLocationListener() {
            @Override
            public void onLocation(float x, float y) {
                Lx = (int) x;  //角速度
                Ly = (int) -y; //线速度
                float Sx, Sy;
                if (Lx == 0)
                    Sx = 0;
                else {
                    Sx = (float) (Math.round((float) Lx * AngleSpeed / (127) * 1000)) / 1000;
                }
                if (Ly == 0)
                    Sy = 0;
                else
                    Sy = (float) (Math.round((float) Ly * LineSpeed / (127) * 1000)) / 1000;

                txLineSpeed.setText(String.format("%s", Sy)); //显示角速度
                txAngleSpeed.setText(String.format("%s", Sx));  //显示线速度
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle("视觉模式");
        if (MainActivity.isStereoFlag()) {
            sendSpeedOrNot = true;
            client.sendStereoSpeed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.clearStereoPoints();
        sendSpeedOrNot = false;
    }

    /**
     * 显示菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stereo_toolbar, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_st_ConfirmSpeed:
                setSpeed();
                break;
        }
    }

    private void setSpeed() {
        sendSetSpeed.setLineSpeed(Float.valueOf(edsetLineSpeed.getText().toString()));
        sendSetSpeed.setAngleSpeed(Float.valueOf(edsetAngleSpeed.getText().toString()));
        LineSpeed = sendSetSpeed.getLineSpeed();
        AngleSpeed = sendSetSpeed.getAngleSpeed();

        if (LineSpeed >= 2)
            LineSpeed = 2;
        if (AngleSpeed >= 2)
            AngleSpeed = 2;
        client.sendSetSpeed();
        Log.i("control_LineSpeed", sendSetSpeed.getLineSpeed() + "");
        Log.i("control_AngleSpeed", sendSetSpeed.getAngleSpeed() + "");
    }

    private void ToolBarInit() {
        toolbar = findViewById(R.id.stereo_toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.stereo_toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int menuItemId = menuItem.getItemId();
                if (client.isRunFlag()) {
                    switch (menuItemId) {
                        case R.id.item_stereo_stereoMode://双目视觉
                            client.sendStereo();
                            break;
                        case R.id.item_stereo_stereoIMUMode://双目视觉+IMU
                            client.sendStereoIMU();
                            break;
                        case R.id.item_stereo_saveStereo://保存地图
                            client.sendSaveStereoMap();
                            client.clearStereoPoints();
                            break;
                        case R.id.item_stereo_recordCharge://记录充电桩模版
                            client.sendMessageToCar(TcpMessage.CMD_FUNCTION, TcpMessage.DATA_SAVE_STEREO_TEMPLATE);
                            break;
                    }
                }
                return true;
            }
        });
    }

    public static boolean isSendSpeedOrNot() {
        return sendSpeedOrNot;
    }

    public static int getLx() {
        return Lx;
    }

    public static int getLy() {
        return Ly;
    }

    public static ImageView get_imageView() {
        return s_imageView;
    }
}
