package br.com.smn.tools.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import br.com.smn.tools.interfaces.OnVideoEventListener;
import br.com.smn.tools.widget.interfaces.OnBackEventListener;
import br.com.smn.tools.widget.interfaces.OnVideoPlayingListener;

public class SMNVideoView extends LinearLayout {
    // Container mestre
    private LinearLayout llGeneralContainer;
    // Frame Container
    private FrameLayout frameContainer;
    // Controll Container
    private LinearLayout llControll;
    // VideoView
    private VideoView vvVideo;
    // SeekBar
    private SeekBar skTimeLine;
    // Play/Pause
    private ImageView ivPlayPauseVideo;
    // Icon Back
    private ImageView ivBack;
    // Time Elapsed
    private TextView tvChapterTitle;
    // Time Elapsed
    private TextView tvTimeElapsed;
    // Buffering
    private TextView tvBuffering;
    // Load antes do buffer estar pronto
    private LinearLayout llTransparenceLoad;
    // Linear layout with title
    private LinearLayout llEnableBack;
    // URL stream
    private String urlVideo;
    // Context Application
    private Context context;
    // Timer para controlar o tempo de vídeo
    private Timer timer;
    // BackPressed Event Listener
    private OnBackEventListener onBackEventListener;

    private boolean enableBack;
    private boolean startOnLoad;

    public SMNVideoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        this.context = context;
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_video, this);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Video, 0, 0);

        //int elapsedTimeColor = a.getColor(R.styleable.Options_elapsedTimeColor, getResources().getColor(R.color.default_time_color));
        //int totalTimeColor = a.getColor(R.styleable.Options_totalTimeColor, getResources().getColor(R.color.default_time_color));
        //int progress = a.getInt(R.styleable.Options_progress, 0);
        //Drawable drawable = a.getDrawable(R.styleable.Options_backgroundContainer);
        //String audioTitle = a.getString(R.styleable.Options_audioTitle);
        //boolean deleteTrack = a.getBoolean(R.styleable.Options_deleteTrack, false);
        boolean enableBack = a.getBoolean(R.styleable.Video_enableBack, false);
        boolean startOnLoad = a.getBoolean(R.styleable.Video_startOnLoad, false);

        a.recycle();

        initComponents(enableBack, startOnLoad);
    }

    private void initComponents(boolean enableBack, boolean startOnLoad){
        llGeneralContainer = findViewById(R.id.llGeneralContainer);
        llEnableBack = findViewById(R.id.llEnableBack);
        frameContainer = findViewById(R.id.frameContainer);
        llControll = findViewById(R.id.llControll);
        vvVideo = findViewById(R.id.vvVideo);
        skTimeLine = findViewById(R.id.skTimeLine);
        ivPlayPauseVideo = findViewById(R.id.ivPlayPauseVideo);
        ivBack = findViewById(R.id.ivBack);
        tvTimeElapsed = findViewById(R.id.tvTimeElapsed);
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        tvBuffering = findViewById(R.id.tvBuffering);
        llTransparenceLoad = findViewById(R.id.llTransparenceLoad);

        if(enableBack){
            ivBack.setVisibility(VISIBLE);
            ivBack.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onBackEventListener != null)
                        onBackEventListener.backPressed();
                }
            });
        }

        frameContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation animControll = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                final Animation animBack = AnimationUtils.loadAnimation(context, R.anim.fade_out);

                if(llControll.isShown()){
                    llControll.setAnimation(animControll);
                    llEnableBack.setAnimation(animBack);

                    animControll.start();
                    animBack.start();

                    llControll.setVisibility(GONE);
                    llEnableBack.setVisibility(GONE);
                }
                else{
                    llControll.setVisibility(VISIBLE);
                    llEnableBack.setVisibility(VISIBLE);
                }
            }
        });

        this.enableBack = enableBack;
        this.startOnLoad = startOnLoad;
    }

    public void readyToPlayForStream(final Activity activity, String URL, final OnVideoEventListener onVideoEventListener){
        vvVideo.setVideoPath(URL);
        vvVideo.setKeepScreenOn(true);

        llTransparenceLoad.setVisibility(VISIBLE);
        vvVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                llTransparenceLoad.setVisibility(GONE);
                llControll.setVisibility(VISIBLE);
                llEnableBack.setVisibility(VISIBLE);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.pause();
                        if(timer != null)
                            timer.cancel();

                        mp.seekTo(0);
                        onVideoEventListener.onPlayingComplete();
                    }
                });

                skTimeLine.setMax(vvVideo.getDuration());

                skTimeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser) {
                            vvVideo.seekTo(progress);
                            tvTimeElapsed.setText(getFormatedCurrentTime());
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                ivPlayPauseVideo.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isPlaying()) {
                            ivPlayPauseVideo.setImageResource(R.drawable.selector_play_video);
                            pause();
                        } else {
                            play(new OnVideoPlayingListener() {
                                @Override
                                public void onPlaying(final int currentPosition, final String formatedTime) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            skTimeLine.setProgress(currentPosition);
                                            tvTimeElapsed.setText(formatedTime);
                                        }
                                    });
                                }
                            });

                            ivPlayPauseVideo.setImageResource(R.drawable.selector_pause_video);
                        }
                    }
                });

                mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        if(((percent * skTimeLine.getMax()) / 100) < skTimeLine.getMax())
                            skTimeLine.setSecondaryProgress((percent * skTimeLine.getMax()) / 100);
                    }
                });

                if(startOnLoad)
                    ivPlayPauseVideo.callOnClick();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            vvVideo.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int what, int i1) {
                    switch(what){
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:{
                            tvBuffering.setVisibility(VISIBLE);
                            break;
                        }

                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:{
                            tvBuffering.setVisibility(GONE);
                            break;
                        }

                        case MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING:{
                            // TODO fazer algo aqui
                            break;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private void play(final OnVideoPlayingListener onVideoPlayingListener){
        timer = new Timer();
        vvVideo.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int currentPosition = vvVideo.getCurrentPosition();

                String formatedTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(currentPosition), TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)));

                onVideoPlayingListener.onPlaying(currentPosition, formatedTime);
            }
        }, 0, 300);
    }

    private void pause(){
        vvVideo.pause();
        timer.cancel();
    }

    public boolean isPlaying(){
        return vvVideo.isPlaying();
    }

    public String getFormatedCurrentTime(){
        int duration = vvVideo.getCurrentPosition();

        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    public void addBackPressed(OnBackEventListener onBackEventListener){
        this.onBackEventListener = onBackEventListener;
    }

    public void setVideoTitle(String title){
        tvChapterTitle.setText(title);
    }

    public void stopVideoActivities(){
        vvVideo.stopPlayback();
        timer.cancel();
    }
}
