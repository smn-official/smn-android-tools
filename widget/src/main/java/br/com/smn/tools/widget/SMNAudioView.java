package br.com.smn.tools.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import br.com.smn.tools.audio.SMNAudio;
import br.com.smn.tools.interfaces.OnAudioPlayingListener;

public class SMNAudioView extends LinearLayout {

    private TextView tvTimeElapsed, tvTotalTime;
    private ImageView ivPlayPause;
    private SeekBar skTimeLine;
    private LinearLayout llAudioLayout;
    private SMNAudio smnAudio;

    public SMNAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void initComponents(Drawable backgroundContainer, int elapsedTime, int totalTime, int progress) {
        llAudioLayout = findViewById(R.id.llAudioLayout);
        if(backgroundContainer != null)
            llAudioLayout.setBackgroundDrawable(backgroundContainer);

        tvTimeElapsed = findViewById(R.id.tvTimeElapsed);
        tvTimeElapsed.setTextColor(elapsedTime);

        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTotalTime.setTextColor(totalTime);

        ivPlayPause = findViewById(R.id.ivPlayPause);

        skTimeLine = findViewById(R.id.skTimeLine);
        skTimeLine.setProgress(progress);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_audio, this);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Options, 0, 0);

        int elapsedTimeColor = a.getColor(R.styleable.Options_elapsedTimeColor, getResources().getColor(R.color.default_time_color));
        int totalTimeColor = a.getColor(R.styleable.Options_totalTimeColor, getResources().getColor(R.color.default_time_color));
        int progress = a.getInt(R.styleable.Options_progress, 0);
        Drawable drawable = a.getDrawable(R.styleable.Options_backgroundContainer);

        a.recycle();

        initComponents(drawable, elapsedTimeColor, totalTimeColor, progress);
    }

    public TextView getTvTimeElapsed() {
        return tvTimeElapsed;
    }

    public void setTvTimeElapsed(TextView tvTimeElapsed) {
        this.tvTimeElapsed = tvTimeElapsed;
    }

    public TextView getTvTotalTime() {
        return tvTotalTime;
    }

    public void setTvTotalTime(TextView tvTotalTime) {
        this.tvTotalTime = tvTotalTime;
    }

    public ImageView getIvPlayPause() {
        return ivPlayPause;
    }

    public void setIvPlayPause(ImageView ivPlayPause) {
        this.ivPlayPause = ivPlayPause;
    }

    public SeekBar getSkTimeLine() {
        return skTimeLine;
    }

    public void setSkTimeLine(SeekBar skTimeLine) {
        this.skTimeLine = skTimeLine;
    }

    public SMNAudio getSmnAudio() {
        return smnAudio;
    }

    public void loadSMNAudio(SMNAudio smnAudio, final Activity activity) {
        this.smnAudio = smnAudio;

        tvTotalTime.setText(smnAudio.getFormatedTotalTime());
        skTimeLine.setMax(smnAudio.getMediaPlayer().getDuration());

        skTimeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    SMNAudioView.this.smnAudio.seekTo(progress);
                    tvTimeElapsed.setText(SMNAudioView.this.smnAudio.getFormatedCurrentTime());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ivPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SMNAudioView.this.smnAudio.isPlaying()) {
                    ivPlayPause.setImageResource(R.drawable.ic_play);
                    SMNAudioView.this.smnAudio.pause();
                } else {
                    SMNAudioView.this.smnAudio.play(new OnAudioPlayingListener() {
                        @Override
                        public void onPlaying(int currentPosition, final String formatedTime) {
                            skTimeLine.setProgress(currentPosition);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvTimeElapsed.setText(formatedTime);
                                }
                            });
                        }
                    });

                    ivPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }
}
