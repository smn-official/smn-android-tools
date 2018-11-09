package br.com.smn.tools;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

import br.com.smn.tools.audio.SMNAudio;
import br.com.smn.tools.exception.SMNException;
import br.com.smn.tools.interfaces.OnPlayerEventListener;
import br.com.smn.tools.widget.SMNAudioView;
import br.com.smn.tools.widget.interfaces.OnDeleteEventListener;

public class MainActivity extends AppCompatActivity {

    private SMNAudioView smnAudioView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smnAudioView = findViewById(R.id.avAudioView);
        smnAudioView.readyToPlayForStreamAsync(this, "https://s3-us-west-2.amazonaws.com/smn-mobile/cdp-desenv/Through+The+Fire+And+Flames.mp3");

        smnAudioView.addDeleteEvent(new OnDeleteEventListener() {
            @Override
            public void onDelete() {
                Toast.makeText(getApplicationContext(), "DELETOU!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
