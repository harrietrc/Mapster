package com.mapster;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

// FragmentActivity extends Activity, unsurprisingly
public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private List<Marker> _markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // R.id.map is added automatically when the layout file is built
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        // Sets this as the callback object for when the GoogleMap instance is ready to use
        mapFragment.getMapAsync(this);

        _markers = new ArrayList<Marker>();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        newMarker(-36.853085, 174.76958, "The University of Auckland", googleMap);
        newMarker(-36.848448,174.762191, "The Sky Tower", googleMap);
        newMarker(-36.8273514, 174.811964, "North Head", googleMap);

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                // Auto-zoom to fit all markers
                // This builder creates a minimum bound based on a set of LatLng points
                LatLngBounds.Builder b = new LatLngBounds.Builder();
                for (Marker m: _markers) {
                    b.include(m.getPosition());
                }
                LatLngBounds bounds = b.build();

                // The second argument is padding
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                googleMap.setOnCameraChangeListener(null);
            }
        });
    }

    /**
     * Convenience method for adding new markers and keeping track of them in a list.
     * @param latitude = Latitude value for marker
     * @param longitude = Longitude value for marker
     * @param name = Name associated with the marker
     * @param map = Map that this marker will be added to
     */
    private void newMarker(double latitude, double longitude, String name, GoogleMap map) {
        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                .title(name));
        _markers.add(marker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
