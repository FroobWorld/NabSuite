package com.froobworld.nabsuite.modules.admin.note;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.File;
import java.util.UUID;
import java.util.regex.Pattern;

public class NoteManager {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    private final AdminModule adminModule;
    protected final DataSaver notesSaver;
    private final BiMap<UUID, PlayerNotes> notesMap = HashBiMap.create();
    private final File directory;

    public NoteManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        directory = new File(adminModule.getDataFolder(), "notes/");
        notesSaver = new DataSaver(adminModule.getPlugin(), 1200);
        notesMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> PlayerNotes.fromJsonString(this, new String(bytes)),
                (fileName, notes) -> notes.getUuid()
        ));
        notesSaver.start();
        notesSaver.addDataType(PlayerNotes.class, notes -> notes.toJsonString().getBytes(), notes -> new File(directory, notes.getUuid().toString() + ".json"));
    }

    public void shutdown() {
        notesSaver.stop();
    }

    public PlayerNotes getNotes(UUID uuid) {
        if (!notesMap.containsKey(uuid)) {
            PlayerNotes playerNotes = new PlayerNotes(this, uuid);
            notesMap.put(uuid, playerNotes);
            notesSaver.scheduleSave(playerNotes);
        }
        return notesMap.get(uuid);
    }

    public PlayerNote createNote(UUID subject, UUID creator, String message) {
        PlayerNote playerNote = new PlayerNote(subject, creator, message);
        getNotes(subject).addNote(playerNote);
        String subjectName = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(subject).getLastName();
        String creatorName;
        if (creator.equals(ConsoleUtils.CONSOLE_UUID)) {
            creatorName = "Console";
        } else {
            creatorName = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(creator).getLastName();
        }
        adminModule.getDiscordStaffLog().sendNoteCreationNotification(playerNote, subjectName, creatorName);

        return playerNote;
    }

}
