package br.com.smn.tools.widget.entity;

import android.os.CountDownTimer;

import java.io.File;
import java.util.Timer;

public class SMNRecordConfigEntity {
    private int recordTime;
    private File recordPath;

    public SMNRecordConfigEntity(int recordTime, File recordPath) {
        this.recordTime = recordTime;
        this.recordPath = recordPath;
    }

    public int getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    public File getRecordPath() {
        return recordPath;
    }

    public void setRecordPath(File recordPath) {
        this.recordPath = recordPath;
    }
}
