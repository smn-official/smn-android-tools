package br.com.smn.tools.widget;

import android.app.Activity;
import android.content.Context;
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
    private SMNAudio smnAudio;

    public SMNAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void initComponents() {
        tvTimeElapsed = findViewById(R.id.tvTimeElapsed);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        ivPlayPause = findViewById(R.id.ivPlayPause);
        skTimeLine = findViewById(R.id.skTimeLine);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_audio, this);

        initComponents();
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

    public void loadSMNAudio(final SMNAudio smnAudio, final Activity activity) {
        this.smnAudio = smnAudio;

        tvTotalTime.setText(smnAudio.getFormatedTotalTime());
        skTimeLine.setMax(smnAudio.getMediaPlayer().getDuration());

        ivPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (smnAudio.isPlaying()) {
                    ivPlayPause.setImageResource(R.drawable.ic_play);
                    smnAudio.pause();
                } else {
                    smnAudio.play(new OnAudioPlayingListener() {
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
