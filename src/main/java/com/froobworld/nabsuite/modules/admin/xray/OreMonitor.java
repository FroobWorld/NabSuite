package com.froobworld.nabsuite.modules.admin.xray;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.util.OreUtils;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.function.Predicate;

public class OreMonitor implements Listener {
    private final AdminModule adminModule;
    private final VeinTracker veinTracker;
    private final OreStatsManager oreStatsManager;

    public OreMonitor(AdminModule adminModule, OreStatsManager oreStatsManager) {
        this.adminModule = adminModule;
        this.oreStatsManager = oreStatsManager;
        this.veinTracker = new VeinTracker();
        adminModule.getNotificationCentre().registerNotificationKey("ore-alert", "nabsuite.orealert");
    }

    private void alertOre(Player miner, Location location, Component oreName, Predicate<Material> oreTypePredicate) {
        int veinSize = veinTracker.addLocation(location, oreTypePredicate);
        if (veinSize > 0) {
            Component notification = miner.displayName()
                    .append(Component.text(" found " + NumberDisplayer.numberToString(veinSize, false) + " ", NamedTextColor.WHITE))
                    .append(oreName)
                    .append(Component.text(".", NamedTextColor.WHITE));
            adminModule.getNotificationCentre().sendNotification("ore-alert", notification, miner.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockBreak(BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        Location location = event.getBlock().getLocation();
        PlayerOreStatsData oreStatsData = oreStatsManager.getOreStatsData(event.getPlayer().getUniqueId());
        boolean usingSilkTouch = event.getPlayer().getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH);

        if (OreUtils.isStone(material)) {
            oreStatsData.incrementStone();
        } else if (OreUtils.isNetherrack(material)) {
            oreStatsData.incrementNetherrack();
        } else if (OreUtils.isCoalOre(material)) {
            if (!usingSilkTouch) oreStatsData.incrementCoal();
        } else if (OreUtils.isIronOre(material)) {
            oreStatsData.incrementIron();
            alertOre(event.getPlayer(), location, Component.text("iron ore").color(NamedTextColor.DARK_GRAY), OreUtils::isIronOre);
        } else if (OreUtils.isCopperOre(material)) {
            oreStatsData.incrementCopper();
        } else if (OreUtils.isGoldOre(material)) {
            oreStatsData.incrementGold();
            alertOre(event.getPlayer(), location, Component.text("gold ore").color(NamedTextColor.GOLD), OreUtils::isGoldOre);
        } else if (OreUtils.isDiamondOre(material)) {
            if (!usingSilkTouch) oreStatsData.incrementDiamond();
            alertOre(event.getPlayer(), location, Component.text("diamond ore").color(NamedTextColor.AQUA), OreUtils::isDiamondOre);
        } else if (OreUtils.isEmeraldOre(material)) {
            if (!usingSilkTouch) oreStatsData.incrementEmerald();
            alertOre(event.getPlayer(), location, Component.text("emerald ore").color(NamedTextColor.GREEN), OreUtils::isEmeraldOre);
        } else if (OreUtils.isLapisOre(material)) {
            if (!usingSilkTouch) oreStatsData.incrementLapis();
            alertOre(event.getPlayer(), location, Component.text("lapis ore").color(NamedTextColor.BLUE), OreUtils::isLapisOre);
        } else if (OreUtils.isRedstoneOre(material)) {
            if (!usingSilkTouch) oreStatsData.incrementRedstone();
        } else if (OreUtils.isNetherGoldOre(material)) {
            oreStatsData.incrementNetherGold();
            alertOre(event.getPlayer(), location, Component.text("nether gold").color(NamedTextColor.GOLD), OreUtils::isNetherGoldOre);
        } else if (OreUtils.isNetherQuartzOre(material)) {
            if (!usingSilkTouch) oreStatsData.incrementQuartz();
        } else if (OreUtils.isNetheriteOre(material)) {
            oreStatsData.incrementNetherite();
            alertOre(event.getPlayer(), location, Component.text("ancient debris").color(NamedTextColor.DARK_PURPLE), OreUtils::isNetheriteOre);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockPlace(BlockPlaceEvent event) {
        Material material = event.getBlock().getType();
        boolean alertableOre = OreUtils.isIronOre(material) || OreUtils.isGoldOre(material) || OreUtils.isDiamondOre(material) ||
                OreUtils.isEmeraldOre(material) || OreUtils.isLapisOre(material) || OreUtils.isNetherGoldOre(material) || OreUtils.isNetheriteOre(material);

        if (alertableOre) {
            veinTracker.playerPlaced(event.getBlock().getLocation());
        }
    }

}
