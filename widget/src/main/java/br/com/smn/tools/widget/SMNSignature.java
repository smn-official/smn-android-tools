package br.com.smn.tools.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.kyanogen.signatureview.SignatureView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import br.com.smn.tools.exception.SMNSignatureException;
import br.com.smn.tools.util.SMNUtilities;

public class SMNSignature {
    private SignatureView signatureView;
    private File signaturePath;
    private Context context;

    public SMNSignature(@NonNull SignatureView signatureView, @NonNull File signaturePath, @NonNull Context context) throws SMNSignatureException {
        this.signatureView = signatureView;
        this.signaturePath = signaturePath;
        this.context = context;

        SMNUtilities.createDirectory(signaturePath.getAbsolutePath(), context);
    }

    public SMNSignature(@NonNull SignatureView signatureView, @NonNull String signaturePath, @NonNull Context context) throws SMNSignatureException {
        this.signatureView = signatureView;
        this.signaturePath = new File(signaturePath);
        this.context = context;

        SMNUtilities.createDirectory(signaturePath, context);
    }

    public void clearSignature(){
        signatureView.clearCanvas();
    }

    public String saveSignatureOnDisk(Context context){
        Bitmap bitmap = signatureView.getSignatureBitmap();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes);

        Bitmap b = Bitmap.createScaledBitmap(bitmap, 1000, 100, false);

        if (!signaturePath.exists()) {
            signaturePath.mkdirs();
        }

        try {
            File f = new File(signaturePath, Calendar.getInstance()
                    .getTimeInMillis() + ".png");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(context,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Toast.makeText(context, "Assinatura salva com sucesso", Toast.LENGTH_LONG).show();

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return null;
    }

    public SignatureView getSignatureView() {
        return signatureView;
    }

    public void setSignatureView(SignatureView signatureView) {
        this.signatureView = signatureView;
    }

    public File getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(File signaturePath) {
        this.signaturePath = signaturePath;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
