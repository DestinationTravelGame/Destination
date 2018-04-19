package com.example.erik.destination;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.erik.destination.Question.MultipleQuestionWithMoreTrue;
import com.example.erik.destination.Question.MultipleQuestionWithOneTrue;
import com.example.erik.destination.Question.Question;
import com.example.erik.destination.Question.SelectDifference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Erik on 3/20/2017.
 */

public class Questions {
    static HashMap<String,Question> questions=new HashMap<>();
    DatabaseReference database= FirebaseDatabase.getInstance().getReference().child("questions");
    HashMap<String, ArrayList<String>> waitingCheckpoints=new HashMap<>();

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
                public void onDataChange(final DataSnapshot dataSnapshot) {
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

                                for (DataSnapshot dat : dataSnapshot.child("answers").child("true_answer").getChildren()) {
                                    trueAnswers.put(dat.getKey(), (String) dat.getValue());
                                }
                                for (DataSnapshot dat : dataSnapshot.child("answers").child("false_answer").getChildren()) {
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
                            final SelectDifference[] question = {new SelectDifference()};
                            question[0].setId(dataSnapshot.getKey());
                            //question.setRingSize(dataSnapshot.child("ring_radius").getValue(long.class).intValue());


                            if(dataSnapshot.hasChild("change_point")){
                                question[0].setTrueAnswer(dataSnapshot.child("change_point").child("x").getValue(Double.class),dataSnapshot.child("change_point").child("y").getValue(Double.class));
                                question[0].setRingSizeInPercent(dataSnapshot.child("change_point").child("ring_radius").getValue(Double.class));
                            }
                            if(dataSnapshot.hasChild("comments")){
                                HashMap<String,String> comments=new HashMap<>();
                                for(DataSnapshot dat:dataSnapshot.child("comments").getChildren()){
                                    comments.put(dat.getKey(),dat.getValue(String.class));
                                }
                                question[0].setComments(comments);
                            }
                            StorageReference imageRef=FirebaseStorage.getInstance().getReference().child("questions").child("sel_diff").child(question[0].getId());
                            GlideApp.with(MapsActivity.context).asBitmap().load(imageRef).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    question[0].setDownloaded(true);
                                    questions.put(dataSnapshot.getKey(), question[0]);
                                    sendQuestionsIfReady(dataSnapshot.getKey());
                                    Log.v(Constants.TAG,question[0].getId()+"  ready");
                                }
                            });
                            /*imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    question[0].setURl(uri);
                                   // Log.v(Constants.TAG,question[0].getId()+" "+uri.toString());
                                    Picasso.with(MapsActivity.context).load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(new Target() {
                                        @Override
                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                                        }
                                        @Override
                                        public void onBitmapFailed(Drawable errorDrawable) {

                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                                        }
                                    });*/


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

                                for (DataSnapshot dat : dataSnapshot.child("answers").child("true_answers").getChildren()) {
                                    HashMap<String,String> trueAnswerForAllLanguages = new HashMap<>();
                                    for (DataSnapshot dat2 : dat.getChildren()) {
                                        trueAnswerForAllLanguages.put(dat2.getKey(),String.valueOf(dat2.getValue()));
                                    }
                                    if (!trueAnswerForAllLanguages.isEmpty()){
                                        trueAnswers.add(trueAnswerForAllLanguages);
                                    }
                                }
                                for (DataSnapshot dat : dataSnapshot.child("answers").child("false_answers").getChildren()) {
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
    public void startDownloadQuestions(String checkpointId,ArrayList<String> questionsId){

        if(questionsId!=null) {
            ArrayList<String>  questionsIdClone=new ArrayList<>();
            questionsIdClone.addAll(questionsId);
            waitingCheckpoints.put(checkpointId, questionsIdClone);
            for (int i=0;i<questionsId.size();++i) {
                downloadQuestion(questionsId.get(i));
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
    public static Question getQuestionById(String Id){
        return questions.get(Id);
    }
}