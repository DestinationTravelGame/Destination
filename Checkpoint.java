package com.example.erik.destination;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Erik on 3/20/2017.
 */

public class Checkpoint {
    private String Id;//
    private String Country;//
    private String Region;//
    private String City;//
    private LatLng location;//
    private Map<String,String> Title=new HashMap<>();//
    private Map<String,String> Description=new HashMap<>();//
    private Vector<String> Questions;//
    private Vector<URI> Photos;//
    private Vector<String> Types;//
    private URI MarkerURI;//
    private int difficultyLevel;//
    private int NumberPlayed;//
    private int NumberOnlinePlayers;//
    private float userRate;//
    private Vector<String> reviews;//
    private HashMap<String,Integer> Scores=new HashMap<>();
    private boolean isLoaded;
    private MarkerView marker;
    private Constants constants=new Constants();
    private int markerRadius=constants.getDistance();


    private Polygon circle;
    ValueAnimator.AnimatorUpdateListener pulseTool=new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float animatedFraction = animation.getAnimatedFraction();

           // Log.e(constants.getLogTag(), " Animacia" + animatedFraction);
            circle.remove();
            circle=drawCircle(animatedFraction * 100,location);
        }
    };
    ValueAnimator pulse=new ValueAnimator();


    Checkpoint(){
    }
    public void addToMap(Context appContext,boolean checkpointModeOnOff){
        //TODO UserDownload
        Bitmap markerIcon= BitmapFactory.decodeResource(appContext.getResources(), R.drawable.icon_1);
        switch (Scores.get("score_type_1")){
            case 2:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_2);
                break;
            case 3:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_3);
                break;
            case 4:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_4);
                break;
            case 5:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_5);
                break;
            case 6:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_6);
                break;
            case 7:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_7);
                break;
            case 8:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_8);
                break;
            case 9:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_9);
                break;
            case 10:
                markerIcon=BitmapFactory.decodeResource(appContext.getResources(),R.drawable.icon_10);
                break;
        }

        int width=(appContext.getResources().getDisplayMetrics().widthPixels)/10;
        int height=(int)(((double)width/ (double) markerIcon.getWidth())*(double) markerIcon.getHeight());
        Log.v(constants.getLogTag(),width+"  "+height);
        markerIcon=Bitmap.createScaledBitmap(markerIcon,width,height,true);

        IconFactory iconFactory = IconFactory.getInstance(appContext);
        com.mapbox.mapboxsdk.annotations.Icon icon = iconFactory.fromBitmap(markerIcon);

        MarkerViewOptions markerOptions=new MarkerViewOptions().position(location).icon(icon).visible(checkpointModeOnOff);
        marker=MapsActivity.mMap.addMarker(markerOptions);
        marker.setTitle(Title.get("arm_title"));
        marker.setSnippet(Description.get("arm_desc"));
        Checkpoints.setTag(marker,Id);
        isLoaded=true;
    }
    public void removeFromMap(){
        if(isLoaded) {
            marker.remove();
            stopPulsing();
            //circle.remove();
           // circle=null;
        }
    }

    public void update(Checkpoint a){
        if(a.Description!=null)
        setDescription(a.Description);
        if(a.difficultyLevel!=-1)
        setDifficultyLevel(a.difficultyLevel);
        if(a.MarkerURI!=null)
        setMarkerURI(a.MarkerURI.toString());
        setNumberOnlinePlayers(a.NumberOnlinePlayers);
        if(a.Questions!=null)
        setQuestions(a.Questions);
        if(a.reviews!=null)
        setReviews(a.reviews);
        setNumberPlayed(a.NumberPlayed);
        if(a.Title!=null)
        setTitle(a.Title);
        setUserRate(a.userRate);
        setDifficultyLevel(a.difficultyLevel);
        if(a.Photos!=null)
        setPhotos(a.Photos);
        if(a.Types!=null)
        setTypes(a.Types);
    }
    public void start(){
        Log.v(constants.getLogTag(),"Checkpoint:"+Id+" started");
        isUserNear(true);
    }
    public void isUserNear(boolean t){

    }
    private ValueAnimator lastPulseAnimator;

    public void changeRadiusWithZoom(LatLng farRight,LatLng nearLeft){
       /* if(circle==null){
            if(pulse.isRunning()) {
                float[] distance=new float[1];
                Location.distanceBetween(farRight.getLatitude(), farRight.getLongitude(), nearLeft.getLatitude(), nearLeft.getLongitude(), distance);
                stopPulsing();
                pulse.setIntValues(0,(int)distance[0]/4);
                pulse.start();
            }
        }*/
    }
    public void startPulsing(){
       /* if(circle==null) {
            circle = drawCircle(15,location);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.setRepeatMode(ValueAnimator.RESTART);  /* PULSE */
           /* pulse.setIntValues(0, 200);
            pulse.setDuration(1000);
            pulse.setEvaluator(new IntEvaluator());
            pulse.setInterpolator(new AccelerateDecelerateInterpolator());
            pulse.addUpdateListener(pulseTool);
            pulse.start();
        }else
            if(!pulse.isRunning()) {
                circle=drawCircle(15,location);
                pulse.start();
            }*/
            //handlerForPulsing.post(pulse);

    }

   /* Handler handlerForPulsing=new Handler();
    private double infoSend=0;
    Runnable pulse=new Runnable() {
        @Override
        public void run() {
            if(circle!=null){
                circle.remove();
            }
            circle=drawCircle(infoSend*15,location);
            if(infoSend>1){
                infoSend=0;
            }else
                infoSend+=0.05;
            handlerForPulsing.postDelayed(pulse,100);
        }
    };*/
    public void stopPulsing(){
        /*if(circle!=null) {
           //pulse;
           // handlerForPulsing.removeCallbacks(pulse);
            circle.remove();
            circle=null;
        }*/

    }
    @NonNull
    private Polygon drawCircle(double radius, LatLng centre){
        List<LatLng> polygon=new ArrayList<>();
            /*double alfa=i*(360/Double.valueOf(constants.numberOfLinesOnPolygon));
            double x=0;
            double y;
            if(alfa>=0 && alfa<=180) {
                x = Math.sqrt(radius * radius - (radius - radius * (1 - Math.cos(Math.toRadians(alfa)))) * (radius - radius * (1 - Math.cos(Math.toRadians(alfa))))) + centre.getLatitude();
            }
            if(alfa>180 && alfa<360){
                x = -Math.sqrt(radius * radius - (radius - radius * (1 - Math.cos(Math.toRadians(alfa)))) * (radius - radius * (1 - Math.cos(Math.toRadians(alfa))))) + centre.getLatitude();
            }
            y=radius+centre.getLongitude()-radius*(1-Math.cos(Math.toRadians(alfa)));

        }*/

            double distRadians = radius / 6371000.0; // earth radius in meters
            double centerLatRadians = Math.toRadians(centre.getLatitude());
            double centerLonRadians = Math.toRadians(centre.getLongitude());
            for (int i = 0; i < constants.numberOfLinesOnPolygon; i++) {
                double degrees = i*(360/Double.valueOf(constants.numberOfLinesOnPolygon));
                double degreeRadians = Math.toRadians(degrees);
                double pointLatRadians = Math.asin( Math.sin(centerLatRadians) * Math.cos(distRadians) + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians));
                double pointLonRadians = centerLonRadians + Math.atan2( Math.sin(degreeRadians) * Math.sin(distRadians) * Math.cos(centerLatRadians),
                        Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians) );
                double pointLat =Math.toDegrees(pointLatRadians);
                double pointLon = Math.toDegrees(pointLonRadians);
                polygon.add(new LatLng(pointLat,pointLon));
            }
        return MapsActivity.mMap.addPolygon(new PolygonOptions().addAll(polygon).fillColor(Color.parseColor("#8cff0000")));
    }
    public String chooseSomeQuestion(){
        Random a=new Random();
        String[] questionArray=Questions.toArray(new String[0]);
        if(questionArray.length!=0)
        {
            return questionArray[a.nextInt(questionArray.length)];
        }
        return null;
    }
    public MarkerView getMarker() {
        return marker;
    }

    @Override
    public String toString() {
        return "Checkpoint:Id"+Id+Title+Description+location+Country+Region+City;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Checkpoint a=(Checkpoint)obj;
        return a.getLocation().equals(this.location);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Checkpoint a=new Checkpoint();
        a.setCity(City);
        a.setCountry(Country);
        a.setRegion(Region);
        a.setDescription(Description);
        a.setDifficultyLevel(difficultyLevel);
        a.setId(Id);
        a.setLocation(location);
        a.setMarkerURI(MarkerURI.toString());
        a.setNumberOnlinePlayers(NumberOnlinePlayers);
        a.setQuestions(Questions);
        a.setReviews(reviews);
        a.setNumberPlayed(NumberPlayed);
        a.setTitle(Title);
        a.setUserRate(userRate);
        a.setDifficultyLevel(difficultyLevel);
        a.setPhotos(Photos);
        a.setTypes(Types);
        return super.clone();
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
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

    public Vector<String> getQuestions() {
        return Questions;
    }

    public void setQuestions(Vector<String> questions) {
        Questions = questions;

    }

    public Vector<URI> getPhotos() {
        return Photos;
    }

    public void setPhotos(Vector<URI> photos) {
        Photos = photos;
    }

    public Vector<String> getTypes() {
        return Types;
    }

    public void setTypes(Vector<String> types) {
        Types = types;
    }

    public URI getMarkerURI() {
        return MarkerURI;
    }

    public void setMarkerURI(String markerURI) {
            MarkerURI =URI.create(markerURI);
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

    public HashMap<String, Integer> getScores() {
        return Scores;
    }

    public void setScores(HashMap<String, Integer> scores) {
        Scores = scores;
    }

    public int getMarkerRadius() {
        return markerRadius;
    }

    public void setMarkerRadius(int markerRadius) {
        this.markerRadius = markerRadius;
    }
}
