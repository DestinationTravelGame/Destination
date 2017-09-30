package com.example.erik.destination;

import android.content.Context;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static com.example.erik.destination.MapsActivity.mMap;

/**
 * Created by Erik on 3/20/2017.
 */

public class Missions {
    private HashMap<String,Mission> missionsMap=new HashMap<>();
    private ArrayList<String> missionsId=new ArrayList<>();
    private DatabaseReference database= FirebaseDatabase.getInstance().getReference();
    private Constants constants=new Constants();
    private boolean thisModeIsActive=false;
    private ArrayList<String> listForWaitingMissions=new ArrayList<>();
    Context context;
    private boolean isStarted=false;
    private static HashMap<MarkerView, String> missionsMarkerTags = new HashMap<>();

    public static void setTag(MarkerView marker, String id) {
        missionsMarkerTags.put(marker, id);
    }

    Missions(){}

    public HashMap<String, Mission> getMissions() {
        return missionsMap;
    }

     public void start(Context context){
        database.keepSynced(true);
         this.context=context;
         isStarted=true;
        database.child("missions_id").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                missionsId.add(dataSnapshot.getValue(String.class));
                checkIfNewCheckpointInView(dataSnapshot.getKey());
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
     }

    private void checkIfNewCheckpointInView(String Id) {
            if(isStarted){
            String Id1 = Id.replaceAll(",", ".");
            String[] splitedId = Id1.split("_");
            if (mMap.getCameraPosition().zoom>constants.maxZoom && mMap.getProjection().getVisibleRegion().latLngBounds.contains(new LatLng(Double.valueOf(splitedId[splitedId.length - 2]), Double.valueOf(splitedId[splitedId.length - 1])))) {
                downloadMission(Id);
            }
        }
    }

    private void downloadMission(String Id) {
        if (!missionsMap.containsKey(Id)) {
            DatabaseReference data = FirebaseDatabase.getInstance().getReference("missions");
            String[] splitedId = Id.split("_");
            for (int i = 0; i < splitedId.length - 2; i++) {
                data = data.child(splitedId[i]);
            }
            data = data.child(Id);
            data.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Mission a = new Mission();
                    a.setId(dataSnapshot.getKey());
                    String b = dataSnapshot.getKey();
                    b = b.replaceAll(",", ".");
                    String[] splitedId = b.split("_");
                    a.setLocation(new LatLng(Double.valueOf(splitedId[splitedId.length - 2]), Double.valueOf(splitedId[splitedId.length - 1])));
                    if (dataSnapshot.hasChild("checkpoints")) {
                        Vector<String> checkpoints = new Vector<>();
                        for (DataSnapshot dat : dataSnapshot.child("checkpoints").getChildren()) {
                            checkpoints.add((String) dat.getValue());
                        }
                        a.setCheckpointsIDs(checkpoints);
                    }
                    if (dataSnapshot.hasChild("titles")) {
                        Map<String, String> titles = new HashMap<>();
                        for (DataSnapshot dat : dataSnapshot.child("titles").getChildren()) {
                            titles.put(dat.getKey(), (String) dat.getValue());
                        }
                        a.setTitle(titles);
                    }
                    if (dataSnapshot.hasChild("description")) {
                        Map<String, String> description = new HashMap<>();
                        for (DataSnapshot dat : dataSnapshot.child("description").getChildren()) {
                            description.put(dat.getKey(), (String) dat.getValue());
                        }
                        a.setDescription(description);
                    }
                    a.setTimeMinutes((int) dataSnapshot.child("time").getValue());
                    if (dataSnapshot.hasChild("types")) {
                        Vector<Integer> types = new Vector<>();
                        for (DataSnapshot dat : dataSnapshot.child("types").getChildren()) {
                            types.add((int) dat.getValue());
                        }
                        a.setTypes(types);
                    }
                    if (dataSnapshot.hasChild("user_rate"))
                        a.setUserRate((float) dataSnapshot.child("user_rate").getValue());
                    if (dataSnapshot.hasChild("reviews")) {
                        Vector<String> reviews = new Vector<>();
                        for (DataSnapshot dat : dataSnapshot.child("reviews").getChildren()) {
                            reviews.add((String) dat.getValue());
                        }
                        a.setReviews(reviews);
                    }
                    if (dataSnapshot.hasChild("roads")) {
                        HashMap<String, HashMap<String, Boolean>> roads = new HashMap<>();
                        HashMap<String, Boolean> each = new HashMap<>();
                        for (DataSnapshot dat : dataSnapshot.child("roads").getChildren()) {
                            if (dat.getChildrenCount() != 0) {
                                for (DataSnapshot dat1 : dat.getChildren()) {
                                    if (dat1.getValue() != null)
                                        each.put(dat1.getKey(), (boolean) dat1.getValue());
                                }
                                roads.put(dat.getKey(), each);
                            }
                        }
                        a.setRoads(roads);
                    }
                    if (dataSnapshot.hasChild("difficulty_level"))
                        a.setDifficultyLevel((int) dataSnapshot.child("difficulty_level").getValue());
                    if (dataSnapshot.hasChild("number_played"))
                        a.setNumberPlayed((int) dataSnapshot.child("number_played").getValue());
                    if (dataSnapshot.hasChild("number_of_online_users"))
                        a.setNumberOnlinePlayers((int) dataSnapshot.child("number_of_online_users").getValue());
                    if (dataSnapshot.hasChild("marker_url")) {
                        a.setMarkerURI((String) dataSnapshot.child("marker_url").getValue());
                        //TODO add marker with Picasso
                    }
                    if (dataSnapshot.hasChild("number_completed"))
                        a.setNumberPlayed((int) dataSnapshot.child("number_completed").getValue());
                    missionsMap.put(dataSnapshot.getKey(), a);
                    listForWaitingMissions.add(a.getId());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private android.os.Handler handlerForWaiter=new android.os.Handler();
    private Runnable waiterForUnreadyMissions=new Runnable() {
        @Override
        public void run() {
            ArrayList<String> listForWaitingMissionsClone=(ArrayList<String>)listForWaitingMissions.clone();
            if(!listForWaitingMissionsClone.isEmpty()) {
                for (String missionId : listForWaitingMissionsClone) {
                    boolean mijankyal = true;
                    for (String checkpointId : missionsMap.get(missionId).getCheckpointsIDs()) {
                        if (!Checkpoints.getCheckpoint(checkpointId).isLoaded())
                            Checkpoints.downloadCheckpoint(checkpointId);
                        mijankyal &= Checkpoints.getCheckpoint(checkpointId).isLoaded();
                    }
                    if (mijankyal) {
                        missionsMap.get(missionId).setLoaded(true);
                        missionsMap.get(missionId).addToMap(context, thisModeIsActive);
                        listForWaitingMissions.remove(missionId);
                    }
                }
            }
            handlerForWaiter.postDelayed(waiterForUnreadyMissions,2000);
        }
    };

    public void startThisMode(){
        thisModeIsActive=true;
        if(!missionsMarkerTags.isEmpty()) {
            for (MarkerView marker : missionsMarkerTags.keySet()) {
                marker.setVisible(true);
            }
        }
    }
    public void stopThisMode(){
        thisModeIsActive=false;
        if(!missionsMarkerTags.isEmpty()) {
            for (MarkerView marker : missionsMarkerTags.keySet()) {
                marker.setVisible(false);
            }
        }
    }
}
