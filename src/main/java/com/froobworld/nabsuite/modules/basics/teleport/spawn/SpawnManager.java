package com.froobworld.nabsuite.modules.basics.teleport.spawn;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.*;
import java.util.UUID;

public class SpawnManager implements Listener {
    private final BasicsModule basicsModule;
    private final File spawnFile;
    private Location spawnLocation;

    public SpawnManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        this.spawnFile = new File(basicsModule.getDataFolder(), "spawn.json");
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
    }

    public Location getSpawnLocation() {
        return spawnLocation == null ? Bukkit.getWorlds().get(0).getSpawnLocation() : spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        Bukkit.getWorlds().get(0).setSpawnLocation(spawnLocation);
        Bukkit.getScheduler().runTaskAsynchronously(basicsModule.getPlugin(), () -> writeSpawn(spawnLocation, spawnFile));
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!event.isAnchorSpawn() && !event.isBedSpawn()) {
            event.setRespawnLocation(getSpawnLocation());
        }
    }

    private void readSpawn() {
        if (!spawnFile.exists()) {
            spawnLocation = null;
            return;
        }
        try (JsonReader jsonReader = new JsonReader(new FileReader(spawnFile))) {
            UUID worldUuid = null;
            Double x = null;
            Double y = null;
            Double z = null;
            Float yaw = null;
            Float pitch = null;
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "world" -> worldUuid = UUID.fromString(jsonReader.nextString());
                    case "x" -> x = jsonReader.nextDouble();
                    case "y" -> y = jsonReader.nextDouble();
                    case "z" -> z = jsonReader.nextDouble();
                    case "yaw" -> yaw = (float) jsonReader.nextDouble();
                    case "pitch" -> pitch = (float) jsonReader.nextDouble();
                }
            }
            jsonReader.endObject();
            //noinspection ConstantConditions
            spawnLocation = new Location(Bukkit.getWorld(worldUuid), x, y, z, yaw, pitch);
        } catch (Exception e) {
            spawnLocation = null;
        }
    }

    private static synchronized void writeSpawn(Location location, File spawnFile) {
        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(spawnFile))) {
            jsonWriter.beginObject()
                    .name("world").value(location.getWorld().getUID().toString())
                    .name("x").value(location.getX())
                    .name("y").value(location.getY())
                    .name("z").value(location.getZ())
                    .name("yaw").value(location.getYaw())
                    .name("pitch").value(location.getPitch())
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
