package com.froobworld.nabsuite.data;

import com.froobworld.nabsuite.util.CheckedBiConsumer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class SimpleDataSchema<T> {
    private final Map<String, SchemaEntry<T>> schemaEntryMap;

    private SimpleDataSchema(Map<String, SchemaEntry<T>> schemaEntryMap) {
        this.schemaEntryMap = schemaEntryMap;
    }

    public void populateFromJsonString(T data, String jsonString) throws IOException {
        try (JsonReader jsonReader = new JsonReader(new StringReader(jsonString))) {
            populate(data, jsonReader);
        }
    }

    public void populate(T data, JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            SchemaEntry<T> schemaEntry = schemaEntryMap.get(jsonReader.nextName());
            if (schemaEntry != null) {
                schemaEntry.parseToPopulate.accept(jsonReader, data);
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }

    public String toJsonString(T data) throws IOException {
        try (StringWriter stringWriter = new StringWriter(); JsonWriter jsonWriter = new JsonWriter(stringWriter)) {
            write(data, jsonWriter);
            return stringWriter.toString();
        }
    }

    public void write(T data, JsonWriter jsonWriter) throws IOException {
        jsonWriter.setIndent("  ");
        jsonWriter.beginObject();
        for (Map.Entry<String, SchemaEntry<T>> entry : schemaEntryMap.entrySet()) {
            if (entry.getValue().shouldWrite.apply(data)) {
                jsonWriter.name(entry.getKey());
                entry.getValue().readToSerialise.accept(data, jsonWriter);
            }
        }
        jsonWriter.endObject();
    }

    public static class Builder<T> {
        private final Map<String, SchemaEntry<T>> schemaEntryMap;

        public Builder() {
            this.schemaEntryMap = new LinkedHashMap<>();
        }


        public Builder<T> addField(String fieldName, Function<T, Boolean> shouldWrite,CheckedBiConsumer<JsonReader, T> parseToPopulate, CheckedBiConsumer<T, JsonWriter> readToSerialise) {
            return addField(fieldName, new SchemaEntry<>(shouldWrite, parseToPopulate, readToSerialise));
        }

        public  Builder<T> addField(String fieldName, SchemaEntry<T> schemaEntry) {
            schemaEntryMap.put(fieldName, schemaEntry);
            return this;
        }

        public SimpleDataSchema<T> build() {
            return new SimpleDataSchema<>(schemaEntryMap);
        }

    }

    public static class SchemaEntry<T> {
        private final Function<T, Boolean> shouldWrite;
        private final CheckedBiConsumer<JsonReader, T> parseToPopulate;
        private final CheckedBiConsumer<T, JsonWriter> readToSerialise;

        public SchemaEntry(Function<T, Boolean> shouldWrite, CheckedBiConsumer<JsonReader, T> parseToPopulate, CheckedBiConsumer<T, JsonWriter> readToSerialise) {
            this.shouldWrite = shouldWrite;
            this.parseToPopulate = parseToPopulate;
            this.readToSerialise = readToSerialise;
        }
    }
}
