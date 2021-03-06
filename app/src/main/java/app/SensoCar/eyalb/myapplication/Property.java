package app.SensoCar.eyalb.myapplication;

import android.media.MediaRecorder;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Property implements propertyInterface {
    private String name;
    private Uri uri;
    private File file;
    private Date start;
    private Date end;
    private File doc;
    private fileManager fileManager;

    public Property(String str, fileManager fMan) {
        name = str;
        fileManager = fMan;
    }

    MediaRecorder mediaRecorder;

    public void setStart() {
        start = new Date();
    }

    public void setEnd() {
        end = new Date();
    }

    @Override
    public void init(File path) {
        try {
            file = new File(path, name + "Output.mp3");
            uri = Uri.fromFile(file);
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startExamining() {
        setStart();

        MediaRecorderReady();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopExamining() {
        setEnd();
        createDocumentation();
        mediaRecorder.stop();
    }

    private void createDocumentation() {
        try {
            doc = new File(file.getParentFile(), "documentation.txt");
            doc.createNewFile();
            FileOutputStream docStream = new FileOutputStream(doc);
            OutputStreamWriter streamWriter = new OutputStreamWriter(docStream);


            DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            streamWriter.write("start: " + df.format(start)+"\nend: " + df.format(end));

            streamWriter.close();

            docStream.flush();

            docStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(uri.getPath());
    }

    public void upload() {
        fileManager.upload(file);
        fileManager.upload(doc);
    }
}