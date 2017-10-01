package com.example.erik.destination;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.erik.destination.Question.MultipleQuestionWithMoreTrue;
import com.example.erik.destination.Question.MultipleQuestionWithOneTrue;
import com.example.erik.destination.Question.SelectDifference;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.example.erik.destination.Checkpoints.isNearCheckpoint;
import static com.example.erik.destination.Constants.checkpointMode;
import static com.example.erik.destination.Constants.missionMode;
import static com.example.erik.destination.Constants.missionStartedMode;
import static com.example.erik.destination.Constants.secondsBeforeQuestion;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback{


    public static MapboxMap mMap;
    private MapView mapFragment;
    private ImageView personPhoto;
    public static TextView userScoreType1;//
    private RelativeLayout userProfileLayout;
    private Button passCheckpoint;
    private Button startMission;
    private Button signOut;
    private ImageView selectDifferenceRing;
    private LinearLayout questionSelectDifference;
    public static Checkpoints checkpoints;//
    public static Missions missions;//
    private User user = new User();
    private Marker curLocMarker;
    GoogleApiClient mGoogleApiClient;
    Constants constants = new Constants();
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    LatLng CurrentLocation;
    DatabaseReference userDatabase;

    TextView questionMultipleChoiceOneTrue;
    RadioButton boxAnswer1MultipleChoiceOneTrue;
    RadioGroup answersMultipleChoiceOneTrue;
    RelativeLayout questionsMultipleChoiceOneTrue;


    private float startingPointerX;//for red circle
    private float startingPointery;//for red circle
    private float startingViewX;//for red circle
    private float startingViewY;//for red circle



    private int whichModeIsActive=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
                /*
                Logo(Mec poqr)
                wireframe(screens) transition
                gunayin gamman
                checkpoint icon+profile icons
                types icons
                billboard
                sticker
                facebook
                */
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goToLogin();
        } else {
            checkpoints = new Checkpoints();
            missions=new Missions();
            Mapbox.getInstance(this, "pk.eyJ1IjoiZGVzdGluYXRpb250cmF2ZWxnYW1lIiwiYSI6ImNqMWVvM2pyZjAwMDQyd3Bpdm1oaG1iaHMifQ.9BRL6-apqVp2_GfXMCK59g");

            setContentView(R.layout.activity_maps);
            questionSelectDifference = (LinearLayout) findViewById(R.id.questions_select_difference);
            personPhoto = (ImageView) findViewById(R.id.personPhoto);
            userProfileLayout = (RelativeLayout) findViewById(R.id.userProfileLayout);
            passCheckpoint = (Button) findViewById(R.id.passCheckpoint);
            questionMultipleChoiceOneTrue = (TextView) findViewById(R.id.question_multiple_choice_one_true);
            boxAnswer1MultipleChoiceOneTrue = (RadioButton) findViewById(R.id.boxAnswer1_multiple_choice_one_true);
            answersMultipleChoiceOneTrue = (RadioGroup) findViewById(R.id.answers_multiple_choice_one_true);
            questionsMultipleChoiceOneTrue = (RelativeLayout) findViewById(R.id.questions_multiple_choice_one_true);
            userScoreType1 = (TextView) findViewById(R.id.score_type_1);

            startMission=(Button)findViewById(R.id.startMission);
            startMission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startMode(missionStartedMode,(String)startMission.getTag());
                    startMission.setTag(null);

                }
            });
            userDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            passCheckpoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Constants.Pair<String, Float> nearCheckpointId = (Constants.Pair<String, Float>) passCheckpoint.getTag();
                    if(nearCheckpointId!=null){
                        user.setCheckpointDone(nearCheckpointId.first);
                        user.addScore((TextView)findViewById(R.id.score_type_1),"score_type_1",checkpoints.getCheckpointById(nearCheckpointId.first).getScores().get("score_type_1"));
                        passCheckpoint.setTag(null);
                        passCheckpoint.setVisibility(View.GONE);
                    }
                }
            });

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            userProfileLayout.getLayoutParams().height = (int) (size.y * 0.75);
            userProfileLayout.getLayoutParams().width = (int) (size.x * 0.75);
            //  userProfileLayout.setAlpha(0.0F);
            personPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userProfileLayout.getVisibility() == View.GONE) {
                        userProfileLayout.setVisibility(View.VISIBLE);
                        //Animation slide_down= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
                        //Animation slide_up= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up);
                        //userProfileLayout.startAnimation(slide_down);
                    } else {
                        userProfileLayout.setVisibility(View.GONE);
                        //Animation slide_down= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
                        //Animation slide_up= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up);
                        //userProfileLayout.startAnimation(slide_down);
                    }
                }
            });

            userProfileLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });

            signOut = (Button) findViewById(R.id.signOut);
            signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO finish everything
                    goToLogin();
                }
            });
            selectDifferenceRing = (ImageView) findViewById(R.id.selectDifferenceRing);
            selectDifferenceRing.setOnTouchListener(new View.OnTouchListener() {
                                                        @Override
                                                        public boolean onTouch(View view, MotionEvent motionEvent) {
                                                            switch (motionEvent.getActionMasked()) {
                                                                case MotionEvent.ACTION_DOWN:
                                                                    startingViewX = view.getX();
                                                                    startingViewY = view.getY();
                                                                    startingPointerX = motionEvent.getRawX();
                                                                    startingPointery = motionEvent.getRawY();
                                                                    break;
                                                                case MotionEvent.ACTION_MOVE:
                                                                    float pointerX = motionEvent.getRawX();
                                                                    float pointerY = motionEvent.getRawY();

                                                                    float dx = pointerX - startingPointerX;
                                                                    float dy = pointerY - startingPointery;

                                                                    float viewX = startingViewX + dx;
                                                                    float viewY = startingViewY + dy;
                                                                    view.setX(viewX);
                                                                    view.setY(viewY);
                                                                    break;
                                                            }
                                                            return true;
                                                        }
                                                    }
            );

            if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {//user photo download and set
                Picasso.Builder builder = new Picasso.Builder(this);                //
                builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));  //
                Picasso build = builder.build();                                    //
                //
                build.setIndicatorsEnabled(false);                                  //
                build.setLoggingEnabled(true);
                personPhoto.buildDrawingCache(true);
                try {                                                               //
                    Picasso.setSingletonInstance(build);                            //
                } catch (IllegalStateException e) {                                 //
                }                                                                   //
                //
                Picasso.with(MapsActivity.this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(personPhoto, new Callback() {
                    @Override                                                       //
                    public void onSuccess() {
                        Drawable draw = personPhoto.getDrawable();
                        if (draw instanceof BitmapDrawable) {
                            personPhoto.setImageBitmap(constants.getCircleCroppedBitmap(((BitmapDrawable) draw).getBitmap()));
                            ((ImageView) findViewById(R.id.personPhotoInLayout)).setImageBitmap(constants.getCircleCroppedBitmap(((BitmapDrawable) draw).getBitmap()));
                        } else {
                            Bitmap bit = Bitmap.createBitmap(draw.getIntrinsicWidth(), draw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bit);
                            draw.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                            draw.draw(canvas);
                            personPhoto.setImageBitmap(constants.getCircleCroppedBitmap(bit));
                            ((ImageView) findViewById(R.id.personPhotoInLayout)).setImageBitmap(constants.getCircleCroppedBitmap(bit));
                        }
                    }                                                               //

                    @Override
                    public void onError() {
                        Picasso.with(MapsActivity.this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(personPhoto, new Callback() {
                            @Override
                            public void onSuccess() {
                                Drawable draw = personPhoto.getDrawable();
                                if (draw instanceof BitmapDrawable) {
                                    personPhoto.setImageBitmap(constants.getCircleCroppedBitmap(((BitmapDrawable) draw).getBitmap()));
                                    ((ImageView) findViewById(R.id.personPhotoInLayout)).setImageBitmap(constants.getCircleCroppedBitmap(((BitmapDrawable) draw).getBitmap()));
                                } else {
                                    Bitmap bit = Bitmap.createBitmap(draw.getIntrinsicWidth(), draw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(bit);
                                    draw.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                    draw.draw(canvas);
                                    personPhoto.setImageBitmap(constants.getCircleCroppedBitmap(bit));
                                    ((ImageView) findViewById(R.id.personPhotoInLayout)).setImageBitmap(constants.getCircleCroppedBitmap(bit));
                                }
                            }

                            @Override
                            public void onError() {

                            }
                        });

                    }
                });                                                                 //

            }
            // setContentView(R.layout.activity_maps);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mapFragment = (MapView) findViewById(R.id.map);
            mapFragment.onCreate(savedInstanceState);
            mapFragment.getMapAsync(this);

        }
    }



    protected synchronized void buildGoogleApiClient() {
        Log.i(constants.getLogTag(), "buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    protected void createLocationRequest() {
        Log.i(constants.getLogTag(), "createLocationRequest");
        mGoogleApiClient.connect();
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(constants.getLogTag(), "onLocationChanged");
        mCurrentLocation = location;

        UpdateCurrentLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(constants.getLogTag(), "onConnected");
        if (mCurrentLocation == null) {
            if (getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{ACCESS_FINE_LOCATION},0);
                }
                Log.i(constants.getLogTag(), "Permission test failed");
                //return;
            }else {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    public void UpdateCurrentLocation() {
        if (CurrentLocation == null) {
            curLocMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(constants.getMyLocationTitleString()).snippet(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocMarker.getPosition(), 20));
        }
        CurrentLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        curLocMarker.setPosition(CurrentLocation);
        if(whichModeIsActive==checkpointMode)
            checkpointNear();
        if(whichModeIsActive==missionMode)
            missionNear();
        if(whichModeIsActive==missionStartedMode) {
            //TODO startedMission();
        }
    }




    @Override
    public void onConnectionSuspended(int i) {
        Log.i(constants.getLogTag(), "onConnectedSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(constants.getLogTag(), "onConnectionFailed");
    }

    VisibleRegion mapView;

    @Override
    public void onMapReady(MapboxMap Map) {
        buildGoogleApiClient();
        mMap = Map;
        user.start();
        runStart.run();
        mMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                                          @Override
                                          public boolean onMarkerClick(@NonNull Marker marker) {
                                              if(marker.equals(curLocMarker)){
                                                  marker.showInfoWindow(mMap,mapFragment);
                                                  return false;
                                              }
                                              else{
                                                  if(marker.getSnippet().equals("checkpoint")){
                                                      showCheckpointProperties(marker.getTitle(),checkpoints.getCheckpointById(marker.getTitle()).getTitle().get("arm_title"),checkpoints.getCheckpointById(marker.getTitle()).getDescription().get("arm_desc"),checkpoints.getCheckpointById(marker.getTitle()).getPhotos());
                                                      return true;
                                                  }
                                                  if(marker.getSnippet().equals("mission")){
                                                      if(missions.SelectedMission!=null) {
                                                          missions.deselectMission();
                                                          startMission.setTag(null);
                                                          startMission.setVisibility(View.GONE);
                                                      }
                                                      marker.remove();
                                                      missions.thisMissionSelected(marker.getTitle());
                                                      startMission.setTag(marker.getTitle());
                                                      startMission.setVisibility(View.VISIBLE);
                                                      return true;
                                                  }
                                                  return true;
                                              }
                                          }
                                      });
                mMap.setAllowConcurrentMultipleOpenInfoWindows(false);
        mapView = mMap.getProjection().getVisibleRegion();
        mMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if(mMap!=null) {
                    LatLng farRight = mMap.getProjection().getVisibleRegion().farRight;
                    LatLng nearLeft = mMap.getProjection().getVisibleRegion().nearLeft;
                    if (((farRight.getLatitude() - mapView.farRight.getLatitude()) > 0.0000000001 || (farRight.getLongitude() - mapView.farRight.getLongitude()) > 0.0000000001 || (nearLeft.getLatitude() - mapView.nearLeft.getLatitude()) > 0.0000000001 || (nearLeft.getLongitude() - mapView.nearLeft.getLongitude()) > 0.0000000001)) {
                        // Log.v(constants.getLogTag(), String.valueOf(mMap.getProjection().getVisibleRegion().toString()));
                        if (whichModeIsActive == checkpointMode) {
                            checkpoints.updateMapForNewCheckpoints();
                        }
                        if (whichModeIsActive == missionMode) {
                            missions.updateMapForNewMissions();
                        }
                        mapView = mMap.getProjection().getVisibleRegion();
                    }
                }
            }
        });
        mMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
               /* for (Marker a : mMap.getMarkers())
                    a.hideInfoWindow();*/
               if(missions.SelectedMission!=null)
                   missions.getMissionById(missions.SelectedMission).addToMap(getApplicationContext());
               missions.deselectMission();
                startMission.setTag(null);
                startMission.setVisibility(View.GONE);
               findViewById(R.id.checkpointView).setVisibility(View.GONE);
                curLocMarker.hideInfoWindow();
            }
        });
    }

    Handler handlerForRunnables=new Handler();

    Runnable runStart = new Runnable() {
        @Override
        public void run() {
            if (CurrentLocation == null || !user.isLoaded())
                handlerForRunnables.postDelayed(runStart, 500);
            if (user.getScores().get("score_type_1") != null)
                userScoreType1.setText(user.getScores().get("score_type_1").toString());
            ((TextView) findViewById(R.id.nameSurname)).setText(user.getName());
            //TODO es verevin@ ankap a
            checkpoints.start(getApplicationContext());
            missions.start(getApplicationContext());
            startMode(checkpointMode);
           // passCheckpointQuest.postDelayed(checkpointReach, 13);//vor karoxanam knopki het kayfer anem
        }

    };
    void startMode(int mode){
        whichModeIsActive=mode;
        if(mode== checkpointMode){
            missions.stopThisMode();
            mMap.clear();
            if(mCurrentLocation!=null)
            curLocMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(constants.getMyLocationTitleString()).snippet(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
            checkpoints.startThisMode();
            //passCheckpointQuest.postDelayed(checkpointReach,20);
            findViewById(R.id.checkpointView).setVisibility(View.GONE);

        }
        if(mode== missionMode){
            passCheckpoint.setVisibility(View.GONE);
            checkpoints.stopThisMode();
            mMap.clear();
            if(mCurrentLocation!=null)
            curLocMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(constants.getMyLocationTitleString()).snippet(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
            missions.startThisMode();
            findViewById(R.id.checkpointView).setVisibility(View.GONE);
        }
        if(mode== missionStartedMode){

        }
        findViewById(R.id.passCheckpoint).setTag(null);//
        findViewById(R.id.passCheckpoint).setVisibility(View.GONE);//
        findViewById(R.id.checkpointTimeProgress).setVisibility(View.GONE);//
        ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).setProgress(0);//
        findViewById(R.id.checkpointTimeProgress).setTag(null);// to work the checkpoint mode after changing mode

        findViewById(R.id.passCheckpoint).setVisibility(View.GONE);
        findViewById(R.id.startMission).setVisibility(View.GONE);

    }
    private void startMode(int mode, String id) {
        if(mode==missionStartedMode){
            startMission.setVisibility(View.GONE);
            if(whichModeIsActive==missionMode)
                whichModeIsActive=missionStartedMode;//TODO porcelu hamar
            user.startMission(id,missions.missionStared(id));
        }
    }
    private void checkpointNear(){
        if(whichModeIsActive== checkpointMode) {
            Constants.Pair<String, Float> curr = checkpoints.isNearToAnyCheckpoint(CurrentLocation);
            if (curr == null) {
                passCheckpoint.setVisibility(View.GONE);
            }
            if (passCheckpoint.getTag() == null) {
                if (curr != null) {
                    if (checkpoints.checkpointMap.get(curr.first).isLoaded() && !user.ifCheckpointDone(checkpoints.checkpointMap.get(curr.first).getId())) {
                        //passCheckpoint.setVisibility(View.VISIBLE);
                        passCheckpoint.setTag(curr);
                        checkpoints.checkpointMap.get(((Constants.Pair<String, Float>) passCheckpoint.getTag()).first).startPulsing();
                        findViewById(R.id.checkpointTimeProgress).setVisibility(View.VISIBLE);
                        if(checkpoints.checkpointMap.get(((Constants.Pair<String, Float>) passCheckpoint.getTag()).first).getMinTime()!=0){
                            ((ProgressBar)findViewById(R.id.checkpointTimeProgress)).setMax(checkpoints.checkpointMap.get(((Constants.Pair<String, Float>) passCheckpoint.getTag()).first).getMinTime());
                        }else{
                            ((ProgressBar)findViewById(R.id.checkpointTimeProgress)).setMax(constants.minTimeToStayInCheckpoint);
                        }
                        if(user.getCurrentCheckpoint(curr.first)==null)
                            user.setCurrentCheckpoint(curr.first, ((ProgressBar)findViewById(R.id.checkpointTimeProgress)).getMax());
                        ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).setProgress( ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getMax()-user.getCurrentCheckpoint((((Constants.Pair<String, Float>) passCheckpoint.getTag()).first)));
                        findViewById(R.id.checkpointTimeProgress).postDelayed(checkpointTimeCheck,1000);

                    }
                }
                //passCheckpointQuest.postDelayed(checkpointReach, 500);
            }
        }
    }
    Runnable checkpointTimeCheck= new Runnable() {
        @Override
        public void run() {
            if (whichModeIsActive == checkpointMode) {
                if((Constants.Pair<String, Float>) passCheckpoint.getTag()!=null){
                if (isNearCheckpoint(((Constants.Pair<String, Float>) passCheckpoint.getTag()).first, curLocMarker.getPosition())) {
                    if (((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getProgress() < ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getMax()) {
                        findViewById(R.id.checkpointTimeProgress).postDelayed(this, 1000);
                        ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).setProgress( ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getMax()-user.getCurrentCheckpoint((((Constants.Pair<String, Float>) passCheckpoint.getTag()).first)) + 1);
                        user.setCurrentCheckpoint((((Constants.Pair<String, Float>) passCheckpoint.getTag()).first),((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getMax()-((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getProgress());

                    } else {
                        findViewById(R.id.passCheckpoint).setVisibility(View.VISIBLE);
                        findViewById(R.id.checkpointTimeProgress).setVisibility(View.GONE);
                        ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).setProgress(0);
                        user.setCurrentCheckpoint((((Constants.Pair<String, Float>) passCheckpoint.getTag()).first),0);
                    }
                }
                }

            }
            if(whichModeIsActive==missionStartedMode){
                if (isNearCheckpoint((String) findViewById(R.id.checkpointTimeProgress).getTag(), curLocMarker.getPosition())) {
                    if (((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getProgress() < ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getMax()) {
                        findViewById(R.id.checkpointTimeProgress).postDelayed(this, 1000);
                        ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).setProgress(((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getMax() - user.getCurrentCheckpoint((String) findViewById(R.id.checkpointTimeProgress).getTag()) + 1);
                        user.setCurrentCheckpoint(((String) findViewById(R.id.checkpointTimeProgress).getTag()), ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getMax() - ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).getProgress());

                    } else {
                        findViewById(R.id.passCheckpoint).setVisibility(View.GONE);
                        findViewById(R.id.checkpointTimeProgress).setVisibility(View.GONE);
                        findViewById(R.id.checkpointTimeProgress).setTag(null);
                        ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).setProgress(0);
                        user.setCheckpointDone((String) findViewById(R.id.checkpointTimeProgress).getTag());
                        user.addScore((TextView) findViewById(R.id.score_type_1), "score_type_1", checkpoints.checkpointMap.get((String) findViewById(R.id.checkpointTimeProgress).getTag()).getScores().get("score_type_1"));
                    }
                }else{
                    //TODO notify user that will not get point for this checkpoint
                }
            }/* else{//todo think about this
                findViewById(R.id.passCheckpoint).setTag(null);
                findViewById(R.id.passCheckpoint).setVisibility(View.GONE);
                findViewById(R.id.checkpointTimeProgress).setVisibility(View.GONE);
                ((ProgressBar) findViewById(R.id.checkpointTimeProgress)).setProgress(0);
                findViewById(R.id.checkpointTimeProgress).setTag(null);
            }*/
        }
    };
   /* private void startedMission() {
        String currentCheckpointId=user.currentMission.getCurrentCheckpoint();
        startedMissionCheckpointNear(currentCheckpointId);
        if(isNearCheckpoint(currentCheckpointId,curLocMarker.getPosition()) && !user.currentMission.isCurrentCheckpointDoing()){
            showCheckpointQuestion(currentCheckpointId);
            startQuestionTime();
        }
    }

    int startedQuestionFinishTime;
    int
    Runnable startQuestionTime =new Runnable() {

        int finishTime;
        @Override
        public void run() {
            findViewById(R.id.question_timer)
        }
    };
    private void startQuestionTime() {
        startedQuestionFinishTime= SystemClock.elapsedRealtime()+
    }

    private void startedMissionCheckpointNear(String id){
        if(id!=null){
            if(whichModeIsActive==missionStartedMode){
                if(!user.ifCheckpointDone(id)) {
                    Checkpoint checkpoint = checkpoints.getCheckpointById(id);
                    if (isNearCheckpoint(id, curLocMarker.getPosition())) {
                        findViewById(R.id.checkpointTimeProgress).setTag(id);
                        checkpointTimeCheck.run();
                    }
                }
            }
        }
    }


    private void startMissionCheckpoint(String id){

    }

    Runnable startQuestionTime= new Runnable() {
        @Override
        public void run() {

        }
    };

    private void missionNear() {

    }
*/
   private void missionNear() {
       Constants.Pair<String,Double> missionPair=missions.isNearToAnyMission(curLocMarker.getPosition());
       if(missionPair!=null){
           //ToDo Say that you are near to a mission ?start?
       }

   }
    private void goToLogin() {
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        whichModeIsActive=-1;
        if(mMap!=null)
            mMap.clear();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        if(missions!=null) {
            missions.removeStaticVariables();
            missions = null;
        }else
            missions=null;
        if(checkpoints!=null){
            checkpoints.removeStaticVariables();
            checkpoints=null;
        }else
            checkpoints=null;
        Intent loginIntent = new Intent(this, SignInActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
        mMap=null;
        MapsActivity.this.finish();
    }


    Target targetForSellectDifference;
    void showCheckpointQuestion(final String id) {
        findViewById(R.id.before_question_textview).setVisibility(View.VISIBLE);
        for(int i=secondsBeforeQuestion ;i>0;i--){
            try {
                ((TextView)findViewById(R.id.before_question_textview)).setText(i);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        findViewById(R.id.before_question_textview).setVisibility(View.GONE);
        String selectedQuestionId = checkpoints.checkpointMap.get(id).chooseSomeQuestion();
        //if(checkpoints.questions.getQuestion(selectedQuestionId) instanceof MultipleQuestionWithOneTrue){
        if (checkpoints.questions.getQuestion(selectedQuestionId) instanceof MultipleQuestionWithOneTrue) {
            MultipleQuestionWithOneTrue question = (MultipleQuestionWithOneTrue) checkpoints.questions.getQuestion(selectedQuestionId);
            String[] answers = question.chooseRandomMixedAnsweres("arm");
            if (answers != null) {
                int Rid = R.id.boxAnswer2_multiple_choice_one_true;
                for (int i = 0; i < answers.length - 1; i++) {
                    RadioButton temp = new RadioButton(this);
                    temp.setId(Rid);
                    temp.setLayoutParams(boxAnswer1MultipleChoiceOneTrue.getLayoutParams());
                    temp.setText(answers[i]);
                    answersMultipleChoiceOneTrue.addView(temp);
                    ++Rid;
                }
                boxAnswer1MultipleChoiceOneTrue.setText(answers[answers.length - 1]);
//                radioGroup.addView(boxAnswer1MultipleChoiceOneTrue);
                questionMultipleChoiceOneTrue.setText(question.getQuestionText().get("arm"));
            }
            answersMultipleChoiceOneTrue.invalidate();
            questionsMultipleChoiceOneTrue.setVisibility(View.VISIBLE);

            questionsMultipleChoiceOneTrue.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
            questionsMultipleChoiceOneTrue.setTag(new Constants.Pair<>(id, selectedQuestionId));
        }
        if(checkpoints.questions.getQuestion(selectedQuestionId) instanceof SelectDifference){
            final SelectDifference question= (SelectDifference) checkpoints.questions.getQuestion(selectedQuestionId);
            targetForSellectDifference=new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap1, Picasso.LoadedFrom from) {
                    final Bitmap bitmap=bitmap1;
                    findViewById(R.id.questions_select_difference).setVisibility(View.VISIBLE);
                    ((ImageView)findViewById(R.id.selectDifferenceImage)).setImageBitmap(bitmap);
                    Point a=new Point();
                    int[] positionOfImage=new int[2];
                    findViewById(R.id.selectDifferenceImage).getLocationOnScreen(positionOfImage);
                    getWindowManager().getDefaultDisplay().getSize(a);
                    int imageWidth=a.x-findViewById(R.id.questions_select_difference).getPaddingTop();
                    ViewGroup.LayoutParams params1=findViewById(R.id.selectDifferenceImage).getLayoutParams();
                    params1.width=imageWidth;
                    params1.height= (int) (((double)imageWidth/bitmap.getWidth())*bitmap.getHeight());
                    findViewById(R.id.selectDifferenceImage).setLayoutParams(params1);
                    question.setRealRingSize((int) (question.getRingSizeInPercent()*imageWidth/100));
                    question.setTrueAnswerForThisScreen((int) (imageWidth*question.getTrueAnswer().first/100), (int) (imageWidth*question.getTrueAnswer().second/100));
                    ViewGroup.LayoutParams params=findViewById(R.id.selectDifferenceRing).getLayoutParams();
                    params.width=question.getRealRingSize();
                    params.height=question.getRealRingSize();
                    findViewById(R.id.selectDifferenceRing).setLayoutParams(params);
                    findViewById(R.id.questions_select_difference).setTag(new Constants.Pair<>(id,question));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            Picasso.with(getApplicationContext()).load(question.getURl()).memoryPolicy(MemoryPolicy.NO_CACHE).into(targetForSellectDifference);
        }
        if(checkpoints.questions.getQuestion(selectedQuestionId) instanceof MultipleQuestionWithMoreTrue){
            MultipleQuestionWithMoreTrue question= (MultipleQuestionWithMoreTrue) checkpoints.questions.getQuestion(selectedQuestionId);
            ((TextView)findViewById(R.id.question_multiple_choice_more_true)).setText(question.getQuestionText().get("arm"));
            String[] mixed=question.chooseRandomMixedAnsweres("arm");
            int Rid = R.id.checkBox0_multiple_choice_more_true;
            ((CheckBox)findViewById(Rid)).setText(mixed[0]);
            Rid++;
            for (int i = 1; i < mixed.length; i++) {
                String aMixed = mixed[i];
                CheckBox temp = new CheckBox(this);
                temp.setId(Rid);
                temp.setLayoutParams(findViewById(R.id.checkBox0_multiple_choice_more_true).getLayoutParams());
                temp.setText(aMixed);
                ((LinearLayout) findViewById(R.id.answers_multiple_choice_more_true)).addView(temp);
                ++Rid;
            }
            findViewById(R.id.questions_multiple_choice_more_true).setTag(new Constants.Pair<>(id,question));
            findViewById(R.id.questions_multiple_choice_more_true).setVisibility(View.VISIBLE);
        }
    }

    public void submitAnswerForMultipleChoiceOneTrue(View view) {
        Constants.Pair<String, String> idAndQuestionId = (Constants.Pair<String, String>) questionsMultipleChoiceOneTrue.getTag();
        if (answersMultipleChoiceOneTrue.getCheckedRadioButtonId() != -1) {
            RadioButton selected = (RadioButton) findViewById(answersMultipleChoiceOneTrue.getCheckedRadioButtonId());
            answersMultipleChoiceOneTrue.clearCheck();
            if (((MultipleQuestionWithOneTrue) checkpoints.questions.getQuestion(idAndQuestionId.getSecond())).isAnswerTrue(selected.getText().toString(), "arm")) {
                Toast.makeText(getApplicationContext(), "True", Toast.LENGTH_LONG).show();
                user.setCheckpointDone(idAndQuestionId.getFirst());

                user.addScore((TextView) findViewById(R.id.score_type_1), "score_type_1", checkpoints.checkpointMap.get(idAndQuestionId.getFirst()).getScores().get("score_type_1"));
            } else {
                Toast.makeText(getApplicationContext(), "False", Toast.LENGTH_LONG).show();
            }
            int Rid = R.id.boxAnswer2_multiple_choice_one_true;
            int numberTodo = answersMultipleChoiceOneTrue.getChildCount() - 1;
            for (int i = 0; i < numberTodo; i++) {
                answersMultipleChoiceOneTrue.removeView(findViewById(Rid));
                Rid++;
            }
            questionsMultipleChoiceOneTrue.setTag(null);
            questionsMultipleChoiceOneTrue.setVisibility(View.GONE);
        } else {
            Toast.makeText(getApplicationContext(), "Please choose some", Toast.LENGTH_LONG).show();

        }
    }

    public void submitAnswerForSelectDifference(View view) {
        Constants.Pair<String,SelectDifference> idAndQuestion= (Constants.Pair<String,SelectDifference>) findViewById(R.id.questions_select_difference).getTag();
        //question.setTrueAnswerForThisScreen((int) ((question.getTrueAnswer().first*findViewById(R.id.selectDifferenceImage).getWidth()/100)+findViewById(R.id.questions_select_difference).getPaddingLeft()), (int) ((question.getTrueAnswer().second*findViewById(R.id.selectDifferenceImage).getHeight()/100)+findViewById(R.id.questions_select_difference).getPaddingLeft()));

        Rect r=new Rect();
        Rect rect=new Rect();
        findViewById(R.id.selectDifferenceImage).getGlobalVisibleRect(rect);
        findViewById(R.id.selectDifferenceRing).getGlobalVisibleRect(r);
        if(r.contains(idAndQuestion.second.getTrueAnswerForThisScreen().first+rect.left,idAndQuestion.second.getTrueAnswerForThisScreen().second+rect.top)){
        //if(Math.sqrt((findViewById(R.id.selectDifferenceRing).getY()+findViewById(R.id.selectDifferenceRing).getPivotY() - question.getTrueAnswerForThisScreen().second)*(findViewById(R.id.selectDifferenceRing).getY()+findViewById(R.id.selectDifferenceRing).getPivotY()- question.getTrueAnswerForThisScreen().second) + (findViewById(R.id.selectDifferenceRing).getX()+findViewById(R.id.selectDifferenceRing).getPivotX() - question.getTrueAnswerForThisScreen().first)*(findViewById(R.id.selectDifferenceRing).getX()+findViewById(R.id.selectDifferenceRing).getPivotX() - question.getTrueAnswerForThisScreen().first))<=findViewById(R.id.selectDifferenceRing).getHeight()/2){
            Toast.makeText(this,"True",Toast.LENGTH_LONG).show();
            user.setCheckpointDone(idAndQuestion.first);
            user.addScore((TextView) findViewById(R.id.score_type_1), "score_type_1", checkpoints.checkpointMap.get(idAndQuestion.getFirst()).getScores().get("score_type_1"));
        }
        else
            Toast.makeText(this,"False",Toast.LENGTH_LONG).show();
        findViewById(R.id.questions_select_difference).setTag(null);
        findViewById(R.id.questions_select_difference).setVisibility(View.GONE);
    }
    // }
    public void submitAnswerForMultipleChoiceMoreTrue(View view) {
        List<String> selectedAnswers=new ArrayList<>();
        int id=R.id.checkBox0_multiple_choice_more_true;
        int numberTodo=0;
        for(int i=0;i<11;i++) {
            if (findViewById(id) != null) {
                if (((CheckBox) findViewById(id)).isChecked()) {
                    selectedAnswers.add(String.valueOf(((CheckBox) findViewById(id)).getText()));
                }
                ++id;
                numberTodo++;
            }
            else
                break;
        }
        Constants.Pair<String,MultipleQuestionWithMoreTrue> idAndQuestion= (Constants.Pair<String, MultipleQuestionWithMoreTrue>) findViewById(R.id.questions_multiple_choice_more_true).getTag();
        if(idAndQuestion.second.isAnswerTrue(selectedAnswers,"arm")){
            Toast.makeText(getApplicationContext(), "True", Toast.LENGTH_LONG).show();
            user.setCheckpointDone(idAndQuestion.getFirst());

            user.addScore((TextView) findViewById(R.id.score_type_1), "score_type_1", checkpoints.checkpointMap.get(idAndQuestion.getFirst()).getScores().get("score_type_1"));
        }else{
            Toast.makeText(getApplicationContext(), "False", Toast.LENGTH_LONG).show();
        }
        int Rid=R.id.checkBox0_multiple_choice_more_true;
        Rid++;
        for (int i = 0; i < numberTodo-1; i++) {
            ((LinearLayout)findViewById(R.id.answers_multiple_choice_more_true)).removeView(findViewById(Rid));
            Rid++;
        }
        ((CheckBox)findViewById(R.id.checkBox0_multiple_choice_more_true)).setChecked(false);
        findViewById(R.id.questions_multiple_choice_more_true).setVisibility(View.GONE);
    }
    public void switchModeToMissionsOrCheckpoints(View view) {


       /* View screenView = findViewById(R.id.imageViewForChangingScreen).getRootView();
        screenView.setDrawingCacheEnabled(true);
        final Bitmap[] bitmap = {Bitmap.createBitmap(screenView.getDrawingCache())};
        screenView.setDrawingCacheEnabled(false);*/
        mMap.snapshot(new MapboxMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
               /*
                for (int i=0;i<snapshot.getWidth();i++)
                    for (int j=0;j<snapshot.getHeight();j++)
                        if(bitmap[0].getPixel(i,j)== Color.TRANSPARENT)
                            bitmap[0].setPixel(i,j,snapshot.getPixel(i,j));
                ((ImageView)findViewById(R.id.imageViewForChangingScreen)).setImageBitmap(bitmap[0]);
                findViewById(R.id.imageViewForChangingScreen).setVisibility(View.VISIBLE);*/
                ToggleButton swaper = (ToggleButton) findViewById(R.id.toggleButtonSwaper);
                if (swaper.isChecked()) {
                    mMap.setStyle("mapbox://styles/mapbox/dark-v9", new MapboxMap.OnStyleLoadedListener() {
                        @Override
                        public void onStyleLoaded(String style) {
                            /*Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                                    R.anim.slide_down);
                            slide_down.setDuration(1000);
                            findViewById(R.id.imageViewForChangingScreen).startAnimation(slide_down);*/
                            startMode(missionMode);
                        }
                    });
                } else
                    mMap.setStyle("mapbox://styles/mapbox/light-v9", new MapboxMap.OnStyleLoadedListener() {
                        @Override
                        public void onStyleLoaded(String style) {
                            /*Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                                    R.anim.slide_up);
                            slide_up.setDuration(1000);
                            findViewById(R.id.imageViewForChangingScreen).startAnimation(slide_up);*/
                            startMode(checkpointMode);
                        }
                    });
            }
        });


    }


    private void showCheckpointProperties(String id,String Title, String description, List<String> imageUrls){
        if(Title!=null){
            ((TextView)(findViewById(R.id.checkpointViewTitle))).setText(Title);
        }
        else
            ((TextView)(findViewById(R.id.checkpointViewTitle))).setText("");
        if(description!=null){
            ((TextView)(findViewById(R.id.checkpointViewDescription))).setText(description);
        }
        else
            ((TextView)(findViewById(R.id.checkpointViewDescription))).setText("");
        //TODO show imaegs

        List<String> realImageUrls=new ArrayList<>();
        String[] splitedId=id.split("_");
        StorageReference data= FirebaseStorage.getInstance().getReference();
       for(int i=0;i<splitedId.length-2;i++){

            data=data.child(splitedId[i]);
        }
        /*for(String num:imageUrls){
            realImageUrls.add(data.child(id+"_"+num));
        }*/

        ((ImageView) findViewById(R.id.checkpointViewImageView1)).setImageResource(0);
        ((ImageView) findViewById(R.id.checkpointViewImageView2)).setImageResource(0);
        ((ImageView) findViewById(R.id.checkpointViewImageView3)).setImageResource(0);
        ((ImageView) findViewById(R.id.checkpointViewImageView4)).setImageResource(0);
        if(imageUrls!=null) {
            if(imageUrls.size()>0) {
                if (imageUrls.get(0) != null)
                    Picasso.with(MapsActivity.this).load(imageUrls.get(0).toString()).networkPolicy(NetworkPolicy.NO_CACHE).into((ImageView) findViewById(R.id.checkpointViewImageView1));
               if(imageUrls.size()>1) {
                   if (imageUrls.get(1) != null)
                       Picasso.with(MapsActivity.this).load(imageUrls.get(1).toString()).networkPolicy(NetworkPolicy.NO_CACHE).into((ImageView) findViewById(R.id.checkpointViewImageView2));
                    if(imageUrls.size()>2){
                        if (imageUrls.get(2) != null)
                            Picasso.with(MapsActivity.this).load(imageUrls.get(2).toString()).networkPolicy(NetworkPolicy.NO_CACHE).into((ImageView) findViewById(R.id.checkpointViewImageView3));
                        if(imageUrls.size()>3){
                            if (imageUrls.get(3) != null)
                                Picasso.with(MapsActivity.this).load(imageUrls.get(3).toString()).networkPolicy(NetworkPolicy.NO_CACHE).into((ImageView) findViewById(R.id.checkpointViewImageView4));
                        }else{
                            findViewById(R.id.checkpointViewImageView4).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
                        }
                    }else{
                        findViewById(R.id.checkpointViewImageView3).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
                        findViewById(R.id.checkpointViewImageView4).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
                    }
               }else{
                    findViewById(R.id.checkpointViewImageView2).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
                    findViewById(R.id.checkpointViewImageView3).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
                    findViewById(R.id.checkpointViewImageView4).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
               }

            }else{
                findViewById(R.id.checkpointViewImageView1).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
                findViewById(R.id.checkpointViewImageView2).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
                findViewById(R.id.checkpointViewImageView3).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
                findViewById(R.id.checkpointViewImageView4).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0f));
            }
        }
        findViewById(R.id.checkpointView).setVisibility(View.VISIBLE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mapFragment != null)
            mapFragment.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapFragment != null)
            mapFragment.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapFragment != null)
            mapFragment.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mapFragment != null)
            mapFragment.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapFragment != null)
            mapFragment.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapFragment != null)
            mapFragment.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapFragment != null)
            mapFragment.onSaveInstanceState(outState);
    }


}