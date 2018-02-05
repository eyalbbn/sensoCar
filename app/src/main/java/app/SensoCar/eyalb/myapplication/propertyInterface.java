package app.SensoCar.eyalb.myapplication;

import java.io.File;

public interface propertyInterface {
    void init(File path);
    void startExamining();
    void stopExamining();
    void upload();
}
