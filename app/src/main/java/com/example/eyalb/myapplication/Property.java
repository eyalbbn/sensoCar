package com.example.eyalb.myapplication;

import android.media.MediaRecorder;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Property {
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

    public void startExamining(File path) {
        setStart();
        /* implement your code here */

        try {
            file = new File(path, name + "Output.mp3");
            uri = Uri.fromFile(file);
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaRecorderReady();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
            streamWriter.write("start: " + df.format(start));
            streamWriter.write("\n");
            streamWriter.write("end: " + df.format(end));

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

    public void upload(String curr_date) {
        fileManager.upload(uri, curr_date);
        fileManager.upload(Uri.fromFile(doc), curr_date);
    }
}