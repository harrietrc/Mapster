package com.mapster.map.models;

import android.util.Log;

import com.mapster.date.CustomDate;
import com.mapster.json.StatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tommyngo on 30/04/15.
 */
public class MapInformation{
    private List<Path> _paths;
    private List<Routes> _routes;
    private String _origin;
    private String _destination;
    private Distance _totalDistance;
    private CustomDate _date;
    private StatusCode _status;
    private List<CustomDate> _timeReachEachLocation;
    private CustomDate _dateToCalculate;

    // List of dates/times for all waypoints
    private List<CustomDate> _dates;

    public MapInformation(CustomDate date){
        _paths = new ArrayList<>();
        _routes = new ArrayList<>();
        _timeReachEachLocation = new ArrayList<>();
        _totalDistance = new Distance();
        _date = new CustomDate(date.toString());
        _dateToCalculate = new CustomDate(date.toString());

        // TODO Don't need both _dates and _dates, but just getting this working first
        _dates = new ArrayList<>();
        _dates.add(new CustomDate(date.getDateTime())); // Constructor call to break reference to _date obj
    }

    public CustomDate getStartDate() {
        return _dates.get(0);
    }

    public List<CustomDate> getDates() {
        return _dates;
    }

    public void addDate(CustomDate date) {
        _dates.add(date);
    }

    public void setStatus(StatusCode code){
        _status = code;
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

    public void setTotalDistanceValue(int totalDistanceValue){
        _totalDistance.setValue(totalDistanceValue);
    }

    public void setTotalDistanceString(String totalDistanceString){
        _totalDistance.setName(totalDistanceString);
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

    public List<Path> getPaths() {
        return _paths;
    }

    public StatusCode getStatus(){
        return _status;
    }

    public void setPaths(List<Path> paths) {
        this._paths = paths;
    }

    public void addPath(Path path){
        if(path.getDate() == null) {
            CustomDate date = new CustomDate(_date.toString());
            _date.addSeconds(path.getDuration().getValue());
            date.addSeconds(path.getDuration().getValue());
            path.setDate(date);
        } else {
            _date = path.getDate();
        }
        _paths.add(path);
    }

    public List<Routes> getRoutes() {
        return _routes;
    }

    public void setRoutes(List<Routes> routes) {
        this._routes = routes;
    }

    public void addRoutes(Routes routes){
        _routes.add(routes);
    }

    public void setDate(CustomDate date){ _date = date; }

    public CustomDate getDate() { return _date; };

    public void addTimeReachEachLocation(CustomDate d){
        _timeReachEachLocation.add(d);
    }

    public List<CustomDate> getTimeReachEachLocation(){
        return _timeReachEachLocation;
    }

    public void setTimeReachEachLocation(List<CustomDate> list){
        _timeReachEachLocation = list;
    }
}
