package com.example.finderapp.Models;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

public class PolylineData {

    private Polyline polyline;
    private DirectionsLeg directionsLeg;

    public PolylineData() {
    }

    public PolylineData(Polyline polyline, DirectionsLeg directionsLeg) {
        this.polyline = polyline;
        this.directionsLeg = directionsLeg;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public DirectionsLeg getDirectionsLeg() {
        return directionsLeg;
    }

    public void setDirectionsLeg(DirectionsLeg directionsLeg) {
        this.directionsLeg = directionsLeg;
    }

    @Override
    public String toString() {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", directionsLeg=" + directionsLeg +
                '}';
    }
}
