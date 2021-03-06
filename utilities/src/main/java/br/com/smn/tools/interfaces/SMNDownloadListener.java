package br.com.smn.tools.interfaces;

import java.io.IOException;

public interface SMNDownloadListener {
    void onStart();
    void onProgress(int percent);
    void onFail(IOException ex);
    void onComplete();
}
