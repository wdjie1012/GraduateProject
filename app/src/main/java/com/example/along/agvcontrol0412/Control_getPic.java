package com.example.along.agvcontrol0412;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
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
import android.support.v7.app.AlertDialog;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.DockSite;
import common.LimitLine;
import common.MyPicture;
import common.NavigationMessage;
import MyInterface.UpMap;
import MyInterface.UpMapMes;
import message.TcpMessage;
import okhttp3.ResponseBody;
import recev.Map_xy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import cz.msebera.android.httpclient.Header;
import send.SendSetSpeed;
import socket.RockerView;
import socket.TcpClient;

/**
 * ???????????? limitLines????????????List??????????????????set
 * dockSites.clear();??????????????????????????????????????????
 */

public class Control_getPic extends AppCompatActivity implements View.OnClickListener {

    private String baseUrl = "http://192.168.1.101/";
    public static Bitmap bitmap;
    private byte[] bitmapbuff;//????????????????????????
    private Toolbar toolbar;
    private RockerView mRockerView;
    private boolean widgetState = false;
    private static ImageView c_imageView;
    private TextView edsetLineSpeed, edsetAngleSpeed, txLineSpeed, txAngleSpeed, dispLineSp, dispAngleSp;
    public static TextView ct_voltage, tv_savePbstream;
    private Button bnConfirmSpeed;
    private static boolean sendSpeedOrNot = true;
    private static int Lx = 0, Ly = 0;
    public static float LineSpeed = (float) 0.6, AngleSpeed = (float) 0.4;
    public static final int CHOOSE_PHOTO = 2;
    private boolean drawOrNot = false;//????????????????????????????????????????????? ????????????????????????
    private static List<LimitLine> limitLines = new ArrayList<>();     //????????????
    private static List<LimitLine> limitLineslast = new ArrayList<>();
    private int limitLinePointR = 2;
    public static HashMap<String, DockSite> dockSites = new HashMap<>();//?????????
    private static String fileName;
    //-----------------stereo------------------
    private static boolean stereoFlag = false;
    //-----------------------------------------

    private TcpClient client = TcpClient.getInstance();
    private SendSetSpeed sendSetSpeed = SendSetSpeed.getInstance_SendSetSpeed();
    private MyPicture myPicture = MyPicture.getInstance_myPicture();
    private Map_xy map_xy = Map_xy.getInstance_map_xy();
    private NavigationMessage navigationMessage = NavigationMessage.getInstance_navigationMessage();

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_get_pic);

        ToolBarInit();//??????????????????

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
        //***??????xml***//
        mRockerView = findViewById(R.id.my_rocker);
        edsetLineSpeed = findViewById(R.id.ed_setLineSpeed);
        edsetAngleSpeed = findViewById(R.id.ed_setAngleSpeed);
        txLineSpeed = findViewById(R.id.tx_lineSpeed);
        txAngleSpeed = findViewById(R.id.tx_angleSpeed);
        dispLineSp = findViewById(R.id.tx_maxLineSpeed);
        dispAngleSp = findViewById(R.id.tx_maxAngleSpeed);
        bnConfirmSpeed = findViewById(R.id.bn_ConfirmSpeed);
        ct_voltage = findViewById(R.id.ctl_battery);
        tv_savePbstream = findViewById(R.id.ctl_savePbstream);
//        Button btcreatePic=findViewById(R.id.bt_createPic);
//        Button btsaveMap=findViewById(R.id.bt_saveMap);
//        Button btcharge=findViewById(R.id.bt_charge);
        c_imageView = this.findViewById(R.id.c_image_View);
        c_imageView.setOnTouchListener(new TouchListener());
        //***??????***//
        bnConfirmSpeed.setOnClickListener(this);
