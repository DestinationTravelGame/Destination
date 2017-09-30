package com.example.erik.destination.Question;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class MultipleQuestionWithOneTrue extends Question{
    private String id;
    private HashMap<String,String> questionText=new HashMap<>();
    private HashMap<String,String> trueAnswer=new HashMap<>();
    private Vector<HashMap<String,String>> falseAnswers=new Vector<>();
    private boolean isDownloaded=false;
    public MultipleQuestionWithOneTrue(){}


    public String[] chooseRandomMixedAnsweres(String lng){
        //if(falseAnswers.containsKey(lng)) {
            String[] mixed = new String[falseAnswers.size()+1];
            mixed[0]=trueAnswer.get(lng);
            int i=1;
            for( HashMap<String,String> a:falseAnswers)
            {
                if(a.get(lng)!=null){
                    mixed[i]=a.get(lng);
                    ++i;
                }
            }

            int index;
            String temp;
            Random random = new Random();
            for (int in = mixed.length - 1; in > 0; in--)
            {
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

    public HashMap<String, String> getTrueAnswer() {
        return trueAnswer;
    }

    public void setTrueAnswer(HashMap<String, String> trueAnswer) {
        this.trueAnswer = trueAnswer;
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

    public boolean isAnswerTrue(String answer,String lng){
        return trueAnswer.get(lng).equals(answer);
    }

    public void setDownloaded(boolean downloaded) {
        super.isDownloaded=downloaded;
        isDownloaded = downloaded;
    }
    public void setNull(boolean a){
        super.setNull(a);
    }
}
