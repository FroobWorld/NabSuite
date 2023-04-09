package com.froobworld.nabsuite.modules.nabmode.nabdimension;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.nabmode.NabModeModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import static org.joor.Reflect.*;

public class NabDimensionManager implements Listener {
    private final NabModeModule nabModeModule;
    private boolean haveWarned = false;
    private final World nabWorld;

    public NabDimensionManager(NabModeModule nabModeModule) {
        this.nabModeModule = nabModeModule;
        nabWorld = Bukkit.getServer().createWorld(WorldCreator.name("nabworld"));
        nabModeModule.getPlugin().getSLF4JLogger().info("Loaded nabworld with uid '" + nabWorld.getUID() + "'");
        nabModeModule.getPlugin().getHookManager().getSchedulerHook().runRepeatingTask(this::loop, 1, 1);
        Bukkit.getPluginManager().registerEvents(this, nabModeModule.getPlugin());
    }

    public World getNabWorld() {
        return nabWorld;
    }

    private boolean hasFlagSet() {
        try {
            return on(nabWorld)
                    .call("getHandle")
                    .call("n_") // get WorldData
                    .get("creativeLevel");
        } catch (Exception e) {
            if (!haveWarned) {
                nabModeModule.getPlugin().getSLF4JLogger().error("Mappings update required?", e);
                haveWarned = true;
            }
            return false;
        }
    }

    @EventHandler
    private void onAdvancementProgress(PlayerAdvancementCriterionGrantEvent event) {
        if (event.getPlayer().getWorld().equals(nabWorld)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onExperienceGain(PlayerPickupExperienceEvent event) {
        if (event.getPlayer().getWorld().equals(nabWorld)) {
            event.getExperienceOrb().setExperience(0);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getLocation().getWorld().equals(nabWorld) && !hasFlagSet()) {
            BasicsModule basicsModule = nabModeModule.getPlugin().getModule(BasicsModule.class);
            Location spawnLocation;
            if (basicsModule != null) {
                spawnLocation = basicsModule.getSpawnManager().getSpawnLocation();
            } else {
                spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            }
            event.getPlayer().teleport(spawnLocation);
            event.getPlayer().sendMessage(Component.text("You've been moved to spawn. The world you were previously in is currently disabled.", NamedTextColor.RED));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo().getWorld().equals(nabWorld) && !hasFlagSet()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Teleport failed. The world you tried to teleport to is currently disabled.", NamedTextColor.RED));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getPlayer().getWorld().equals(nabWorld)) {
                if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Component.text("Ender chests are disabled in this world.", NamedTextColor.RED));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onWorldChange(PlayerChangedWorldEvent event) {
        if (!nabModeModule.getNabModeManager().isNabMode(event.getPlayer()) || !event.getPlayer().getWorld().equals(nabWorld)) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                event.getPlayer().setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    private void loop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            nabModeModule.getPlugin().getHookManager().getSchedulerHook().runEntityTaskAsap(() -> {
                if (player.getWorld().equals(nabWorld) && !hasFlagSet()) {
                    BasicsModule basicsModule = nabModeModule.getPlugin().getModule(BasicsModule.class);
                    Location spawnLocation;
                    if (basicsModule != null) {
                        spawnLocation = basicsModule.getSpawnManager().getSpawnLocation();
                    } else {
                        spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
                    }
                    player.teleport(spawnLocation);
                    player.sendMessage(Component.text("You've been moved to spawn. The world you were previously in is currently disabled.", NamedTextColor.RED));
                }
                if (nabModeModule.getNabModeManager().isNabMode(player) && player.getWorld().equals(nabWorld)) {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        player.setGameMode(GameMode.CREATIVE);
                    }
                } else {
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                }
            }, null, player);
        }
    }

}
