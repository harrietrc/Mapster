package com.mapster.map.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by tommyngo on 10/08/15.
 */
public class SortedCoordinate {
    private String _modeTransport;
    private List<LatLng> _sortedCoordinateList;

    public SortedCoordinate(String mode, List<LatLng> sortedCoordinate){
        _modeTransport = mode;
        _sortedCoordinateList = sortedCoordinate;
    }

    public String getModeTransport(){
        return _modeTransport;
    }

    public List<LatLng> getSortedCoordinateList(){
        return _sortedCoordinateList;
    }

}
