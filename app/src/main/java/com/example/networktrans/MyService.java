package com.example.networktrans;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;

public class MyService extends Service {
    private TFLiteClassificationUtil tfLiteClassificationUtil;
    String view;
    TensorBuffer x =  TensorBuffer.createDynamic(DataType.FLOAT32);
    TensorBuffer y =  TensorBuffer.createDynamic(DataType.FLOAT32);


    public class SystemInfoThread extends Thread{
        private String view;
        public SystemInfoThread(String view){
            this.view = view;
        }
        @Override
        public void run(){
            while(SystemInformationUtils.lastTimestamp == null){
                try {
                    Thread.sleep(1000);
                    SystemInformationUtils.init(view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            while(true){
                try {
                    Thread.sleep(1000);
                    calculate();
//                    Toast.makeText(MyService.this, "running", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void init() {
        view = "com.ss.android.ugc.aweme/com.ss.android.ugc.aweme.splash.SplashActivity#0";
    }

    public MyService() {
    }

    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }


    @Override
    public void onCreate(){
        super.onCreate();

        init();
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        Log.d("TAG", "onStart");
        String channelId = null;
        // 8.0 以上需要特殊处理
        channelId = createNotificationChannel("kim.hsl", "ForegroundService");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(1)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);

        try {
            long t1 = System.currentTimeMillis();
            loadModel();
            long t2 = System.currentTimeMillis();
            Log.d("TAG","load model takes " + (t2 - t1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String command = "echo \"userspace\" >  /sys/devices/system/cpu/cpufreq/policy0/scaling_governor"; // little cpu governor
        CommandExecution.execCommand(command,true);
        command = "echo \"userspace\" >  /sys/devices/system/cpu/cpufreq/policy4/scaling_governor";  // big cpu governor
        CommandExecution.execCommand(command,true);
        MyService.SystemInfoThread sit = new MyService.SystemInfoThread(view);
        sit.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private void loadModel() {
        String classificationModelPath = getCacheDir().getAbsolutePath() + File.separator + "model.tflite";
        Utils.copyFileFromAsset(MyService.this, "model.tflite", classificationModelPath);
        // load the model
        try {
            tfLiteClassificationUtil = new TFLiteClassificationUtil(classificationModelPath);
            Toast.makeText(MyService.this, "load model succeeded", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MyService.this, "load model failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void calculate() throws Exception {
        int[] shape = {1,3};
        int[] shape2 ={1,9};

        float fps = 0F;
        try {
            fps = Float.parseFloat(SystemInformationUtils.getFps());
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        float [] input = {Float.parseFloat(SystemInformationUtils.getBigCpuFreq()),Float.parseFloat(SystemInformationUtils.getLittleCpuFreq()),fps};

        x.loadArray(input,new int[]{3});
        y = TensorBuffer.createFixedSize(shape2,DataType.FLOAT32);

        long t1 = System.currentTimeMillis();
        tfLiteClassificationUtil.predict(x.getFloatArray(), y.getBuffer());
        long t2 = System.currentTimeMillis();
        Log.d("TAG","predict takes " + (t2 - t1));

        int choice = 0;
        float max = Float.MIN_VALUE;
        for(int i = 0; i < y.getFloatArray().length; i++){
            if(y.getFloatArray()[i] > max){
                max = y.getFloatArray()[i];
                choice = i;
            }
        }

        Log.d("TAG","************************************");
        Log.d("TAG","this time "+ "little:" + input[1] + "\tbig:" + input[0] + "\tfps:" + input[2]);
        t1 = System.currentTimeMillis();
        CPUFreqSetting.setFreq(choice);
        t2 = System.currentTimeMillis();
        Log.d("TAG","set frequency takes " + (t2 - t1));
    }
}