package com.mapster.map.models;

import com.mapster.date.CustomDate;

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
    private Duration _totalDuration;
    private CustomDate _date;

    public MapInformation(CustomDate date){
        _paths = new ArrayList<>();
        _routes = new ArrayList<>();
        _totalDistance = new Distance();
        _totalDuration = new Duration();
        _date = date;
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

    public List<Path> getPaths() {
        return _paths;
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


}
