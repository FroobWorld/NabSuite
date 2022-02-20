package com.froobworld.nabsuite.data;

import com.froobworld.nabsuite.util.CheckedBiConsumer;
import com.froobworld.nabsuite.util.CheckedBiFunction;
import com.froobworld.nabsuite.util.CheckedFunction;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SchemaEntries {

    private SchemaEntries() {}

    public static <T> SimpleDataSchema.SchemaEntry<T> booleanEntry(Function<T, Boolean> fieldReference, BiConsumer<T, Boolean> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> fieldPopulator.accept(t, jsonReader.nextBoolean()),
                (t, jsonWriter) -> jsonWriter.value(fieldReference.apply(t))
        );
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> stringEntry(Function<T, String> fieldReference, BiConsumer<T, String> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> fieldPopulator.accept(t, jsonReader.nextString()),
                (t, jsonWriter) -> jsonWriter.value(fieldReference.apply(t))
        );
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> integerEntry(Function<T, Integer> fieldReference, BiConsumer<T, Integer> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> fieldPopulator.accept(t, jsonReader.nextInt()),
                (t, jsonWriter) -> jsonWriter.value(fieldReference.apply(t))
        );
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> longEntry(Function<T, Long> fieldReference, BiConsumer<T, Long> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> fieldPopulator.accept(t, jsonReader.nextLong()),
                (t, jsonWriter) -> jsonWriter.value(fieldReference.apply(t))
        );
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> doubleEntry(Function<T, Double> fieldReference, BiConsumer<T, Double> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> fieldPopulator.accept(t, jsonReader.nextDouble()),
                (t, jsonWriter) -> jsonWriter.value(fieldReference.apply(t))
        );
    }

    public static <T, E extends Enum<E>> SimpleDataSchema.SchemaEntry<T> enumEntry(Class<E> type, Function<T, E> fieldReference, BiConsumer<T, E> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> fieldPopulator.accept(t, Enum.valueOf(type, jsonReader.nextString())),
                (t, jsonWriter) -> jsonWriter.value(fieldReference.apply(t).toString())
        );
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> worldEntry(Function<T, World> fieldReference, BiConsumer<T, World> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> fieldPopulator.accept(t, Bukkit.getWorld(UUID.fromString(jsonReader.nextString()))),
                (t, jsonWriter) -> jsonWriter.value(fieldReference.apply(t).getUID().toString())
        );
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> locationEntry(Function<T, Location> fieldReference, BiConsumer<T, Location> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> {
                    World world = null;
                    double x = 0, y = 0, z = 0;
                    float yaw = 0, pitch = 0;
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String name = jsonReader.nextName();
                        if (name.equalsIgnoreCase("world")) {
                            world = Bukkit.getWorld(UUID.fromString(jsonReader.nextString()));
                        } else if (name.equalsIgnoreCase("x")) {
                            x = jsonReader.nextDouble();
                        } else if (name.equalsIgnoreCase("y")) {
                            y = jsonReader.nextDouble();
                        } else if (name.equalsIgnoreCase("z")) {
                            z = jsonReader.nextDouble();
                        } else if (name.equalsIgnoreCase("yaw")) {
                            yaw = (float) jsonReader.nextDouble();
                        } else if (name.equalsIgnoreCase("pitch")) {
                            pitch = (float) jsonReader.nextDouble();
                        }
                    }
                    jsonReader.endObject();
                    fieldPopulator.accept(t, new Location(world, x, y, z, yaw, pitch));
                },
                (t, jsonWriter) -> {
                    Location location = fieldReference.apply(t);
                    jsonWriter.beginObject();
                    jsonWriter.name("world");
                    jsonWriter.value(location.getWorld().getUID().toString());
                    jsonWriter.name("x");
                    jsonWriter.value(location.getX());
                    jsonWriter.name("y");
                    jsonWriter.value(location.getY());
                    jsonWriter.name("z");
                    jsonWriter.value(location.getZ());
                    jsonWriter.name("yaw");
                    jsonWriter.value(location.getYaw());
                    jsonWriter.name("pitch");
                    jsonWriter.value(location.getPitch());
                    jsonWriter.endObject();
                }
        );
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> vectorEntry(Function<T, Vector> fieldReference, BiConsumer<T, Vector> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> {
                    double x = 0, y = 0, z = 0;
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String name = jsonReader.nextName();
                        if (name.equalsIgnoreCase("x")) {
                            x = jsonReader.nextDouble();
                        } else if (name.equalsIgnoreCase("y")) {
                            y = jsonReader.nextDouble();
                        } else if (name.equalsIgnoreCase("z")) {
                            z = jsonReader.nextDouble();
                        }
                    }
                    jsonReader.endObject();
                    fieldPopulator.accept(t, new Vector(x, y, z));
                },
                (t, jsonWriter) -> {
                    Vector vector = fieldReference.apply(t);
                    jsonWriter.beginObject();
                    jsonWriter.name("x");
                    jsonWriter.value(vector.getX());
                    jsonWriter.name("y");
                    jsonWriter.value(vector.getY());
                    jsonWriter.name("z");
                    jsonWriter.value(vector.getZ());
                    jsonWriter.endObject();
                }
        );
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> uuidEntry(Function<T, UUID> fieldReference, BiConsumer<T, UUID> fieldPopulator) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> fieldPopulator.accept(t, UUID.fromString(jsonReader.nextString())),
                (t, jsonWriter) -> jsonWriter.value(fieldReference.apply(t).toString())
        );
    }

    public static <T, E, C extends Collection<E>> SimpleDataSchema.SchemaEntry<T> collectionEntry(Function<T, C> fieldReference, BiConsumer<T, C> fieldPopulator, CheckedBiFunction<JsonReader, T, E> entryParser, CheckedBiConsumer<E, JsonWriter> entrySerialiser, Supplier<C> emptyCollectionSupplier) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> {
                    C collection = emptyCollectionSupplier.get();
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        collection.add(entryParser.apply(jsonReader, t));
                    }
                    jsonReader.endArray();
                    fieldPopulator.accept(t, collection);
                },
                (t, jsonWriter) -> {
                    jsonWriter.beginArray();
                    for (E element : fieldReference.apply(t)) {
                        entrySerialiser.accept(element, jsonWriter);
                    }
                    jsonWriter.endArray();
                }
        );
    }

    public static <T, E> SimpleDataSchema.SchemaEntry<T> listEntry(Function<T, List<E>> fieldReference, BiConsumer<T, List<E>> fieldPopulator, CheckedBiFunction<JsonReader, T, E> entryParser, CheckedBiConsumer<E, JsonWriter> entrySerialiser) {
        return collectionEntry(fieldReference, fieldPopulator, entryParser, entrySerialiser, ArrayList::new);
    }

    public static <T, E> SimpleDataSchema.SchemaEntry<T> setEntry(Function<T, Set<E>> fieldReference, BiConsumer<T, Set<E>> fieldPopulator, CheckedBiFunction<JsonReader, T, E> entryParser, CheckedBiConsumer<E, JsonWriter> entrySerialiser) {
        return collectionEntry(fieldReference, fieldPopulator, entryParser, entrySerialiser, HashSet::new);
    }

    public static <T> SimpleDataSchema.SchemaEntry<T> stringListEntry(Function<T, List<String>> fieldReference, BiConsumer<T, List<String>> fieldPopulator) {
        return listEntry(fieldReference, fieldPopulator, (jsonReader, t) -> jsonReader.nextString(), (string, jsonWriter) -> jsonWriter.value(string));
    }

    public static <T, V, M extends Map<String, V>> SimpleDataSchema.SchemaEntry<T> mapEntry(Function<T, M> fieldReference, BiConsumer<T, M> fieldPopulator, CheckedFunction<JsonReader, V> entryParser, CheckedBiConsumer<V, JsonWriter> entrySerialiser, Supplier<M> emptyMapSupplier) {
        return new SimpleDataSchema.SchemaEntry<>(
                t -> fieldReference.apply(t) != null,
                (jsonReader, t) -> {
                    M map = emptyMapSupplier.get();
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        map.put(jsonReader.nextName(), entryParser.apply(jsonReader));
                    }
                    jsonReader.endArray();
                    fieldPopulator.accept(t, map);
                },
                (t, jsonWriter) -> {
                    jsonWriter.beginArray();
                    for (Map.Entry<String, V> entry : fieldReference.apply(t).entrySet()) {
                        jsonWriter.name(entry.getKey());
                        entrySerialiser.accept(entry.getValue(), jsonWriter);
                    }
                    jsonWriter.endArray();
                }
        );
    }

}
