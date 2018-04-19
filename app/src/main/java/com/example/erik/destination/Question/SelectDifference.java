package com.example.erik.destination.Question;

import android.net.Uri;

import com.example.erik.destination.Constants.Pair;

import java.util.HashMap;

/**
 * Created by Comp on 08/04/2017.
 */

public class SelectDifference extends Question {
    private String id;
    private Uri URl;
    private Pair<Double,Double> trueAnswer=new Pair<>();
    private Pair<Integer,Integer> trueAnswerForThisScreen=new Pair<>();
    private double ringSizeInPercent=-1;
    private int realRingSize=-1;
    private HashMap<String,String> comments=new HashMap<>();

    public SelectDifference() {
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        super.id=id;
        this.id = id;
    }

    public Uri getURl() {
        return URl;
    }

    public void setURl(Uri URl) {
        this.URl = URl;
    }

    public HashMap<String, String> getComments() {
        return comments;
    }

    public void setComments(HashMap<String, String> comments) {
        this.comments = comments;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public boolean isAnswerTrue(String answer,String lng){
        return true;//TODO
    }

    public void setDownloaded(boolean downloaded) {
        super.isDownloaded=downloaded;
        isDownloaded = downloaded;
    }
    public void setNull(boolean a){
        super.setNull(a);
    }

    public Pair<Double, Double> getTrueAnswer() {
        return trueAnswer;
    }

    public void setTrueAnswer(double x,double y) {
        this.trueAnswer = new Pair<>(x,y);
    }

    public Pair<Integer, Integer> getTrueAnswerForThisScreen() {
        return trueAnswerForThisScreen;
    }

    public void setTrueAnswerForThisScreen(int x,int y) {
        this.trueAnswerForThisScreen = new Pair<>(x,y);
    }

    public double getRingSizeInPercent() {
        return ringSizeInPercent;
    }

    public void setRingSizeInPercent(double ringSizeInPercent) {
        this.ringSizeInPercent = ringSizeInPercent;
    }

    public int getRealRingSize() {
        return realRingSize;
    }

    public void setRealRingSize(int realRingSize) {
        this.realRingSize = realRingSize;
    }
}
