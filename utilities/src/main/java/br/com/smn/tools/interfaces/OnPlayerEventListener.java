package br.com.smn.tools.interfaces;

public interface OnPlayerEventListener {
    void onAudioReady(int duration, String totalTime);
    void onPlayingComplete();
    void onAudioReadyError(Exception e);
}
