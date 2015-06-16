package com.mapster.itinerary.serialisation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mapster.itinerary.ItineraryItem;

import java.lang.reflect.Type;

/**
 * Created by Harriet on 6/16/2015.
 */
public class ItineraryItemAdapter implements JsonSerializer<ItineraryItem>,
        JsonDeserializer<ItineraryItem> {

    private static final String CLASSNAME = "CLASSNAME";
    private static final String INSTANCE  = "INSTANCE";

    @Override
    public ItineraryItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonPrimitive primitive = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String className = primitive.getAsString();

        Class<?> klass = null;
        try {
            klass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new JsonParseException(e.getMessage());
        }
        return context.deserialize(jsonObject.get(INSTANCE), klass);
    }

    @Override
    public JsonElement serialize(ItineraryItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonItem = new JsonObject();
        String className = src.getClass().getCanonicalName();
        jsonItem.addProperty(CLASSNAME, className);
        JsonElement element = context.serialize(src);
        jsonItem.add(INSTANCE, element);
        return jsonItem;
    }
}
