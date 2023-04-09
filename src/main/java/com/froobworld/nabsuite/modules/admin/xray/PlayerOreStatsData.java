package com.froobworld.nabsuite.modules.admin.xray;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerOreStatsData {
    static final SimpleDataSchema<PlayerOreStatsData> SCHEMA = new SimpleDataSchema.Builder<PlayerOreStatsData>()
            .addField("uuid", SchemaEntries.uuidEntry(
                    data -> data.uuid,
                    (data, uuid) -> data.uuid = uuid
            ))
            .addField("stone", SchemaEntries.integerEntry(
                    data -> data.stone,
                    (data, stone) -> data.stone = stone
            ))
            .addField("coal", SchemaEntries.integerEntry(
                    data -> data.coal,
                    (data, coal) -> data.coal = coal
            ))
            .addField("iron", SchemaEntries.integerEntry(
                    data -> data.iron,
                    (data, iron) -> data.iron = iron
            ))
            .addField("copper", SchemaEntries.integerEntry(
                    data -> data.copper,
                    (data, copper) -> data.copper = copper
            ))
            .addField("gold", SchemaEntries.integerEntry(
                    data -> data.gold,
                    (data, gold) -> data.gold = gold
            ))
            .addField("diamond", SchemaEntries.integerEntry(
                    data -> data.diamond,
                    (data, diamond) -> data.diamond = diamond
            ))
            .addField("emerald", SchemaEntries.integerEntry(
                    data -> data.emerald,
                    (data, emerald) -> data.emerald = emerald
            ))
            .addField("lapis", SchemaEntries.integerEntry(
                    data -> data.lapis,
                    (data, lapis) -> data.lapis = lapis
            ))
            .addField("redstone", SchemaEntries.integerEntry(
                    data -> data.redstone,
                    (data, redstone) -> data.redstone = redstone
            ))
            .addField("netherrack", SchemaEntries.integerEntry(
                    data -> data.netherrack,
                    (data, netherrack) -> data.netherrack = netherrack
            ))
            .addField("nether-gold", SchemaEntries.integerEntry(
                    data -> data.netherGold,
                    (data, netherGold) -> data.netherGold = netherGold
            ))
            .addField("quartz", SchemaEntries.integerEntry(
                    data -> data.quartz,
                    (data, quartz) -> data.quartz = quartz
            ))
            .addField("netherite", SchemaEntries.integerEntry(
                    data -> data.netherite,
                    (data, netherite) -> data.netherite = netherite
            ))
            .build();

    public final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final OreStatsManager oreStatsManager;
    private UUID uuid;
    private int stone, coal, iron, copper, gold, diamond, emerald, lapis, redstone;
    private int netherrack, netherGold, quartz, netherite;

    private PlayerOreStatsData(OreStatsManager oreStatsManager) {
        this.oreStatsManager = oreStatsManager;
    }

    public PlayerOreStatsData(OreStatsManager oreStatsManager, UUID uuid) {
        this.oreStatsManager = oreStatsManager;
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getStone() {
        return stone;
    }

    public int getCoal() {
        return coal;
    }

    public int getIron() {
        return iron;
    }

    public int getCopper() {
        return copper;
    }

    public int getGold() {
        return gold;
    }

    public int getDiamond() {
        return diamond;
    }

    public int getEmerald() {
        return emerald;
    }

    public int getLapis() {
        return lapis;
    }

    public int getRedstone() {
        return redstone;
    }

    public int getNetherrack() {
        return netherrack;
    }

    public int getNetherGold() {
        return netherGold;
    }

    public int getQuartz() {
        return quartz;
    }

    public int getNetherite() {
        return netherite;
    }

    void incrementStone() {
        lock.writeLock().lock();
        stone++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementCoal() {
        lock.writeLock().lock();
        coal++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementIron() {
        lock.writeLock().lock();
        iron++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementCopper() {
        lock.writeLock().lock();
        copper++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementGold() {
        lock.writeLock().lock();
        gold++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementDiamond() {
        lock.writeLock().lock();
        diamond++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementEmerald() {
        lock.writeLock().lock();
        emerald++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementLapis() {
        lock.writeLock().lock();
        lapis++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementRedstone() {
        lock.writeLock().lock();
        redstone++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementNetherrack() {
        lock.writeLock().lock();
        netherrack++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementNetherGold() {
        lock.writeLock().lock();
        netherGold++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementQuartz() {
        lock.writeLock().lock();
        quartz++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    void incrementNetherite() {
        lock.writeLock().lock();
        netherite++;
        lock.writeLock().unlock();
        oreStatsManager.dataSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            lock.readLock().lock();
            try {
                return SCHEMA.toJsonString(this);
            } finally {
                lock.readLock().unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PlayerOreStatsData fromJsonString(OreStatsManager oreStatsManager, String jsonString) {
        PlayerOreStatsData data = new PlayerOreStatsData(oreStatsManager);
        try {
            SCHEMA.populateFromJsonString(data, jsonString);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
