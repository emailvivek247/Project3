package net.javacoding.xsearch.foundation;

import java.io.File;

public class ForkThread extends Thread {
	
    String[] command = null;
    String environment = null;
    File workingDirectory = null;

    public ForkThread(String[] cmd){
        this.command = cmd;
        this.environment = null;
    }
    
    public ForkThread(String[] cmd, String env){
        this.command = cmd;
        this.environment = env;
    }
    
    public ForkThread(String[] cmd, String env, File f) {
        this.command = cmd;
        this.environment = env;
        this.workingDirectory = f;
    }

    public void run(){
        Execute exe = new Execute(command, environment, workingDirectory);
        exe.start();
    }

    private static void main(String[] args){

        String[] cmd = new String[3+args.length];
        cmd[0] = System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java";
        cmd[1] = "-cp";
        cmd[2] = System.getProperty("java.class.path");
        for(int i = 0; i< args.length; i++){
            cmd[3+i] = args[i];
        }
        ForkThread ft = new ForkThread(cmd);
        //ForkThread ft = new ForkThread(new String[]{"e:\\WINXP\\notepad.exe"});
        ft.run();
    }
}
