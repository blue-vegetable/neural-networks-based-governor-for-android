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


    public static class CommandResult {   // 该类为返回值的类
        public int result = -1;
        public String errorMsg;
        public String successMsg;
    }


    public static CommandResult execCommand(String command, boolean isRoot) {   // 需要显示输出时调用的shell用的函数
        String[] commands = {command};
        return actualExecCommand(commands, isRoot,true);
    }

    public static void easyExec(String command, boolean isRoot){              // 不需要输出时的函数
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
        StringBuilder successMsg;
        StringBuilder errorMsg;
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

            if(outputOrNot){    // 下面都是些处理输出的代码
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
        } catch (Exception e) {   // 基本不会出现，但是写个
            String errmsg = e.getMessage();
            if (errmsg != null) {
                Log.e(TAG, errmsg);
            } else {
                e.printStackTrace();
            }
        } finally {   // 善后工作，该回收的回收
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
