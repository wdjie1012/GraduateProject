package common;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

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
import java.util.List;
import java.util.Map;

import model.CarMessage;
import model.Region;
import model.State;

public class ToolKit {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static float byteToFloat(byte[] b) {
        // 4 bytes
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 4; shiftBy++) {
            accum |= (b[shiftBy] & 0xff) << shiftBy * 8;
        }
        return Float.intBitsToFloat(accum);
    }

    public static byte[] FloatToByte(float x) {
        byte[] b = new byte[4];
        int l = Float.floatToIntBits(x);
        for (int i = 0; i < 4; i++) {
            b[i] = Integer.valueOf(l).byteValue();
            l = l >> 8;
        }
        return b;
    }

    public static int byteToInt(byte[] b) {
        return b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }

    public static byte[] intToByte(int val) {
        byte[] b = new byte[4];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        b[2] = (byte) ((val >> 16) & 0xff);
        b[3] = (byte) ((val >> 24) & 0xff);
        return b;
    }

    /**
     * ??????????????????
     *
     * @param values ??????
     * @param picW   ???
     * @param picH   ???
     * @return ?????????
     */
    public static Bitmap createBitmap(byte[] values, int picW, int picH) {
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

    public static void drawPointOnBitmap(Map<Integer, CarMessage>carMap,
                                         Bitmap bitmap, int height, ImageView imageView,int carChoseNumber,
                                         List<State>list) {
        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        //????????????
        paint.setColor(Color.BLACK);
        if(!list.isEmpty()){
            for(State state:list){
                float x = state.getX() * 100 / 5;
                float y = height - ( state.getY() * 100 / 5);
                float angle= state.getAngle();

                //?????????
                canvas.rotate(-angle, x, y);
                Path path = new Path();
                path.moveTo(x + 5, y);
                path.lineTo(x - 5, y + 3);
                path.lineTo(x - 5, y - 3);
                path.close();
                canvas.drawPath(path, paint);
                canvas.rotate(angle,x,y);
                canvas.drawText(state.getName(), x - 8, y - 4, paint);
            }
        }

        //???????????????
        //-------------????????????-----------------//
//        List<Region>regionList=new ArrayList<>();
//        Region r0=new Region((float)0,(float)0,1,"r0");
//        Region r1=new Region((float)0,(float)6.5,1,"r1");
//        Region r2=new Region((float)-6.5,(float)0,1,"r2");
//        Region r3=new Region((float)0,(float)-6.5,1,"r3");
//        Region r4=new Region((float)6.5,(float)0,1,"r4");
//        regionList.add(r0);
//        regionList.add(r1);
//        regionList.add(r2);
//        regionList.add(r3);
//        regionList.add(r4);
//
//        for(Region r:regionList) {
//            float x = (r.x+(float)15.5) * 100 / 5;
//            float y = height - ( (r.y+(float)15.5) * 100 / 5);
//            float R=r.r*100/5;
//
//            paint.setColor(0x2361a7ff);
//            canvas.drawCircle(x, y, R , paint);
//            paint.setColor(Color.BLACK);
//            canvas.drawText(r.name,x-8,y-4,paint);
//        }
        //-------------------------------------//
        //-------------?????????-----------------//
        List<Region>regionList=new ArrayList<>();
        Region r0=new Region((float)0,(float)-3.4,1,"r0");
        Region r1=new Region((float)-4,(float)-3.4,1,"r1");
        Region r2=new Region((float)4,(float)-3.4,1,"r2");
        regionList.add(r0);
        regionList.add(r1);
        regionList.add(r2);

        for(Region r:regionList) {
            float x = (r.x+(float)15.5) * 100 / 5;
            float y = height - ( (r.y+(float)15.5) * 100 / 5);
            float R=r.r*100/5;

            paint.setColor(0x2361a7ff);
            canvas.drawCircle(x, y, R , paint);
            paint.setColor(Color.BLACK);
            canvas.drawText(r.name,x-8,y-4,paint);
        }
        //-------------------------------------//

        //?????????
        for(int i=1;i<=carMap.size();i++) {
            if(i==carChoseNumber)    paint.setColor(Color.RED);
            else    paint.setColor(Color.BLACK);;
            CarMessage carMessage =carMap.get(i);
            float x = carMessage.getX() * 100 / 5;
            float y = height - ( carMessage.getY() * 100 / 5);
            float angle= carMessage.getAngle();

            //?????????
            canvas.rotate(-angle, x, y);
            Path path = new Path();
            path.moveTo(x + 5, y);
            path.lineTo(x - 5, y + 3);
            path.lineTo(x - 5, y - 3);
            path.close();
            canvas.drawPath(path, paint);
            canvas.rotate(angle,x,y);
            canvas.drawText("car"+i, x - 8, y - 4, paint);
        }
        setBitmap(bmp,imageView);
    }


    public static void setBitmap(Bitmap bitmap, ImageView imageView){
        handler.post(() -> imageView.setImageBitmap(bitmap));
    }


    /**
     * ???????????????????????????
     * @param activity
     */
    public static void getStateInPhone(List<State>list, Activity activity){
        String path = Environment.getExternalStorageDirectory() + "/AgvCar/mapState/";
        File pfile = new File(path);
        if(pfile != null){
            File file = new File(path);
            final String[] fileNames = file.list();
            final int[] chooseItem = new int[1];
            final String[] radioItems = new String[fileNames.length];
            for (int i = 0; i < fileNames.length; i++)
                radioItems[i] = fileNames[i];
            AlertDialog.Builder radioDialog = new AlertDialog.Builder(activity);
            radioDialog.setTitle("??????????????????");
            radioDialog.setSingleChoiceItems(radioItems, 0, (dialog, which) -> chooseItem[0] = which);
            radioDialog.setPositiveButton("??????", (dialog, which) -> {
                String file1 = Environment.getExternalStorageDirectory() + "/AgvCar/mapState/" + fileNames[chooseItem[0]];
                String line = "";
                InputStream instream;
                try {
                    Gson gson = new Gson();
                    instream = new FileInputStream(file1);
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    line = buffreader.readLine();
                    List<State> dock = gson.fromJson(line, new TypeToken<List<State>>() {
                    }.getType());
                    list.clear();
                    list.addAll(dock);
                    instream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            });
            radioDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            radioDialog.show();
        }
    }


    /**
     * ????????????????????????
     * @param activity
     */
    public static void saveStateInPhone(Activity activity){
        if(isGrantExternalRW(activity)) {
            List<State> list = new ArrayList<>();
            list.add(new State("s1", 0, 0, 0));
            list.add(new State("s2", -8, 3, 0));

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_");
            String preName = df.format(new Date()) + "multiRobots";
            Gson gson = new Gson();
            String stateTmpMes = gson.toJson(list);//????????????

            File file = new File(Environment.getExternalStorageDirectory() + "/AgvCar/mapState", preName + ".txt");
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
                bufferedOutputStream.write(stateTmpMes.getBytes());
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
    }

    private static boolean isGrantExternalRW(Activity activity) {
        if (activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }

}
