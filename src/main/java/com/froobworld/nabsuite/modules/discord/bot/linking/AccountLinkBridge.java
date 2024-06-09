package com.froobworld.nabsuite.modules.discord.bot.linking;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AccountLinkBridge extends ListenerAdapter {
    private final AccountLinkManager accountLinkManager;

    public AccountLinkBridge(DiscordModule discordModule, AccountLinkManager accountLinkManager) {
        this.accountLinkManager = accountLinkManager;
        discordModule.getDiscordBot().getJda().addEventListener(this);
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        try {
            int code = Integer.parseInt(event.getMessage().getContentRaw());
            UUID uuid = accountLinkManager.getUuidForCode(code);
            if (uuid == null) {
                event.getChannel().sendMessage("Unknown code. Please try again.").queue();
            } else {
                accountLinkManager.setLinked(uuid, event.getAuthor());
                PlayerIdentity playerIdentity = accountLinkManager.getLinkedMinecraftAccount(event.getAuthor());
                if (playerIdentity != null) {
                    event.getChannel().sendMessage("Your Discord account has been successfully linked to `" + playerIdentity.getLastName() + "`.").queue();
                } else {
                    event.getChannel().sendMessage("Something went wrong while linking your account.").queue();
                }
            }
        } catch (Exception ignored) {}
    }

}
