package br.com.smn.tools.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
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
    private Timer timer;
    private long elapsedIndeterminateTime = 0;

    public SMNRecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_record, this);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Record, 0, 0);

        int recDimensions = a.getDimensionPixelSize(R.styleable.Record_recDimensions, 0);

        a.recycle();

        initComponents(recDimensions);
    }

    private void initComponents(int recDimensions){
        ivRecord = findViewById(R.id.ivRecord);
        tvTimer = findViewById(R.id.tvTimer);

        if(recDimensions != 0){
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, recDimensions, getResources().getDisplayMetrics());

            ivRecord.getLayoutParams().height = dimensionInDp;
            ivRecord.getLayoutParams().width = dimensionInDp;
            ivRecord.requestLayout();
        }
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

    public void addRecordConfigIndeterminate(final SMNRecordConfigEntity smnRecordConfigEntity, final OnRecordListener onRecordListener){
        smnRecord = new SMNRecord();
        this.smnRecordConfigEntity = smnRecordConfigEntity;
        timer = new Timer();

        ivRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(smnRecord.isRecording()){
                    stopRecordIndeterminate();
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

    private void stopRecordIndeterminate(){
        ivRecord.setImageResource(R.drawable.ic_mic);
        ivRecord.setBackgroundResource(R.drawable.selector_blue);
        tvTimer.setText("00:00");
        tvTimer.setVisibility(View.GONE);

        smnRecord.stopRecording();

        timer.cancel();

        elapsedIndeterminateTime = 0;
    }
}
