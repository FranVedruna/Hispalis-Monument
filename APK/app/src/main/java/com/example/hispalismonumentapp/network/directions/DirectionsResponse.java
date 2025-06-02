package com.example.hispalismonumentapp.network.directions;

import java.util.List;

public class DirectionsResponse {
    public List<Route> routes;

    public static class Route {
        public List<Leg> legs;
        public List<Integer> waypoint_order;
    }

    public static class Leg {
        public Duration duration;
        public Distance distance;
    }

    public static class Duration {
        public String text;
        public int value;
    }

    public static class Distance {
        public String text;
        public int value;
    }
}
