package com.example.erik.destination;

/**
 * Created by Erik on 3/14/2017.
 */

public class Constants {
    private static final String TAG = "Stex em";
    private static final int distance = 15;
    public final int numberOfLinesOnPolygon = 3;
    public final int maxZoom = 8;
    private String MyLocationTitleString = "MyLocation";

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

        Pair(T first, S second) {
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

}
