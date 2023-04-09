package com.froobworld.nabsuite.modules.basics.teleport.portal;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import org.bukkit.Location;

import javax.sound.sampled.Port;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Portal {
    private static final SimpleDataSchema<Portal> SCHEMA = new SimpleDataSchema.Builder<Portal>()
            .addField("name", SchemaEntries.stringEntry(
                    portal -> portal.name,
                    (portal, name) -> portal.name = name
            ))
            .addField("location", SchemaEntries.locationEntry(
                    portal -> portal.location,
                    (portal, location) -> portal.location = location
            ))
            .addField("radius", SchemaEntries.doubleEntry(
                    portal -> portal.radius,
                    (portal, radius) -> portal.radius = radius
            ))
            .addField("link", SchemaEntries.stringEntry(
                    portal -> portal.link,
                    (portal, link) -> portal.link = link
            ))
            .addField("creator", SchemaEntries.uuidEntry(
                    portal -> portal.creator,
                    (portal, creator) -> portal.creator = creator
            ))
            .addField("created", SchemaEntries.longEntry(
                    portal -> portal.created,
                    (portal, created) -> portal.created = created
            ))
            .addField("use-relative-positioning", SchemaEntries.booleanEntry(
                    portal -> portal.useRelativePositioning,
                    (portal, useRelativePositioning) -> portal.useRelativePositioning = useRelativePositioning
            ))
            .build();

    public final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final PortalManager portalManager;
    private String name;
    private Location location;
    private double radius;
    private String link;
    private UUID creator;
    private long created;
    private boolean useRelativePositioning;

    private Portal(PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    public Portal(PortalManager portalManager, String name, Location location, double radius, UUID creator) {
        this.portalManager = portalManager;
        this.name = name;
        this.location = location;
        this.radius = radius;
        this.creator = creator;
        this.created = System.currentTimeMillis();
    }

    public Location getLocation() {
        return location;
    }

    public double getRadius() {
        return radius;
    }

    public Portal getLink() {
        lock.readLock().lock();
        try {
            if (link == null) {
                return null;
            }
            return portalManager.getPortal(link);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setLink(Portal portal) {
        lock.writeLock().lock();
        try {
            Portal currentLink = getLink();
            if (currentLink != null) {
                currentLink.lock.writeLock().lock();
                try {
                    if (currentLink.getLink().equals(this)) {
                        currentLink.setLinkOneWay(null);
                    }
                } finally {
                    currentLink.lock.writeLock().unlock();
                }
            }
            this.link = portal == null ? null : portal.getName();
            if (portal != null) {
                portal.lock.writeLock().lock();
                try {
                    Portal otherPortalLink = portal.getLink();
                    if (otherPortalLink != null) {
                        otherPortalLink.lock.writeLock().lock();
                        try {
                            if (otherPortalLink.getLink().equals(portal)) {
                                otherPortalLink.setLink(null);
                            }
                        } finally {
                            otherPortalLink.lock.writeLock().unlock();
                        }
                    }
                    portal.setLinkOneWay(this);
                } finally {
                    portal.lock.writeLock().unlock();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        portalManager.portalSaver.scheduleSave(this);
    }

    private void setLinkOneWay(Portal portal) {
        lock.writeLock().lock();
        try {
            link = portal == null ? null : portal.getName();
        } finally {
            lock.writeLock().unlock();
        }
        portalManager.portalSaver.scheduleSave(this);
    }

    public String getName() {
        return name;
    }

    public UUID getCreator() {
        return creator;
    }

    public long getTimeCreated() {
        return created;
    }

    public boolean useRelativePosition() {
        return useRelativePositioning;
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

    public static Portal fromJsonString(PortalManager portalManager, String jsonString) {
        Portal portal = new Portal(portalManager);
        try {
            SCHEMA.populateFromJsonString(portal, jsonString);
            return portal;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
