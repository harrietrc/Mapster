package com.mapster.json;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.map.information.Distance;
import com.mapster.map.information.Duration;
import com.mapster.map.information.MapInformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tommyngo on 14/03/15.
 */
public class JSONParser {
    public enum ModeColor{
        WALKING("#bdbdbd"), DRIVING("#0000ff"), BICYCLING("#00e500");
        private final String name;
        private ModeColor(String name){
            this.name = name;
        }
    }
    private MapInformation mapInformation;
    public JSONParser (){
        mapInformation = new MapInformation();
    }
    public MapInformation parse(JSONObject jObject) {
        HashMap<String, Integer> helper = new HashMap<>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        JSONArray jSubSteps = null;
        try {
            String status = jObject.getString("status");
            if (!status.equals("OK")){
                return null;
            }
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    parseTotalDistanceDuration(jLegs.getJSONObject(j), "distance");
                    parseTotalDistanceDuration(jLegs.getJSONObject(j), "duration");
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        try {
                            jSubSteps = ((JSONObject) jSteps.get(k)).getJSONArray("steps");
                            jSubSteps.getJSONObject(k).getString("html_instructions");
                        } catch (JSONException e){
                            jSubSteps = null;
                        }
                        if (jSubSteps != null){
                            for (int l = 0; l < jSubSteps.length(); l++){
                                path.addAll(getPath(jSubSteps, l));
//                                Log.d("PATH",getPath(jSubSteps, l).toString());
                                assignInformationToMapInformation(jSubSteps, l);
                            }
                            mapInformation.addRoutes(path);
                            path.clear();
                            addColorToRoute(jSteps.getJSONObject(k));

                        } else {
//                            Log.d("PATH",getPath(jSteps, k).toString());
                            assignInformationToMapInformation(jSteps, k);
                            mapInformation.addRoutes(getPath(jSteps, k));
                            addColorToRoute(jSteps.getJSONObject(k));
                        }
                        if (jSteps.getJSONObject(k).get("travel_mode").equals("TRANSIT")) {
                            JSONObject transitJSON = jSteps.getJSONObject(k).getJSONObject("transit_details");
                            StringBuilder transit = new StringBuilder();
                            transit.append("From departure stop: ");
                            transit.append("<b>" + transitJSON.getJSONObject("departure_stop").get("name") + "</b>");
                            transit.append("<br/>");
                            transit.append(" To arrival stop: ");
                            transit.append("<b>" + transitJSON.getJSONObject("arrival_stop").get("name") + "</b>");
                            transit.append("<br/>");
                            transit.append("Head Sign: ");
                            transit.append("<b>" + transitJSON.getString("headsign") + "</b>");
                            transit.append("<br/>");
                            JSONObject line = transitJSON.getJSONObject("line");
                            if (!line.isNull("short_name")) {
                                transit.append(line.getJSONObject("vehicle").get("name") + " number: ");
                                transit.append("<b>" + line.getString("short_name") + "</b>");
                                transit.append("<br/>");
                            }
                            transit.append(line.getJSONObject("vehicle").get("name") + " name: ");
                            transit.append("<b>" + line.getString("name") + "</b>");
                            transit.append("<br/>");
                            transit.append("Number of stops: " + "<b>" + transitJSON.getString("num_stops")+ "</b>");
                            mapInformation.addInstructions(transit.toString());
                            mapInformation.addDistance(new Distance("", 0));
                            mapInformation.addDuration(new Duration("", 0));
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapInformation;
    }

    private void addColorToRoute(JSONObject object){
        try {
            switch (object.getString("travel_mode")) {
                case "TRANSIT":
                    mapInformation.addRouteColor(Color.parseColor(object.getJSONObject("transit_details").getJSONObject("line").getString("color")));
                    break;
                case "WALKING":
                    mapInformation.addRouteColor(Color.parseColor(ModeColor.WALKING.name));
                    break;
                case "DRIVING":
                    mapInformation.addRouteColor(Color.parseColor(ModeColor.DRIVING.name));
                    break;
                case "BICYCLING":
                    mapInformation.addRouteColor(Color.parseColor(ModeColor.BICYCLING.name));
                    break;
            }
        } catch(JSONException e){
            e.printStackTrace();
        }
    }

    private List<HashMap<String, String>> getPath(JSONArray array, int k){
        String polyline = "";
        List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
        try {
            polyline = (String) ((JSONObject) ((JSONObject) array.get(k)).get("polyline")).get("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<LatLng> list = decodePoly(polyline);
        /** Traversing all points */
        for (int l = 0; l < list.size(); l++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("lat",
                    Double.toString(((LatLng) list.get(l)).latitude));
            hm.put("lng",
                    Double.toString(((LatLng) list.get(l)).longitude));
            path.add(hm);
        }
        return path;
    }

    /**
     * Method Courtesy :
     * jeffreysambells.com/2010/05/27
     * /decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void assignInformationToMapInformation(JSONArray array, int position){
        // Get Instruction
        try {
            mapInformation.addInstructions(array.getJSONObject(position).getString("html_instructions"));
            //Get Distance
            parseDistanceDuration(array.getJSONObject(position), "distance");
            // Get Duration
            parseDistanceDuration(array.getJSONObject(position), "duration");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseTotalDistanceDuration(JSONObject jsonObject, String name){
        Distance totalDistance;
        Duration totalDuration;
        try {
            switch (name) {
                case "distance":
                    totalDistance = new Distance(jsonObject.getJSONObject(name).getString("text"), jsonObject.getJSONObject(name).getInt("value"));
                    mapInformation.setTotalDistance(totalDistance);
                    break;
                case "duration":
                    totalDuration = new Duration(jsonObject.getJSONObject(name).getString("text"), jsonObject.getJSONObject(name).getInt("value"));
                    mapInformation.setTotalDuration(totalDuration);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseDistanceDuration(JSONObject jsonObject, String name){
        Distance distance;
        Duration duration;
        try {
            switch (name) {
                case "distance":
                    distance = new Distance(jsonObject.getJSONObject(name).getString("text"), jsonObject.getJSONObject(name).getInt("value"));
                    mapInformation.addDistance(distance);
                    break;
                case "duration":
                    duration = new Duration(jsonObject.getJSONObject(name).getString("text"), jsonObject.getJSONObject(name).getInt("value"));
                    mapInformation.addDuration(duration);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
