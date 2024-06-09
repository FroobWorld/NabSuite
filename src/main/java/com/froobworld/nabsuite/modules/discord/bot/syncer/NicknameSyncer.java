package com.froobworld.nabsuite.modules.discord.bot.syncer;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.UUID;

public class NicknameSyncer {
    private final DiscordModule discordModule;

    public NicknameSyncer(DiscordModule discordModule) {
        this.discordModule = discordModule;
    }

    public void syncMember(Member member) {
        Member selfMember = discordModule.getDiscordBot().getGuild().getSelfMember();
        if (!selfMember.canInteract(member)) {
            return; // insufficient perms
        }
        PlayerIdentity playerIdentity = discordModule.getDiscordBot().getAccountLinkManager().getLinkedMinecraftAccount(member.getUser());
        if (playerIdentity != null) {
            if (member.getNickname() == null || !member.getNickname().equals(playerIdentity.getLastName())) {
                member.modifyNickname(playerIdentity.getLastName()).queue();
            }
        }
    }

    public void syncPlayer(UUID uuid) {
        Guild guild = discordModule.getDiscordBot().getGuild();
        if (guild == null) {
            return;
        }
        discordModule.getDiscordBot().getAccountLinkManager().getLinkedDiscordUser(uuid).thenAccept(user -> {
            if (user == null) {
                return;
            }
            guild.retrieveMember(user).queue(member -> {
                if (member != null) {
                    syncMember(member);
                }
            }, throwable -> {});
        });
    }

}
