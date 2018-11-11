package br.com.smn.tools.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import br.com.smn.tools.audio.SMNRecord;
import br.com.smn.tools.widget.interfaces.OnRecordListener;
import br.com.smn.tools.widget.entity.SMNRecordConfigEntity;

public class SMNRecordView extends LinearLayout {

    private ImageView ivRecord;
    private TextView tvTimer;
    private SMNRecordConfigEntity smnRecordConfigEntity;
    private SMNRecord smnRecord;
    private CountDownTimer countDownTimer;

    public SMNRecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_record, this);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Record, 0, 0);

        a.recycle();

        ivRecord = findViewById(R.id.ivRecord);
        tvTimer = findViewById(R.id.tvTimer);
    }

    public SMNRecordConfigEntity getSmnRecordConfigEntity() {
        return smnRecordConfigEntity;
    }

    public void addRecordConfig(final SMNRecordConfigEntity smnRecordConfigEntity, final OnRecordListener onRecordListener){
        smnRecord = new SMNRecord();

        this.smnRecordConfigEntity = smnRecordConfigEntity;

        ivRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(smnRecord.isRecording()){
                    stopRecord();
                }
                else{
                    try {
                        smnRecord.beginRecording(smnRecordConfigEntity.getRecordPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        onRecordListener.onError(e);
                        return;
                    }

                    ivRecord.setImageResource(R.drawable.ic_stop);
                    ivRecord.setBackgroundResource(R.drawable.selector_red);
                    tvTimer.setVisibility(View.VISIBLE);

                    onRecordListener.startRecording();

                    countDownTimer = new CountDownTimer(SMNRecordView.this.smnRecordConfigEntity.getRecordTime(), 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                            String totalTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                            tvTimer.setText(totalTime);
                            onRecordListener.onTick(millisUntilFinished, totalTime);
                        }

                        @Override
                        public void onFinish() {
                            if(smnRecord.isRecording())
                                stopRecord();
                        }
                    }.start();
                }
            }
        });
    }

    private void startRecord(){

    }

    private void stopRecord(){
        ivRecord.setImageResource(R.drawable.ic_mic);
        ivRecord.setBackgroundResource(R.drawable.selector_blue);
        tvTimer.setText("00:00");
        tvTimer.setVisibility(View.GONE);

        smnRecord.stopRecording();

        countDownTimer.cancel();
    }
}
