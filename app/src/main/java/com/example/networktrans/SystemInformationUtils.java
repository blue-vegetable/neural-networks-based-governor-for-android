package com.example.networktrans;

import android.util.Log;

import java.util.ArrayList;


public class SystemInformationUtils {
    static int fps = 0;
    static String view;
    static Float baseTimestamp;
    static Float lastTimestamp;

    public static String getBigCpuFreq(){
        String command = "cat /sys/devices/system/cpu/cpufreq/policy4/cpuinfo_cur_freq";
        return CommandExecution.execCommand(command,true).successMsg;
    }

    public static String getLittleCpuFreq(){
        String command = "cat /sys/devices/system/cpu/cpufreq/policy0/cpuinfo_cur_freq";
        return CommandExecution.execCommand(command,true).successMsg;
    }

    public static void init(String view1) throws Exception {
        view = view1;
        CommandExecution.easyExec("dumpsys SurfaceFlinger --latency-clear ", false);

        ArrayList<Float> timestamps = getFrameData();
        baseTimestamp = 0F;
        for(final Float value: timestamps){
            if(value != 0){
                baseTimestamp = value;
            }
        }
        if(baseTimestamp == 0){
            Log.d("TAG", "Initial frame collect failed");
            throw new Exception("=====please make the screen move=====");
        }
        lastTimestamp = timestamps.get(timestamps.size() -2);
    }

    private static ArrayList<Float> getFrameData(){
        String command = "dumpsys SurfaceFlinger --latency" + " " + view;
        // dumpsys SurfaceFlinger --latency com.example.networktrans/com.example.networktrans.MainActivity#0
        String output = CommandExecution.execCommand(command,true).successMsg;
        String [] outputSplited = output.split("\n");
        if (outputSplited.length == 0){
            Log.d("TAG","dumpsys sufaceflinger layername empty");
        }
        ArrayList<Float> timestamps = new ArrayList<>();
        int nanosecondsPerSecond = 1000000000;
        float refreshPeriod = Float.parseFloat(outputSplited[0]) / nanosecondsPerSecond;
        long  pendingFenceTimestamp = (1L << 63) - 1;
        for(final String value : outputSplited){
            if(value.length() <= 17  || value.length() >= 70){
                continue;
            }
            String [] valueSplited = value.split("\t");
            long start = Long.parseLong(valueSplited[0]);
            long submitting = Long.parseLong(valueSplited[1]);
            long submitted = Long.parseLong(valueSplited[2]);
            long timestamp = submitting;
            if(timestamp == pendingFenceTimestamp){
                continue;
            }
            float timestampSecond = timestamp * 1.0F /  nanosecondsPerSecond;
            timestamps.add(timestampSecond);
        }
        return timestamps;
    }

    public static String getFps() throws Exception {
        String command = "dumpsys SurfaceFlinger --latency-clear";
        CommandExecution.easyExec(command,true);
        ArrayList<Float> oldTimestamps = getFrameData();
        Thread.sleep(1000);
        ArrayList<Float> newTimestamps = getFrameData();
        for(final Float value : newTimestamps){
            if(value > lastTimestamp){
                oldTimestamps.add(value);
            }
        }
        if(oldTimestamps.size() > 0){
            lastTimestamp = oldTimestamps.get(oldTimestamps.size() - 1);
        }

        ArrayList<Float> adjustedTimestamps = new ArrayList<>();
        for(float value: oldTimestamps){
            value -= baseTimestamp;
            if(value <= 1000000){
                adjustedTimestamps.add(value);
            }
        }
        if(adjustedTimestamps.size() == 0){
            throw new Exception("=====此次没有成功获得帧率=====");
        }
        float fromTime = adjustedTimestamps.get(adjustedTimestamps.size() - 1) - 1.0F;
        if(fromTime < 0F){
            throw new Exception("=====此次没有成功获得帧率=====");
        }
        int fpsCount = 0;
        for(final float value : adjustedTimestamps){
            if(value > fromTime){
                fpsCount++;
            }
        }
        fps = fpsCount;
        return Integer.toString(fps);
    }

    public static String getCurrentFocusWindow(){
        String str = CommandExecution.execCommand("dumpsys window | grep mCurrentFocus",true).successMsg;
        String [] strSplited = str.split(" ");
        int n = strSplited.length;
        return strSplited[n-1].substring(0, strSplited[n-1].length()-2);
    }
}
