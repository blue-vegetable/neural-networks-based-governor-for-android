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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Log.d("TAG","activity here");
//        setEventListener();
//        try {
//            long t1 = System.currentTimeMillis();
//            loadModel();
//            long t2 = System.currentTimeMillis();
//            Log.d("TAG","load model takes " + (t2 - t1));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String command = "echo \"userspace\" >  /sys/devices/system/cpu/cpufreq/policy0/scaling_governor"; // little cpu governor
//        CommandExecution.execCommand(command,true);
//        command = "echo \"userspace\" >  /sys/devices/system/cpu/cpufreq/policy4/scaling_governor";  // big cpu governor
//        CommandExecution.execCommand(command,true);
//        SystemInfoThread sit = new SystemInfoThread(view);
//        sit.start();
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
                case R.id.button3: buffer.add(2.0F);break;
                default: break;
            }
        }
    };




}