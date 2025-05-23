package com.froobworld.nabsuite.modules.admin.vanish;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class VanishEnforcer implements Listener {
    private static final int PERK_DISTANCE = 150;
    private static final long PERK_CHECK_TIMEOUT = TimeUnit.MINUTES.toMillis(1);
    private final AdminModule adminModule;
    private final VanishManager vanishManager;
    private final Map<Player, Long> lastSuccessfulPerkCheck = new WeakHashMap<>();

    public VanishEnforcer(AdminModule adminModule, VanishManager vanishManager) {
        this.adminModule = adminModule;
        this.vanishManager = vanishManager;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(adminModule.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
            if (vanishManager.isVanished(player)) {
                perkCheck(player);
            }
        }), 20, 20);
    }

    private boolean perkCheck(Player player) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (player.canSee(otherPlayer) && !otherPlayer.canSee(player) && otherPlayer.getWorld() == player.getWorld()) {
                if (otherPlayer.getLocation().distanceSquared(player.getLocation()) <= PERK_DISTANCE * PERK_DISTANCE) {
                    lastSuccessfulPerkCheck.put(player, System.currentTimeMillis());
                    return true;
                }
            }
        }
        return System.currentTimeMillis() - lastSuccessfulPerkCheck.getOrDefault(player, 0L) < PERK_CHECK_TIMEOUT;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        vanishManager.globalUpdateVanish();
        if (vanishManager.isVanished(event.getPlayer())) {
            event.getPlayer().sendMessage(
                    Component.text("You are currently vanished.").color(NamedTextColor.YELLOW)
            );
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (vanishManager.isVanished(player) && perkCheck(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (event.getFoodLevel() > event.getEntity().getFoodLevel()) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (vanishManager.isVanished(player) && perkCheck(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        if (vanishManager.isVanished(player) && perkCheck(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWardenAngerChange(WardenAngerChangeEvent event) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        if (vanishManager.isVanished(player) && perkCheck(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPhantomSpawn(PhantomPreSpawnEvent event) {
        if (!(event.getSpawningEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getSpawningEntity();
        if (vanishManager.isVanished(player) && perkCheck(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockForm(EntityBlockFormEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockBreakEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamageEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (vanishManager.isVanished((Player) event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupExperience(PlayerPickupExperienceEvent event) {
        if (event.getExperienceOrb().getSpawnReason() == ExperienceOrb.SpawnReason.FURNACE) {
            return;
        }
        Player player = event.getPlayer();
        if (vanishManager.isVanished(player) && perkCheck(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (vanishManager.isVanished(event.getEntity())) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            event.setKeepLevel(true);
            event.setShouldDropExperience(false);
            event.getEntity().sendMessage(
                    Component.text("As you are vanished, you have kept your items and experience.")
            );
        }
    }

    @EventHandler
    public void onGameEvent(BlockReceiveGameEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (vanishManager.isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onElytraBoost(PlayerElytraBoostEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.getFirework().setSilent(true);
            FireworkMeta fireworkMeta = event.getFirework().getFireworkMeta();
            fireworkMeta.clearEffects();
            event.getFirework().setFireworkMeta(fireworkMeta);
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (!player.equals(event.getPlayer())) {
                    player.hideEntity(adminModule.getPlugin(), event.getFirework());
                }
            });
        }
    }

    @EventHandler
    public void onAdvancementProgress(PlayerAdvancementCriterionGrantEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        if (event.getEntity() instanceof Firework && ((Firework) event.getEntity()).getAttachedTo() instanceof Player) {
            return;
        }
        if (vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof Tameable tameable)) {
            return;
        }
        if (!(tameable.getOwner() instanceof Player player)) {
            return;
        }
        if (vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        if (!(event.getEntity() instanceof Tameable)) {
            return;
        }
        if (!(event.getTargetEntity() instanceof Player player)) {
            return;
        }
        if (vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }
}
