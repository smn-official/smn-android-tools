package br.com.smn.tools.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import br.com.smn.tools.audio.SMNAudio;
import br.com.smn.tools.exception.SMNException;
import br.com.smn.tools.interfaces.OnAudioPlayingListener;
import br.com.smn.tools.interfaces.OnPlayerEventListener;
import br.com.smn.tools.interfaces.SMNDownloadListener;
import br.com.smn.tools.util.SMNUtilities;
import br.com.smn.tools.widget.entity.AudioPropertiesEntity;
import br.com.smn.tools.widget.entity.DownloadAudioEntity;
import br.com.smn.tools.widget.interfaces.OnDeleteEventListener;

public class SMNAudioView extends LinearLayout {

    private TextView tvTimeElapsed, tvTotalTime, tvAudioTitle, tvDownloadPercent;
    private ImageView ivPlayPause, ivDeleteTrack, ivDownloadTrack, ivReload;
    private SeekBar skTimeLine;
    private LinearLayout llAudioLayout;
    private FrameLayout llDownloadContainer;
    private SMNAudio smnAudio;
    private String urlStream;
    private OnDeleteEventListener onDeleteEventListener;
    private ProgressBar pbLoadAudioPlay;
    private boolean backToBegin;
    private Context context;
    private File downloadPath;

    public SMNAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        this.context = context;
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

        ivDownloadTrack = findViewById(R.id.ivDownloadTrack);
        tvDownloadPercent = findViewById(R.id.tvDownloadPercent);
        llDownloadContainer = findViewById(R.id.llDownloadContainer);
        if(downloadTrack)
            ivDownloadTrack.setVisibility(View.VISIBLE);

