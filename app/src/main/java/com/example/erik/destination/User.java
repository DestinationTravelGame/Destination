package com.example.erik.destination;

import android.os.SystemClock;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.Set;

import static com.example.erik.destination.Constants.missionCheckpointNotStarted;
import static com.example.erik.destination.Constants.missionCheckpointStartedNotDone2QuestionsLast;
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
                    currentMission.setCurrentCheckpoint((String) dataSnapshot.child("CurrentMission").child("CurrentCheckpoint").getValue());
                    HashMap<Integer, HashMap<String,Integer>> Checkpoints = new HashMap<>();
                    for (DataSnapshot dat : dataSnapshot.child("CurrentMission").child("Checkpoints").getChildren()) {
                        if(dat.getKey().equals("UnknownPoint")){
                            LatLng unknown=new LatLng(dat.child("Lat").getValue(double.class),dat.child("Long").getValue(double.class));
                        } else {
                            HashMap<String,Integer> hash=new HashMap<>();
                            for(DataSnapshot dat1:dat.getChildren()){
                                hash.put(dat1.getKey(),dat1.getValue(Long.class).intValue());
                            }
                            Checkpoints.put(Integer.valueOf(dat.getKey()), hash);
                        }
                    }

                    currentMission.setCheckpoints(Checkpoints);
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
            DatabaseReference currnetDatabase = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CurrentMission");
            long current = SystemClock.elapsedRealtime();
            currentMission.setMissionID(id);
            currnetDatabase.child("MissionID").setValue(id);
            HashMap<Integer,String> checkpointClone = (HashMap<Integer,String>) mission.getCheckpointsIDs().clone();
            HashMap<Integer,HashMap<String,Integer>> checkpointsSetIdInMissStrIdIsDone = new HashMap<>();
            HashMap<String,HashMap<String,Integer>> checkpointsSetIdInMissStrIdIsDoneFordDatabase = new HashMap<>();
            for (int i=0;i<checkpointClone.size();i++) {
                HashMap<String,Integer> hashMap=new HashMap<String,Integer>();
                hashMap.put(mission.getCheckpointsIDs().get(i),missionCheckpointNotStarted);
                checkpointsSetIdInMissStrIdIsDone.put(i,hashMap);
                checkpointsSetIdInMissStrIdIsDoneFordDatabase.put(String.valueOf(i),hashMap);
            }
            currnetDatabase.child("Checkpoints").setValue(checkpointsSetIdInMissStrIdIsDoneFordDatabase);
            currnetDatabase.child("Checkpoints").child("UnknownPoint").child("Lat").setValue(unknownPoint.getLatitude());
            currnetDatabase.child("Checkpoints").child("UnknownPoint").child("Long").setValue(unknownPoint.getLongitude());
            currentMission.setCheckpoints(checkpointsSetIdInMissStrIdIsDone);
            currentMission.setCurrentCheckpoint(checkpointClone.get(0));
            return currentMission.getFinishTime();
        }
        return 0;
    }

    public void missionCheckpointStarted(){

        database.child("CurrentMission").child(String.valueOf(getCurrentCheckpoint(currentMission.getCurrentCheckpoint())));
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
        private HashMap<Integer,HashMap<String,Integer>> Checkpoints = new HashMap<>();
        private String CurrentCheckpoint;
        private int StartTime;
        private int FinishTime;
        private Constants constants = new Constants();
        private HashMap<String, Integer> Tools = new HashMap<>();
        private LatLng unknownPoint;


        public String currentCheckpointId() {
            if (Checkpoints != null) {
                Set<String> checkpointsClone = ((HashMap<String, Integer>) Checkpoints.clone()).keySet();
                for (int i=0;i<checkpointsClone.size();i++) {
                    if (getPairForCheckpoints(Checkpoints.get(i)).second == missionCheckpointNotStarted) {
                        return getPairForCheckpoints(Checkpoints.get(i)).first;
                    }
                }
            }
            return null;
        }
        private Constants.Pair<String,Integer> getPairForCheckpoints(HashMap<String,Integer> hashMap){
            HashMap<String,Integer> hashMap1= (HashMap<String, Integer>) hashMap.clone();
            for(String id:hashMap1.keySet()){
                return new Constants.Pair<>(id,hashMap1.get(id));
            }
            return null;
        }
        public boolean isCurrentCheckpointDoing(){
            if(User.this.getCurrentCheckpoint(currentCheckpointId())>=missionCheckpointStartedNotDone2QuestionsLast)
                return true;
            return false;
        }

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

        public HashMap<Integer, HashMap<String, Integer>> getCheckpoints() {
            return Checkpoints;
        }

        public void setCheckpoints(HashMap<Integer,HashMap<String,Integer>> checkpoints) {
            Checkpoints = checkpoints;
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
    }


}
