package com.example.testcallback;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextView tvPing;
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btOk = (Button) findViewById(R.id.btOk);
        tvPing = (TextView) findViewById(R.id.tvPing);
        pb = (ProgressBar) findViewById(R.id.progressBar);

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ping ping = new Ping(new Ping.PingCallBack() {
                    @Override
                    public void onPingResult(double ping, int progress) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvPing.setText((int)ping+" ms");
                                pb.setProgress(progress);
                            }
                        });

                    }

                    @Override
                    public void onFinish(double min, double max, double med, double jitter, double loss, int qtdEnviado, int qtdeRecebido) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvPing.setText("Min: "+min+" Max: "+max + " Med " + med+" Jitter: "+(int)jitter +" Loss: "+loss +"% Qtde Enviado: "+qtdEnviado+" QtdeRecebido: "+qtdeRecebido);
                                pb.setProgress(0);
                                Toast.makeText(getApplicationContext(),"Ok", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onErro(String erro) {
                    }
                });
                pb.setMax(ping.getQtdPacage());
                ping.setIpAddress("velocidade.online.net.br");
                ping.start();
            }
        });
    }
}