package com.example.erik.destination;

import android.content.Context;
import android.location.Location;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static com.example.erik.destination.MapsActivity.mMap;

/**
 * Created by Erik on 3/20/2017.
 */

public class Checkpoints {
    public static Map<String,Checkpoint> checkpointMap=new HashMap<String, Checkpoint>();
    private Vector<String> checkpointsId=new Vector<>();
    private DatabaseReference databaseId=FirebaseDatabase.getInstance().getReference("checkpoints_id");
    private boolean isStarted=false;
    private Context contextForResourses;
    public static Questions questions=new Questions();
    private static Vector<String> vectorForWaitingCheckpoints=new Vector<>();
    private Vector<String> VectorForChangedWaitingCheckpoints=new Vector<>();
    private boolean thisModeIsActive=false;

    private Constants constants=new Constants();

    private static HashMap<MarkerView, String> checkpointsMarkerTags = new HashMap<>();

    public static Checkpoint getCheckpoint(String id){
        return checkpointMap.get(id);
    }
    public static void setTag(MarkerView marker, String id) {
        checkpointsMarkerTags.put(marker, id);
    }
    public static Set<MarkerView> getKeys(){
        return checkpointsMarkerTags.keySet();
    }

    Checkpoints(){
    }
    public Constants.Pair<String,Float> isNearToAnyCheckpoint(LatLng location){
        Constants.Pair<String,Float> returning = null;
        if(!checkpointMap.isEmpty() && location!=null) {
            for (Checkpoint a : checkpointMap.values()) {
                if(a!=null) {
                    float[] distance = new float[1];
                    Location.distanceBetween(a.getLocation().getLatitude(), a.getLocation().getLongitude(), location.getLatitude(), location.getLongitude(), distance);
                    if (distance[0] < a.getMarkerRadius())
                        if(returning==null) {
                            returning = new Constants.Pair<>(a.getId(), distance[0]);
                        }else if(returning.getSecond()>distance[0])
                            returning=new Constants.Pair<>(a.getId(),distance[0]);
                }
            }
        }
        return returning;
    }
    public void start(Context appContext){
        contextForResourses=appContext;
        isStarted=true;
        databaseId.keepSynced(true);
        handlerForWaiter.post(waiterForQuestionsToBeReady);
        databaseId.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                checkpointsId.add(dataSnapshot.getKey());
                checkIfNewCheckpointInView(dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                changeCheckpoint(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //TODO if mission started think:)
                checkpointsId.remove(dataSnapshot.getKey());
                if(checkpointMap.get(dataSnapshot.getKey())!=null){
                    checkpointMap.get(dataSnapshot.getKey()).removeFromMap();
                    checkpointMap.remove(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void startThisMode(){
        thisModeIsActive=true;
        if(!checkpointsMarkerTags.isEmpty()) {
            for (MarkerView marker : checkpointsMarkerTags.keySet()) {
                marker.setVisible(true);
            }
        }
    }
    public void stopThisMode(){
        thisModeIsActive=false;
        if(!checkpointsMarkerTags.isEmpty()) {
            for (MarkerView marker : checkpointsMarkerTags.keySet()) {
                marker.setVisible(false);
            }
        }
    }


    public void updateMapForNewCheckpoints(){

        checkWhichCheckpointsInView();
    }
    private void checkWhichCheckpointsInView(){
        if(isStarted && !checkpointMap.isEmpty()) {
            for (String id : checkpointsId) {
                if(checkpointMap.get(id)==null) {
                    String id1 = id.replaceAll(",", ".");
                    String[] splitedId = id1.split("_");
                    if (mMap.getCameraPosition().zoom>constants.maxZoom && mMap.getProjection().getVisibleRegion().latLngBounds.contains(new LatLng(Double.valueOf(splitedId[splitedId.length - 2]), Double.valueOf(splitedId[splitedId.length - 1])))) {
                        downloadCheckpoint(id);
                    }
                }
            }
        }
    }
    private void checkIfNewCheckpointInView(String Id){
        if(isStarted) {
            String Id1 = Id.replaceAll(",", ".");
            String[] splitedId = Id1.split("_");
            if (mMap.getCameraPosition().zoom>constants.maxZoom && mMap.getProjection().getVisibleRegion().latLngBounds.contains(new LatLng(Double.valueOf(splitedId[splitedId.length - 2]), Double.valueOf(splitedId[splitedId.length - 1])))) {
                downloadCheckpoint(Id);
            }
        }
    }
    public static void downloadCheckpoint(String Id){
        if(!checkpointMap.containsKey(Id)) {
            DatabaseReference data = FirebaseDatabase.getInstance().getReference("checkpoints");
            String[] splitedId = Id.split("_");
            for (int i = 0; i < splitedId.length - 2; i++) {
                data = data.child(splitedId[i]);
            }
            data = data.child(Id);
            data.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Checkpoint a = new Checkpoint();
                    a.setId(dataSnapshot.getKey());
                    String b = dataSnapshot.getKey();
                    b = b.replaceAll(",", ".");
                    String[] splitedId = b.split("_");
                    a.setLocation(new LatLng(Double.valueOf(splitedId[splitedId.length - 2]), Double.valueOf(splitedId[splitedId.length - 1])));
                    if (dataSnapshot.hasChild("number_of_online_users"))
                        a.setNumberOnlinePlayers((int) dataSnapshot.child("number_of_online_users").getValue());

                    if (dataSnapshot.hasChild("number_played"))
                        a.setNumberPlayed((int) dataSnapshot.child("number_played").getValue());

                    if (dataSnapshot.hasChild("difficulty"))
                        a.setDifficultyLevel(dataSnapshot.child("difficulty").getValue(long.class).intValue());

                    if (dataSnapshot.hasChild("marker_uri")) {
                        a.setMarkerURI((String) dataSnapshot.child("marker_uri").getValue());
                        //TODO add marker with Picasso
                    }

                    if (dataSnapshot.hasChild("title")) {
                        Map<String, String> titles = new HashMap<>();
                        for (DataSnapshot dat : dataSnapshot.child("title").getChildren()) {
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
                    if (dataSnapshot.hasChild("checkpoints_type")) {
                        Vector<String> types = new Vector<>();
                        for (DataSnapshot dat : dataSnapshot.child("checkpoints_type").getChildren()) {
                            types.add((String) dat.getValue());
                        }
                        a.setTypes(types);
                    }
                    if (dataSnapshot.hasChild("scores")) {
                        HashMap<String, Integer> scores = new HashMap<>();
                        for (DataSnapshot score : dataSnapshot.child("scores").getChildren()) {
                            scores.put(score.getKey(), score.getValue(long.class).intValue());
                        }
                        a.setScores(scores);
                    }
                    if(dataSnapshot.hasChild("marker_radius"))
                        a.setMarkerRadius(dataSnapshot.child("marker_radius").getValue(long.class).intValue());
                    if (dataSnapshot.hasChild("country"))
                        a.setCountry((String) dataSnapshot.child("country").getValue());

                    if (dataSnapshot.hasChild("region"))
                        a.setRegion((String) dataSnapshot.child("region").getValue());

                    if (dataSnapshot.hasChild("city"))
                        a.setCity((String) dataSnapshot.child("city").getValue());

                    if (dataSnapshot.hasChild("user_rate"))
                        a.setUserRate((float) dataSnapshot.child("user_rate").getValue());

                    if (dataSnapshot.hasChild("questions")) {
                        Vector<String> questions = new Vector<>();
                        for (DataSnapshot dat : dataSnapshot.child("questions").getChildren()) {
                            questions.add((String) dat.getValue());
                        }
                        a.setQuestions(questions);
                    }
                    if (dataSnapshot.hasChild("reviews")) {
                        Vector<String> questions = new Vector<>();
                        for (DataSnapshot dat : dataSnapshot.child("reviews").getChildren()) {
                            questions.add((String) dat.getValue());
                        }
                        a.setReviews(questions);
                    }
                    if (dataSnapshot.hasChild("photos")) {
                        Vector<URI> photos = new Vector<>();
                        for (DataSnapshot dat : dataSnapshot.child("photos").getChildren()) {
                            String t = (String) dat.getValue();
                            photos.add(URI.create(t));
                            //TODO add photos
                        }
                        a.setPhotos(photos);
                    }
                    if(a.getQuestions()!=null) {
                        for (String id : a.getQuestions())
                            questions.downloadQuestion(id);
                    }
                    checkpointMap.put(dataSnapshot.getKey(), a);
                    vectorForWaitingCheckpoints.add(dataSnapshot.getKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private void changeCheckpoint(String id){
        String id1=id.replaceAll(",",".");
        String[] splitedId=id1.split("_");
        DatabaseReference data=FirebaseDatabase.getInstance().getReference("checkpoints");
        for(int i=0;i<splitedId.length-2;i++){
            data=data.child(splitedId[i]);
        }
        data=data.child(id);
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Checkpoint a = new Checkpoint();
                a.setId(dataSnapshot.getKey());
                String b = dataSnapshot.getKey();
                b = b.replaceAll(",", ".");
                String[] splitedId=b.split("_");
                a.setLocation(new LatLng(Double.valueOf(splitedId[splitedId.length-2]), Double.valueOf(splitedId[splitedId.length-1])));
                if (dataSnapshot.hasChild("number_of_online_users"))
                    a.setNumberOnlinePlayers((int) dataSnapshot.child("number_of_online_users").getValue());

                if (dataSnapshot.hasChild("number_played"))
                    a.setNumberPlayed((int) dataSnapshot.child("number_played").getValue());

                if (dataSnapshot.hasChild("difficulty_level"))
                    a.setDifficultyLevel(dataSnapshot.child("difficulty").getValue(long.class).intValue());

                if (dataSnapshot.hasChild("marker_uri")) {
                    a.setMarkerURI((String) dataSnapshot.child("marker_uri").getValue());
                    //TODO add marker with Picasso
                }

                if (dataSnapshot.hasChild("title")) {
                    Map<String, String> titles = new HashMap<>();
                    for (DataSnapshot dat : dataSnapshot.child("title").getChildren()) {
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
                if (dataSnapshot.hasChild("checkpoints_type")) {
                    Vector<String> types = new Vector<>();
                    for (DataSnapshot dat : dataSnapshot.child("checkpoints_type").getChildren()) {
                        types.add((String) dat.getValue());
                    }
                    a.setTypes(types);
                }

                if(dataSnapshot.hasChild("scores")){
                    HashMap<String,Integer> scores=new HashMap<>();
                    for (DataSnapshot score:dataSnapshot.child("scores").getChildren()) {
                        scores.put(score.getKey(),score.getValue(long.class).intValue());
                    }
                    a.setScores(scores);
                }

                if(dataSnapshot.hasChild("marker_radius")) {
                    a.setMarkerRadius(dataSnapshot.child("marker_radius").getValue(long.class).intValue());
                    checkpointMap.get(dataSnapshot.getKey()).setMarkerRadius(a.getMarkerRadius());
                }

                if (dataSnapshot.hasChild("country"))
                    a.setCountry((String) dataSnapshot.child("country").getValue());

                if (dataSnapshot.hasChild("region"))
                    a.setRegion((String) dataSnapshot.child("region").getValue());

                if (dataSnapshot.hasChild("city"))
                    a.setCity((String) dataSnapshot.child("city").getValue());

                if (dataSnapshot.hasChild("user_rate"))
                    a.setUserRate((float) dataSnapshot.child("user_rate").getValue());

                if (dataSnapshot.hasChild("questions")) {
                    Vector<String> questions = new Vector<>();
                    for (DataSnapshot dat : dataSnapshot.child("questions").getChildren()) {
                        questions.add((String) dat.getValue());
                    }
                    a.setQuestions(questions);
                }
                if (dataSnapshot.hasChild("reviews")) {
                    Vector<String> questions = new Vector<>();
                    for (DataSnapshot dat : dataSnapshot.child("reviews").getChildren()) {
                        questions.add((String) dat.getValue());
                    }
                    a.setReviews(questions);
                }
                if (dataSnapshot.hasChild("photos")) {
                    Vector<URI> photos = new Vector<>();
                    for (DataSnapshot dat : dataSnapshot.child("photos").getChildren()) {
                        String t = (String) dat.getValue();
                        photos.add(URI.create(t));
                        //TODO add photos
                    }
                    a.setPhotos(photos);
                }
                if(a.getQuestions()!=null) {
                    for (String id : a.getQuestions())
                        questions.downloadQuestion(id);
                }
                checkpointMap.get(dataSnapshot.getKey()).update(a);
                if(a.getQuestions().equals(checkpointMap.get(dataSnapshot.getKey()).getQuestions())){
                    VectorForChangedWaitingCheckpoints.add(dataSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
   private android.os.Handler handlerForWaiter=new android.os.Handler();
   private Runnable waiterForQuestionsToBeReady=new Runnable() {
        @Override
        public void run() {
            Vector<String> vectorForWaitingCheckpointsForRun= (Vector<String>) vectorForWaitingCheckpoints.clone();
            //Log.v(constants.getLogTag()+"vectorFor",vectorForWaitingCheckpointsForRun.toString());
            if(!vectorForWaitingCheckpointsForRun.isEmpty()){
              //  Log.v(constants.getLogTag(),vectorForWaitingCheckpointsForRun.toString());
                for(String id : vectorForWaitingCheckpointsForRun){
                    boolean t=true;
                    if(checkpointMap.get(id).getQuestions()!=null) {
                        Vector<String> questionsForRun= (Vector<String>) checkpointMap.get(id).getQuestions().clone();
                        for (String questionId : questionsForRun) {
                            if (questions.isQuestionReady(questionId) == 0) {
                                t = false;
                            }
                        }
                    }
                    if(t) {
                        if(!checkpointMap.get(id).isLoaded()) {
                            checkpointMap.get(id).addToMap(contextForResourses,thisModeIsActive);
                        }
                        vectorForWaitingCheckpoints.remove(id);
                    }
                }
            }
            if(!VectorForChangedWaitingCheckpoints.isEmpty()){

            }
            handlerForWaiter.postDelayed(waiterForQuestionsToBeReady,2000);
        }
    };



}
