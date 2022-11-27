package com.example.networktrans;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    private Button btn_1;
    private Button btn_2;
    private Button btn_equ;
    private TextView output;
    private TFLiteClassificationUtil tfLiteClassificationUtil;
    private ArrayList<Float> buffer = new ArrayList<>();
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
                    Thread.sleep(100);
                    SystemInformationUtils.init(view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            while(true){
                try {
                    Thread.sleep(1000);
                    calculate();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setEventListener();
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
        SystemInfoThread sit = new SystemInfoThread(view);
        sit.start();
    }

    private void init() {
        btn_1 =findViewById(R.id.button1);
        btn_2 = findViewById(R.id.button2);
        btn_equ = findViewById(R.id.button3);
        output = findViewById(R.id.textView);
        view = "com.ss.android.ugc.aweme/com.ss.android.ugc.aweme.splash.SplashActivity#0";
    }

    private void setEventListener(){
        btn_1.setOnClickListener(ClickInHere);
        btn_2.setOnClickListener(ClickInHere);
        btn_equ.setOnClickListener(ClickInHere);
    }

    private View.OnClickListener ClickInHere = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.button1: buffer.add(1.0F); output.setText("1");
                    try {
                        Runtime.getRuntime().exec("su");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.button2: buffer.add(2.0F);  output.setText("2"); break;
                case R.id.button3: try {
                    calculate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                default: break;
            }
        }
    };

    private void loadModel() {
        String classificationModelPath = getCacheDir().getAbsolutePath() + File.separator + "model.tflite";
        Utils.copyFileFromAsset(MainActivity.this, "model.tflite", classificationModelPath);
        // load the model
        try {
            tfLiteClassificationUtil = new TFLiteClassificationUtil(classificationModelPath);
            Toast.makeText(MainActivity.this, "load model succeeded", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "load model failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
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