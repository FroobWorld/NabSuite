package com.froobworld.nabsuite.modules.admin.vanish;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.dynmap.DynmapAPI;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

public class VanishManager {
    public static final String VANISH_PERMISSION = "nabsuite.vanish";
    public static final String VANISH_SEE_PERMISSION = "nabsuite.vanish.see";
    private final NamespacedKey vanishPdcKey;
    private final NamespacedKey vanishBackExemptionKey;
    private final AdminModule adminModule;
    private final Map<Player, Boolean> vanishCache = new WeakHashMap<>();

    public VanishManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        this.vanishPdcKey = NamespacedKey.fromString("vanish", adminModule.getPlugin());
        this.vanishBackExemptionKey = NamespacedKey.fromString("vanish-back-exemption", adminModule.getPlugin());
        Bukkit.getPluginManager().registerEvents(new VanishEnforcer(this), adminModule.getPlugin());
    }

    void globalUpdateVanish() {
        Bukkit.getOnlinePlayers().forEach(this::updateVanish);
    }

    void updateVanish(Player player) {
        if (isVanished(player) && !player.hasPermission(VANISH_PERMISSION)) {
            setVanished(player, false);
        }
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (otherPlayer.equals(player)) {
                continue;
            }
            if (isVanished(player) && !otherPlayer.hasPermission(VANISH_SEE_PERMISSION)) {
                otherPlayer.hidePlayer(adminModule.getPlugin(), player);
            } else {
                otherPlayer.showPlayer(adminModule.getPlugin(), player);
            }
        }
        BasicsModule basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
        if (isVanished(player)) {
            basicsModule.getBackManager().addBackExemption(player, vanishBackExemptionKey);
            setSilentChests(player, true);
        } else {
            basicsModule.getBackManager().removeBackExemption(player, vanishBackExemptionKey);
            setSilentChests(player, false);
        }
        DynmapAPI dynmapAPI = adminModule.getPlugin().getHookManager().getDynmapHook().getDynmapAPI();
        if (dynmapAPI != null) {
            dynmapAPI.assertPlayerInvisibility(player, isVanished(player), adminModule.getPlugin());
        }
    }

    public void setVanished(Player player, boolean vanished) {
        Component notification = Component.empty();
        if (vanished) {
            player.getPersistentDataContainer().set(vanishPdcKey, PreVanishLocation.DATA_TYPE, new PreVanishLocation(player.getLocation()));
            vanishCache.put(player, true);
            notification = notification.append(player.displayName())
                    .append(Component.text(" has vanished."))
                    .color(NamedTextColor.YELLOW);
        } else {
            PreVanishLocation preVanishLocation = player.getPersistentDataContainer().get(vanishPdcKey, PreVanishLocation.DATA_TYPE);
            player.getPersistentDataContainer().remove(vanishPdcKey);
            vanishCache.put(player, false);
            notification = notification.append(player.displayName())
                    .append(Component.text(" is no longer vanished."))
                    .color(NamedTextColor.YELLOW);
            if (preVanishLocation != null) {
                adminModule.getPlugin().getModule(BasicsModule.class).getPlayerTeleporter().teleport(player, preVanishLocation.location);
            }
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(VANISH_SEE_PERMISSION) || onlinePlayer.equals(player)) {
                onlinePlayer.sendMessage(notification);
            }
        }
        updateVanish(player);
    }

    public boolean isVanished(Player player) {
        return vanishCache.computeIfAbsent(player, p -> p.getPersistentDataContainer().get(vanishPdcKey, PreVanishLocation.DATA_TYPE) != null);
    }


    private static class PreVanishLocation {
        private static final SimpleDataSchema<PreVanishLocation> SCHEMA = new SimpleDataSchema.Builder<PreVanishLocation>()
                .addField("location", SchemaEntries.locationEntry(
                        preVanishLocation -> preVanishLocation.location,
                        (preVanishLocation, location) -> preVanishLocation.location = location
                ))
                .build();
        private static final PersistentDataType<String, PreVanishLocation> DATA_TYPE = new PersistentDataType<>() {
            @Override
            public @NotNull Class<String> getPrimitiveType() {
                return String.class;
            }

            @Override
            public @NotNull Class<PreVanishLocation> getComplexType() {
                return PreVanishLocation.class;
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public String toPrimitive(@NotNull PreVanishLocation preVanishLocation, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
                try {
                    return PreVanishLocation.SCHEMA.toJsonString(preVanishLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public PreVanishLocation fromPrimitive(@NotNull String s, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
                try {
                    PreVanishLocation preVanishLocation = new PreVanishLocation();
                    PreVanishLocation.SCHEMA.populateFromJsonString(preVanishLocation, s);
                    return preVanishLocation;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        private Location location;

        private PreVanishLocation() {}

        public PreVanishLocation(Location location) {
            this.location = location;
        }

    }

    private void setSilentChests(Player player, boolean silentChests) {
        if (Reflect.on(player).call("getHandle").fields().containsKey("silentChests")) {
            Reflect.on(player)
                    .call("getHandle")
                    .set("silentChests", silentChests);
        }
    }

}
