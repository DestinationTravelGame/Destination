package com.example.erik.destination;

import com.example.erik.destination.Question.MultipleQuestionWithOneTrue;
import com.example.erik.destination.Question.Question;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Erik on 3/20/2017.
 */

public class Questions {
    HashMap<String,Question> questions=new HashMap<>();
    DatabaseReference database= FirebaseDatabase.getInstance().getReference().child("questions");

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
                            questions.put(dataSnapshot.getKey(), question);

                        }
                    } else {
                        questions.put(dataSnapshot.getKey(), new Question());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}
