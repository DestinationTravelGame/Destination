package com.example.erik.destination.Question;

import com.example.erik.destination.Constants;

import java.util.HashMap;

/**
 * Created by Comp on 08/04/2017.
 */

public class SelectDifference extends Question {
    private String id;
    private String URl;
    private Constants.Pair<Double,Double> trueAnswer=new Constants.Pair<>();
    private Constants.Pair<Integer,Integer> trueAnswerForThisScreen=new Constants.Pair<>();
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

    public String getURl() {
        return URl;
    }

    public void setURl(String URl) {
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

    public Constants.Pair<Double, Double> getTrueAnswer() {
        return trueAnswer;
    }

    public void setTrueAnswer(double x,double y) {
        this.trueAnswer = new Constants.Pair<>(x,y);
    }

    public Constants.Pair<Integer, Integer> getTrueAnswerForThisScreen() {
        return trueAnswerForThisScreen;
    }

    public void setTrueAnswerForThisScreen(int x,int y) {
        this.trueAnswerForThisScreen = new Constants.Pair<>(x,y);
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
