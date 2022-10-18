package com.example.testcallback;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Ping extends Thread{


    PingCallBack pingCallBack;
    String ipAddress;

    int qtdPacage = 20;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getQtdPacage() {
        return qtdPacage;
    }

    public void setQtdPacage(int qtdPacage) {
        this.qtdPacage = qtdPacage;
    }

    public Ping(PingCallBack pingCallBack) {
        this.pingCallBack = pingCallBack;
    }

    @Override
    public void run() {
        String pingCommand = "/system/bin/ping -c " + qtdPacage + " -i 0.2 " + ipAddress;
        String inputLine = "";

        double ping = 0, max = 0, min = 0, med = 0, jitter = 0, loss = 0;
        int qtdeEnviado = 0, qtdeRecebido = 0, progress = 0;

        try {
            // execute the command on the environment interface
            Process process = Runtime.getRuntime().exec(pingCommand);
            // gets the input stream to get the output of the executed command
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            inputLine = bufferedReader.readLine();
            while ((inputLine != null)) {
                if (inputLine.length() > 0 && inputLine.contains("avg")) {
                    Log.i("PING", inputLine);
                    String array[] = {};

                    array = inputLine.replace("rtt min/avg/max/mdev = ","").replace("ms","").replace(" ","").split("/");
                    min = Double.parseDouble(array[0]);
                    max = Double.parseDouble(array[2]);
                    med = Double.parseDouble(array[1]);
                    if(array[3].contains("pipe")){
                        jitter = Double.parseDouble(array[3].split(",")[0]);
                    }else{
                        jitter = Double.parseDouble(array[3]);
                    }


                    pingCallBack.onFinish(min, max, med, jitter, loss, qtdeEnviado, qtdeRecebido);

                    break;
                }
                inputLine = bufferedReader.readLine();
                try {
                    if (inputLine.length() > 0 && inputLine.contains("time="))
                        //Log.v("PING", inputLine.substring(inputLine.lastIndexOf("time=")).replace("time=","").replace(" ms",""));
                        ping = Double.parseDouble(inputLine.substring(inputLine.lastIndexOf("time=")).replace("time=","").replace(" ms","").replace("(DUP!)","").trim());
                    if (inputLine.length() > 0 && inputLine.contains("icmp_seq")) {
                        String array[] = {};

                        array = inputLine.substring(inputLine.indexOf("icmp_seq=")).replace(" ", "/").split("/");
                        progress = Integer.parseInt( array[0].replace("icmp_seq=",""));
                        Log.v("PING", array[0].replace("icmp_seq=",""));
                    }
                }catch (Exception e){this.stop(); pingCallBack.onErro(e.getMessage()); e.printStackTrace();}
                pingCallBack.onPingResult(ping, progress);

                if (inputLine.length() > 0 && inputLine.contains("packets")) {  // when we get to the last line of executed ping command
                    String array[] = {};
                    array = inputLine.split(",");
                    loss = Double.parseDouble(array[array.length - 2].replace(" ","").replace("%packetloss",""));

                    qtdeEnviado = Integer.parseInt(array[0].replace("packets transmitted", "").trim());
                    qtdeRecebido = Integer.parseInt(array[1].replace("received", "").trim());

                    Log.v("PING", "Transmitidos: " + array[0].replace("packets transmitted", "").replace(" ","") +"Recebidos: " + array[1].replace("received","").replace(" ",""));
                }
                Log.v("IMPUTE", inputLine);
            }
            //Log.v("PING", min+"\n"+med+"\n"+max+"\n"+jitter);
        }
        catch (IOException e){
            Log.v("PING", "getLatency: EXCEPTION");
            e.printStackTrace();
            pingCallBack.onErro(e.getMessage());
        }
    }


    public interface PingCallBack {
         void onPingResult(double ping, int progress);
         void onFinish(double min, double max, double med, double jitter, double loss, int qtdeEnviado, int qtdeRecebido);
         void onErro(String erro);
    }
}
