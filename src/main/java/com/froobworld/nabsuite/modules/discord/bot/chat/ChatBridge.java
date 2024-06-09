package com.froobworld.nabsuite.modules.discord.bot.chat;

import com.froobworld.nabsuite.modules.discord.DiscordModule;

public class ChatBridge {
    private final Discord2MinecraftBridge discord2MinecraftBridge;
    private final Minecraft2DiscordBridge minecraft2DiscordBridge;

    public ChatBridge(DiscordModule discordModule) {
        this.discord2MinecraftBridge = new Discord2MinecraftBridge(discordModule);
        this.minecraft2DiscordBridge = new Minecraft2DiscordBridge(discordModule);
    }

    public void shutdown() {
        minecraft2DiscordBridge.shutdown();
    }
}
