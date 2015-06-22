package com.mapster.map.models;

import java.util.HashMap;
import java.util.List;

/**
 * Created by tommyngo on 11/06/15.
 */
public class Routes {
    private List<HashMap<String, String>> _routePoints;
    private int _color;

    public Routes(List<HashMap<String, String>> routePoints, int color){
        this._routePoints = routePoints;
        _color = color;
    }

    public List<HashMap<String, String>> getRoutePoints() {
        return _routePoints;
    }

    public void setRoutePoints(List<HashMap<String, String>> routePoints) {
        this._routePoints = routePoints;
    }

    public int getColor() {
        return _color;
    }

    public void setColor(int color) {
        this._color = color;
    }
}
