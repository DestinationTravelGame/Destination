package com.example.erik.destination;

import com.example.erik.destination.Question.MultipleQuestionWithMoreTrue;
import com.example.erik.destination.Question.MultipleQuestionWithOneTrue;
import com.example.erik.destination.Question.Question;
import com.example.erik.destination.Question.SelectDifference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by Erik on 3/20/2017.
 */

public class Questions {
    HashMap<String,Question> questions=new HashMap<>();
    DatabaseReference database= FirebaseDatabase.getInstance().getReference().child("questions");
    HashMap<String, List<String>> waitingCheckpoints=new HashMap<>();

    public Question getQuestion(String s){
        return questions.get(s);
    }
    public int isQuestionReady(String id){
        if(questions.get(id)==null)
            return 0;
        if(questions.get(id).isNull())
            return -1;
        if(!questions.get(id).isDownloaded())
            return 0;

        return 1;
    }
    public void downloadQuestion(String id){
        if(!questions.containsKey(id)) {
            final String[] idSplited = id.split("&");
            database.keepSynced(true);
            database.child(idSplited[0]).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (idSplited[0].equals("mul_one_true")) {
                            MultipleQuestionWithOneTrue question = new MultipleQuestionWithOneTrue();
                            //TODO if eveything ok not null else null
                            if (dataSnapshot.hasChild("question_text")) {
                                HashMap<String, String> questionText = new HashMap<>();
                                for (DataSnapshot dat : dataSnapshot.child("question_text").getChildren()) {
                                    questionText.put(dat.getKey(), (String) dat.getValue());
                                }
                                question.setQuestionText(questionText);
                            }
                            if (dataSnapshot.hasChild("answers")) {
                                HashMap<String, String> trueAnswers = new HashMap<>();
                                Vector<HashMap<String,String>> falseAnswers = new Vector<>();

                                for (DataSnapshot dat : dataSnapshot.child("answers").child("trueAnswer").getChildren()) {
                                    trueAnswers.put(dat.getKey(), (String) dat.getValue());
                                }
                                for (DataSnapshot dat : dataSnapshot.child("answers").child("falseAnswer").getChildren()) {
                                    HashMap<String,String> falseAnswerForAllLanguages = new HashMap<>();
                                    for (DataSnapshot dat2 : dat.getChildren()) {
                                        falseAnswerForAllLanguages.put(dat2.getKey(),dat2.getValue(String.class));
                                    }
                                    if (!falseAnswerForAllLanguages.isEmpty()){

                                        falseAnswers.add(falseAnswerForAllLanguages);
                                    }
                                }
                                question.setTrueAnswer(trueAnswers);
                                question.setFalseAnswers(falseAnswers);
                            }
                            question.setDownloaded(true);
                            question.setId(dataSnapshot.getKey());
                            questions.put(dataSnapshot.getKey(), question);
                            sendQuestionsIfReady(dataSnapshot.getKey());
                        }
                        if(idSplited[0].equals("sel_diff")){
                            SelectDifference question=new SelectDifference();
                            question.setId(dataSnapshot.getKey());
                            //question.setRingSize(dataSnapshot.child("ring_radius").getValue(long.class).intValue());
                            question.setURl(dataSnapshot.child("changed_image_url").getValue(String.class));

                            if(dataSnapshot.hasChild("change_point")){
                                question.setTrueAnswer(dataSnapshot.child("change_point").child("x").getValue(Double.class),dataSnapshot.child("change_point").child("y").getValue(Double.class));
                                question.setRingSizeInPercent(dataSnapshot.child("change_point").child("ring_radius").getValue(Double.class));
                            }
                            if(dataSnapshot.hasChild("comments")){
                                HashMap<String,String> comments=new HashMap<>();
                                for(DataSnapshot dat:dataSnapshot.child("comments").getChildren()){
                                    comments.put(dat.getKey(),dat.getValue(String.class));
                                }
                                question.setComments(comments);
                            }

                            question.setDownloaded(true);
                            question.setId(dataSnapshot.getKey());
                            questions.put(dataSnapshot.getKey(), question);
                            sendQuestionsIfReady(dataSnapshot.getKey());
                        }
                        if (idSplited[0].equals("mul_more_true")) {
                            MultipleQuestionWithMoreTrue question = new MultipleQuestionWithMoreTrue();
                            //TODO if eveything ok not null else null
                            if (dataSnapshot.hasChild("question_text")) {
                                HashMap<String, String> questionText = new HashMap<>();
                                for (DataSnapshot dat : dataSnapshot.child("question_text").getChildren()) {
                                    questionText.put(dat.getKey(), (String) dat.getValue());
                                }
                                question.setQuestionText(questionText);
                            }
                            if (dataSnapshot.hasChild("answers")) {
                                Vector<HashMap<String, String>> trueAnswers = new Vector<>();
                                Vector<HashMap<String,String>> falseAnswers = new Vector<>();

                                for (DataSnapshot dat : dataSnapshot.child("answers").child("trueAnswers").getChildren()) {
                                    HashMap<String,String> trueAnswerForAllLanguages = new HashMap<>();
                                    for (DataSnapshot dat2 : dat.getChildren()) {
                                        trueAnswerForAllLanguages.put(dat2.getKey(),String.valueOf(dat2.getValue()));
                                    }
                                    if (!trueAnswerForAllLanguages.isEmpty()){
                                        trueAnswers.add(trueAnswerForAllLanguages);
                                    }
                                }
                                for (DataSnapshot dat : dataSnapshot.child("answers").child("falseAnswers").getChildren()) {
                                    HashMap<String,String> falseAnswerForAllLanguages = new HashMap<>();
                                    for (DataSnapshot dat2 : dat.getChildren()) {
                                        falseAnswerForAllLanguages.put(dat2.getKey(),String.valueOf(dat2.getValue()));
                                    }
                                    if (!falseAnswerForAllLanguages.isEmpty()){
                                        falseAnswers.add(falseAnswerForAllLanguages);
                                    }
                                }
                                question.setTrueAnswer(trueAnswers);
                                question.setFalseAnswers(falseAnswers);
                            }
                            question.setDownloaded(true);
                            question.setId(dataSnapshot.getKey());
                            questions.put(dataSnapshot.getKey(), question);
                            sendQuestionsIfReady(dataSnapshot.getKey());
                        }
                    } else {
                        questions.put(dataSnapshot.getKey(), new Question());
                        sendQuestionsIfReady(dataSnapshot.getKey());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else{
            sendQuestionsIfReady(id);
        }

    }
    public void startDownloadQuestions(String checkpointId,List<String> questionsId){
        if(questionsId!=null) {
            List<String>questionsIdClone= (List<String>) ((ArrayList<String>)questionsId).clone();
            waitingCheckpoints.put(checkpointId, questionsIdClone);
            for (String questionId : questionsIdClone) {
                downloadQuestion(questionId);
            }
        }else
            Checkpoints.questionsCallback(checkpointId);
    }
    public void sendQuestionsIfReady(String questionId) {
        String[] checkpointsId=waitingCheckpoints.keySet().toArray(new String[waitingCheckpoints.keySet().size()]);
        for (String aCheckpointsId : checkpointsId) {
            if (waitingCheckpoints.get(aCheckpointsId).remove(questionId)) {
                if (waitingCheckpoints.get(aCheckpointsId).isEmpty()) {
                    Checkpoints.questionsCallback(aCheckpointsId);
                    waitingCheckpoints.remove(aCheckpointsId);
                }
            }
        }
    }
}