package com.example.hispalismonumentapp.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionsMapResponse {
    public String status;

    @SerializedName("error_message")
    public String errorMessage;

    public List<Route> routes;

    public static class Route {
        public OverviewPolyline overview_polyline;
        public List<Leg> legs;
    }

    public static class OverviewPolyline {
        public String points;
    }

    public static class Leg {
        public List<Step> steps;
        public Location start_location;
        public Location end_location;
        public TextValue distance;
        public TextValue duration;
    }

    public static class Step {
        public TextValue distance;
        public TextValue duration;
        public Location start_location;
        public Location end_location;
        public Polyline polyline;
        public String html_instructions;
        public String travel_mode;
    }

    public static class Polyline {
        public String points;
    }

    public static class Location {
        public double lat;
        public double lng;
    }

    public static class TextValue {
        public String text;
        public int value;
    }
}
