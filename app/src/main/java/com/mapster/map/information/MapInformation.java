package com.mapster.map.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tommyngo on 30/04/15.
 */
public class MapInformation {
    private List<String> _instructions;
    private List<Distance> _distance;
    private List<Duration> _duration;
    private List<List<HashMap<String, String>>> _routes;
    private String _origin;
    private String _destination;
    private Distance _totalDistance;
    private Duration _totalDuration;
    private List<Integer> _routesColor;

    public MapInformation(){
        _instructions = new ArrayList<>();
        _distance = new ArrayList<>();
        _duration = new ArrayList<>();
        _routes = new ArrayList<>();
        _totalDistance = new Distance();
        _totalDuration = new Duration();
        _routesColor = new ArrayList<>();
    }

    public void setOrigin(String origin){
        _origin = origin;
    }

    public void setDestination(String destination){
        _destination = destination;
    }

    public void setTotalDistance(Distance totalDistance){
        _totalDistance = totalDistance;
    }

    public void setTotalDuration(Duration totalDuration){
        _totalDuration = totalDuration;
    }

    public void setTotalDistanceValue(int totalDistanceValue){
        _totalDistance.setValue(totalDistanceValue);
    }

    public void setTotalDistanceString(String totalDistanceString){
        _totalDistance.setName(totalDistanceString);
    }

    public void setTotalDurationValue(int totalDurationValue){
        _totalDuration.setValue(totalDurationValue);
    }

    public void setTotalDurationString(String totalDurationString){
        _totalDuration.setName(totalDurationString);
    }

    public void setInstruction(List<String> instruction){
        _instructions = instruction;
    }

    public void setDistance(List<Distance> distance){
        _distance = distance;
    }

    public void setDuration(List<Duration> duration){
        _duration = duration;
    }

    public void setRoutes(List<List<HashMap<String, String>>> routes){
        _routes = routes;
    }

    public void addRoutes(List<HashMap<String, String>> path){
        _routes.add(path);
    }

    public void addInstructions(String instruction){
        _instructions.add(instruction);
    }

    public void addDistance(Distance distance){
        _distance.add(distance);
    }

    public void addDuration(Duration duration){
        _duration.add(duration);
    }

    public List<String> getInstructions(){
        return _instructions;
    }

    public List<Distance> getDistance(){
        return _distance;
    }

    public List<Duration> getDuration(){
        return _duration;
    }

    public List<List<HashMap<String, String>>> getRoutes(){
        return _routes;
    }

    public String getOrigin(){
        return _origin;
    }

    public String getDestination(){
        return _destination;
    }

    public Distance getTotalDistance(){
        return _totalDistance;
    }

    public Duration getTotalDuration(){
        return _totalDuration;
    }

    public List<Integer> getRouteColor (){ return _routesColor; }

    public void setRoutesColor(List<Integer> routeColor) { this._routesColor = routeColor; }

    public void addRouteColor( int color ){ this._routesColor.add(color); }
}
