package com.froobworld.nabsuite.modules.admin.ticket;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.admin.config.AdminConfig;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            .addField("subject", SchemaEntries.uuidEntry(
                    ticket -> ticket.subject,
                    (ticket, subject) -> ticket.subject = subject
            ))
            .addField("type", SchemaEntries.stringEntry(
                    ticket -> ticket.type,
                    (ticket, type) -> ticket.type = type
            ))
            .addField("level", SchemaEntries.stringEntry(
                    ticket -> ticket.level,
                    (ticket, level) -> ticket.level = level
            ))
            .addField("staffLogId", SchemaEntries.longEntry(
                    ticket -> ticket.staffLogId,
                    (ticket, staffLogId) -> ticket.staffLogId = staffLogId
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
    private final TicketManager ticketManager;
    private int id;
    private long timestamp;
    private UUID creator;
    private UUID subject;
    private String type;
    private String level;
    private Long staffLogId;
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

    Ticket(TicketManager ticketManager, int id, UUID creator, UUID subject, Location location, String type, String message) {
        this.ticketManager = ticketManager;
        this.id = id;
        this.timestamp = System.currentTimeMillis();
        this.creator = creator;
        this.subject = subject;
        this.location = location;
        this.type = type;
        this.level = getTypeSettings().level.get();
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

    public UUID getSubject() {
        return subject;
    }

    public Location getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public String getLevel() {
        return level == null ? "default" : level;
    }

    public void setLevel(String level) {
        this.level = level;
        ticketManager.ticketSaver.scheduleSave(this);
    }

    public Long getStaffLogId() {
        return staffLogId;
    }

    public void setStaffLogId(Long staffLogId) {
        this.staffLogId = staffLogId;
        ticketManager.ticketSaver.scheduleSave(this);
    }

    public String getMessage() {
        return message;
    }

    public boolean isOpen() {
        return open;
    }

    public List<TicketNote> getNotes() {
        return List.copyOf(notes);
    }

    public void addNote(UUID creator, String message) {
        notes.add(new TicketNote(System.currentTimeMillis(), creator, message));
        ticketManager.ticketSaver.scheduleSave(this);
    }

    public void close(UUID closer, String message) {
        if (open) {
            open = false;
            notes.add(new TicketNote(System.currentTimeMillis(), closer, "(Closed with response: '" + message + "')"));
            ticketManager.ticketSaver.scheduleSave(this);
        }
    }

    public Boolean canDelegate() {
        return isOpen() &&
                getTypeSettings().allowDelegate.get() &&
                ticketManager.getAdminModule().getAdminConfig().ticketLevels.get().indexOf(getLevel()) > 0 ;
    }

    public Boolean canEscalate() {
        if (!isOpen()) {
            return false;
        }
        int index = ticketManager.getAdminModule().getAdminConfig().ticketLevels.get().indexOf(getLevel());
        int size = ticketManager.getAdminModule().getAdminConfig().ticketLevels.get().size();
        return index >= 0 && index < (size - 1);
    }

    public Component getSummary() {
        return ticketManager.getTicketSummary(this);
    }

    public String getPermission() {
        return "nabsuite.ticket." + getLevel();
    }

    private AdminConfig.TicketType getTypeSettings() {
        return ticketManager.getAdminModule().getAdminConfig().ticketTypes.of(type == null ? "default" : type);
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
            return SCHEMA.toJsonString(this);
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
