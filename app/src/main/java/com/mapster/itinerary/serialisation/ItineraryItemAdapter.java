package com.mapster.itinerary.serialisation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mapster.itinerary.ItineraryItem;

import java.lang.reflect.Type;

/**
 * Created by Harriet on 6/16/2015.
 */
public class ItineraryItemAdapter implements JsonSerializer<ItineraryItem>,
        JsonDeserializer<ItineraryItem> {
    @Override
    public ItineraryItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(ItineraryItem src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }
}
