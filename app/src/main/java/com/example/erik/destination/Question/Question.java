package com.example.erik.destination.Question;

/**
 * Created by Comp on 08/04/2017.
 */

public class Question {
    protected String id;
    protected boolean isDownloaded=false;
    private boolean isNull=true;

    public String getId() {
        return id;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean aNull) {
        isNull = aNull;
    }
}
