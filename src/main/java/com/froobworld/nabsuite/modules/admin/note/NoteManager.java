package com.froobworld.nabsuite.modules.admin.note;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.PunishmentLogItem;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.util.ComponentUtils;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
        return createNote(subject, creator, message, true);
    }
    private PlayerNote createNote(UUID subject, UUID creator, String message, boolean log) {
        PlayerNote playerNote = new PlayerNote(subject, creator, message);
        getNotes(subject).addNote(playerNote);
        if (log) {
            adminModule.getPunishmentManager().getPunishments(subject).addPunishmentLogItem(new PunishmentLogItem(
                    adminModule.getPlugin().getPlayerIdentityManager(),
                    PunishmentLogItem.Type.NOTE_ADDED,
                    subject,
                    creator,
                    playerNote.getTimeCreated(),
                    -1,
                    message
            ));
        }

        return playerNote;
    }

    public void sendWarning(PlayerIdentity subject, UUID creator, String message) {
        BasicsModule basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
        basicsModule.getMailCentre().sendSystemMail(subject.getUuid(), "You have received a warning with message \"" + message + "\".");
        PlayerNote playerNote = createNote(subject.getUuid(), creator, "Warning sent with message \"" + message + "\".", false);
        adminModule.getPunishmentManager().getPunishments(subject.getUuid()).addPunishmentLogItem(new PunishmentLogItem(
                adminModule.getPlugin().getPlayerIdentityManager(),
                PunishmentLogItem.Type.WARN,
                subject.getUuid(),
                creator,
                playerNote.getTimeCreated(),
                -1,
                message
        ));
        if (subject.asPlayer() != null) {
            subject.asPlayer().sendMessage(
                    Component.newline()
                            .append(Component.text("You have received a warning with the following message:", NamedTextColor.RED))
                            .append(Component.newline())
                            .append(Component.newline())
                            .append(ComponentUtils.clickableUrls(Component.text(message, NamedTextColor.GOLD)))
                            .append(Component.newline())
            );
        }
    }

}
