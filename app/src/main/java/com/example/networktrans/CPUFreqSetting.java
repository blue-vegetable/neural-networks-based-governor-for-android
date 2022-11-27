package com.example.networktrans;

import android.util.Log;

public class CPUFreqSetting {
    private static int [] littleChoice = {1612800, 1689600 ,1766400 };
    private static int [] bigChoice = {2476800,2553600,2649600 };

    public static void setFreq(int choice) throws Exception {
        if(choice >= 9 || choice < 0){
            Log.d("TAG","do nothing this time");
            return;
        }

        int bigFreq = bigChoice[choice / 3];
        int littleFreq = littleChoice[choice % 3];
//        String command = "echo \"userspace\" >  /sys/devices/system/cpu/cpufreq/policy0/scaling_governor"; // little cpu governor
//        CommandExecution.execCommand(command,true);
//        command = "echo \"userspace\" >  /sys/devices/system/cpu/cpufreq/policy4/scaling_governor";  // big cpu governor
//        CommandExecution.execCommand(command,true);
        String command = "echo " + littleFreq + " > /sys/devices/system/cpu/cpufreq/policy0/scaling_setspeed"; // set little cpu frequency;
        CommandExecution.execCommand(command,true);
        command = "echo " + bigFreq + " > /sys/devices/system/cpu/cpufreq/policy4/scaling_setspeed"; // set big cpu frequency;
        CommandExecution.execCommand(command,true);


        // below is check code
//        Log.d("TAG","this time , choice is " + Integer.toString(choice));
//        Log.d("TAG","set freq as below : === little:"+littleFreq + "  big:"+bigFreq + "===");
//        Log.d("TAG", "cat cpuinfo_cur_freq file:");
//        command = "cat /sys/devices/system/cpu/cpufreq/policy0/cpuinfo_cur_freq";
//        Log.d("TAG","little_cur_freq="+CommandExecution.execCommand(command,true).successMsg);
//        command = "cat /sys/devices/system/cpu/cpufreq/policy4/cpuinfo_cur_freq";
//        Log.d("TAG","big_cur_freq=" + CommandExecution.execCommand(command,true).successMsg);
//        Log.d("TAG","************************************\n\n");
    }

}