        ivDownloadTrack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SMNUtilities.isStoragePermissionGranted(getContext())){
                    if(SMNUtilities.checkIfStreamFileExistOnDisk(urlStream, downloadPath))
                        Toast.makeText(getContext(), "Arquivo já existe em: " + downloadPath.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    else{
                        try {
                            downloadAudio();
                        } catch (SMNException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else
                    Toast.makeText(getContext(), "Precisa de permissão para manipular arquivos", Toast.LENGTH_LONG).show();
            }
        });

        pbLoadAudioPlay = findViewById(R.id.pbLoadAudioPlay);
        ivReload = findViewById(R.id.ivReload);
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
        this.backToBegin = a.getBoolean(R.styleable.Options_backToBegin, false);

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

    /**
     * Método síncrono que carrega uma view de audio por stream de dados, a view geralmente aparece após o audio ser carregado pelo buffer e estiver pronto para execução
     * @param activity referência de contexto para onde o audio será apresentado
     * @param urlStream URL do audio para ser reproduzido
     * @param downloadPath Informando um diretório no parametro será onde o audio será salvo caso iniciado um download, se passado nulo, mesmo habilitando o icone de download ele desaparecerá na view
     */
    public void readyToPlayForStream(@NonNull final Activity activity, @NonNull String urlStream, File downloadPath){
        this.urlStream = urlStream;
        this.downloadPath = downloadPath;

        if(downloadPath == null)
            ivDownloadTrack.setVisibility(View.GONE);
        else {
            if(SMNUtilities.checkIfStreamFileExistOnDisk(this.urlStream, this.downloadPath))
                ivDownloadTrack.setImageResource(R.drawable.ic_check);
        }

        this.smnAudio = new SMNAudio();

        smnAudio.readyToPlayForStream(urlStream, new OnPlayerEventListener() {
            @Override
            public void onAudioReady(int i, String s) {
                loadSMNAudio(activity);
            }

            @Override
            public void onPlayingComplete() {
                if(backToBegin){
                    smnAudio.getMediaPlayer().seekTo(0);
                    skTimeLine.setProgress(0);
                    ivPlayPause.setImageResource(R.drawable.ic_play);
                    tvTimeElapsed.setText("00:00");
                }
            }

            @Override
            public void onAudioReadyError(Exception e) {

            }
        });
    }

    /**
     * Método assíncrono que carrega uma view de audio por stream de dados, a view aparece com um loading e aguarda até que o áudio esteja pronto para executar
     * @param activity referência de contexto para onde o audio será apresentado
     * @param urlStream URL do audio para ser reproduzido
     * @param downloadPath Informando um diretório no parametro será onde o audio será salvo caso iniciado um download, se passado nulo, mesmo habilitando o icone de download ele desaparecerá na view
     */
    public void readyToPlayForStreamAsync(@NonNull final Activity activity, @NonNull String urlStream, File downloadPath){
        this.urlStream = urlStream;
        this.downloadPath = downloadPath;

        if(downloadPath == null)
            ivDownloadTrack.setVisibility(View.GONE);
        else {
            if(SMNUtilities.checkIfStreamFileExistOnDisk(this.urlStream, this.downloadPath))
                ivDownloadTrack.setImageResource(R.drawable.ic_check);
        }

        this.smnAudio = new SMNAudio();

        new AsyncLoadAudio(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        ivReload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncLoadAudio(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    /**
     * Método síncrono que carrega uma view de audio por stream de dados, a view geralmente aparece após o audio ser carregado pelo buffer e estiver pronto para execução.
     * @param activity referência de contexto para onde o audio será apresentado.
     * @param urlStream URL do audio para ser reproduzido.
     * @param downloadPath Informando um diretório no parametro será onde o audio será salvo caso iniciado um download, se passado nulo, mesmo habilitando o icone de download ele desaparecerá na view
     * @param onPlayerEventListener Listener que joga para o desenvolvedor os eventos que acontecem no player caso seja necessário um tratamento adicional.
     */
    public void readyToPlayForStream(@NonNull final Activity activity, @NonNull String urlStream, File downloadPath, final OnPlayerEventListener onPlayerEventListener){
        this.urlStream = urlStream;
        this.downloadPath = downloadPath;

        if(downloadPath == null)
            ivDownloadTrack.setVisibility(View.GONE);
        else {
            if(SMNUtilities.checkIfStreamFileExistOnDisk(this.urlStream, this.downloadPath))
                ivDownloadTrack.setImageResource(R.drawable.ic_check);
        }

        this.smnAudio = new SMNAudio();

        smnAudio.readyToPlayForStream(urlStream, new OnPlayerEventListener() {
            @Override
            public void onAudioReady(int i, String s) {
                loadSMNAudio(activity);
                onPlayerEventListener.onAudioReady(i, s);
            }

            @Override
            public void onPlayingComplete() {
                if(backToBegin){
                    smnAudio.getMediaPlayer().seekTo(0);
                    skTimeLine.setProgress(0);
                    ivPlayPause.setImageResource(R.drawable.ic_play);
                    tvTimeElapsed.setText("00:00");
                }
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

    private void downloadAudio()throws SMNException{
        SMNUtilities.createDirectory(this.downloadPath, context);

        String urlStream = this.urlStream;
        if(urlStream == null || urlStream.isEmpty())
            throw new SMNException("URL de áudio não encontrada! Verifique se o método 'readyToPlayForStream' foi invocado previamente.");

        new AsyncDownloadAudio(urlStream, downloadPath).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class AsyncLoadAudio extends AsyncTask<Void, Void, Void>{
        private Activity activity;
        private AudioPropertiesEntity audioPropertiesEntity;

        public AsyncLoadAudio(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            audioPropertiesEntity = new AudioPropertiesEntity();
            audioPropertiesEntity.setStatusProgress(0);

            pbLoadAudioPlay.setVisibility(View.VISIBLE);
            ivPlayPause.setVisibility(View.GONE);
            ivReload.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            smnAudio.readyToPlayForStream(urlStream, new OnPlayerEventListener() {
                @Override
                public void onAudioReady(int i, String s) {
                    audioPropertiesEntity.setStatusProgress(1);
                    audioPropertiesEntity.setTotalTime(i);
                    audioPropertiesEntity.setTotalTimeFormatted(s);

                    publishProgress(null);
                }

                @Override
                public void onPlayingComplete() {
                    audioPropertiesEntity.setStatusProgress(2);

                    publishProgress(null);
                }

                @Override
                public void onAudioReadyError(Exception e) {
                    audioPropertiesEntity.setStatusProgress(3);
                    audioPropertiesEntity.setE(e);

                    publishProgress(null);
                }
            });

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            switch(audioPropertiesEntity.getStatusProgress()){
                case 0:{
                    // STATUS INICIO ANTES DO LOAD
                    break;
                }

                case 1:{
                    pbLoadAudioPlay.setVisibility(View.GONE);
                    ivPlayPause.setVisibility(View.VISIBLE);
                    ivReload.setVisibility(View.GONE);

                    loadSMNAudio(activity);
                    break;
                }

                case 2:{
                    if(backToBegin){
                        smnAudio.getMediaPlayer().seekTo(0);
                        skTimeLine.setProgress(0);
                        ivPlayPause.setImageResource(R.drawable.ic_play);
                        tvTimeElapsed.setText("00:00");
                    }
                    break;
                }

                case 3:{
                    pbLoadAudioPlay.setVisibility(View.GONE);
                    ivPlayPause.setVisibility(View.GONE);
                    ivReload.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    }

    private class AsyncDownloadAudio extends AsyncTask<Void, Void, Void>{
        private String urlStream;
        private File downloadPath;

        private DownloadAudioEntity downloadAudioEntity;

        private AsyncDownloadAudio(String urlStream, File downloadPath) {
            this.urlStream = urlStream;
            this.downloadPath = downloadPath;

            downloadAudioEntity = new DownloadAudioEntity(0, 0, null);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SMNUtilities.downloadFile(urlStream, downloadPath, new SMNDownloadListener() {
                @Override
                public void onStart() {
                    publishProgress(null);
                }

                @Override
                public void onProgress(int percent) {
                    downloadAudioEntity.setStatus(1);
                    downloadAudioEntity.setProgress(percent);

                    publishProgress(null);
                }

                @Override
                public void onFail(IOException ex) {
                    downloadAudioEntity.setStatus(3);
                    downloadAudioEntity.setIoException(ex);

                    publishProgress(null);
                }

                @Override
                public void onComplete() {
                    downloadAudioEntity.setStatus(2);
                    publishProgress(null);
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            switch(downloadAudioEntity.getStatus()){
                case 0:{
                    ivDownloadTrack.setVisibility(View.GONE);
                    llDownloadContainer.setVisibility(View.VISIBLE);
                    break;
                }

                case 1:{
                    tvDownloadPercent.setText(String.valueOf(downloadAudioEntity.getProgress()));
                    break;
                }

                case 2:{
                    llDownloadContainer.setVisibility(View.GONE);
                    ivDownloadTrack.setVisibility(View.VISIBLE);

                    ivDownloadTrack.setImageResource(R.drawable.ic_check);

                    Toast.makeText(getContext(), "Download completed.", Toast.LENGTH_LONG).show();
                    break;
                }

                case 3:{
                    llDownloadContainer.setVisibility(View.GONE);
                    ivDownloadTrack.setVisibility(View.VISIBLE);

                    Toast.makeText(getContext(), "Download failed... TRY AGAIN.", Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
    }
}
