package com.example.networktrans;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
    String TAG = "MyService";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public class SystemInfoThread extends Thread{
        private String view;
        public SystemInfoThread(String view){
            this.view = view;
        }
        @Override
        public void run(){
            //////////////////
//            while(SystemInformationUtils.lastTimestamp == null){
//                try {
//                    Thread.sleep(3000);
//                    SystemInformationUtils.init(view);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            while(true){
//                try {
//                    Thread.sleep(1000);
//                    calculate();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            ///////////////



            boolean currentViewIsOrNot = false;
            String currentActivity = null;
            while(true){
                if(!currentViewIsOrNot) {    // 如果当前的view不是想要的view
                    do {   // 开始循环获得当前的view
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        currentActivity = SystemInformationUtils.getCurrentFocusWindow();
                        Log.d(TAG, "current view is " + currentActivity);
                    } while (!currentActivity.equals(view.substring(0, view.length() - 2)));  // 判断当前前台是否是抖音，如果不是，则继续循环
                    // 能出循环说明当前已经是抖音里面了
                    try {
                        Thread.sleep(4000);
                        SystemInformationUtils.init(view);  // init 如果失败，说明当前不在抖音中，或者此时很卡
                        currentViewIsOrNot = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;                           // 从头开始
                    }
                }

                if(SystemInformationUtils.lastTimestamp == null){
                    try {
                        Thread.sleep(4000);
                        SystemInformationUtils.init(view);  // init 如果失败，说明当前不在抖音中，或者此时很卡
                    } catch (Exception e) {
                        e.printStackTrace();
                        currentViewIsOrNot = false;
                        continue;                           // 从头开始
                    }
                }

                try {                                       // 能走到这里，说明已经init成功了
                    Thread.sleep(1000);
                    calculate();                            // 使用模型计算下一次的频率以及设置频率
                } catch (Exception e) {
                    currentViewIsOrNot = false;
                    SystemInformationUtils.lastTimestamp = null;
                    Log.d(TAG,"here is not the wanted view ");
                    e.printStackTrace();
                }
            }
        }
    }

    private void init() {
        view = "com.ss.android.ugc.aweme/com.ss.android.ugc.aweme.splash.SplashActivity#0";   // 目前view先作为固定的值，到时再拓展
    }

    public MyService() {
    }

    private String createNotificationChannel(String channelId, String channelName){       // 这部分控制通知栏上显示的前台服务的情况
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent,  PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();
        init();

        Context context = getApplicationContext();
        Intent intent = new Intent(context,MyService.class);
        context.startForegroundService(intent);
//        Notification notification1 = createForegroundNotification();  // 启动前台服务的通知
//        startForeground(1, notification1);

        try {                                                // 这部分载入模型
            long t1 = System.currentTimeMillis();
            loadModel();
            long t2 = System.currentTimeMillis();
            Log.d(TAG,"load model takes " + (t2 - t1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String command = "echo \"userspace\" >  /sys/devices/system/cpu/cpufreq/policy0/scaling_governor"; // little cpu governor
        CommandExecution.easyExec(command,true);
        command = "echo \"userspace\" >  /sys/devices/system/cpu/cpufreq/policy4/scaling_governor";  // big cpu governor
        CommandExecution.easyExec(command,true);

        MyService.SystemInfoThread sit = new MyService.SystemInfoThread(view);    // 启动调频的线程
        sit.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private void loadModel() {
        String classificationModelPath = getCacheDir().getAbsolutePath() + File.separator + "model.tflite";  // 获取asset文件夹的目录
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

    private void calculate() throws Exception {                            // 模型预测和频率修改的函数
        int[] shape = {1,3};        //  模型的输入的shape
        int[] shape2 ={1,9};        //  模型的输出的shape

        float fps = 0F;
        fps = Float.parseFloat(SystemInformationUtils.getFps());         // 获得fps

        float [] input = {Float.parseFloat(SystemInformationUtils.getBigCpuFreq()),Float.parseFloat(SystemInformationUtils.getLittleCpuFreq()),fps}; // 构建inpt

        x.loadArray(input,new int[]{3});
        y = TensorBuffer.createFixedSize(shape2,DataType.FLOAT32);      // tensorbuffer所特有的加载方式

        long t1 = System.currentTimeMillis();
        tfLiteClassificationUtil.predict(x.getFloatArray(), y.getBuffer());    // 真正进行预测的代码
        long t2 = System.currentTimeMillis();
        Log.d(TAG,"predict takes " + (t2 - t1));

        int choice = 0;                                                 // 这部分找到结果中的最大值及其下标
        float max = Float.MIN_VALUE;
        for(int i = 0; i < y.getFloatArray().length; i++){
            if(y.getFloatArray()[i] > max){
                max = y.getFloatArray()[i];
                choice = i;
            }
        }

        Log.d(TAG,"************************************");
        Log.d(TAG,"this time "+ "little:" + input[1] + "\tbig:" + input[0] + "\tfps:" + input[2]);
        t1 = System.currentTimeMillis();
        CPUFreqSetting.setFreq(choice);
        t2 = System.currentTimeMillis();
        Log.d(TAG,"set frequency takes " + (t2 - t1));
    }

    protected Notification createForegroundNotification(){
        String channelId = "ForegroundService";
        String channelName = "MyService";

        //通知内容
        return new NotificationCompat.Builder(this,channelId)
                .build();
    }
}