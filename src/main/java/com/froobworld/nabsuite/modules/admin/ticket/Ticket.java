package com.froobworld.nabsuite.modules.admin.ticket;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.gson.stream.JsonReader;
import org.bukkit.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Ticket {
    static final SimpleDataSchema<Ticket> SCHEMA = new SimpleDataSchema.Builder<Ticket>()
            .addField("id", SchemaEntries.integerEntry(
                    ticket -> ticket.id,
                    (ticket, id) -> ticket.id = id
            ))
            .addField("timestamp", SchemaEntries.longEntry(
                    ticket -> ticket.timestamp,
                    (ticket, timestamp) -> ticket.timestamp = timestamp
            ))
            .addField("creator", SchemaEntries.uuidEntry(
                    ticket -> ticket.creator,
                    (ticket, creator) -> ticket.creator = creator
            ))
            .addField("location", SchemaEntries.locationEntry(
                    ticket -> ticket.location,
                    (ticket, location) -> ticket.location = location
            ))
            .addField("message", SchemaEntries.stringEntry(
                    ticket -> ticket.message,
                    (ticket, message) -> ticket.message = message
            ))
            .addField("open", SchemaEntries.booleanEntry(
                    ticket -> ticket.open,
                    (ticket, open) -> ticket.open = open
            ))
            .addField("notes", SchemaEntries.listEntry(
                    ticket -> ticket.notes,
                    (ticket, notes) -> ticket.notes = notes,
                    (jsonReader, ticket) -> TicketNote.fromJsonReader(jsonReader),
                    TicketNote.SCHEMA::write
            ))
            .build();
    public final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final TicketManager ticketManager;
    private int id;
    private long timestamp;
    private UUID creator;
    private Location location;
    private String message;
    private boolean open;
    private List<TicketNote> notes;

    private Ticket(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    Ticket(TicketManager ticketManager, int id, UUID creator, Location location, String message) {
        this.ticketManager = ticketManager;
        this.id = id;
        this.timestamp = System.currentTimeMillis();
        this.creator = creator;
        this.location = location;
        this.message = message;
        this.open = true;
        this.notes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getCreator() {
        return creator;
    }

    public Location getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOpen() {
        lock.readLock().lock();
        try {
            return open;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<TicketNote> getNotes() {
        lock.readLock().lock();
        try {
            return List.copyOf(notes);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addNote(UUID creator, String message) {
        lock.writeLock().lock();
        try {
            notes.add(new TicketNote(System.currentTimeMillis(), creator, message));
        } finally {
            lock.writeLock().unlock();
        }
        ticketManager.ticketSaver.scheduleSave(this);
    }

    public void close(UUID closer, String message) {
        lock.writeLock().lock();
        try {
            if (open) {
                open = false;
                notes.add(new TicketNote(System.currentTimeMillis(), closer, "(Closed with response: '" + message + "')"));
                ticketManager.ticketSaver.scheduleSave(this);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static Ticket fromJsonString(TicketManager ticketManager, String jsonString) {
        Ticket ticket = new Ticket(ticketManager);
        try {
            SCHEMA.populateFromJsonString(ticket, jsonString);
            return ticket;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    String toJsonString() {
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

    public static class TicketNote {
        private static final SimpleDataSchema<TicketNote> SCHEMA = new SimpleDataSchema.Builder<TicketNote>()
                .addField("timestamp", SchemaEntries.longEntry(
                        ticketNote -> ticketNote.timestamp,
                        (ticketNote, timestamp) -> ticketNote.timestamp = timestamp
                ))
                .addField("creator", SchemaEntries.uuidEntry(
                        ticketNote -> ticketNote.creator,
                        (ticketNote, creator) -> ticketNote.creator = creator
                ))
                .addField("message", SchemaEntries.stringEntry(
                        ticketNote -> ticketNote.message,
                        (ticketNote, message) -> ticketNote.message = message
                ))
                .build();
        private long timestamp;
        private UUID creator;
        private String message;

        public TicketNote(long timestamp, UUID creator, String message) {
            this.timestamp = timestamp;
            this.creator = creator;
            this.message = message;
        }

        private TicketNote() {}

        public long getTimestamp() {
            return timestamp;
        }

        public UUID getCreator() {
            return creator;
        }

        public String getMessage() {
            return message;
        }

        public static TicketNote fromJsonReader(JsonReader jsonReader) {
            TicketNote ticketNote = new TicketNote();
            try {
                SCHEMA.populate(ticketNote, jsonReader);
                return ticketNote;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}
