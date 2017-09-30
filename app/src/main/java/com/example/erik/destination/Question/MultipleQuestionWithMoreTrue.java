package com.example.erik.destination.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Comp on 08/04/2017.
 */

public class MultipleQuestionWithMoreTrue extends Question {
    private String id;
    private HashMap<String,String> questionText=new HashMap<>();
    private Vector<HashMap<String,String>> trueAnswers= new Vector<>();
    private Vector<HashMap<String,String>> falseAnswers=new Vector<>();
    private boolean isDownloaded=false;
    public MultipleQuestionWithMoreTrue(){}


    public String[] chooseRandomMixedAnsweres(String lng){
        //if(falseAnswers.containsKey(lng)) {
        List<String> falseAndTrue = new ArrayList<>();
        int i=1;
        for( HashMap<String,String> a:falseAnswers){
            if(a.get(lng)!=null){
                falseAndTrue.add(a.get(lng));
                ++i;
            }
        }
        for( HashMap<String,String> a:trueAnswers){
            if(a.get(lng)!=null){
                falseAndTrue.add(a.get(lng));
                ++i;
            }
        }

        int index;
        String temp;
        Random random = new Random();
        String[] mixed=new String[falseAndTrue.size()];
        mixed=falseAndTrue.toArray(mixed);
        for (int in = mixed.length-1; in > 0; in--){
            index = random.nextInt(in + 1);
            temp = mixed[index];
            mixed[index] = mixed[in];
            mixed[in] = temp;
        }
        return mixed;
        // }
        // return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        super.id=id;
        this.id = id;
    }

    public HashMap<String, String> getQuestionText() {
        return questionText;
    }

    public void setQuestionText(HashMap<String, String> questionText) {
        this.questionText = questionText;
    }

    public Vector<HashMap<String, String>> getTrueAnswer() {
        return trueAnswers;
    }

    public void setTrueAnswer(Vector<HashMap<String, String>> trueAnswer) {
        this.trueAnswers = trueAnswer;
    }

    public Vector<HashMap<String,String>> getFalseAnswers() {
        return falseAnswers;
    }

    public void setFalseAnswers(Vector<HashMap<String,String>> falseAnswers) {
        this.falseAnswers = falseAnswers;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public boolean isAnswerTrue(List<String> answer,String lng){
        List<String> trueAns=new ArrayList<>();
        for(HashMap<String,String> a:trueAnswers){
            if(a.containsKey(lng)){
                trueAns.add(a.get(lng));
            }
        }
        if(answer.size()==trueAns.size()){
            for(String a:answer){
                if(!trueAns.contains(a)){
                    return false;
                }
            }
        }else{
            return false;
        }
        return true;
    }

    public void setDownloaded(boolean downloaded) {
        super.isDownloaded=downloaded;
        isDownloaded = downloaded;
    }
    public void setNull(boolean a){
        super.setNull(a);
    }
}


