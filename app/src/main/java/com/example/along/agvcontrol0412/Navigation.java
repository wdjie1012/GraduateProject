package com.example.along.agvcontrol0412;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import model.ConflictArea;
import common.DockSite;
import common.MyPicture;
import common.NavigationMessage;
import model.Region;
import common.TaskChainItem;
import MyInterface.GetMapMes;
import MyInterface.GetStatMes;
import message.TcpMessage;
import okhttp3.ResponseBody;
import recev.Map_xy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import send.Map_xy_send;
import send.NavigationMap;
import send.SendSetSpeed;
import send.TargetPoint;
import socket.RockerView;
import socket.TcpClient;
import socket.TcpQtClient;

/**
 * 加载完图像要等等再点站点，要不然会加载不完整
 * client加了static 不知道会不会有问题
 */

public class Navigation extends AppCompatActivity implements View.OnClickListener {

    private String baseUrl = "http://192.168.1.101/";
    private static String imagePath;//图片路径
    private static Bitmap bitmap;
    private Toolbar toolbar;
    private static ImageView n_imageView;
    public static TextView tv_status;
    public static TextView nv_voltage;
    private Button btsendTarget, btHome, btGoCharging, btStop;
    private TextView edsetLineSpeed, edsetAngleSpeed, txLineSpeed, txAngleSpeed, dispLineSp, dispAngleSp;
    private Button bnConfirmSpeed;
    private RockerView mRockerView;
    private float pi = (float) 3.1415926;
    private static TcpClient client = TcpClient.getInstance();
    private static Map_xy_send map_xy_send = Map_xy_send.getInstance_map_xy_send();
    private TargetPoint targetPoint = TargetPoint.getInstance_TargetPoint();
    private float point_x, point_y;
    private static int Lx = 0, Ly = 0;
    private boolean loadMapFlag = false;
    private boolean widgetState = false;
    public static float LineSpeed = (float) 0.6, AngleSpeed = (float) 0.4;
    public static final int CHOOSE_PHOTO = 2;
    //-----conflict area------//
    private static int conflictMode=-1; //-1 无 | 2 双出口 | 4 四出口
    private static int conflictCounts=0;
    private static int conflictRegionR=0;
    private static ConflictArea conflictAreaTmp;
    private static ArrayList<ConflictArea> conflictAreas=new ArrayList<>();
    //------------------------//
    private static HashMap<String, DockSite> dockSites = new HashMap<>();
    private static HashMap<String, DockSite> navigation_dockSites = new HashMap<>();
    private static ArrayList<DockSite> taskSites = new ArrayList<>();//任务链列表
    private static ArrayList<TaskChainItem> taskChainItem = new ArrayList<>();//用来显示任务链的list
    private NavigationMap navigationMap = NavigationMap.getInstance_navigationMap();
    private NavigationMessage navigationMessage = NavigationMessage.getInstance_navigationMessage();
    private SendSetSpeed sendSetSpeed = SendSetSpeed.getInstance_SendSetSpeed();
    private TcpQtClient qtClient = TcpQtClient.getInstance();
    private Map_xy map_xy = Map_xy.getInstance_map_xy();
    private static boolean relocationSendSpeed = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);


        ToolBarInit();
        n_imageView = this.findViewById(R.id.n_image_View);
        btHome = this.findViewById(R.id.bt_home);
        btGoCharging = this.findViewById(R.id.bt_goCharging);
        btStop = this.findViewById(R.id.bt_Stop);
        tv_status = this.findViewById(R.id.ms_tv_status);
        nv_voltage = this.findViewById(R.id.navi_batteryVoltage);
        /*btsendTarget=this.findViewById(R.id.bt_sendTarget);
        edSendX=this.findViewById(R.id.ed_sendX);
        edSendY=this.findViewById(R.id.ed_sendY);*/
        n_imageView.setOnTouchListener(new TouchListener());
        //btsendTarget.setOnClickListener(this);


        mRockerView = findViewById(R.id.nv_my_rocker);
        edsetLineSpeed = findViewById(R.id.ed_nv_setLineSpeed);
        edsetAngleSpeed = findViewById(R.id.ed_nv_setAngleSpeed);
        txLineSpeed = findViewById(R.id.tx_nv_lineSpeed);
        txAngleSpeed = findViewById(R.id.tx_nv_angleSpeed);
        dispLineSp = findViewById(R.id.tx_nv_maxLineSpeed);
        dispAngleSp = findViewById(R.id.tx_nv_maxAngleSpeed);
        bnConfirmSpeed = findViewById(R.id.bn_nv_ConfirmSpeed);

        bnConfirmSpeed.setOnClickListener(this);

        if (bitmap != null) {
            n_imageView.setImageBitmap(bitmap);
            client.setMybitmap(bitmap);
            client.isFinish_recevPic(true);
        }


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
        toolbar.setTitle("导航模式");
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.isFinish_recevPic(false);
        loadMapFlag = false;
        relocationSendSpeed=false;
    }

    private void ToolBarInit() {
        toolbar = findViewById(R.id.ms_toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.mission_toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int menuItemId = menuItem.getItemId();
                if (client.isRunFlag()) {
                    switch (menuItemId) {
                        case R.id.item_ms_task://添加任务
                            showTaskDialog();
                            widgetShow(true);
                            break;
                        case R.id.item_ms_downloadmap://推送地图，从手机中加载图片
                            navigation_dockSites.clear();
                            bitmap = null;
                            LoadImageFromPhone();
                            break;
                        case R.id.item_locate://添加站点
                            recordStation();
                            break;
                        case R.id.item_ms_relocation://
                            widgetShow(widgetState);
                            break;
                        case R.id.item_ms_downloadStat://加载站点信息
                            LoadStationFromServer();
                            break;
                        case R.id.item_ms_saveState://存储站点信息
                            saveStationInPhone();
                            break;
                        case R.id.item_ms_taskChain://任务链
                            startActivity(new Intent(Navigation.this, DialogNavigate.class));
                            break;
                        case R.id.item_stop://停止动作
                            StopMove();
                            break;
                        case R.id.item_goCharge://前往充电桩充电
                            widgetShow(true);
                            goCharging();
                            break;
                        case R.id.item_ms_stereoBack://视觉模式下 后退
                            client.sendMessageToCar(TcpMessage.CMD_FUNCTION, TcpMessage.DATA_STEREO_BACK);
                            break;
                        case R.id.item_ms_locate_conflict:
                            chooseConflictMode();
                            break;
                        case R.id.item_ms_back:
                            undoOperation();
                            break;
                    }
                }
                return true;
            }
        });
    }

    private void undoOperation(){
        if(!conflictAreas.isEmpty()){
            conflictAreas.remove(conflictAreas.size()-1);
        }
    }

    private void chooseConflictMode() {
//        final int[] chooseItem = new int[1];
//        final String radioItems[] = new String[]{"双出口", "四出口"};
//        AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
//        radioDialog.setTitle("选择冲突区域类型");
//        radioDialog.setSingleChoiceItems(radioItems, 0, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                chooseItem[0] = which;
//            }
//        });
//        radioDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (chooseItem[0] == 0)
//                    conflictMode = 2;
//                 else
//                    conflictMode = 4;
//
//                dialog.dismiss();
//            }
//        });
//        radioDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                conflictMode=-1;
//                dialog.dismiss();
//            }
//        });
//        radioDialog.show();



        final EditText editText = new EditText(this);
        final int[] chooseItem = new int[1];
        final String radioItems[] = new String[]{"双出口", "四出口"};

        AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
        radioDialog.setTitle("选择冲突区域类型")
                .setView(editText)
                .setSingleChoiceItems(radioItems, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chooseItem[0] = which;
                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (chooseItem[0] == 0)
                            conflictMode = 2;
                         else
                            conflictMode = 4;
                        String str = editText.getText().toString();
                        str = str.replaceAll("[0-9]", "");
                        if (str.length() == 0 && !editText.getText().toString().equals("")) {
                            conflictRegionR=Integer.parseInt(editText.getText().toString());
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Navigation.this, "输入有误", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        conflictMode=-1;
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //设置摇杆可见
    private void widgetShow(boolean flag) {
        if (flag == false) {
            widgetState = true;
            relocationSendSpeed = true;
            client.relocationSendSpeed();
            this.findViewById(R.id.navigation_rocker).setVisibility(View.VISIBLE);
        } else {
            widgetState = false;
            relocationSendSpeed = false;
            this.findViewById(R.id.navigation_rocker).setVisibility(View.GONE);
        }
    }

    private void saveStationInPhone() {
        final EditText inputServer = new EditText(this);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_");
        inputServer.setText(df.format(new Date()));
        AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
        radioDialog.setTitle("保存站点记录")
                .setView(inputServer);
        radioDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        radioDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //存入文档记录站点记录
                saveStationInPhone(inputServer.getText().toString(),true);
                saveStationInPhone(inputServer.getText().toString(),false);

            }
        });
        radioDialog.create().show();
    }

    private void saveStationInPhone(String name,boolean ifConflict) {
        Gson gson = new Gson();
        File file;
        String mes;
        if(!ifConflict) {
            List<DockSite> sendMesList = new ArrayList<>();
            for (DockSite dockSite : navigation_dockSites.values())
                sendMesList.add(dockSite);
            mes = gson.toJson(sendMesList);//站点信息
            file = new File(Environment.getExternalStorageDirectory() + "/AgvCar/mapState", name + ".txt");
        }else{
            mes=gson.toJson(conflictAreas);
            file = new File(Environment.getExternalStorageDirectory() + "/AgvCar/mapConflictState", name + ".txt");
        }

        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(mes.getBytes());
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private void recordStation() {
        if(conflictMode==-1) {
            final EditText inputServer = new EditText(this);
            AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
            radioDialog.setTitle("设置站点")
                    .setView(inputServer);
            radioDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            radioDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //记录站点
                    recordStation(inputServer.getText().toString());
                }
            });
            radioDialog.create().show();
        }else {
            if(conflictCounts==0)
                conflictAreaTmp=new ConflictArea(conflictMode,new Region[conflictMode+1]);
            final EditText inputServer = new EditText(this);
            AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
            radioDialog.setTitle("设置冲突区域站点("+conflictCounts+"/"+conflictMode+")")
                    .setView(inputServer);
            radioDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            radioDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //记录站点
                    recordConflictStation(inputServer.getText().toString(),conflictCounts);
                    conflictCounts++;
                    if(conflictCounts>conflictMode) {
                        conflictMode = -1;
                        conflictCounts=0;
                        conflictAreas.add(conflictAreaTmp);
                    }
                }
            });
            radioDialog.create().show();
        }
    }

    private void recordConflictStation(String name,int number){
        conflictAreaTmp.regions[number]=new Region(map_xy.getX()*100/5,map_xy.getY()*100/5,conflictRegionR,name);
    }

    private void recordStation(String name) {
        navigation_dockSites.put(name, new DockSite(name, map_xy.getX() * 100 / 5, map_xy.getY() * 100 / 5, map_xy.getAngle()));
    }

    private void showTaskDialog() {
        final int[] chooseItem = new int[1];
        final String radioItems[] = new String[navigation_dockSites.size()];
        Set<String> s_keys = navigation_dockSites.keySet();
        List<String> l_keys = new ArrayList<>(s_keys);
        for (int i = 0; i < navigation_dockSites.size(); i++)
            radioItems[i] = l_keys.get(i);
        AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
        radioDialog.setTitle("任务");
        radioDialog.setSingleChoiceItems(radioItems, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chooseItem[0] = which;
            }
        });
        radioDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                taskSites.add(dockSites.get(radioItems[chooseItem[0]]));
                //发布任务
                map_xy_send.setX(navigation_dockSites.get(radioItems[chooseItem[0]]).x);
                map_xy_send.setY(navigation_dockSites.get(radioItems[chooseItem[0]]).y);
                map_xy_send.setAngle(navigation_dockSites.get(radioItems[chooseItem[0]]).angle);
                if (client.isRunFlag()) {
                    client.sendMapLocation();
                }
                Log.i("正常任务点", "x=" + map_xy_send.getX() + ",y=" + map_xy_send.getY() + ",angle=" + map_xy_send.getAngle());
                tv_status.setText("正在前往：  " + radioItems[chooseItem[0]]);
                dialog.dismiss();
            }
        });
        radioDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        radioDialog.show();
    }

    /**
     * 显示菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mission_toolbar, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_nv_ConfirmSpeed:
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

    /**
     * 设置目的点
     */
    private final class TouchListener implements View.OnTouchListener {
        private Matrix currentMatrix = new Matrix();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    float point_x, point_y;
                    currentMatrix.set(n_imageView.getImageMatrix());
                    float sizeW_times = (float) (Math.round((float) n_imageView.getWidth() / client.getPicW() * 1000)) / 1000;//map_xy.getX()
                    float sizeH_times = (float) (Math.round((float) n_imageView.getHeight() / client.getPicH() * 1000)) / 1000;//map_xy.getY()
                    float size = sizeW_times < sizeH_times ? sizeW_times : sizeH_times;

                    float qW = (n_imageView.getWidth() / size - client.getPicW()) / 2;
                    point_x = event.getX() / size - qW;
                    float qH = (n_imageView.getHeight() / size - client.getPicH()) / 2;
                    point_y = event.getY() / size - qH;
                    if (point_x > client.getPicW())
                        point_x = client.getPicW();
                    else if (point_x <= 0)
                        point_x = 0;
                    if (point_y > client.getPicH())
                        point_y = client.getPicH();
                    else if (point_y <= 0)
                        point_y = 0;

                    map_xy_send.setX(point_x);
                    map_xy_send.setY(client.getPicH() - point_y);
                    map_xy_send.setAngle(0);
                    Log.i("按下的点:x,y:", "" + map_xy_send.getX() + "," + map_xy_send.getY());
                    widgetShow(true);
                    if (client.isRunFlag())
                        client.sendMapLocation();//map_xy_send.getX(),map_xy_send.getY()

                    break;
            }
            return true;
        }
    }


    private void sendHome() {
        map_xy_send.setX(client.getOriginal_pointX());
        map_xy_send.setY(client.getOriginal_pointY());
        map_xy_send.setAngle(client.getOriginal_pointAngle());

        if (client.isRunFlag())
            client.sendMapLocation();
    }

    private void goCharging() {
        final int[] chooseItem = new int[1];
        final String radioItems[] = new String[navigation_dockSites.size()];
        Set<String> s_keys = navigation_dockSites.keySet();
        List<String> l_keys = new ArrayList<>(s_keys);
        for (int i = 0; i < navigation_dockSites.size(); i++)
            radioItems[i] = l_keys.get(i);
        AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
        radioDialog.setTitle("选择充电桩");
        radioDialog.setSingleChoiceItems(radioItems, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chooseItem[0] = which;
            }
        });
        radioDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                map_xy_send.setX(navigation_dockSites.get(radioItems[chooseItem[0]]).x);
                map_xy_send.setY(navigation_dockSites.get(radioItems[chooseItem[0]]).y);
                map_xy_send.setAngle(navigation_dockSites.get(radioItems[chooseItem[0]]).angle);

                if (client.isRunFlag()) {
                    client.goCharging(); //send location first
                    client.chargefunc();//then send function
                }
                Log.i("充电点", "x=" + map_xy_send.getX() + ",y=" + map_xy_send.getY() + ",angle=" + map_xy_send.getAngle());
                tv_status.setText("正在前往：  " + radioItems[chooseItem[0]]);
                dialog.dismiss();
            }
        });
        radioDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        radioDialog.show();
    }

    private void StopMove() {
        if (client.isRunFlag())
            client.sendMessageToCar(TcpMessage.CMD_FUNCTION, TcpMessage.DATA_STOP);
        map_xy_send.setX(0);
        map_xy_send.setY(0);
        map_xy_send.setAngle(0);
        Navigation.tv_status.setText("当前无任务");
    }

    public static int getLx() {
        return Lx;
    }

    public static int getLy() {
        return Ly;
    }

    public static boolean isRelocationSendSpeed() {
        return relocationSendSpeed;
    }

    public static ImageView getImageView() {
        return n_imageView;
    }

    public static HashMap<String, DockSite> getDockSites() {
        return dockSites;
    }

    private void LoadStationFromServer() {
        navigation_dockSites.clear();
        readMapStateMesFromPhone();
    }

    public void readMapStateMesFromPhone() {
        String path = Environment.getExternalStorageDirectory() + "/AgvCar/mapState/";
        File pfile = new File(path);
        if (pfile != null) {
            File file = new File(path);
            final String[] fileNames = file.list();

            final int[] chooseItem = new int[1];
            final String radioItems[] = new String[fileNames.length];

            for (int i = 0; i < fileNames.length; i++)
                radioItems[i] = fileNames[i];
            AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
            radioDialog.setTitle("载入站点记录");
            radioDialog.setSingleChoiceItems(radioItems, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    chooseItem[0] = which;
                }
            });
            radioDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String file = Environment.getExternalStorageDirectory() + "/AgvCar/mapState/" + fileNames[chooseItem[0]];
                    String line = "";
                    InputStream instream = null;
                    try {
                        Gson gson = new Gson();
                        instream = new FileInputStream(file);
                        InputStreamReader inputreader = new InputStreamReader(instream);
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        line = buffreader.readLine();
                        List<DockSite> dock = gson.fromJson(line, new TypeToken<List<DockSite>>() {
                        }.getType());
                        for (int i = 0; i < dock.size(); i++) {
                            DockSite dockSite = dock.get(i);
                            navigation_dockSites.put(dockSite.name, dockSite);
                        }
                        instream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });
            radioDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            radioDialog.show();
        }
    }

    public static void performTaskChain(final ArrayList<TaskChainItem> taskChainList) {
        taskChainItem = taskChainList;
        for (TaskChainItem item : taskChainItem) {
            Log.i("tag", item.getItemName() + ",delay=" + item.getDelay());
        }
        client.setAchieveGoalFlag(true);//一开始就让他处于 已到达目标点 的状态，允许他发送目标点
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 0;
                    while (true) {
                        if (client.isAchieveGoalFlag() && taskChainItem.size() != 0) {//只有到达目标点才可以发送目标点，否则小车在行进过程中不能另外发送目标点
                            Log.i("tag", "begin to send location");
                            map_xy_send.setX(navigation_dockSites.get(taskChainItem.get(0).getItemName()).x);
                            map_xy_send.setY(navigation_dockSites.get(taskChainItem.get(0).getItemName()).y);
                            map_xy_send.setAngle(navigation_dockSites.get(taskChainItem.get(0).getItemName()).angle);
                            client.sendMapLocation();
                            tv_status.setText("正在前往：  " + taskChainItem.get(0).getItemName());
//                            taskChainItem.remove(0);//执行完一个任务，就把顶上的任务去掉
                        } else if (taskChainItem.size() == 0) //当任务列表为空的时候，跳出线程
                            break;
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void readFromPhone(String fileName, int kind) {
        Gson gson = new Gson();
        try {
            String file = Environment.getExternalStorageDirectory() + "/AgvCar" + "/mapMes" + "/" + fileName + ".txt";
            String line = "";
            InputStream instream = new FileInputStream(file);
            InputStreamReader inputreader = new InputStreamReader(instream);
            BufferedReader buffreader = new BufferedReader(inputreader);

            if (kind == 1) {  //获取站点信息等
                line = buffreader.readLine();
                Log.i("tag", "dock=" + line);
                Log.i("tag", "fileName" + fileName);
                List<DockSite> dock = gson.fromJson(line, new TypeToken<List<DockSite>>() {
                }.getType());
                for (int i = 0; i < dock.size(); i++) {
                    DockSite dockSite = dock.get(i);
                    dockSites.put(dockSite.name, dockSite);
                }
                navigationMessage.setDockSites(dockSites);//备份
                Log.i("a", "b");
            }
            if (kind == 2) {  //获取图像尺寸等
                line = buffreader.readLine();
                MyPicture myPicture = gson.fromJson(line, MyPicture.class);
                //获得长和宽
                navigationMap.setRow(myPicture.row);
                navigationMap.setColumn(myPicture.column);
                navigationMap.setStatesNum(myPicture.station_number);
                navigationMap.setOrigin_x(myPicture.origin_x);
                navigationMap.setOrigin_y(myPicture.origin_y);
                //发送地图给小车
                //client.sendMapToCar(imagePath);
                client.sendMapToCar();
                //client类显示图像用
                client.setPicH(navigationMap.getRow());
                client.setPicW(navigationMap.getColumn());
                Log.i("tag", "sendMaptoCar!!!!!!!!!!!");
                //发给qt地图
//                qtClient.sendMap(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/"+navigationMessage.getImagePath()+".png");

            }
            instream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeTaskChainItem() {
        taskChainItem.remove(0);
    }

    public static ArrayList<TaskChainItem> getTaskChainItem() {
        return taskChainItem;
    }

    public static String getImagePath() {
        return getFileName(imagePath);
    }

    //****************************************************************//
    //******************以下都是加载图像到手机**************************//

    /**
     * 加载图像
     */
    private void LoadImageFromPhone() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data);
                    } else {
                        handleImageBeforeKitkat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitkat(Intent data) {
        imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是File类型的uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath);//根据图片路径显示图片
//        Log.i("TAG","-------------->path="+getFileName(imagePath));
//        Log.i("TAG","-------------->path="+imagePath);
//        getMapMes(getFileName(imagePath));
        navigationMessage.setImagePath(getFileName(imagePath));//备份
        readFromPhone(getFileName(imagePath), 2);
        loadMapFlag = true;
        //从服务器数据库中加载站点信息
//        getStatMes(getFileName(imagePath));
    }

    private void getStatMes(String picName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        GetStatMes getStatMes = retrofit.create(GetStatMes.class);
        Call<ResponseBody> data = getStatMes.getmes(picName);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Gson gson = new Gson();
                try {
                    String mes = response.body().string();
                    List<DockSite> dock = gson.fromJson(mes, new TypeToken<List<DockSite>>() {
                    }.getType());
                    for (int i = 0; i < dock.size(); i++) {
                        DockSite dockSite = dock.get(i);
                        dockSites.put(dockSite.name, dockSite);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

    }

    private void getMapMes(String picName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        GetMapMes getMapMes = retrofit.create(GetMapMes.class);
        Call<ResponseBody> data = getMapMes.getmes(picName);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Gson gson = new Gson();
                try {
                    String mes = response.body().string();
                    MyPicture myPicture = gson.fromJson(mes, MyPicture.class);
                    //获得长和宽之后(再把信息发给小车,目前不发，因为发过去图片已经就知道长和宽了)
                    navigationMap.setRow(myPicture.row);
                    navigationMap.setColumn(myPicture.column);
                    navigationMap.setStatesNum(myPicture.station_number);
                    navigationMap.setOrigin_x(myPicture.origin_x);
                    navigationMap.setOrigin_y(myPicture.origin_y);
                    //发送地图给小车
                    client.sendMapToCar(imagePath);
                    //client类显示图像用
                    client.setPicH(navigationMap.getRow());
                    client.setPicW(navigationMap.getColumn());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    /**
     * 取得图片名字
     *
     * @param pathandname
     * @return
     */
    public static String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }

    }


    private void handleImageBeforeKitkat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            bitmap = BitmapFactory.decodeFile(imagePath);
            n_imageView.setImageBitmap(bitmap);
            client.setMybitmap(bitmap);
            client.isFinish_recevPic(true);
            navigationMessage.setBitmap(bitmap);//备份
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }
    //******************以上都是加载图像到手机**************************//
    //****************************************************************//

    public static HashMap<String, DockSite> getNavigation_dockSites() {
        return navigation_dockSites;
    }

    public static ArrayList<ConflictArea>getConflictAreas(){
        return conflictAreas;
    }

    public static void setBitmap(Bitmap bitmap) {
        Navigation.bitmap = bitmap;
    }

    public static Bitmap getBitmap() {
        return bitmap;
    }
}
