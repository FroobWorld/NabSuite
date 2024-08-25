package com.froobworld.nabsuite.modules.basics.channel;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.user.User;
import com.froobworld.nabsuite.user.UserManager;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class ChatChannelManager {
    public static final String EDIT_ALL_CHANNELS_PERMISSION = "nabsuite.editallchannels";
    public static final Pattern channelNamePattern = Pattern.compile("^[a-z0-9]{3,10}$");
    private static final Pattern fileNamePattern = Pattern.compile("^[a-z0-9]+\\.json$");
    protected final DataSaver channelSaver;
    private final BasicsModule basicsModule;
    private final BiMap<String, ChatChannel> channelMap = HashBiMap.create();
    private final File directory;
    private final ChatChannelMessageCentre messageCentre;

    public ChatChannelManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        directory = new File(basicsModule.getDataFolder(), "channels/");
        channelSaver = new DataSaver(basicsModule.getPlugin(), 1200);
        channelMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> ChatChannel.fromJsonString(this, basicsModule.getPlugin().getUserManager(), new String(bytes)),
                (fileName, channel) -> channel.getName().toLowerCase()
        ));
        channelSaver.start();
        channelSaver.addDataType(ChatChannel.class, channel -> channel.toJsonString().getBytes(), channel -> new File(directory, channel.getName() + ".json"));
        messageCentre = new ChatChannelMessageCentre(basicsModule);
        new ChannelDeletionPolicyEnforcer(basicsModule);
    }

    public void shutdown() {
        channelSaver.stop();
    }

    public ChatChannel createChannel(UUID creator, String name, User owner) {
        if (getChannel(name) != null) {
            throw new IllegalStateException("Channel with that name already exists");
        }
        if (!channelNamePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Name does not match pattern: " + channelNamePattern);
        }
        ChatChannel channel = new ChatChannel(this, basicsModule.getPlugin().getUserManager(), creator, name, owner);
        channelMap.put(name.toLowerCase(), channel);
        channelSaver.scheduleSave(channel);

        return channel;
    }

    public void deleteChannel(ChatChannel channel) {
        channelMap.remove(channel.getName().toLowerCase());
        channelSaver.scheduleDeletion(channel);
    }

    public ChatChannel getChannel(String name) {
        return channelMap.get(name.toLowerCase());
    }

    public Set<ChatChannel> getChannels() {
        return channelMap.values();
    }

    public ChatChannelMessageCentre getMessageCentre() {
        return messageCentre;
    }
}
