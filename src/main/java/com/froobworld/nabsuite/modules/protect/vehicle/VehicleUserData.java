package com.froobworld.nabsuite.modules.protect.vehicle;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class VehicleUserData {
    public static final PersistentDataType<String, VehicleUserData> DATA_TYPE = new DataType();
    private final Set<UUID> users = new HashSet<>();

    public void addUser(Player player) {
        users.add(player.getUniqueId());
    }

    public boolean isUser(Player player) {
        return users.contains(player.getUniqueId());
    }

    private static class DataType implements PersistentDataType<String, VehicleUserData> {

        @Override
        public @NotNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NotNull Class getComplexType() {
            return VehicleUserData.class;
        }

        @Override
        public @NotNull String toPrimitive(@NotNull VehicleUserData complex, @NotNull PersistentDataAdapterContext context) {
            return complex.users.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(";"));
        }

        @Override
        public @NotNull VehicleUserData fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
            VehicleUserData vehicleUserData = new VehicleUserData();
            for (String uuid : primitive.split(";")) {
                if (uuid.isEmpty()) {
                    continue;
                }
                vehicleUserData.users.add(UUID.fromString(uuid));
            }
            return vehicleUserData;
        }

    }

}
