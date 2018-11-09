package br.com.smn.tools.widget.asynctask;

import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;

import br.com.smn.tools.interfaces.SMNDownloadListener;
import br.com.smn.tools.util.SMNUtilities;

public class DownloadAsyncTask extends AsyncTask<Void, Integer, Void> {
    private String url;
    private File outputLocation;
    private SMNDownloadListener smnDownloadListener;

    public DownloadAsyncTask(String url, File outputLocation, SMNDownloadListener smnDownloadListener) {
        this.url = url;
        this.outputLocation = outputLocation;
        this.smnDownloadListener = smnDownloadListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        smnDownloadListener.onStart();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SMNUtilities.downloadFile(url, outputLocation, new SMNDownloadListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int percent) {
                publishProgress(percent);
            }

            @Override
            public void onFail(IOException ex) {
                smnDownloadListener.onFail(ex);
            }

            @Override
            public void onComplete() {
                smnDownloadListener.onComplete();
            }
        });

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        smnDownloadListener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
