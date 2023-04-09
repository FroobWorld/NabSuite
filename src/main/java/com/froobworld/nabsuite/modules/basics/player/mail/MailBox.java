package com.froobworld.nabsuite.modules.basics.player.mail;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    public final ReadWriteLock lock = new ReentrantReadWriteLock();
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
        lock.readLock().lock();
        try {
            return List.copyOf(mail);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void markAsRead() {
        lock.writeLock().lock();
        this.unread = false;
        lock.writeLock().unlock();
        mailCentre.mailBoxSaver.scheduleSave(this);
    }

    public boolean hasUnread() {
        return unread && !mail.isEmpty();
    }

    void addMail(Mail mail) {
        lock.writeLock().lock();
        try {
            this.mail.add(mail);
            this.unread = true;
        } finally {
            lock.writeLock().unlock();
        }
        mailCentre.mailBoxSaver.scheduleSave(this);
    }

    public void clearMail() {
        lock.writeLock().lock();
        try {
            mail.clear();
        } finally {
            lock.writeLock().unlock();
        }
        mailCentre.mailBoxSaver.scheduleSave(this);
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