//        btsaveMap.setOnClickListener(this);
//        btcreatePic.setOnClickListener(this);
//        btcharge.setOnClickListener(this);
        //bnsavePic.setOnClickListener(this);
        //bnloadPic.setOnClickListener(this);
        //***??????????????????***//
        mRockerView.setOnLocation(new RockerView.OnLocationListener() {
            @Override
            public void onLocation(float x, float y) {
                Lx = (int) x;  //?????????
                Ly = (int) -y; //?????????
//                Log.i("Lx",Lx+"");
//                Log.i("Ly",Ly+"");
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

                txLineSpeed.setText(String.format("%s", Sy)); //???????????????
                txAngleSpeed.setText(String.format("%s", Sx));  //???????????????
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendSpeedOrNot = false;
        client.clearStereoPoints();
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle("????????????");
        if (MainActivity.isReceive_flag()) {
            sendSpeedOrNot = true;
            client.sendSpeed();
        }
    }

    private void ToolBarInit() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.mytoolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int menuItemId = menuItem.getItemId();
//                if (client.isRunFlag()) {
                    switch (menuItemId) {
                        case R.id.id_item_mapping://????????????
                            drawOrNot = false;
                            client.sendMessageToCar(TcpMessage.CMD_MAIN_FUNCTION, TcpMessage.DATA_REQUEST_SLAM);//???????????????????????????????????????????????????
                            MainActivity.setReceive_flag(true);
                            break;
                        case R.id.id_item_save://????????????    /*?????????pbstream?????????*/
                            if (MainActivity.isReceive_flag()) {//???????????????????????????
                                client.sendMessageToCar(TcpMessage.CMD_MAIN_FUNCTION, TcpMessage.DATA_END_MAP_SEND);//????????????
                                MainActivity.setReceive_flag(false);
                                bitmapbuff = client.getEditBuff();//????????????????????????????????????(bitmapbuff ??????????????????)
                                drawMap();
                            }
                            client.sendMessageToCar(TcpMessage.CMD_FUNCTION, TcpMessage.DATA_SAVE_MAP);//????????????
                            savePicture();   //????????????????????????
                            inputPictureName();//????????????????????????????????????
                            break;
                        case R.id.id_item_recordCharge:
                            client.sendMessageToCar(TcpMessage.CMD_FUNCTION, TcpMessage.DATA_SAVE_LASER_TEMPLATE);//?????????????????????
                            break;
                    }
//                }
                return true;
            }
        });
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param
     */
    private void drawMap() {
        if (bitmapbuff != null) {
            Bitmap bmp = client.getMybitmap().copy(Bitmap.Config.ARGB_8888, true);
            c_imageView.setImageBitmap(bmp);
        }
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param limitLines
     * @param myDockSites
     */
    private void drawMap(List<LimitLine> limitLines, HashMap<String, DockSite> myDockSites) {
        if (client.getMybitmap() != null) {//?????????????????????????????????
            Bitmap bmp = client.getMybitmap().copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            Log.i("RED", "RED");
            for (LimitLine point : limitLines)
                canvas.drawCircle(point.x, point.y, limitLinePointR, paint);

            paint.setColor(Color.GRAY);
            for (LimitLine point : limitLineslast)
                canvas.drawCircle(point.x, point.y, limitLinePointR, paint);

            paint.setColor(Color.GREEN);
            for (DockSite dockSite : myDockSites.values()) {
                float x = dockSite.x;
                float y = client.getPicH() - dockSite.y;
                float angle = dockSite.angle;
                canvas.rotate(-angle, x, y);
                Path path = new Path();
                path.moveTo(x + 5, y);
                path.lineTo(x - 5, y + 3);
                path.lineTo(x - 5, y - 3);
                path.close();
                canvas.drawPath(path, paint);
                canvas.rotate(angle, x, y); //?????????????????????????????????????????????????????????
            }
            c_imageView.setImageBitmap(bmp);
        }
    }


    /**
     * ????????????
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mytoolbar, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_ConfirmSpeed:
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
     * ?????????????????????????????????
     */
    private final class TouchListener implements View.OnTouchListener {
        private Matrix currentMatrix = new Matrix();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float point_x, point_y;
            currentMatrix.set(c_imageView.getImageMatrix());
            float sizeW_times = (float) (Math.round((float) c_imageView.getWidth() / client.getPicW() * 1000)) / 1000;//map_xy.getX()
            float sizeH_times = (float) (Math.round((float) c_imageView.getHeight() / client.getPicH() * 1000)) / 1000;//map_xy.getY()
            float size = sizeW_times < sizeH_times ? sizeW_times : sizeH_times;

            float qW = (c_imageView.getWidth() / size - client.getPicW()) / 2;
            point_x = event.getX() / size - qW;
            float qH = (c_imageView.getHeight() / size - client.getPicH()) / 2;
            point_y = event.getY() / size - qH;
            if (point_x > client.getPicW())
                point_x = client.getPicW();
            else if (point_x <= 0)
                point_x = 0;
            if (point_y > client.getPicH())
                point_y = client.getPicH();
            else if (point_y <= 0)
                point_y = 0;
            if (drawOrNot) {//???????????????????????????(????????????????????????????????????)
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        limitLines.add(new LimitLine((int) point_x, (int) point_y));
                        drawMap(limitLines, dockSites);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        limitLines.add(new LimitLine((int) point_x, (int) point_y));
                        drawMap(limitLines, dockSites);
                        break;
                }
            }
            return true;
        }
    }

    /**
     * ????????????????????????????????????
     */
    private void savePicture() {
        bitmap = createBitmap(bitmapbuff, client.getPicW(), client.getPicH());
        myPicture.setRow(client.getPicH());
        myPicture.setColumn(client.getPicW());
        myPicture.setStation_number(dockSites.size());
        myPicture.setOrigin_x(client.getOriginal_pointX());
        myPicture.setOrigin_y(client.getOriginal_pointY());
    }

    /**
     * ??????????????????
     *
     * @param values ??????
     * @param picW   ???
     * @param picH   ???
     * @return ?????????
     */
    private static Bitmap createBitmap(byte[] values, int picW, int picH) {
        if (values == null || picW <= 0 || picH <= 0)
            return null;
        //??????8??????????????????
        Bitmap bitmap = Bitmap.createBitmap(picW, picH, Bitmap.Config.ARGB_8888);
        int pixels[] = new int[picW * picH];
        for (int i = 0; i < pixels.length; ++i) {
            //??????????????????????????????
            int temp;
            temp = values[i] > 0 ? values[i] : (values[i] + 255);
            pixels[i] = temp * 256 * 256 + temp * 256 + temp + 0xFF000000;//
        }
        bitmap.setPixels(pixels, 0, picW, 0, 0, picW, picH);
        values = null;
        pixels = null;
        return bitmap;
    }


    public static byte[] getByteStream(String fileName) {
        try {
            String file = Environment.getExternalStorageDirectory() + "/AgvCar" + "/pbstream" + "/" + fileName;
            // ???????????????
            FileInputStream input = new FileInputStream(file);
            // ???????????????
            byte[] buf = new byte[input.available()];
            // ??????????????????
            input.read(buf);
            // ???????????????
            input.close();
            // ????????????
            return buf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * ??????byte??????????????????
     *
     * @param bytes ?????????????????????byte??????
     */
    public static void createFileWithByte(byte[] bytes) {
        /**
         * ??????File???????????????????????????????????????????????????????????????
         */
        File file = new File(Environment.getExternalStorageDirectory() + "/AgvCar" + "/pbstream", fileName);
        // ??????FileOutputStream??????
        FileOutputStream outputStream = null;
        // ??????BufferedOutputStream??????
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // ???????????????????????????
            if (file.exists()) {
                file.delete();
            }
            // ?????????????????????????????????????????????????????????
            file.createNewFile();
            // ??????FileOutputStream??????
            outputStream = new FileOutputStream(file);
            // ??????BufferedOutputStream??????
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // ???????????????????????????????????????byte??????
            bufferedOutputStream.write(bytes);
            // ?????????????????????????????????????????????????????????flush()??????????????????????????????????????????
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // ??????????????????
            e.printStackTrace();
        } finally {
            // ????????????????????????
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

    /**
     * ?????????????????????????????????
     */
    private void inputPictureName() {
        final EditText inputServer = new EditText(this);
        AlertDialog.Builder radioDialog = new AlertDialog.Builder(this);
        radioDialog.setTitle("????????????")
                .setView(inputServer);
        radioDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        radioDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //????????????????????????
                fileName = inputServer.getText().toString();
                SavaImageInPhone(bitmap, inputServer.getText().toString());
//                client.receivePbstream(); //??????pbstream??????
            }
        });
        radioDialog.create().show();
    }

    /**
     * ????????????????????????,?????????????????????????????????????????????????????????????????????
     *
     * @param bitmap
     * @param name
     */
    public void SavaImageInPhone(Bitmap bitmap, String name) {
        String picpath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
        File file = new File(picpath);
        if (!file.exists())
            file.mkdirs();
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        this.sendBroadcast(intent);
        try {
            File picFile = new File(picpath, name + ".png");
            if (picFile.exists())
                Toast.makeText(Control_getPic.this, "???????????????", Toast.LENGTH_SHORT).show();
            else {
                FileOutputStream fileOutputStream = new FileOutputStream(picFile.getPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", "");
                this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + picFile.getAbsolutePath())));
                //??????????????????
                uploadMapMes(name);//??????????????????
                uploadMap(name);           //??????????????????????????????
                //??????????????????
                phoneUploadMapMes(name);
                //??????????????????
//                uploadPicture(name);       //??????
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void phoneUploadMapMes(String picName) {
        Gson gson = new Gson();
        List<DockSite> sendMesList = new ArrayList<>();
        for (DockSite dockSite : dockSites.values())
            sendMesList.add(dockSite);
        String mypic = gson.toJson(myPicture);//??????????????????????????????
        writeInPhone(picName, mypic);
    }

    /**
     * ??????????????????????????????
     */
    private void uploadPicture(String pictureName) {
        String url = baseUrl + "MyTest/UploadFileServlet";
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + pictureName + ".png";
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams param = new RequestParams();
        try {
            File file = new File(filePath);
            param.put("file", file);
            httpClient.post(url, param, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(Control_getPic.this, "????????????", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadMapMes(String picName) {
        Gson gson = new Gson();
        List<DockSite> sendMesList = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        //?????????
        UpMapMes upMapMes = retrofit.create(UpMapMes.class);
//        int count=0;
        for (DockSite dockSite : dockSites.values())
            sendMesList.add(dockSite);
        String sendMes = gson.toJson(sendMesList);
        Call<ResponseBody> data = upMapMes.uploadMes(picName, sendMes);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

    }

    /**
     * ??????????????????????????????????????????
     * 1. ???????????????????????????????????????
     * 2. ??????x,y,angle
     */
    private void uploadMap(String picName) {
        Gson gson = new Gson();
        String mypic = gson.toJson(myPicture);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        UpMap upMap = retrofit.create(UpMap.class);
        Call<ResponseBody> data = upMap.upmap(picName, mypic);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseMes = response.body().string();
                    if (responseMes.equals("true"))
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                saveMapMesSuccess();
                            }
                        });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    public void writeInPhone(String fileName, String string) {
        /**
         * ??????File???????????????????????????????????????????????????????????????
         */
        File file = new File(Environment.getExternalStorageDirectory() + "/AgvCar" + "/mapMes", fileName + ".txt");//
        // ??????FileOutputStream??????
        FileOutputStream outputStream = null;
        // ??????BufferedOutputStream??????
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // ???????????????????????????
            if (file.exists()) {
                file.delete();
            }
            // ?????????????????????????????????????????????????????????
            file.createNewFile();
            // ??????FileOutputStream??????
            outputStream = new FileOutputStream(file);
            // ??????BufferedOutputStream??????
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // ???????????????????????????????????????byte??????
            bufferedOutputStream.write(string.getBytes());
            // ?????????????????????????????????????????????????????????flush()??????????????????????????????????????????
            bufferedOutputStream.flush();
            Log.i("tag", "____________????????????????????????");
        } catch (Exception e) {
            // ??????????????????
            e.printStackTrace();
        } finally {
            // ????????????????????????
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

    private void saveMapMesSuccess() {
        Toast.makeText(this, "????????????????????????", Toast.LENGTH_SHORT).show();
    }

    public static ImageView getImageView() {
        return c_imageView;
    }

    public static int getLx() {
        return Lx;
    }

    public static int getLy() {
        return Ly;
    }

    public static boolean isSendSpeedOrNot() {
        return sendSpeedOrNot;
    }

    public static boolean isStereoFlag() {
        return stereoFlag;
    }

    public void SavaImage(Bitmap bitmap, String path) {
        String picpath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
        File file = new File(picpath);
        if (!file.exists()) file.mkdirs();

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        this.sendBroadcast(intent);
        try {
            FileOutputStream fileOutputStream = null;
            File picFile = new File(picpath, "aa.png");
            fileOutputStream = new FileOutputStream(picFile.getPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();

            MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", "");
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + picFile.getAbsolutePath())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LoadImage() {
        if (ContextCompat.checkSelfPermission(Control_getPic.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Control_getPic.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //?????????document?????????uri????????????document id??????
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
            //?????????content?????????uri??????????????????????????????
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //?????????File?????????uri?????????????????????????????????
            imagePath = uri.getPath();
        }
        displayImage(imagePath);//??????????????????????????????
    }

    private void handleImageBeforeKitkat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);

    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //??????uri???selection??????????????????????????????
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
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            c_imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }


}
