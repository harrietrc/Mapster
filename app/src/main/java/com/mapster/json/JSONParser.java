package com.mapster.json;

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
    private MapInformation mapInformation;
    public JSONParser (){
        mapInformation = new MapInformation();
    }
    public MapInformation parse(JSONObject jObject) {
        HashMap<String, Integer> helper = new HashMap<>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
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
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps
                                .get(k)).get("polyline")).get("points");
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
                        // Get Instruction
                        mapInformation.addInstructions(jSteps.getJSONObject(k).getString("html_instructions"));

                        //Get Distance
                        parseDistanceDuration(jSteps.getJSONObject(k), "distance");
                        // Get Duration
                        parseDistanceDuration(jSteps.getJSONObject(k), "duration");
                    }
                    mapInformation.addRoutes(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapInformation;
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
