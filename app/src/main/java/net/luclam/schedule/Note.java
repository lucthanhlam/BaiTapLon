package net.luclam.schedule;


import androidx.annotation.NonNull;

import java.io.Serializable;

public class Note implements Serializable {
    private String uid;

    private String title, description, time, date;

    public Note() {
    }

    public Note(String uid, String title, String description, String time, String date) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.time = time;
        this.date = date;
    }

    public Note(String title, String description, String time, String date) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @NonNull
    @Override
    public String toString() {
        return uid + "/" + title;
    }
}
