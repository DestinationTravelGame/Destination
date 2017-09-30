package com.example.erik.destination;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.erik.destination.Question.MultipleQuestionWithOneTrue;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback, Animation.AnimationListener {


    public static MapboxMap mMap;
    private MapView mapFragment;
    private ImageView personPhoto;
    public static TextView userScoreType1;
    private RelativeLayout userProfileLayout;
    private Button passCheckpointQuest;
    private Button signOut;
    private ImageView selectDifferenceRing;
    private RelativeLayout questionSelectDifference;
    private Checkpoints checkpoints = new Checkpoints();
    private Missions missions = new Missions();
    private User user = new User();
    private MarkerView curLocMarker;
    GoogleApiClient mGoogleApiClient;
    Constants constants = new Constants();
    private GoogleApiClient client;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goToLogin();
        } else {
            Mapbox.getInstance(this, "pk.eyJ1IjoiZGVzdGluYXRpb250cmF2ZWxnYW1lIiwiYSI6ImNqMWVvM2pyZjAwMDQyd3Bpdm1oaG1iaHMifQ.9BRL6-apqVp2_GfXMCK59g");
            setContentView(R.layout.activity_maps);
            questionSelectDifference = (RelativeLayout) findViewById(R.id.questions_select_difference);
            personPhoto = (ImageView) findViewById(R.id.personPhoto);
            userProfileLayout = (RelativeLayout) findViewById(R.id.userProfileLayout);
            passCheckpointQuest = (Button) findViewById(R.id.passCheckpointQuest);
            questionMultipleChoiceOneTrue = (TextView) findViewById(R.id.question_multiple_choice_one_true);
            boxAnswer1MultipleChoiceOneTrue = (RadioButton) findViewById(R.id.boxAnswer1_multiple_choice_one_true);
            answersMultipleChoiceOneTrue = (RadioGroup) findViewById(R.id.answers_multiple_choice_one_true);
            questionsMultipleChoiceOneTrue = (RelativeLayout) findViewById(R.id.questions_multiple_choice_one_true);
            userScoreType1 = (TextView) findViewById(R.id.score_type_1);

            userDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            passCheckpointQuest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Constants.Pair<String, Float> nearCheckpointId = (Constants.Pair<String, Float>) passCheckpointQuest.getTag();
                    checkpoints.checkpointMap.get(nearCheckpointId.getFirst()).start();
                    showCheckpointQuestion(checkpoints.checkpointMap.get(nearCheckpointId.getFirst()).getId());
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
                            personPhoto.setImageBitmap(getCircleCroppedBitmap(((BitmapDrawable) draw).getBitmap()));
                            ((ImageView) findViewById(R.id.personPhotoInLayout)).setImageBitmap(getCircleCroppedBitmap(((BitmapDrawable) draw).getBitmap()));
                        } else {
                            Bitmap bit = Bitmap.createBitmap(draw.getIntrinsicWidth(), draw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bit);
                            draw.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                            draw.draw(canvas);
                            personPhoto.setImageBitmap(getCircleCroppedBitmap(bit));
                            ((ImageView) findViewById(R.id.personPhotoInLayout)).setImageBitmap(getCircleCroppedBitmap(bit));
                        }
                    }                                                               //

                    @Override
                    public void onError() {
                        Picasso.with(MapsActivity.this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(personPhoto, new Callback() {
                            @Override
                            public void onSuccess() {
                                Drawable draw = personPhoto.getDrawable();
                                if (draw instanceof BitmapDrawable) {
                                    personPhoto.setImageBitmap(getCircleCroppedBitmap(((BitmapDrawable) draw).getBitmap()));
                                    ((ImageView) findViewById(R.id.personPhotoInLayout)).setImageBitmap(getCircleCroppedBitmap(((BitmapDrawable) draw).getBitmap()));
                                } else {
                                    Bitmap bit = Bitmap.createBitmap(draw.getIntrinsicWidth(), draw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(bit);
                                    draw.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                    draw.draw(canvas);
                                    personPhoto.setImageBitmap(getCircleCroppedBitmap(bit));
                                    ((ImageView) findViewById(R.id.personPhotoInLayout)).setImageBitmap(getCircleCroppedBitmap(bit));
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
                Log.i(constants.getLogTag(), "Permission test failed");
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    public void UpdateCurrentLocation() {
        if (CurrentLocation == null) {
            curLocMarker = mMap.addMarker(new MarkerViewOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(constants.getMyLocationTitleString()).snippet(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocMarker.getPosition(), 14));
        }
        CurrentLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        curLocMarker.setPosition(CurrentLocation);
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
            public boolean onMarkerClick(Marker marker) {
                if (!marker.equals(curLocMarker)) {
                    marker.showInfoWindow(mMap, mapFragment);//Not Current Marker
                    return true;
                } else {
                    marker.showInfoWindow(mMap, mapFragment);
                    return true;
                }
            }
        });
        mMap.setAllowConcurrentMultipleOpenInfoWindows(false);
        mapView = mMap.getProjection().getVisibleRegion();
        mMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                LatLng farRight = mMap.getProjection().getVisibleRegion().farRight;
                LatLng nearLeft = mMap.getProjection().getVisibleRegion().nearLeft;
                if (((farRight.getLatitude() - mapView.farRight.getLatitude()) > 0.0000000001 || (farRight.getLongitude() - mapView.farRight.getLongitude()) > 0.0000000001 || (nearLeft.getLatitude() - mapView.nearLeft.getLatitude()) > 0.0000000001 || (nearLeft.getLongitude() - mapView.nearLeft.getLongitude()) > 0.0000000001)) {
                    // Log.v(constants.getLogTag(), String.valueOf(mMap.getProjection().getVisibleRegion().toString()));
                    if (questionSelectDifference.getTag() != null) {
                        if (checkpoints.checkpointMap.get(((Constants.Pair<String, Float>) questionSelectDifference.getTag()).first) != null) {
                            checkpoints.checkpointMap.get(((Constants.Pair<String, Float>) questionSelectDifference.getTag()).first).changeRadiusWithZoom(mMap.getProjection().getVisibleRegion().farRight, mMap.getProjection().getVisibleRegion().nearLeft);
                        }
                    }
                    checkpoints.updateMapForNewCheckpoints();
                    mapView = mMap.getProjection().getVisibleRegion();
                }
            }
        });
        mMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                for (Marker a : mMap.getMarkers())
                    a.hideInfoWindow();
            }
        });
    }

    Runnable checkpointReach = new Runnable() {
        @Override
        public void run() {
            Constants.Pair<String, Float> curr = checkpoints.isNearToAnyCheckpoint(CurrentLocation);
            if (curr == null) {
                if (passCheckpointQuest.getTag() != null) {
                    if (checkpoints.checkpointMap.get(((Constants.Pair<String, Float>) passCheckpointQuest.getTag()).first) != null)
                        checkpoints.checkpointMap.get(((Constants.Pair<String, Float>) passCheckpointQuest.getTag()).first).stopPulsing();
                }
                passCheckpointQuest.setVisibility(View.GONE);
            } else {
                if (checkpoints.checkpointMap.get(curr.first).isLoaded() && !user.ifCheckpointDone(checkpoints.checkpointMap.get(curr.first).getId())) {
                    passCheckpointQuest.setVisibility(View.VISIBLE);
                    passCheckpointQuest.setTag(curr);
                    checkpoints.checkpointMap.get(((Constants.Pair<String, Float>) passCheckpointQuest.getTag()).first).startPulsing();
                }
            }
            passCheckpointQuest.postDelayed(checkpointReach, 500);
        }
    };
    Runnable runStart = new Runnable() {
        @Override
        public void run() {
            if (CurrentLocation == null || !user.isLoaded())
                passCheckpointQuest.postDelayed(runStart, 500);//kap chuni harceri het uxxaki grel em vor inqn iran kanchi
            if (user.getScores().get("score_type_1") != null)
                userScoreType1.setText(user.getScores().get("score_type_1").toString());
            ((TextView) findViewById(R.id.nameSurname)).setText(user.getName());
            //TODO
            checkpoints.start(getApplicationContext());
            checkpoints.startThisMode();
            missions.start(getApplicationContext());
            missions.stopThisMode();
            passCheckpointQuest.postDelayed(checkpointReach, 13);
        }

    };

    private void goToLogin() {
        FirebaseAuth.getInstance().signOut();
        Intent loginIntent = new Intent(this, SignInActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (mMap != null)
            mMap.clear();
        startActivity(loginIntent);
        MapsActivity.this.finish();
    }

    public Bitmap getCircleCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    void showCheckpointQuestion(String id) {
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
    }

    public void submitAnswerForMultipleChoiceOneTrue(View view) {
        Constants.Pair<String, String> idAndQuestionId = (Constants.Pair<String, String>) questionsMultipleChoiceOneTrue.getTag();
        if (answersMultipleChoiceOneTrue.getCheckedRadioButtonId() != -1) {
            RadioButton selected = (RadioButton) findViewById(answersMultipleChoiceOneTrue.getCheckedRadioButtonId());
            answersMultipleChoiceOneTrue.clearCheck();
            if (((MultipleQuestionWithOneTrue) checkpoints.questions.getQuestion(idAndQuestionId.getSecond())).isAnswerTrue(selected.getText().toString(), "arm")) {
                Toast.makeText(getApplicationContext(), "True", Toast.LENGTH_LONG).show();
                user.setCheckpointDone(idAndQuestionId.getFirst());

                user.addScore(getApplicationContext(), "score_type_1", checkpoints.checkpointMap.get(idAndQuestionId.getFirst()).getScores().get("score_type_1"));
                userScoreType1.setText(user.getScores().get("score_type_1").toString());
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
    // }

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
                            checkpoints.stopThisMode();
                            missions.startThisMode();
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
                            missions.stopThisMode();
                            checkpoints.startThisMode();
                        }
                    });
            }
        });


    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        findViewById(R.id.imageViewForChangingScreen).setVisibility(View.GONE);
        animation.cancel();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

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
