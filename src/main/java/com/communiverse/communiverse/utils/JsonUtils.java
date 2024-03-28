package com.communiverse.communiverse.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * Utility class for working with JSON data and Gson library.
 */
public class JsonUtils {

    private static GsonBuilder gsonBuilder = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .addDeserializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getAnnotation(JsonIgnore.class) != null;
                }
                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return clazz.isAnnotationPresent(JsonIgnore.class);
                }})
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getAnnotation(JsonIgnore.class) != null;
                }
                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return clazz.isAnnotationPresent(JsonIgnore.class);
                }});

    private static final Gson gson = gsonBuilder.setPrettyPrinting().create();

    public static Gson getGson() {
        return gson;
    }

    /**
     * Converts a JSON string to a Java object of a specified type.
     *
     * @param json The JSON string.
     * @param type The target type.
     * @param <T>  The generic type.
     * @return The Java object of the specified type.
     */
    public static <T>T fromJson(String json, Type type) {
        return getGson().fromJson(json, type);
    }

    /**
     * Converts a Java object to its JSON representation.
     *
     * @param src The Java object.
     * @return The JSON string.
     */
    public static String toJson(Object src) {
        return getGson().toJson(src);
    }
}
