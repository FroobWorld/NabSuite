package com.froobworld.nabsuite.data.identity;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Deprecated // Mojang to remove API access to name history
public class NameHistory {
    private final List<NameHistoryEntry> nameHistoryEntries;

    public NameHistory(List<NameHistoryEntry> nameHistoryEntries) {
        this.nameHistoryEntries = nameHistoryEntries;
    }

    public final List<NameHistoryEntry> getEntries() {
        return new ArrayList<>(nameHistoryEntries);
    }

    public static CompletableFuture<NameHistory> fromUuid(UUID uuid) {
        final CompletableFuture<NameHistory> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            List<NameHistoryEntry> nameHistoryEntries = new ArrayList<>();
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names").openConnection();
                JsonReader jsonReader = new JsonReader(new InputStreamReader(connection.getInputStream()));
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject();
                    String previousName = null;
                    long time = -1;
                    while (jsonReader.hasNext()) {
                        String name = jsonReader.nextName();
                        if (name.equalsIgnoreCase("name")) {
                            previousName = jsonReader.nextString();
                        } else if (name.equalsIgnoreCase("changedToAt")) {
                            time = jsonReader.nextLong();
                        }
                    }
                    nameHistoryEntries.add(new NameHistoryEntry(time, previousName));
                    jsonReader.endObject();
                }
                jsonReader.endArray();
            } catch (IOException e) {
                future.completeExceptionally(e);
                return;
            }
            future.complete(new NameHistory(nameHistoryEntries));
        });
        return future;
    }

    public static class NameHistoryEntry {
        public final long time;
        public final String name;

        private NameHistoryEntry(long time, String name) {
            this.time = time;
            this.name = name;
        }
    }

}
