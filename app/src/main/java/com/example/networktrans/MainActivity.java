package com.example.networktrans;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private Button btnStart;
    private Button btnStop;
    private Button btnCheck;
    private TextView output;
    private Intent intent;
    private boolean serviceRunningOrNot = false;
    private String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setEventListener();
        Log.d(TAG,"activity here");

    }

    private void init() {
        btnStart = findViewById(R.id.button1);
        btnStop = findViewById(R.id.button2);
        btnCheck = findViewById(R.id.button3);
        output = findViewById(R.id.textView3);
        intent = new Intent(this, MyService.class);

    }

    private void setEventListener(){
        btnCheck.setOnClickListener(ClickInHere);
        btnStart.setOnClickListener(ClickInHere);
        btnStop.setOnClickListener(ClickInHere);
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        stopService(serviceIntent);
    }


    @SuppressLint("SetTextI18n")
    private void checkService() {
        String toastString = "big cpu frequency:" + SystemInformationUtils.getBigCpuFreq() + "\n" +
                "small cpu frequency:" + SystemInformationUtils.getLittleCpuFreq() + "\n";
        if(serviceRunningOrNot) {
            output.setText("running");
        }else{
            output.setText("dead");
        }

    }

    private View.OnClickListener ClickInHere = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            final int temp = v.getId();
            switch(temp) {
                case R.id.button1:startService();break;
                case R.id.button2:stopService();break;
                case R.id.button3:checkService();break;
                default: break;
            }
        }
    };







}