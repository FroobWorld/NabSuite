package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.teleport.home.Home;
import com.froobworld.nabsuite.modules.basics.teleport.home.Homes;
import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import com.froobworld.nabsuite.util.ConsoleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class RestrictionEnforcer implements Listener {
    private static final long RESTRICTION_DURATION = TimeUnit.HOURS.toMillis(24);
    private static final double SPAWN_NO_INTERACT_DISTANCE = 500;
    private static final double HOUSE_NO_INTERACT_DISTANCE = 100;
    private static final long NOTIFICATION_RATE_LIMIT = TimeUnit.MILLISECONDS.toMillis(500);
    private final AdminModule adminModule;
    private final BasicsModule basicsModule;
    private final PunishmentManager punishmentManager;
    private final Map<Player, Long> lastFailureMessage = new WeakHashMap<>();

    public RestrictionEnforcer(AdminModule adminModule, PunishmentManager punishmentManager) {
        this.adminModule = adminModule;
        this.basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
        this.punishmentManager = punishmentManager;
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(adminModule.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(this::verifyRestrictedStatus), 100, 100);
    }

    public RestrictionPunishment restrict(PlayerIdentity player, CommandSender mediator, String reason) {
        Punishments punishments = punishmentManager.getPunishments(player.getUuid());
        RestrictionPunishment restrictionPunishment = new RestrictionPunishment(reason, ConsoleUtils.getSenderUUID(mediator), System.currentTimeMillis());
        punishments.setRestrictionPunishment(restrictionPunishment);

        Player onlinePlayer = player.asPlayer();
        if (onlinePlayer != null) {
            onlinePlayer.sendMessage(
                    Component.text("You have been restricted (" + reason + ").")
                            .append(Component.newline())
                            .append(Component.text("You are unable to build near spawn or other players' homes."))
                            .append(Component.newline())
                            .append(Component.text("You will be unrestricted pending staff review."))
                            .color(NamedTextColor.RED)
            );
        }

        punishments.addPunishmentLogItem(new PunishmentLogItem(
                punishmentManager.adminModule.getPlugin().getPlayerIdentityManager(),
                PunishmentLogItem.Type.RESTRICTED,
                player.getUuid(),
                restrictionPunishment.getMediator(),
                restrictionPunishment.getTime(),
                -1,
                restrictionPunishment.getReason()
        ));
        return restrictionPunishment;
    }

    public void unrestrict(PlayerIdentity player, UUID mediator) {
        unrestrict(false, player, mediator);
    }

    public void expireRestriction(PlayerIdentity player) {
        unrestrict(true, player, ConsoleUtils.CONSOLE_UUID);
    }

    private void unrestrict(boolean automatic, PlayerIdentity player, UUID mediator) {
        Punishments punishments = punishmentManager.getPunishments(player.getUuid());
        punishments.setRestrictionPunishment(null);
        punishments.addPunishmentLogItem(new PunishmentLogItem(
                punishmentManager.adminModule.getPlugin().getPlayerIdentityManager(),
                automatic ? PunishmentLogItem.Type.UNRESTRICTED_AUTOMATIC : PunishmentLogItem.Type.UNRESTRICTED_MANUAL,
                player.getUuid(),
                mediator,
                System.currentTimeMillis(),
                -1,
                null
        ));
        Player onlinePlayer = player.asPlayer();
        if (onlinePlayer != null) {
            onlinePlayer.sendMessage(
                    Component.text("You have been unrestricted.", NamedTextColor.YELLOW)
            );
        }
    }

    private void verifyRestrictedStatus(Player player) {
        RestrictionPunishment restrictionPunishment = punishmentManager.getPunishments(player.getUniqueId()).getRestrictionPunishment();
        if (restrictionPunishment != null) {
            if (System.currentTimeMillis() >= restrictionPunishment.getTime() + RESTRICTION_DURATION) {
                expireRestriction(adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(player));
            }
        }
    }

    private boolean canInteract(Player player, Location location, boolean informOnFailure) {
        if (basicsModule == null) {
            return true;
        }
        verifyRestrictedStatus(player);
        RestrictionPunishment restrictionPunishment = punishmentManager.getPunishments(player.getUniqueId()).getRestrictionPunishment();
        if (restrictionPunishment == null) {
            return true;
        }

        boolean failed = false;
        Location spawnLocation = basicsModule.getSpawnManager().getSpawnLocation();
        if (spawnLocation != null && spawnLocation.getWorld().equals(player.getWorld())) {
            failed = Math.max(Math.abs(spawnLocation.getBlockX() - location.getBlockX()), Math.abs(spawnLocation.getBlockZ() - location.getBlockZ())) < SPAWN_NO_INTERACT_DISTANCE;
        }
        if (!failed) {
            for (Homes homes : basicsModule.getHomeManager().getAllHomes()) {
                if (player.getUniqueId().equals(homes.getUuid())) {
                    continue;
                }
                for (Home home : homes.getHomes()) {
                    Location homeLocation = home.getLocation();
                    if (!homeLocation.getWorld().equals(location.getWorld())) {
                        continue;
                    }
                    if (Math.max(Math.abs(homeLocation.getBlockX() - location.getBlockX()), Math.abs(homeLocation.getBlockZ() - location.getBlockZ())) < HOUSE_NO_INTERACT_DISTANCE) {
                        if (!basicsModule.getPlayerDataManager().getFriendManager().areFriends(player, homes.getUuid())) {
                            failed = true;
                            break;
                        }
                    }
                }
            }
        }
        if (failed) {
            if (System.currentTimeMillis() - lastFailureMessage.getOrDefault(player, -1L) > NOTIFICATION_RATE_LIMIT) {
                player.sendMessage(
                        Component.text("You have been restricted (" + restrictionPunishment.getReason() + ").")
                                .append(Component.newline())
                                .append(Component.text("You are unable to build near spawn or other players' homes."))
                                .append(Component.newline())
                                .append(Component.text("You will be unrestricted pending staff review."))
                                .color(NamedTextColor.RED)
                );
                lastFailureMessage.put(player, System.currentTimeMillis());
            }
        }
        return !failed;
    }

    @EventHandler(ignoreCancelled = true)
    private void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR || event.getClickedBlock() == null) {
            return;
        }
        if (!canInteract(event.getPlayer(), event.getClickedBlock().getLocation(), event.getAction() != Action.PHYSICAL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBucketFill(PlayerBucketFillEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onBlockIgniteEvent(BlockIgniteEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onSignChange(SignChangeEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onHangingPlace(HangingPlaceEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onHangingBreak(HangingBreakByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getRemover());
        if (causer == null) {
            return;
        }
        if (!canInteract(causer, event.getEntity().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onHitArmourStand(EntityDamageByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getDamager());
        if (causer == null) {
            return;
        }
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }
        if (!canInteract(causer, event.getEntity().getLocation(), true)) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getRightClicked().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getRightClicked().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getEntity().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getEntity().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getEntity().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onCombustEntity(EntityCombustByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getCombuster());
        if (causer == null) {
            return;
        }
        if (!canInteract(causer, event.getEntity().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getDamager());
        if (causer == null) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (event.getEntity() instanceof Monster && event.getEntity().customName() == null && causer.equals(((Monster) event.getEntity()).getTarget())) {
            return; // Allow the killing of un-named monsters that are attacking the player
        }
        if (!canInteract(causer, event.getEntity().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onVehicleDamage(VehicleDamageEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getAttacker());
        if (causer == null) {
            return;
        }
        if (!canInteract(causer, event.getVehicle().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onVehicleDestroy(VehicleDestroyEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getAttacker());
        if (causer == null) {
            return;
        }
        if (!canInteract(causer, event.getVehicle().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onVehicleEnter(VehicleEnterEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getEntered());
        if (causer == null) {
            return;
        }
        if (!canInteract(causer, event.getVehicle().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onArmourStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (!canInteract(event.getPlayer(), event.getRightClicked().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

}
