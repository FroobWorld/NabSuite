package com.froobworld.nabsuite.data.playervar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class PlayerVars {
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();

    private PlayerVarsManager playerVarsManager;
    @Expose private final UUID uuid;
    @Expose private final Map<String, JsonElement> vars;

    PlayerVars(PlayerVarsManager playerVarsManager, UUID uuid) {
        this.playerVarsManager = playerVarsManager;
        this.uuid = uuid;
        this.vars = new HashMap<>();
    }

    private PlayerVars() {
        this(null, null);
    }

    public UUID getUuid() {
        return uuid;
    }

    public <T> T getOrDefault(String key, Class<T> clazz, T defaultValue) {
        T value = get(key, clazz);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public <T> T get(String key, Class<T> clazz) {
        JsonElement element = vars.get(key);
        if (element == null) {
            return null;
        }
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Value for key '" + key + "' is not a JSON primitive.");
        }
        return gson.fromJson(element, clazz);
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        JsonElement element = vars.get(key);
        if (element == null) {
            return null;
        }
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Value for key '" + key + "' is not a JSON array.");
        }
        Type listType = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(element, listType);
    }

    public void put(String key, Object value) {
        if (value == null) {
            vars.put(key, null);
        } else {
            JsonElement element = gson.toJsonTree(value);
            if (!element.isJsonPrimitive()) {
                throw new IllegalArgumentException(
                        "Value must be a JSON primitive (int, long, boolean, or string)."
                );
            }
            vars.put(key, element);
        }
        playerVarsManager.playerVarsSaver.scheduleSave(this);
    }

    public void putCollection(String key, Collection<?> value) {
        if (value == null) {
            vars.put(key, null);
        } else {
            JsonElement element = gson.toJsonTree(value);
            if (!element.isJsonArray()) {
                throw new IllegalArgumentException("Value must be converted to a JSON array.");
            }
            JsonArray array = element.getAsJsonArray();
            for (JsonElement el : array) {
                if (!el.isJsonPrimitive()) {
                    throw new IllegalArgumentException("Each element in the collection must be a JSON primitive.");
                }
            }
            vars.put(key, element);
        }
        playerVarsManager.playerVarsSaver.scheduleSave(this);
    }

    public String toJsonString() {
        return gson.toJson(this);
    }

    public static PlayerVars fromJsonString(PlayerVarsManager playerVarsManager, String json) {
        PlayerVars playerVars = gson.fromJson(json, PlayerVars.class);
        playerVars.playerVarsManager = playerVarsManager;
        return playerVars;
    }

}
