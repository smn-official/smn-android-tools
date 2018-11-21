package br.com.smn.tools.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import br.com.smn.tools.audio.SMNRecord;
import br.com.smn.tools.widget.entity.SMNRecordDialogConfigEntity;
import br.com.smn.tools.widget.interfaces.OnRecordListener;

public class SMNRecordDialog {
    private static long elapsedIndeterminateTime;

    public static Dialog showRecordDialogIndeterminate(Activity act, SMNRecordDialogConfigEntity config, final File audioPath, final OnRecordListener onRecordListener){
        elapsedIndeterminateTime = 0;
        final SMNRecord smnRecord;
        final Timer timer;

        final Dialog dialog = new Dialog(act);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_record);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        final TextView tvTimer = dialog.findViewById(R.id.tvTimer);
        final ImageView ivRec = dialog.findViewById(R.id.ivRec);

        tvTitle.setText(config.getTitle() == null || config.getTitle().isEmpty() ? "Grave uma Mensagem" : config.getTitle());

        smnRecord = new SMNRecord();

        timer = new Timer();

        ivRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(smnRecord.isRecording()){
                    ivRec.setImageResource(R.drawable.ic_mic);
                    ivRec.setBackgroundResource(R.drawable.selector_blue);
                    tvTimer.setText("00:00");
                    tvTimer.setVisibility(View.VISIBLE);
                    smnRecord.stopRecording();
                    timer.cancel();
                    dialog.dismiss();
                }
                else{
                    try {
                        smnRecord.beginRecording(audioPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        onRecordListener.onError(e);
                        return;
                    }

                    ivRec.setImageResource(R.drawable.ic_stop);
                    ivRec.setBackgroundResource(R.drawable.selector_red);
                    tvTimer.setVisibility(View.VISIBLE);

                    onRecordListener.startRecording();

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            elapsedIndeterminateTime += 1000;

                            String totalTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(elapsedIndeterminateTime), TimeUnit.MILLISECONDS.toSeconds(elapsedIndeterminateTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedIndeterminateTime)));

                            tvTimer.setText(totalTime);
                            onRecordListener.onTick(elapsedIndeterminateTime, totalTime);
                        }
                    }, 1000, 1000);
                }
            }
        });

        return dialog;
    }
}
