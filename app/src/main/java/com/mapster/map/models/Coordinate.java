package com.mapster.map.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by tommyngo on 26/06/15.
 */
public class Coordinate implements Serializable {
    private LatLng _coordinate;
    private String _transportMode;
    private String _nameLocation;

    public Coordinate(LatLng coordinate, String transportMode, String name){
        _coordinate = coordinate;
        _nameLocation = name;
        _transportMode = transportMode;
    }

    public LatLng getCoordinate(){
        return _coordinate;
    }

    public String getTransportMode(){
        return _transportMode;
    }

    public String getNameLocation(){
        return _nameLocation;
    }
}
