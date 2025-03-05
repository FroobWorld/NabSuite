package com.froobworld.nabsuite.modules.discord.config;

import com.froobworld.nabconfiguration.*;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabconfiguration.annotations.SectionMap;
import com.froobworld.nabsuite.modules.discord.DiscordModule;

import java.io.File;
import java.util.List;

public class DiscordConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 2;

    public DiscordConfig(DiscordModule discordModule) {
        super(
                new File(discordModule.getDataFolder(), "config.yml"),
                () -> discordModule.getResource("config.yml"),
                i -> discordModule.getResource("config-patches/" + i + ".patch"),
                CONFIG_VERSION
        );
    }

    @Entry(key = "bot-token")
    public final ConfigEntry<String> botToken = new ConfigEntry<>();

    @Entry(key = "guild-id")
    public final ConfigEntry<String> guildId = new ConfigEntry<>();

    @Entry(key = "invite-url")
    public final ConfigEntry<String> inviteUrl = new ConfigEntry<>();

    @Entry(key = "use-webhook")
    public final ConfigEntry<Boolean> useWebhook = new ConfigEntry<>();

    @Section(key = "channels")
    public final Channels channels = new Channels();

    public static class Channels extends ConfigSection {

        @Entry(key = "chat")
        public final ConfigEntry<String> chat = new ConfigEntry<>();

        @Entry(key = "staff-log")
        public final ConfigEntry<String> staffLog = new ConfigEntry<>();

    }

    @Section(key = "message-formats")
    public final MessageFormats messageFormats = new MessageFormats();

    public static class MessageFormats extends ConfigSection {

        @Entry(key = "discord-to-minecraft")
        public final ConfigEntry<String> discordToMinecraft = new ConfigEntry<>();

        @Entry(key = "minecraft-to-discord")
        public final ConfigEntry<String> minecraftToDiscord = new ConfigEntry<>();

    }

    @Section(key = "roles")
    public final Roles roles = new Roles();

    public static class Roles extends ConfigSection {

        @Entry(key = "verified-role")
        public final ConfigEntry<String> verifiedRole = new ConfigEntry<>();

        @Entry(key = "sync-roles")
        public final ConfigEntry<List<String>> syncRoles = ConfigEntries.stringListEntry();

    }

    @Section(key = "commands")
    public final Commands commands = new Commands();

    public static class Commands extends ConfigSection {

        @Entry(key = "enabled")
        public final ConfigEntry<List<String>> enabled = ConfigEntries.stringListEntry();

        @SectionMap(key = "settings", defaultKey = "default")
        public final ConfigSectionMap<String, CommandSettings> settings = new ConfigSectionMap<>(String::new, CommandSettings.class, true);

    }

    public static class CommandSettings extends ConfigSection {

        @Entry(key = "override-name")
        public final ConfigEntry<String> overrideName = new ConfigEntry<>();

        @Entry(key = "public-reply")
        public final ConfigEntry<Boolean> publicReply = new ConfigEntry<>();

    }

}
