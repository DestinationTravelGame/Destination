package com.example.erik.destination;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import static com.example.erik.destination.Constants.distanceBetween;
import static com.example.erik.destination.Constants.distanceMission;
import static com.example.erik.destination.Constants.lastPointRadius;
import static com.example.erik.destination.MapsActivity.checkpoints;
import static com.example.erik.destination.MapsActivity.mMap;

/**
 * Created by Erik on 3/20/2017.
 */

public class Missions {
    private HashMap<String, Mission> missionsMap = new HashMap<>();
    private ArrayList<String> missionsId = new ArrayList<>();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private Constants constants = new Constants();
    private boolean thisModeIsActive = false;
    private ArrayList<String> listForWaitingMissions = new ArrayList<>();
    Context context;
    private boolean isStarted = false;
    public String SelectedMission;
    public String StartedMission;
    private Polyline selectedMissionPolyline;
    private HashMap<String, Polyline> missionsPolyline = new HashMap<>();

    public Mission getMissionById(String id) {
        return missionsMap.get(id);
    }


    Missions() {
    }

    public HashMap<String, Mission> getMissions() {
        return missionsMap;
    }

    public void start(Context context) {
        if (!isStarted) {
            database.keepSynced(true);
            this.context = context;
            isStarted = true;
            handlerForWaiter.postDelayed(waiterForUnreadyMissions, 1000);
            database.child("missions_id").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.v(constants.getLogTag(), "Mission Id added");
                    missionsId.add(dataSnapshot.getKey());
                    checkIfNewMissionInView(dataSnapshot.getKey());
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
    }

    public void updateMapForNewMissions() {
        checkWhichMissionsInView();
    }

    private void checkWhichMissionsInView() {
        if (isStarted) {
            if (!missionsId.isEmpty()) {
                for (String id : missionsId) {

                    if (missionsMap.get(id) == null) {
                        String id1 = id.replaceAll(",", ".");
                        String[] splitedId = id1.split("_");
                        if (mMap.getCameraPosition().zoom > constants.maxZoom && mMap.getProjection().getVisibleRegion().latLngBounds.contains(new LatLng(Double.valueOf(splitedId[splitedId.length - 2]), Double.valueOf(splitedId[splitedId.length - 1])))) {
                            downloadMission(id);
                        }
                    } else {
                        if (!mMap.getMarkers().contains(missionsMap.get(id).getMarker())) {
                            String id1 = id.replaceAll(",", ".");
                            String[] splitedId = id1.split("_");
                            if (mMap.getCameraPosition().zoom > constants.maxZoom && mMap.getProjection().getVisibleRegion().latLngBounds.contains(new LatLng(Double.valueOf(splitedId[splitedId.length - 2]), Double.valueOf(splitedId[splitedId.length - 1])))) {
                                if (missionsMap.get(id).isLoaded()) {
                                    if (thisModeIsActive)
                                        if (!id.equals(SelectedMission)) {
                                            missionsMap.get(id).addToMap(context);
                                            showThisMission(id);
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkIfNewMissionInView(String Id) {
        if (isStarted && thisModeIsActive) {
            String Id1 = Id.replaceAll(",", ".");
            String[] splitedId = Id1.split("_");
            if (mMap.getCameraPosition().zoom > constants.maxZoom && mMap.getProjection().getVisibleRegion().latLngBounds.contains(new LatLng(Double.valueOf(splitedId[splitedId.length - 2]), Double.valueOf(splitedId[splitedId.length - 1])))) {
                downloadMission(Id);
            }
        }
    }

    private void downloadMission(String Id) {
        if (!missionsMap.containsKey(Id)) {
            DatabaseReference data = FirebaseDatabase.getInstance().getReference("missions");
            String[] splitedId = Id.split("_");
            for (int i = 0; i < splitedId.length - 3; i++) {
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
                        HashMap<Integer, String> checkpoints = new HashMap<>();
                        for (DataSnapshot dat : dataSnapshot.child("checkpoints").getChildren()) {
                            checkpoints.put(Integer.valueOf(dat.getKey()), (String) dat.getValue());
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
                    a.setTimeMinutes(dataSnapshot.child("time").getValue(long.class).intValue());
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


    private android.os.Handler handlerForWaiter = new android.os.Handler();
    private Runnable waiterForUnreadyMissions = new Runnable() {
        @Override
        public void run() {
            ArrayList<String> listForWaitingMissionsClone = (ArrayList<String>) listForWaitingMissions.clone();
            if (!listForWaitingMissionsClone.isEmpty())
                for (String missionId : listForWaitingMissionsClone) {
                    boolean mijankyal = true;
                    for (String checkpointId : missionsMap.get(missionId).getCheckpointsIDs().values()) {
                        //checkpoints.downloadCheckpoint(checkpointId);
                        if (checkpoints.getCheckpoint(checkpointId) == null) {
                            checkpoints.downloadCheckpoint(checkpointId);
                            mijankyal &= false;
                        } else
                            mijankyal &= checkpoints.getCheckpointById(checkpointId).isLoaded();
                    }
                    if (mijankyal) {
                        missionsMap.get(missionId).setLoaded(true);
                        if (thisModeIsActive && !mMap.getMarkers().contains(missionsMap.get(missionId).getMarker())) {
                            missionsMap.get(missionId).addToMap(context);
                            showThisMission(missionId);
                        }
                        listForWaitingMissions.remove(missionId);
                    }
                }
            handlerForWaiter.postDelayed(waiterForUnreadyMissions, 2000);
        }
    };

    public void thisMissionSelected(String id) {
        SelectedMission = id;
        PolylineOptions lines = new PolylineOptions();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (String checkpointId : getMissionById(id).getCheckpointsIDs().values()) {
            checkpoints.getCheckpointById(checkpointId).removeFromMapNotSelected();
        }
        int numberOfCheckpoints = getMissionById(id).getCheckpointsIDs().size();

        for (int i = 0; i < numberOfCheckpoints; i++) {
            String checkpointId = getMissionById(id).getCheckpointsIDs().get(i);
            checkpoints.getCheckpointById(checkpointId).addToMapForMissionSelected();
            lines.add(checkpoints.getCheckpointById(checkpointId).getLocation());
            builder.include(checkpoints.getCheckpointById(checkpointId).getLocation());
        }
        lines.color(Color.argb(128, 255, 0, 0));
        lines.width(3);
        selectedMissionPolyline = mMap.addPolyline(lines);

        // builder.include(CurLoc.getPosition());
        LatLngBounds bounds = builder.build();
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.20); // offset from edges of the map 22% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
        getMissionById(SelectedMission).setMissionShown(false);
        if (missionsPolyline != null) {
            if (missionsPolyline.get(id) != null)
                missionsPolyline.get(id).remove();
        }

    }

    public void showThisMission(String id) {
        PolylineOptions lines = new PolylineOptions();
        if (!getMissionById(id).isMissionShown()) {
            getMissionById(id).setMissionShown(true);
            int numberOfCheckpoints = getMissionById(id).getCheckpointsIDs().size();
            for (int i = 0; i < numberOfCheckpoints; i++) {
                String checkpointId = getMissionById(id).getCheckpointsIDs().get(i);
                if (i != 0) {
                    checkpoints.getCheckpointById(checkpointId).addToMapForMissionNotSelected();
                }
                lines.add(checkpoints.getCheckpointById(checkpointId).getLocation());
            }
            lines.color(Color.rgb(255, 211, 211));
            lines.alpha((float) 0.2);
            lines.width(3);
            missionsPolyline.put(id, mMap.addPolyline(lines));
        }

    }

    public void deselectMission() {
        if (SelectedMission != null) {
            for (String checkpointId : missionsMap.get(SelectedMission).getCheckpointsIDs().values()) {
                checkpoints.getCheckpointById(checkpointId).removeFromMapSelected();
            }
            selectedMissionPolyline.remove();
            showThisMission(SelectedMission);
            SelectedMission = null;
        }
    }

    public void startThisMode() {
        thisModeIsActive = true;
        missionsPolyline = new HashMap<>();
        checkWhichMissionsInView();
    }

    public void stopThisMode() {
        thisModeIsActive = false;

        deselectMission();
        missionsPolyline = null;
        if (!missionsMap.isEmpty()) {
            for (String a : missionsMap.keySet()) {
                missionsMap.get(a).setMissionShown(false);
            }
        }
    }

    public LatLng missionStared(String id) {
        StartedMission = id;
        for(int i=0;i<100;i++)
            mMap.addMarker(new MarkerOptions().position(getRandomLocation(missionsMap.get(id).getLocation(), lastPointRadius)));
        return chooseRndPointInRange(missionsMap.get(id).getLocation(), lastPointRadius);

    }

    public void removeStaticVariables() {
        waiterForUnreadyMissions = null;
    }

    private LatLng chooseRndPointInRange(LatLng location, double radius) {
        double y0 = location.getLatitude();
        double x0 = location.getLongitude();
        double rd = radius / 111300; //about 111300 meters in one degree

        double u = Math.random();
        double v = Math.random();

        double w = rd * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        //Adjust the x-coordinate for the shrinking of the east-west distances
        double xp = x / Math.cos(y0);

        double newlat = y + y0;
        double newlon = x + x0;

        return new LatLng(newlat, newlon);
    }

    private LatLng getRandomLocation(LatLng point, int radius) {

        double x0 = point.getLatitude();
        double y0 = point.getLongitude();

        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(y0);

        double foundLatitude = new_x + x0;
        double foundLongitude = y + y0;
        return new LatLng(foundLatitude, foundLongitude);
    }
    public  Constants.Pair<String,Double> isNearToAnyMission(LatLng currentLocation){
        Constants.Pair<String,Double> returning = null;
        if(!missionsMap.isEmpty() && currentLocation!=null){
            for(Mission a:missionsMap.values()){
                if(a.getLocation()!=null) {
                    double distance=distanceBetween(a.getLocation(), currentLocation);
                    if (distance<distanceMission) {//TODO firebaseum avelacnel marker radius
                        if(returning==null){
                            returning=new Constants.Pair<>(a.getId(),distance);
                        }else if(returning.second>distance)
                            returning=new Constants.Pair<>(a.getId(),distance);
                    }
                }
            }
        }
        return returning;
    }
}
