package com.example.networktrans;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.nio.ByteBuffer;

public class TFLiteClassificationUtil {
    private Interpreter tflite;
    private float [] output;
    public TFLiteClassificationUtil(String modelPath) throws Exception {
        File file = new File(modelPath);
        if (!file.exists()) {
            throw new Exception("model file does not exist");
        }
        Interpreter.Options options = new Interpreter.Options();
        tflite = new Interpreter(file, options);
    }

    public void predict(float[] x, ByteBuffer y){
        tflite.run(x,y);
    }
}


