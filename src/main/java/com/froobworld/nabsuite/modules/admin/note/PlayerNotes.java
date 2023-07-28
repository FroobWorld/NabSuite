package com.froobworld.nabsuite.modules.admin.note;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;

import java.io.IOException;
import java.util.*;

public class PlayerNotes {
    private static final SimpleDataSchema<PlayerNotes> SCHEMA = new SimpleDataSchema.Builder<PlayerNotes>()
            .addField("uuid", SchemaEntries.uuidEntry(
                    notes -> notes.uuid,
                    (notes, uuid) -> notes.uuid = uuid
            ))
            .addField("notes", SchemaEntries.listEntry(
                    notes -> notes.notes,
                    (notes, notesSet) -> notes.notes = notesSet,
                    (jsonReader, notes) -> PlayerNote.fromJsonReader(jsonReader),
                    PlayerNote.SCHEMA::write
            ))
            .build();
    private final NoteManager noteManager;
    private UUID uuid;
    private List<PlayerNote> notes;

    public PlayerNotes(NoteManager noteManager, UUID uuid) {
        this(noteManager);
        this.uuid = uuid;
        this.notes = new ArrayList<>();
    }

    private PlayerNotes(NoteManager noteManager) {
        this.noteManager = noteManager;
    }

    public List<PlayerNote> getNotes() {
        return List.copyOf(notes);
    }

    public UUID getUuid() {
        return uuid;
    }

    void addNote(PlayerNote note) {
        notes.add(note);
        noteManager.notesSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PlayerNotes fromJsonString(NoteManager noteManager, String jsonString) {
        PlayerNotes notes = new PlayerNotes(noteManager);
        try {
            SCHEMA.populateFromJsonString(notes, jsonString);
            return notes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
