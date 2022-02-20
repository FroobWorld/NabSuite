package com.froobworld.nabsuite.modules.basics.player.mail;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;

import java.io.IOException;
import java.util.*;

public class MailBox {
    private static final SimpleDataSchema<MailBox> SCHEMA = new SimpleDataSchema.Builder<MailBox>()
            .addField("uuid", SchemaEntries.uuidEntry(
                    homes -> homes.uuid,
                    (homes, uuid) -> homes.uuid = uuid
            ))
            .addField("unread", SchemaEntries.booleanEntry(
                    mailBox -> mailBox.unread,
                    (mailBox, unread) -> mailBox.unread = unread
            ))
            .addField("mail", SchemaEntries.listEntry(
                    mailBox -> mailBox.mail,
                    (mailBox, mailList) -> mailBox.mail = mailList,
                    (jsonReader, mailBox) -> Mail.fromJsonReader(jsonReader),
                    Mail.SCHEMA::write
            ))
            .build();
    private final MailCentre mailCentre;
    private boolean unread;
    private UUID uuid;
    private List<Mail> mail;

    public MailBox(MailCentre mailCentre, UUID uuid) {
        this(mailCentre);
        this.uuid = uuid;
        this.mail = new ArrayList<>();
    }

    private MailBox(MailCentre mailCentre) {
        this.mailCentre = mailCentre;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Mail> getMail() {
        return List.copyOf(mail);
    }

    public void markAsRead() {
        this.unread = false;
        mailCentre.mailBoxSaver.scheduleSave(this);
    }

    public boolean hasUnread() {
        return unread;
    }

    void addMail(Mail mail) {
        this.mail.add(mail);
        this.unread = true;
        mailCentre.mailBoxSaver.scheduleSave(this);
    }

    public void clearMail() {
        mail.clear();
        mailCentre.mailBoxSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MailBox fromJsonString(MailCentre mailCentre, String jsonString) {
        MailBox mailBox = new MailBox(mailCentre);
        try {
            SCHEMA.populateFromJsonString(mailBox, jsonString);
            return mailBox;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
