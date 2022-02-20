package com.froobworld.nabsuite.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class DataLoader {

    public static <D, K> Map<K, D> loadAll(File directory, Predicate<String> namePredicate, Function<byte[], D> deserialiser, BiFunction<String, D, K> identifierFunc) {
        File[] files = directory.listFiles();
        Map<K, D> dataMap = new HashMap<>();
        if (files != null) {
            for (File file : files) {
                if (!namePredicate.test(file.getName())) continue;
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    D data = deserialiser.apply(bytes);
                    K identifier = identifierFunc.apply(file.getName(), data);
                    dataMap.put(identifier, data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataMap;
    }

}
