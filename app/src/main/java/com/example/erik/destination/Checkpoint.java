package com.example.erik.destination;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.example.erik.destination.MapsActivity.mMap;

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
    private ArrayList<String> Questions;//
    private List<String> Photos;//
    private List<String> Types;//
    private URI MarkerURI;//
    private int difficultyLevel;//
    private int NumberPlayed;//
    private int NumberOnlinePlayers;//
    private float userRate;//
    private List<String> reviews;//
    private HashMap<String,Integer> Scores=new HashMap<>();
    private boolean isLoaded;
    private Marker markerCheckpoints;
    private Marker markerMissionSelected;
    private Marker markerMissionNotSelected;
    private Constants constants=new Constants();
    private int markerRadius=constants.getDistance();
    private int minTime=0;


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
    public void addToMapForCheckpoints(){
        //TODO
        int width=(Checkpoints.contextForResources.getResources().getDisplayMetrics().widthPixels)/10;
        int height=width;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap markerIcon = Bitmap.createBitmap(width+2*constants.checkpointCirecleStroke, height+2*constants.checkpointCirecleStroke, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(markerIcon);
        Paint paint=new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(constants.checkpointCirecleStroke);
        canvas.drawCircle(markerIcon.getWidth()/2,markerIcon.getHeight()/2,width/2,paint);

        Paint textPaint = new Paint();
        textPaint.setARGB(200, 254, 0, 0);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(width/2);
        canvas.drawText(String.valueOf(Scores.get("score_type_1")),canvas.getWidth()/2-10,canvas.getHeight()/2+10,textPaint);



        IconFactory iconFactory = IconFactory.getInstance(Checkpoints.contextForResources);
        com.mapbox.mapboxsdk.annotations.Icon icon = iconFactory.fromBitmap(markerIcon);

        MarkerOptions markerOptions=new MarkerOptions().position(location).icon(icon);
        markerCheckpoints= mMap.addMarker(markerOptions);
        markerCheckpoints.setTitle(Id);
        markerCheckpoints.setSnippet("checkpoint");
        Checkpoints.setTag(markerCheckpoints,Id);
        isLoaded=true;
    }

    public void addToMapForMissionSelected(){
        //TODO
        int width=(Checkpoints.contextForResources.getResources().getDisplayMetrics().widthPixels)/10;
        int height=width;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap markerIcon = Bitmap.createBitmap(width+2*constants.checkpointCirecleStroke, height+2*constants.checkpointCirecleStroke, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(markerIcon);
        Paint paint=new Paint();
        paint.setColor(Color.argb(128,255,0,0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(constants.checkpointCirecleStroke);
        canvas.drawCircle(markerIcon.getWidth()/2,markerIcon.getHeight()/2,width/2,paint);

        Paint textPaint = new Paint();
        textPaint.setARGB(128, 254, 0, 0);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(width/2);
        canvas.drawText(String.valueOf(Scores.get("score_type_1")),canvas.getWidth()/2-10,canvas.getHeight()/2+10,textPaint);



        IconFactory iconFactory = IconFactory.getInstance(Checkpoints.contextForResources);
        com.mapbox.mapboxsdk.annotations.Icon icon = iconFactory.fromBitmap(markerIcon);

        MarkerOptions markerOptions=new MarkerOptions().position(location).icon(icon);
        markerMissionSelected= mMap.addMarker(markerOptions);
        markerMissionSelected.setTitle(Id);
        markerMissionSelected.setSnippet("checkpoint");
        Checkpoints.setTag(markerMissionSelected,Id);
        isLoaded=true;
    }
    public void addToMapForMissionNotSelected(){
        //TODO
        int width=(Checkpoints.contextForResources.getResources().getDisplayMetrics().widthPixels)/10;
        int height=width;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap markerIcon = Bitmap.createBitmap(width+2*constants.checkpointCirecleStroke, height+2*constants.checkpointCirecleStroke, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(markerIcon);
        Paint paint=new Paint();
        paint.setARGB(20,255,211,211);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(constants.checkpointCirecleStroke);
        canvas.drawCircle(markerIcon.getWidth()/2,markerIcon.getHeight()/2,width/2,paint);

        Paint textPaint = new Paint();
        textPaint.setARGB(20, 255,211,211);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(width/2);
        canvas.drawText(String.valueOf(Scores.get("score_type_1")),canvas.getWidth()/2-10,canvas.getHeight()/2+10,textPaint);



        IconFactory iconFactory = IconFactory.getInstance(Checkpoints.contextForResources);
        com.mapbox.mapboxsdk.annotations.Icon icon = iconFactory.fromBitmap(markerIcon);

        MarkerOptions markerOptions=new MarkerOptions().position(location).icon(icon);
        markerMissionNotSelected= mMap.addMarker(markerOptions);
        markerMissionNotSelected.setTitle(Id);
        markerMissionNotSelected.setSnippet("checkpoint");
        Checkpoints.setTag(markerMissionNotSelected,Id);
        isLoaded=true;
    }

    public void removeFromMap(){
        if(isLoaded) {
            markerCheckpoints.remove();
            stopPulsing();
            //circle.remove();
           // circle=null;
        }
    }
    public void removeFromMapNotSelected(){
        if(isLoaded & markerMissionNotSelected!=null) {
            markerMissionNotSelected.remove();
            stopPulsing();
            //circle.remove();
            // circle=null;
        }
    }
    public void removeFromMapSelected(){
        if(isLoaded && markerMissionSelected!=null) {
            markerMissionSelected.remove();
            stopPulsing();
            //circle.remove();
            // circle=null;
        }
    }

    public void update(Checkpoint a){
        if(a.Description!=null) {
            setDescription(a.Description);
        }
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
        setMarkerRadius(a.markerRadius);
    }
    //private ValueAnimator lastPulseAnimator;

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
        return mMap.addPolygon(new PolygonOptions().addAll(polygon).fillColor(Color.parseColor("#8cff0000")));
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

    public ArrayList<String> chooseQuestions(int count){
        Collections.shuffle(Questions);
        ArrayList<String> questionsOutput=new ArrayList<>();
        if(count>Questions.size()) {
            questionsOutput.addAll(Questions);
        }
        else{
            for(int i=0;i<count;i++){
                questionsOutput.add(Questions.get(i));
            }
        }
        return questionsOutput;
    }

    public Marker getMarker() {
        return markerCheckpoints;
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
        if(obj instanceof Checkpoint){
            Checkpoint a=(Checkpoint)obj;
            return a.Id.equals(Id);
        }
        return false;
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

    public void setLoaded(boolean t){
        isLoaded=t;
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

    public ArrayList<String> getQuestions() {
        return Questions;
    }

    public void setQuestions(ArrayList<String> questions) {
        Questions = questions;
        //Log.v(constants.getLogTag(),"aaaaaaa"+Id+"  "+questions.toString());

    }

    public List<String> getPhotos() {
        return Photos;
    }

    public void setPhotos(List<String> photos) {
        Photos = photos;
    }

    public List<String> getTypes() {
        return Types;
    }

    public void setTypes(List<String> types) {
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

    public List<String> getReviews() {
        return reviews;
    }

    public void setReviews(List<String> reviews) {
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
        if(markerRadius>constants.getDistance())
            this.markerRadius = markerRadius;
    }

    public int getMinTime() {
        return minTime;
    }

    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }
}
