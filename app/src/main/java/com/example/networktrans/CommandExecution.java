package com.example.networktrans;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

public class CommandExecution {

    public static final String TAG = "CommandExecution";

    public final static String COMMAND_SU       = "su";
    public final static String COMMAND_SH       = "sh";
    public final static String COMMAND_EXIT     = "exit\n";
    public final static String COMMAND_LINE_END = "\n";


    public static class CommandResult {
        public int result = -1;
        public String errorMsg;
        public String successMsg;
    }


    public static CommandResult execCommand(String command, boolean isRoot) {
        String[] commands = {command};
        return actualExecCommand(commands, isRoot,true);
    }

    public static void easyExec(String command, boolean isRoot){
        String [] commands = {command};
        actualExecCommand(commands,isRoot,false);
    }


    public static CommandResult actualExecCommand(String[] commands, boolean isRoot, boolean outputOrNot) {
        CommandResult commandResult = new CommandResult();
        if (commands == null || commands.length == 0) return commandResult;
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command != null) {
                    os.write(command.getBytes());
                    os.writeBytes(COMMAND_LINE_END);
                    os.flush();
                }
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            commandResult.result = process.waitFor();
            if(outputOrNot){
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) successMsg.append(s).append('\n');
                while ((s = errorResult.readLine()) != null) errorMsg.append(s);
                commandResult.successMsg = successMsg.toString();
                commandResult.errorMsg = errorMsg.toString();
            }
        } catch (IOException e) {
            String errmsg = e.getMessage();
            if (errmsg != null) {
                Log.e(TAG, errmsg);
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            String errmsg = e.getMessage();
            if (errmsg != null) {
                Log.e(TAG, errmsg);
            } else {
                e.printStackTrace();
            }
        } finally {
            try {
                if (os != null) os.close();
                if (successResult != null) successResult.close();
                if (errorResult != null) errorResult.close();
            } catch (IOException e) {
                String errmsg = e.getMessage();
                if (errmsg != null) {
                    Log.e(TAG, errmsg);
                } else {
                    e.printStackTrace();
                }
            }
            if (process != null) process.destroy();
        }
        return commandResult;
    }

}
