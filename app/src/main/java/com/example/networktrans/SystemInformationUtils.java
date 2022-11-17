package com.example.networktrans;

import java.util.ArrayList;
import java.util.Collections;

public class SystemInformationUtils {
    public static String getBigCpuFreq(){
        String command = "cat /sys/devices/system/cpu/cpufreq/policy4/cpuinfo_cur_freq";
        return CommandExecution.execCommand(command,true).successMsg;
    }
    public static String getLittleCpuFreq(){
        String command = "cat /sys/devices/system/cpu/cpufreq/policy0/cpuinfo_cur_freq";
        return CommandExecution.execCommand(command,true).successMsg;
    }

    private static ArrayList<String> getFrameData(String view){
        String command = "dumpsys SurfaceFlinger --latency" + " " + view;
        // dumpsys SurfaceFlinger --latency com.example.networktrans/com.example.networktrans.MainActivity#0
        CommandExecution.execCommand(command,true);
        return new ArrayList<String>(Collections.singleton(command));
    }


    public static String getFps(String view){
        String command = "dumpsys SurfaceFlinger --latency-clear";
        CommandExecution.execCommand(command,true);
        getFrameData(view);
        return "";


    }
}
