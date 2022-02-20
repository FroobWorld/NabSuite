package com.froobworld.nabsuite.modules.admin.util;

import org.bukkit.Material;

public final class OreUtils {

    private OreUtils() {}

    public static boolean isCoalOre(Material material) {
        return material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE;
    }

    public static boolean isIronOre(Material material) {
        return material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE;
    }

    public static boolean isCopperOre(Material material) {
        return material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE;
    }

    public static boolean isGoldOre(Material material) {
        return material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE;
    }

    public static boolean isDiamondOre(Material material) {
        return material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE;
    }

    public static boolean isEmeraldOre(Material material) {
        return material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE;
    }

    public static boolean isLapisOre(Material material) {
        return material == Material.LAPIS_ORE || material == Material.DEEPSLATE_LAPIS_ORE;
    }

    public static boolean isRedstoneOre(Material material) {
        return material == Material.REDSTONE_ORE || material == Material.DEEPSLATE_REDSTONE_ORE;
    }

    public static boolean isNetherGoldOre(Material material) {
        return material == Material.NETHER_GOLD_ORE;
    }

    public static boolean isNetherQuartzOre(Material material) {
        return material == Material.NETHER_QUARTZ_ORE;
    }

    public static boolean isNetheriteOre(Material material) {
        return material == Material.ANCIENT_DEBRIS;
    }

    public static boolean isStone(Material material) {
        return material == Material.STONE ||
                material == Material.ANDESITE ||
                material == Material.GRANITE ||
                material == Material.DIORITE ||
                material == Material.TUFF ||
                material == Material.DEEPSLATE;
    }

    public static boolean isNetherrack(Material material) {
        return material == Material.NETHERRACK ||
                material == Material.BLACKSTONE||
                material == Material.BASALT;
    }

}
