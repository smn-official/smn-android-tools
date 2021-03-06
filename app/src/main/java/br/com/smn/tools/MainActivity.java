package br.com.smn.tools;

import android.app.Dialog;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import br.com.smn.tools.audio.SMNAudio;
import br.com.smn.tools.exception.SMNException;
import br.com.smn.tools.interfaces.OnPlayerEventListener;
import br.com.smn.tools.interfaces.OnVideoEventListener;
import br.com.smn.tools.interfaces.SMNDownloadListener;
import br.com.smn.tools.widget.SMNAudioView;
import br.com.smn.tools.widget.SMNRecordDialog;
import br.com.smn.tools.widget.SMNRecordView;
import br.com.smn.tools.widget.SMNVideoView;
import br.com.smn.tools.widget.entity.SMNRecordConfigEntity;
import br.com.smn.tools.widget.entity.SMNRecordDialogConfigEntity;
import br.com.smn.tools.widget.interfaces.OnBackEventListener;
import br.com.smn.tools.widget.interfaces.OnDeleteEventListener;
import br.com.smn.tools.widget.interfaces.OnRecordListener;

public class MainActivity extends AppCompatActivity {

    private SMNAudioView smnAudioView;
    private SMNRecordView smnRecordView;
    private SMNVideoView smnVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smnAudioView = findViewById(R.id.avAudioView);
        smnAudioView.readyToPlayForStreamAsync(
                this,
                "https://s3-us-west-2.amazonaws.com/smn-mobile/cdp-desenv/Through+The+Fire+And+Flames.mp3",
                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SMNTools/Audios")
        );

        smnAudioView.addDeleteEvent(new OnDeleteEventListener() {
            @Override
            public void onDelete() {
                Toast.makeText(getApplicationContext(), "DELETOU!!!", Toast.LENGTH_SHORT).show();

                SMNRecordDialogConfigEntity config = new SMNRecordDialogConfigEntity();
                config.setTitle("Grave uma Mensagem");

                Dialog d = SMNRecordDialog.showRecordDialogIndeterminate(
                        MainActivity.this,
                        config,
                        new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SMNTools/Audios/xkkk.mp3"),
                        new OnRecordListener() {
                            @Override
                            public void startRecording() {

                            }

                            @Override
                            public void onTick(long time, String formattedTime) {

                            }

                            @Override
                            public void onFinish() {

                            }

                            @Override
                            public void onError(IOException e) {

                            }
                        });
                d.show();
            }
        });

        smnRecordView = findViewById(R.id.rvRec);
        smnRecordView.addRecordConfigIndeterminate(
                new SMNRecordConfigEntity(20000, new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SMNTools/Audios/xkkk.mp3")), new OnRecordListener() {
            @Override
            public void startRecording() {
                System.out.println("START RECORDING");
            }

            @Override
            public void onTick(long time, String formattedTime) {
                System.out.println("TIME: " + time + " Formatted: " + formattedTime);
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onError(IOException e) {
                System.out.println();
            }
        });

        smnVideoView = findViewById(R.id.smnVideoView);
        smnVideoView.setVideoTitle("50 - O SÍMBOLO DA PAZ");
        smnVideoView.readyToPlayForStream(this, "https://s3-us-west-2.amazonaws.com/smn-mobile/cdp-desenv/PUF_Boku_no_Hero_51_SD.mp4", new OnVideoEventListener() {
            @Override
            public void onPlayingComplete() {
                System.out.println("COMPLETOU");
            }
        });

        smnVideoView.addBackPressed(new OnBackEventListener() {
            @Override
            public void backPressed() {
                System.out.println("VOLTOU");
            }
        });

    }
}
