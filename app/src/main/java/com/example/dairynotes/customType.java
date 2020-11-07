package com.example.dairynotes;

import java.util.ArrayList;

public class customType {
    private Object myObject;
    private String type, title;
    int duration = 0, progress;


    public customType(Object myObject, String type, String title) {
        this.myObject = myObject;
        this.type = type;
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getMyObject() {
        return myObject;
    }

    public String getType() {
        return type;
    }
}
