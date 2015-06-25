package com.mapster.json;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.date.CustomDate;
import com.mapster.map.models.Distance;
import com.mapster.map.models.Duration;
import com.mapster.map.models.Instruction;
import com.mapster.map.models.MapInformation;
import com.mapster.map.models.Path;
import com.mapster.map.models.Routes;

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
        WALKING("#bdbdbd"), DRIVING("#0000ff"), BICYCLING("#00e500"), TRANSIT("#FF69B4");
        private final String name;
        private ModeColor(String name){
            this.name = name;
        }
    }
    private MapInformation mapInformation;
    public JSONParser (CustomDate date){
        mapInformation = new MapInformation(date);
    }
    public MapInformation parse(JSONObject jObject, MapInformation map) {
        if (map != null)
            this.mapInformation = map;
        HashMap<String, Integer> helper = new HashMap<>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        JSONArray jSubSteps = null;
        Routes routes;
        try {
            String status = jObject.getString("status");
            if (!status.equals("OK")){
                return null;
            }
            StringBuilder string = new StringBuilder();
            System.out.println(mapInformation);
            string.append("Start Date: <b>" + mapInformation.getDate().toString() + "</b>");
            Path originalDate = new Path (new Distance("", 0), new Instruction(string.toString()), new Duration("", 0), mapInformation.getDate(), "");
            mapInformation.addPath(originalDate);
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
                        CustomDate transitDate = null;
                        try {
                            jSubSteps = ((JSONObject) jSteps.get(k)).getJSONArray("steps");
                            jSubSteps.getJSONObject(k).getString("html_instructions");
                        } catch (JSONException e){
                            jSubSteps = null;
                        }
                        if (jSubSteps != null){
                            path = new ArrayList<HashMap<String, String>>();
                            for (int l = 0; l < jSubSteps.length(); l++){
                                path.addAll(getPath(jSubSteps, l));
                                assignInformationToMapInformation(jSubSteps.getJSONObject(l));
                            }
                            routes = new Routes(path, getColorToRoute(jSteps.getJSONObject(k)));
                            mapInformation.addRoutes(routes);
                        } else {
                            if (jSteps.getJSONObject(k).get("travel_mode").equals("TRANSIT")) {
                                JSONObject transitJSON = jSteps.getJSONObject(k)
                                        .getJSONObject("transit_details");
                                StringBuilder transit = new StringBuilder();
                                transit.append("From departure stop: ");
                                transit.append("<b>" + transitJSON.getJSONObject("departure_stop")
                                        .get("name") + "</b>");
                                transit.append("<br/>");

                                transit.append(" To arrival stop: ");
                                transit.append("<b>" + transitJSON.getJSONObject("arrival_stop")
                                        .get("name") + "</b>");
                                transit.append("<br/>");

                                if (!transitJSON.isNull("departure_time")){
                                    transit.append("Departure Time: ");
                                    long value = transitJSON.getJSONObject("departure_time").getLong("value");
                                    transitDate = new CustomDate(value);
                                    transit.append("<b>" + transitDate.toString() + "</b>");
                                    transit.append("<br/>");
                                    transit.append("Time Zone: " + transitDate.getDateTime().getZone());
                                    transit.append("<br/>");
                                }

                                if (!transitJSON.isNull("headsign")) {
                                    transit.append("Head Sign: ");
                                    transit.append("<b>" + transitJSON.getString("headsign") + "</b>");
                                    transit.append("<br/>");
                                }

                                JSONObject line = transitJSON.getJSONObject("line");
                                if (!line.isNull("short_name")) {
                                    transit.append(line.getJSONObject("vehicle").get("name") + " number: ");
                                    transit.append("<b>" + line.getString("short_name") + "</b>");
                                    transit.append("<br/>");
                                }
                                if (!line.getJSONObject("vehicle").isNull("name") && !line.isNull("name")) {
                                    transit.append(line.getJSONObject("vehicle").get("name") + " name: ");
                                    transit.append("<b>" + line.getString("name") + "</b>");
                                    transit.append("<br/>");
                                }

                                transit.append("Number of stops: " + "<b>" + transitJSON
                                        .getString("num_stops") + "</b>");
                                Path pathJourney = null;
                                if (transitDate == null)
                                    pathJourney = new Path(new Distance("", 0),
                                            new Instruction(transit.toString()),new Duration("", 0), jSteps.getJSONObject(k).getString("travel_mode"));
                                else
                                    pathJourney = new Path(new Distance("", 0),
                                            new Instruction(transit.toString()),new Duration("", 0),transitDate ,jSteps.getJSONObject(k).getString("travel_mode"));
                                mapInformation.addPath(pathJourney);
                            }
                            assignInformationToMapInformation(jSteps.getJSONObject(k));

                            routes = new Routes(getPath(jSteps, k), getColorToRoute(jSteps.getJSONObject(k)));
                            mapInformation.addRoutes(routes);

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

    private Integer getColorToRoute(JSONObject object){
        try {
            switch (object.getString("travel_mode")) {
                case "TRANSIT":
                    String color;
                    try {
                        color = object.getJSONObject("transit_details").getJSONObject("line").getString("color");
                    } catch (JSONException e){
                        return Color.parseColor(ModeColor.TRANSIT.name);
                    }
                    return Color.parseColor(color);
                case "WALKING":
//                    Log.d("WALKING", String.valueOf(Color.parseColor(ModeColor.WALKING.name)));
                    return (Color.parseColor(ModeColor.WALKING.name));
                case "DRIVING":
                    return (Color.parseColor(ModeColor.DRIVING.name));
                case "BICYCLING":
                    return (Color.parseColor(ModeColor.BICYCLING.name));
                default:
                    return null;
            }
        } catch(JSONException e){
            e.printStackTrace();
            return null;
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

    private void assignInformationToMapInformation(JSONObject json){
        // Get Instruction
        Instruction instruction;
        Distance distance;
        Duration duration;
        Path path;
        try {
            instruction = new Instruction(json.getString("html_instructions"));
            distance = new Distance(json.getJSONObject("distance").getString("text"),
                                    json.getJSONObject("distance").getInt("value"));
            duration = new Duration(json.getJSONObject("duration").getString("text"),
                                    json.getJSONObject("duration").getInt("value"));
            path = new Path(distance, instruction, duration, json.getString("travel_mode"));
            mapInformation.addPath(path);
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
                    totalDistance = new Distance(jsonObject.getJSONObject(name).getString("text"),
                                                 jsonObject.getJSONObject(name).getInt("value"));
                    mapInformation.setTotalDistance(totalDistance);
                    break;
                case "duration":
                    totalDuration = new Duration(jsonObject.getJSONObject(name).getString("text"),
                                                 jsonObject.getJSONObject(name).getInt("value"));
                    mapInformation.setTotalDuration(totalDuration);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
