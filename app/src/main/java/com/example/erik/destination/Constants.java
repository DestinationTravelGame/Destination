package com.example.erik.destination;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Erik on 3/14/2017.
 */

public class Constants {
    public static final String TAG = "Stex em";
    public static final int distance = 15;
    public static final int distanceMission=15;
    public static final int numberOfLinesOnPolygon = 3;
    public static final int maxZoom = 8;
    private static String MyLocationTitleString = "MyLocation";

    public static final int checkpointMode=0;
    public static final int missionMode=1;
    public static final int missionStartedMode=2;

    public static final int missionCheckpointDone=1;
    public static final int missionCheckpointNotDone=0;
    public static final int missionCheckpointNotStarted=-6;
    public static final int missionCheckpointStartedNotDone2QuestionsLast=-4;
    public static final int stateNotAnswering=-5;
    public static final int stateAnswering=-3;

    public static final int missionCheckpointStartedNotDone1QuestionLast=-1;
    public static final int missionCheckpointStartedNotNear=-5;

    public static final int checkpointCirecleStroke=10;
    public static final int minTimeToStayInCheckpoint=15;//in Sec

    public static final int lastPointRadius=500;//in metr

    public static final int nQuestionsInCheckpoints=2;

    public static final int secondsBeforeQuestion=5;

    public static final int secondsForQuestion=50;//in sec


    public String getLogTag() {
        return TAG;
    }

    public int getDistance() {
        return distance;
    }

    public String getMyLocationTitleString() {
        return MyLocationTitleString;
    }

    public static class Pair<T, S> {
        T first;
        S second;

        public Pair(){}
        public Pair(T first, S second) {
            this.first = first;
            this.second = second;
        }

        public S getSecond() {
            return second;
        }

        public T getFirst() {
            return first;
        }
    }

    public static Bitmap getCircleCroppedBitmap(Bitmap bitmap) {
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

    public static double distanceBetween(LatLng a,LatLng b){
        float[] c=new float[1];
        Location.distanceBetween(a.getLatitude(),a.getLongitude(),b.getLatitude(),b.getLongitude(),c);
        return c[0];
    }

}