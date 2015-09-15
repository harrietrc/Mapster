package com.mapster.infowindow.listeners;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

/**
 * Created by Harriet on 7/29/2015.
 */
public class DirectionsButtonListener implements View.OnClickListener {

    private Context _context;
    private LatLng _location;
    private String _placeName;

    public DirectionsButtonListener(Context context, LatLng location, String name) {
        _context = context;
        _location = location;
        _placeName = name;
    }

    /**
     * Opens Android/Google Maps app (or the browser if the that app is not installed) with
     * directions from the current location to the address of the provided itinerary item.
     */
    @Override
    public void onClick(View directionsButton) {
        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)",
                _location.latitude, _location.longitude, _placeName);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        try {
            // Try to start the Android maps app
            _context.startActivity(mapIntent);
        } catch (ActivityNotFoundException e1) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            try {
                // If it's not installed, try using a browser instead
                _context.startActivity(browserIntent);
            } catch (ActivityNotFoundException e2) {
                // No browser or maps app - alert the user
                Toast.makeText(_context, "No suitable application installed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
