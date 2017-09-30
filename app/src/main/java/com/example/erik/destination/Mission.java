package com.example.erik.destination;

import android.content.Context;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static com.example.erik.destination.MapsActivity.mMap;

/**
 * Created by Erik on 3/20/2017.
 */

public class Mission {
    private String Id;//
    private LatLng location;//
    private HashMap<Integer,String> checkpointsIDs;
    private Map<String,String> Title=new HashMap<>();//
    private Map<String,String> Description=new HashMap<>();//
    private Vector<Integer> Types;//
    private int timeMinutes;
    private URI MarkerURI;//
    private HashMap<String,HashMap<String,Boolean>> roads=new HashMap<String, HashMap<String, Boolean>>();
    private int difficultyLevel;//
    private int NumberPlayed;//
    private int NumberOnlinePlayers;//
    private int NumberCompleted;//
    private float userRate;//
    private Vector<String> reviews;//
    private boolean isLoaded;
    private Marker marker;
    private Constants constants=new Constants();
    private boolean isMissionShown=false;

    public void addToMap(Context context){
        marker= mMap.addMarker(new MarkerOptions().position(location).title(Id).snippet("mission"));
       // MapsActivity.missions.setTag(marker,Id);
        Log.v(constants.getLogTag(),"Mission added");
    }

    public void removeFromMap(){
        if(isLoaded) {
            mMap.removeMarker(marker);
            isMissionShown=false;
        }
        Log.v(constants.getLogTag(),"Mission removed");
    }


    public Marker getMarker() {
        return marker;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public HashMap<Integer, String> getCheckpointsIDs() {
        return checkpointsIDs;
    }

    public void setCheckpointsIDs(HashMap<Integer, String> checkpointsIDs) {
        this.checkpointsIDs = checkpointsIDs;
    }

    public Map<String, String> getTitle() {
        return Title;
    }

    public void setTitle(Map<String, String> title) {
        Title = title;
    }

    public Map<String, String> getDescription() {
        return Description;
    }

    public void setDescription(Map<String, String> description) {
        Description = description;
    }

    public Vector<Integer> getTypes() {
        return Types;
    }

    public void setTypes(Vector<Integer> types) {
        Types = types;
    }

    public int getTimeMinutes() {
        return timeMinutes;
    }

    public void setTimeMinutes(int timeMinutes) {
        this.timeMinutes = timeMinutes;
    }

    public URI getMarkerURI() {
        return MarkerURI;
    }

    public void setMarkerURI(String markerURI) {
        MarkerURI =URI.create(markerURI);
    }

    public HashMap<String, HashMap<String, Boolean>> getRoads() {
        return roads;
    }

    public void setRoads(HashMap<String, HashMap<String, Boolean>> roads) {
        this.roads = roads;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getNumberPlayed() {
        return NumberPlayed;
    }

    public void setNumberPlayed(int numberPlayed) {
        NumberPlayed = numberPlayed;
    }

    public int getNumberOnlinePlayers() {
        return NumberOnlinePlayers;
    }

    public void setNumberOnlinePlayers(int numberOnlinePlayers) {
        NumberOnlinePlayers = numberOnlinePlayers;
    }

    public int getNumberCompleted() {
        return NumberCompleted;
    }

    public void setNumberCompleted(int numberCompleted) {
        NumberCompleted = numberCompleted;
    }

    public float getUserRate() {
        return userRate;
    }

    public void setUserRate(float userRate) {
        this.userRate = userRate;
    }

    public Vector<String> getReviews() {
        return reviews;
    }

    public void setReviews(Vector<String> reviews) {
        this.reviews = reviews;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public boolean isMissionShown() {
        return isMissionShown;
    }

    public void setMissionShown(boolean missionShown) {
        isMissionShown = missionShown;
    }
}
