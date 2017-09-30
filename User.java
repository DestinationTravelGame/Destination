package com.example.erik.destination;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by Comp on 30/03/2017.
 */

public class User  {
    public boolean isLoaded() {
        return isLoaded;
    }

    private class CurrentMission{
        private String MissionID;
        private HashMap<String,Boolean> Checkpoints=new HashMap<>();
        private String CurrentCheckpoint;
        private int StartTime;


        public String getCurrentCheckpoint() {
            return CurrentCheckpoint;
        }

        public void setCurrentCheckpoint(String currentCheckpoint) {
            CurrentCheckpoint = currentCheckpoint;
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

        public HashMap<String, Boolean> getCheckpoints() {
            return Checkpoints;
        }

        public void setCheckpoints(HashMap<String, Boolean> checkpoints) {
            Checkpoints = checkpoints;
        }
    }
    private String Id;
    private String Name;
    private String Nickname;
    private HashMap<String,String> DoneCheckpoints=new HashMap<>();
    private HashMap<String,String> DoneMissions=new HashMap<>();
    private HashMap<String,Integer> Scores=new HashMap<>();
    private CurrentMission currentMission;
    private HashMap<String,Integer> Tools=new HashMap<>();
    private int registrationDate;
    private boolean isLoaded=false;

    DatabaseReference database;
    public User(){}
    public void setCheckpointDone(String id){
        database= FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String key=database.child("DoneCheckpoints").push().getKey();
        database.child("DoneCheckpoints").child(key).setValue(id);
        DoneCheckpoints.put(key,id);
    }
    public void addScore(Context context, String type, int value){
        if(Scores.get(type)==null){
            Scores.put(type,value);
        }else{
            Scores.put(type,Scores.get(type)+value);
        }
        FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Scores").child(type).setValue(Scores.get(type));
        //MapsActivity.userScoreType1.setText(Scores.get(type));

    }

    public void start(){
        database= FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Id=FirebaseAuth.getInstance().getCurrentUser().getUid();
        Name=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        database.keepSynced(true);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Nickname=(String)dataSnapshot.child("Nickname").getValue();
                if(dataSnapshot.child("DoneCheckpoints").exists())
                for (DataSnapshot dat:dataSnapshot.child("DoneCheckpoints").getChildren()) {
                    DoneCheckpoints.put(dat.getKey(),(String)dat.getValue());
                }

                if(dataSnapshot.child("DoneMissions").exists())
                    for (DataSnapshot dat:dataSnapshot.child("DoneMissions").getChildren()) {
                        DoneMissions.put(dat.getKey(),(String)dat.getValue());
                    }

                if(dataSnapshot.child("Scores").exists()) {
                    for (DataSnapshot dat : dataSnapshot.child("Scores").getChildren()) {
                        Scores.put(dat.getKey(), dat.getValue(long.class).intValue());
                    }
                  //  MapsActivity.userScoreType1.setText(Scores.get("score_type_1"));
                }
                if(dataSnapshot.child("Pocket").exists())
                    for (DataSnapshot dat:dataSnapshot.child("Pocket").getChildren()) {
                        Tools.put(dat.getKey(),(Integer) dat.getValue());
                    }
                if(dataSnapshot.child("CurrentMission").getValue()!=null){
                    currentMission=new CurrentMission();
                    currentMission.setMissionID((String)dataSnapshot.child("CurrentMission").child("MissionID").getValue());
                    currentMission.setStartTime((Integer) dataSnapshot.child("CurrentMission").child("StartTime").getValue());
                    currentMission.setCurrentCheckpoint((String)dataSnapshot.child("CurrentMission").child("CurrentCheckpoint").getValue());
                    HashMap<String,Boolean> Checkpoints= new HashMap<>();
                    for (DataSnapshot dat:dataSnapshot.child("CurrentMission").child("Checkpoints").getChildren()) {
                        Checkpoints.put(dat.getKey(),(Boolean)dat.getValue());
                    }
                    currentMission.setCheckpoints(Checkpoints);
                }
                isLoaded=true;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean ifCheckpointDone(String id){
        if(DoneCheckpoints.get(id)==null)
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



}
