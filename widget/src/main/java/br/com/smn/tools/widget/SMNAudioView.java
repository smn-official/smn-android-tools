package br.com.smn.tools.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import br.com.smn.tools.audio.SMNAudio;
import br.com.smn.tools.exception.SMNException;
import br.com.smn.tools.interfaces.OnAudioPlayingListener;
import br.com.smn.tools.interfaces.OnPlayerEventListener;
import br.com.smn.tools.widget.interfaces.OnDeleteEventListener;

public class SMNAudioView extends LinearLayout {

    private TextView tvTimeElapsed, tvTotalTime, tvAudioTitle, tvDownloadPercent;
    private ImageView ivPlayPause, ivDeleteTrack, ivDownloadTrack;
    private SeekBar skTimeLine;
    private LinearLayout llAudioLayout;
    private FrameLayout llDownloadContainer;
    private SMNAudio smnAudio;
    private String urlStream;
    private OnDeleteEventListener onDeleteEventListener;

    public SMNAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void initComponents(Drawable backgroundContainer, int elapsedTime, int totalTime, int progress, String audioTitle, boolean deleteTrack, boolean downloadTrack) {
        llAudioLayout = findViewById(R.id.llAudioLayout);
        if(backgroundContainer != null)
            llAudioLayout.setBackgroundDrawable(backgroundContainer);

        tvAudioTitle = findViewById(R.id.tvAudioTitle);
        if(audioTitle != null && !audioTitle.trim().equals("")) {
            tvAudioTitle.setVisibility(View.VISIBLE);
            tvAudioTitle.setText(audioTitle);
        }

        tvTimeElapsed = findViewById(R.id.tvTimeElapsed);
        tvTimeElapsed.setTextColor(elapsedTime);

        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTotalTime.setTextColor(totalTime);

        ivPlayPause = findViewById(R.id.ivPlayPause);

        ivDeleteTrack = findViewById(R.id.ivDeleteTrack);
        if(deleteTrack)
            ivDeleteTrack.setVisibility(View.VISIBLE);

        ivDeleteTrack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onDeleteEventListener != null)
                    onDeleteEventListener.onDelete();
            }
        });

        skTimeLine = findViewById(R.id.skTimeLine);
        skTimeLine.setProgress(progress);

        tvDownloadPercent = findViewById(R.id.tvDownloadPercent);
        llDownloadContainer = findViewById(R.id.llDownloadContainer);

        ivDownloadTrack = findViewById(R.id.ivDownloadTrack);
        if(downloadTrack)
            ivDownloadTrack.setVisibility(View.VISIBLE);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_audio, this);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Options, 0, 0);

        int elapsedTimeColor = a.getColor(R.styleable.Options_elapsedTimeColor, getResources().getColor(R.color.default_time_color));
        int totalTimeColor = a.getColor(R.styleable.Options_totalTimeColor, getResources().getColor(R.color.default_time_color));
        int progress = a.getInt(R.styleable.Options_progress, 0);
        Drawable drawable = a.getDrawable(R.styleable.Options_backgroundContainer);
        String audioTitle = a.getString(R.styleable.Options_audioTitle);
        boolean deleteTrack = a.getBoolean(R.styleable.Options_deleteTrack, false);
        boolean downloadTrack = a.getBoolean(R.styleable.Options_downloadTrack, false);

        a.recycle();

        initComponents(drawable, elapsedTimeColor, totalTimeColor, progress, audioTitle, deleteTrack, downloadTrack);
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

    private void loadSMNAudio(final Activity activity) {

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

    public void readyToPlayForStream(final Activity activity, String urlStream){
        this.urlStream = urlStream;
        this.smnAudio = new SMNAudio();

        smnAudio.readyToPlayForStream(urlStream, new OnPlayerEventListener() {
            @Override
            public void onAudioReady(int i, String s) {
                loadSMNAudio(activity);
            }

            @Override
            public void onPlayingComplete() {

            }

            @Override
            public void onAudioReadyError(Exception e) {

            }
        });
    }

    public void readyToPlayForStream(final Activity activity, String urlStream, final OnPlayerEventListener onPlayerEventListener){
        this.urlStream = urlStream;
        this.smnAudio = new SMNAudio();

        smnAudio.readyToPlayForStream(urlStream, new OnPlayerEventListener() {
            @Override
            public void onAudioReady(int i, String s) {
                loadSMNAudio(activity);
                onPlayerEventListener.onAudioReady(i, s);
            }

            @Override
            public void onPlayingComplete() {
                onPlayerEventListener.onPlayingComplete();
            }

            @Override
            public void onAudioReadyError(Exception e) {
                onPlayerEventListener.onAudioReadyError(e);
            }
        });
    }

    public void setAudioTitle(String audioTitle){
        if(audioTitle != null && !audioTitle.trim().equals("")) {
            tvAudioTitle.setVisibility(View.VISIBLE);
            tvAudioTitle.setText(audioTitle);
        }
    }

    public void addDeleteEvent(OnDeleteEventListener onDeleteEventListener){
        this.onDeleteEventListener = onDeleteEventListener;
    }
}
