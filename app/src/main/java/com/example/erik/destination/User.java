package com.example.erik.destination;

import android.os.SystemClock;
import android.widget.TextView;

import com.example.erik.destination.Constants.Pair;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.erik.destination.Constants.missionCheckpointDone;
import static com.example.erik.destination.Constants.missionCheckpointNotStarted;
import static com.example.erik.destination.Constants.missionCheckpointStartedNotDone1QuestionLast;
import static com.example.erik.destination.Constants.missionCheckpointStartedNotDone2QuestionsLast;
import static com.example.erik.destination.Constants.missionCheckpointStartedNotNear;
import static com.example.erik.destination.Constants.nQuestionsInCheckpoints;
import static com.example.erik.destination.Constants.stateAnswering;
import static com.example.erik.destination.Constants.stateNotAnswering;
import static com.example.erik.destination.MapsActivity.missions;

/**
 * Created by Comp on 30/03/2017.
 */

public class User {
    public CurrentMission currentMission=new CurrentMission();
    DatabaseReference database;
    private String Id;
    private String Name;
    private String Nickname;
    private HashMap<String, String> DoneCheckpoints = new HashMap<>();
    private HashMap<String, String> DoneMissions = new HashMap<>();
    private HashMap<String, Integer> Scores = new HashMap<>();
    private HashMap<String, Integer> Tools = new HashMap<>();
    private int registrationDate;
    private boolean isLoaded = false;
    private Constants constants = new Constants();
    private HashMap<String, Integer> currentCheckpoints = new HashMap<>();
    public User() {
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public Integer getCurrentCheckpoint(String id) {
        return currentCheckpoints.get(id);
    }

    public void setCurrentCheckpoint(String id,int value) {
        database = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            currentCheckpoints.put(id,value);
            database.child("CurrentCheckpoints").child(id).setValue(value);
    }
    private void removeCurrentCheckpoint(String id){
        database = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentCheckpoints.remove(id);
        database.child("CurrentCheckpoints").child(id).setValue(null);
    }

    public void setCheckpointDone(String id) {
        database = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String key = database.child("DoneCheckpoints").child(String.valueOf(System.currentTimeMillis())).getKey();
        database.child("DoneCheckpoints").child(key).setValue(id);
        DoneCheckpoints.put(key, id);
        removeCurrentCheckpoint(id);
    }

    public void addScore(TextView scoreTextView, String type, int value) {
        if (Scores.get(type) == null) {
            Scores.put(type, value);
        } else {
            Scores.put(type, Scores.get(type) + value);
        }
        scoreTextView.setText(String.valueOf(Scores.get(type)));
        FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Scores").child(type).setValue(Scores.get(type));
        //MapsActivity.userScoreType1.setText(Scores.get(type));
    }

    public void start() {
        database = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        database.keepSynced(true);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Nickname = (String) dataSnapshot.child("Nickname").getValue();
                if (dataSnapshot.child("DoneCheckpoints").exists())
                    for (DataSnapshot dat : dataSnapshot.child("DoneCheckpoints").getChildren()) {
                        DoneCheckpoints.put(dat.getKey(), (String) dat.getValue());
                    }

                if (dataSnapshot.child("DoneMissions").exists())
                    for (DataSnapshot dat : dataSnapshot.child("DoneMissions").getChildren()) {
                        DoneMissions.put(dat.getKey(), (String) dat.getValue());
                    }

                if (dataSnapshot.child("Scores").exists()) {
                    for (DataSnapshot dat : dataSnapshot.child("Scores").getChildren()) {
                        Scores.put(dat.getKey(), dat.getValue(long.class).intValue());
                    }
                    //  MapsActivity.userScoreType1.setText(Scores.get("score_type_1"));
                }
                if (dataSnapshot.child("Pocket").exists())
                    for (DataSnapshot dat : dataSnapshot.child("Pocket").getChildren()) {
                        Tools.put(dat.getKey(), (Integer) dat.getValue());
                    }
                if (dataSnapshot.child("CurrentMission").getValue() != null) {
                    currentMission = new CurrentMission();
                    currentMission.setMissionID((String) dataSnapshot.child("CurrentMission").child("MissionID").getValue());
                    currentMission.setScore(((Long) dataSnapshot.child("CurrentMission").child("Score").getValue()).intValue());
                    ArrayList<Integer> CheckpointsStates = new ArrayList<>();
                    ArrayList<ArrayList<String>> questions=new ArrayList<>();
                    ArrayList<ArrayList<Integer>> questionsScores=new ArrayList<>();
                    LatLng unknown=null;

                    for (DataSnapshot eachCheckpoint : dataSnapshot.child("CurrentMission").child("Checkpoints").getChildren()) {
                        if(eachCheckpoint.getKey().equals("UnknownPoint")){
                            unknown=new LatLng(eachCheckpoint.child("Lat").getValue(double.class),eachCheckpoint.child("Long").getValue(double.class));
                        } else {
                            CheckpointsStates.add(Integer.valueOf(eachCheckpoint.getKey()),eachCheckpoint.child("State").getValue(Long.class).intValue());
                            ArrayList<String> questionsForEach=new ArrayList<>();
                            ArrayList<Integer> questionsScoresForEach=new ArrayList<>();
                            for(DataSnapshot insideQuestions:eachCheckpoint.child("Questions").getChildren()){
                                for(DataSnapshot dat:insideQuestions.getChildren()){
                                    questionsForEach.add(Integer.valueOf(insideQuestions.getKey()),dat.getKey());
                                    questionsScoresForEach.add(Integer.valueOf(insideQuestions.getKey()),dat.getValue(Long.class).intValue());
                                }

                                }
                            questions.add(Integer.valueOf(eachCheckpoint.getKey()),questionsForEach);
                            questionsScores.add(Integer.valueOf(eachCheckpoint.getKey()),questionsScoresForEach);

                        }
                    }
                    currentMission.setUnknownPoint(unknown);
                    currentMission.setQuestions(questions);
                    currentMission.setCheckpointsStates(CheckpointsStates);
                }
                if (dataSnapshot.child("CurrentCheckpoints").getValue() != null) {
                    for (DataSnapshot dat : dataSnapshot.child("CurrentCheckpoints").getChildren())
                        currentCheckpoints.put(dat.getKey(), dat.getValue(long.class).intValue());
                }

                isLoaded = true;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public int startMission(final String id,LatLng unknownPoint) {
        if (!DoneMissions.containsKey(id)) {
            Mission mission = missions.getMissionById(id);
            DatabaseReference currentDatabase = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CurrentMission");
            long current = SystemClock.elapsedRealtime();
            currentMission.setMissionID(id);
            currentDatabase.child("MissionID").setValue(id);
            HashMap<Integer,String> checkpointClone = (HashMap<Integer,String>) mission.getCheckpointsIDs().clone();
            HashMap<Integer,HashMap<String,Integer>> checkpointsSetIdInMissStrIdIsDone = new HashMap<>();
            ArrayList<Integer> CheckpointStates=new ArrayList<>();
            ArrayList<ArrayList<String>> allQuestions=new ArrayList<>();

            for (int i=0;i<checkpointClone.size();i++) {
                HashMap<String,Integer> hashMap=new HashMap<>();
                //For Questions
                Checkpoint currentCheckpoint=Checkpoints.getCheckpoint(mission.getCheckpointsIDs().get(i));
                ArrayList<String> selectedQuestionsInCheckpoint=currentCheckpoint.chooseQuestions(nQuestionsInCheckpoints);
                currentDatabase.child("Checkpoints").child(i+"").child("Questions").setValue(selectedQuestionsInCheckpoint);
                allQuestions.add(i,selectedQuestionsInCheckpoint);
                //
                CheckpointStates.add(i,missionCheckpointNotStarted);
                currentDatabase.child("Checkpoints").child(i+"").child("State").setValue(missionCheckpointNotStarted);
                hashMap.put(mission.getCheckpointsIDs().get(i),missionCheckpointNotStarted);
                checkpointsSetIdInMissStrIdIsDone.put(i,hashMap);

            }

            currentDatabase.child("Checkpoints").child("UnknownPoint").child("Lat").setValue(unknownPoint.getLatitude());
            currentDatabase.child("Checkpoints").child("UnknownPoint").child("Long").setValue(unknownPoint.getLongitude());
            currentDatabase.child("Score").setValue(0);
            currentMission.setQuestions(allQuestions);

            //Setting 0
            ArrayList<ArrayList<Integer>> questions=new ArrayList<>();
            for(int i=0;i<allQuestions.size();++i) {
                ArrayList<Integer> questionScore=new ArrayList<>();
                for (int j = 0; j<allQuestions.get(i).size(); ++j) {
                    questionScore.add(j,0);
                }
                questions.add(i,questionScore);
            }
            //
            currentMission.questionsScores=questions;
            //TODO to start
            currentMission.setScore(0);
            CheckpointStates.set(0,missionCheckpointStartedNotNear);
            currentMission.setCheckpointsStates(CheckpointStates);
            currentDatabase.child("Checkpoints").child(0+"").child("State").setValue(missionCheckpointStartedNotNear);
            currentMission.setStateNotAnswering();
            //TODO
            return currentMission.getFinishTime();
        }
        return 0;
    }

    public boolean ifCheckpointDone(String id) {
        if (DoneCheckpoints.get(id) == null)
            return false;
        else
            return true;
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getNickname() {
        return Nickname;
    }

    public HashMap<String, String> getDoneCheckpoints() {
        return DoneCheckpoints;
    }

    public void setDoneCheckpoints(HashMap<String, String> doneCheckpoints) {
        DoneCheckpoints = doneCheckpoints;
    }

    public HashMap<String, String> getDoneMissions() {
        return DoneMissions;
    }

    public void setDoneMissions(HashMap<String, String> doneMissions) {
        DoneMissions = doneMissions;
    }

    public HashMap<String, Integer> getScores() {
        return Scores;
    }

    public void setScores(HashMap<String, Integer> scores) {
        Scores = scores;
    }

    public HashMap<String, Integer> getTools() {
        return Tools;
    }

    public void setTools(HashMap<String, Integer> tools) {
        Tools = tools;
    }

    public int getRegistrationDate() {
        return registrationDate;
    }

    public class CurrentMission {
        private String MissionID;
        private ArrayList<Integer> CheckpointsStates = new ArrayList<>();
        private int StartTime;
        private int FinishTime;
        private HashMap<String, Integer> Tools = new HashMap<>();
        private LatLng unknownPoint;
        private ArrayList<ArrayList<String>> questions=new ArrayList<>();
        private ArrayList<ArrayList<Integer>> questionsScores=new ArrayList<>();
        private int score=0;
        private int state;


        public String currentCheckpointId() {
            if (CheckpointsStates != null) {

                if(CheckpointsStates.contains(missionCheckpointStartedNotDone1QuestionLast))
                    return Missions.getCheckpointByIndex(MissionID,CheckpointsStates.indexOf(missionCheckpointStartedNotDone1QuestionLast));

                if(CheckpointsStates.contains(missionCheckpointStartedNotDone2QuestionsLast))
                    return Missions.getCheckpointByIndex(MissionID,CheckpointsStates.indexOf(missionCheckpointStartedNotDone2QuestionsLast));

                if(CheckpointsStates.contains(missionCheckpointStartedNotNear))
                    return Missions.getCheckpointByIndex(MissionID,CheckpointsStates.indexOf(missionCheckpointStartedNotNear));

                if(CheckpointsStates.contains(missionCheckpointNotStarted))
                    return Missions.getCheckpointByIndex(MissionID,CheckpointsStates.indexOf(missionCheckpointNotStarted));

            }
            return null;
        }
        public Integer currentCheckpointIndex() {
            if (CheckpointsStates != null) {

                if(CheckpointsStates.contains(missionCheckpointStartedNotDone1QuestionLast))
                    return CheckpointsStates.indexOf(missionCheckpointStartedNotDone1QuestionLast);

                if(CheckpointsStates.contains(missionCheckpointStartedNotDone2QuestionsLast))
                    return CheckpointsStates.indexOf(missionCheckpointStartedNotDone2QuestionsLast);
                if(CheckpointsStates.contains(missionCheckpointStartedNotNear))
                    return CheckpointsStates.indexOf(missionCheckpointStartedNotNear);
                if(CheckpointsStates.contains(missionCheckpointNotStarted))
                    return CheckpointsStates.indexOf(missionCheckpointNotStarted);

            }
            return null;
        }
        public String currentCheckpointQuestion(){
            if (questions != null) {
                int currentCheckpointIndex = currentCheckpointIndex();

                if (CheckpointsStates.get(currentCheckpointIndex) == missionCheckpointStartedNotDone2QuestionsLast)
                    return questions.get(currentCheckpointIndex()).get(0);

                if (CheckpointsStates.get(currentCheckpointIndex) == missionCheckpointStartedNotDone1QuestionLast)
                    return questions.get(currentCheckpointIndex()).get(1);

                if (CheckpointsStates.get(currentCheckpointIndex) == missionCheckpointStartedNotNear)
                    return questions.get(currentCheckpointIndex()).get(0);
            }
            return null;
        }
        private Pair<String,Integer> getPairForCheckpoints(HashMap<String,Integer> hashMap){
            HashMap<String,Integer> hashMap1= (HashMap<String, Integer>) hashMap.clone();
            for(String id:hashMap1.keySet()){
                return new Pair<>(id,hashMap1.get(id));
            }
            return null;
        }
        public boolean isCurrentCheckpointDoing(){
            int a=CheckpointsStates.get(currentCheckpointIndex());
            if(a ==missionCheckpointStartedNotDone2QuestionsLast|| a== missionCheckpointStartedNotDone1QuestionLast)
                if(state!=stateNotAnswering)
                    return true;
            return false;
        }
        public int curQuestionInCurCheckpoint(){
            ArrayList<Integer> questionsInCurrent=questionsScores.get(currentCheckpointIndex());
            int index=0;
            for(int i=0;i<questionsInCurrent.size();++i){
                if(questionsInCurrent.get(i)==0) {
                    index = i;
                    break;
                }
            }
            return index;

        }
        public void setStateAnswering(){
            state=stateAnswering;
        }
        public void setStateNotAnswering(){
            state=stateNotAnswering;
        }

        public void currentQuestionTrueAnswer(int time) {
            score+=time;
            int indexOfAnsweredQuestionInCheckpoint=curQuestionInCurCheckpoint();
            int indexOfAnsweredCheckpoint=currentCheckpointIndex();
            int answeredCheckpointState=CheckpointsStates.get(indexOfAnsweredCheckpoint);
            int answeredCheckpointStateAfter=answeredCheckpointState;
            String idOfAnsweredQuestion=questions.get(indexOfAnsweredCheckpoint).get(indexOfAnsweredQuestionInCheckpoint);

            if(answeredCheckpointState==missionCheckpointStartedNotDone2QuestionsLast) {
                CheckpointsStates.set(indexOfAnsweredCheckpoint, missionCheckpointStartedNotDone1QuestionLast);
                answeredCheckpointStateAfter=missionCheckpointStartedNotDone1QuestionLast;
            }

            if(answeredCheckpointState==missionCheckpointStartedNotDone1QuestionLast){
                CheckpointsStates.set(indexOfAnsweredCheckpoint,missionCheckpointDone);//TODO think
                answeredCheckpointStateAfter=missionCheckpointDone;
                if(CheckpointsStates.size()<=(indexOfAnsweredCheckpoint+1)) {
                    //TODO finished
                }else {
                    CheckpointsStates.set(indexOfAnsweredCheckpoint + 1, missionCheckpointStartedNotNear);
                    database.child("CurrentMission").child("Checkpoints").child((indexOfAnsweredCheckpoint+1)+"").child("State").setValue(missionCheckpointStartedNotNear);

                }
            }

            //setting score in database
            database.child("CurrentMission").child("Score").setValue(score);
            //
            //setting checkpoint state in database
            database.child("CurrentMission").child("Checkpoints").child(indexOfAnsweredCheckpoint+"").child("State").setValue(answeredCheckpointStateAfter);
            //

            //setting question score in list and database
            questionsScores.get(indexOfAnsweredCheckpoint).set(indexOfAnsweredQuestionInCheckpoint,time);

            database.child("CurrentMission").child("Checkpoints").child(indexOfAnsweredCheckpoint+"").
                    child("Questions").child(indexOfAnsweredQuestionInCheckpoint+"").child(idOfAnsweredQuestion).setValue(time);
            //
            setStateNotAnswering();

        }

        public void currentQuestionFalseAnswer(int time){
            int indexOfAnsweredQuestionInCheckpoint=curQuestionInCurCheckpoint();
            int indexOfAnsweredCheckpoint=currentCheckpointIndex();
            int answeredCheckpointState=CheckpointsStates.get(indexOfAnsweredCheckpoint);
            int answeredCheckpointStateAfter=answeredCheckpointState;
            String idOfAnsweredQuestion=questions.get(indexOfAnsweredCheckpoint).get(indexOfAnsweredQuestionInCheckpoint);

            if(answeredCheckpointState==missionCheckpointStartedNotDone2QuestionsLast) {
                CheckpointsStates.set(indexOfAnsweredCheckpoint, missionCheckpointStartedNotDone1QuestionLast);
                answeredCheckpointStateAfter=missionCheckpointStartedNotDone1QuestionLast;
            }

            if(answeredCheckpointState==missionCheckpointStartedNotDone1QuestionLast){
                CheckpointsStates.set(indexOfAnsweredCheckpoint,missionCheckpointDone);//TODO think
                answeredCheckpointStateAfter=missionCheckpointDone;
                if(CheckpointsStates.size()<=(indexOfAnsweredCheckpoint+1)) {
                    //TODO finished
                }else {
                    CheckpointsStates.set(indexOfAnsweredCheckpoint + 1, missionCheckpointStartedNotNear);
                    database.child("CurrentMission").child("Checkpoints").child((indexOfAnsweredCheckpoint+1)+"").child("State").setValue(missionCheckpointStartedNotNear);

                }
            }
            //setting question score in list and database
            questionsScores.get(indexOfAnsweredCheckpoint).set(indexOfAnsweredQuestionInCheckpoint,-1);

            database.child("CurrentMission").child("Checkpoints").child(indexOfAnsweredCheckpoint+"").
                    child("Questions").child(indexOfAnsweredQuestionInCheckpoint+"").child(idOfAnsweredQuestion).setValue(-1);
            //
            //setting score in database
            database.child("CurrentMission").child("Score").setValue(score);
            //
            //setting checkpoint state in database
            database.child("CurrentMission").child("Checkpoints").child(indexOfAnsweredCheckpoint+"").
                    child("State").setValue(answeredCheckpointStateAfter);
            //

            setStateNotAnswering();

        }


        public void nearToCheckpoint(){
            setStateAnswering();
            if(CheckpointsStates.get(currentCheckpointIndex())==missionCheckpointStartedNotNear)
                CheckpointsStates.set(currentCheckpointIndex(),missionCheckpointStartedNotDone2QuestionsLast);
        }

        public String getCheckpointsIdByIndex(int index) {
            return Missions.getCheckpointByIndex(MissionID,index);
        }

        public int getStartTime() {
            return StartTime;
        }

        public void setStartTime(int startTime) {
            StartTime = startTime;
        }

        public String getMissionID() {
            return MissionID;
        }

        public void setMissionID(String missionID) {
            MissionID = missionID;
        }



        public int getFinishTime() {
            return FinishTime;
        }

        public void setFinishTime(int finishTime) {
            FinishTime = finishTime;
        }

        public HashMap<String, Integer> getTools() {
            return Tools;
        }

        public void setTools(HashMap<String, Integer> tools) {
            Tools = tools;
        }

        public LatLng getUnknownPoint() {
            return unknownPoint;
        }

        public void setUnknownPoint(LatLng unknownPoint) {
            this.unknownPoint = unknownPoint;
        }


        public ArrayList<ArrayList<String>> getQuestions() {
            return questions;
        }

        public void setQuestions(ArrayList<ArrayList<String>> questions) {
            this.questions = questions;
        }

        public ArrayList<Integer> getCheckpointsStates() {
            return CheckpointsStates;
        }

        public void setCheckpointsStates(ArrayList<Integer> lCheckpointsStates) {
            CheckpointsStates = lCheckpointsStates;
        }


        public void setScore(int score) {
            this.score= score;
        }
        public  int getState(){
            return state;
        }
    }


}
