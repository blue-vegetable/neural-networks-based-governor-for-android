package com.example.networktrans;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    private Button btn_1;
    private Button btn_2;
    private Button btn_equ;
    private TextView output;
    private TextView littleCpuFreq;
    private TextView bigCpuFreq;
    private TextView fps;
    private float input;
    private ArrayList<String> classNames;
    private TFLiteClassificationUtil tfLiteClassificationUtil;
    private ArrayList<Float> buffer = new ArrayList<>();

    TensorBuffer x =  TensorBuffer.createDynamic(DataType.FLOAT32);
    TensorBuffer y =  TensorBuffer.createDynamic(DataType.FLOAT32);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setEventListener();
    }
    @Override
    protected  void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    private void init(){
        btn_1 = (Button) findViewById(R.id.button1);
        btn_2 = (Button) findViewById(R.id.button2);
        btn_equ = (Button) findViewById(R.id.button3);
        output = (TextView) findViewById(R.id.textView);
        bigCpuFreq = findViewById(R.id.textView3);
        littleCpuFreq = findViewById(R.id.textView5);
        fps = findViewById(R.id.textView7);
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
                case R.id.button1: buffer.add(1.0F); output.setText("1"); break;
                case R.id.button2: buffer.add(2.0F);  output.setText("2"); break;
                case R.id.button3: try {
//                    flushInformation();
                    calculate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                default: break;
            }
        }
    };
    private void flushInformation(){
        littleCpuFreq.setText(SystemInformationUtils.getLittleCpuFreq());
        bigCpuFreq.setText(SystemInformationUtils.getBigCpuFreq());
        String view = "com.example.networktrans/com.example.networktrans.MainActivity#0";
        SystemInformationUtils.getFps(view);
    }

    private void calculate() throws Exception {
        String classificationModelPath = getCacheDir().getAbsolutePath() + File.separator + "model.tflite";
        Utils.copyFileFromAsset(MainActivity.this, "model.tflite", classificationModelPath);

        try {
            tfLiteClassificationUtil = new TFLiteClassificationUtil(classificationModelPath);
            Toast.makeText(MainActivity.this, "模型加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "模型加载失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }

        int size = buffer.size();
        int[] shape = {size};
        float [] temp = new float[size];
        int index = 0;
        for(final Float value: buffer){
            temp[index++] = value;
        }

        x.loadArray(temp,new int[] {index} );
        y = TensorBuffer.createFixedSize(shape,DataType.FLOAT32);
        tfLiteClassificationUtil.predict(x.getFloatArray(), y.getBuffer());

        StringBuilder str = new StringBuilder();
        for(final float value : y.getFloatArray()){
            str.append(",");
            str.append(value);
        }
        output.setText(str.toString());
    }
}