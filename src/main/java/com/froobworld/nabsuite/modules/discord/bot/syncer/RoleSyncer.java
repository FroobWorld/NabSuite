package com.froobworld.nabsuite.modules.discord.bot.syncer;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.LuckPerms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class RoleSyncer {
    private final DiscordModule discordModule;

    public RoleSyncer(DiscordModule discordModule) {
        this.discordModule = discordModule;
    }

    public void syncMember(Member member) {
        PlayerIdentity playerIdentity = discordModule.getDiscordBot().getAccountLinkManager().getLinkedMinecraftAccount(member.getUser());
        if (playerIdentity == null) {
            return;
        }
        LuckPerms luckPerms = discordModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms == null) {
            return;
        }
        Member selfMember = discordModule.getDiscordBot().getGuild().getSelfMember();
        if (!selfMember.canInteract(member)) {
            return; // insufficient perms
        }

        luckPerms.getUserManager().loadUser(playerIdentity.getUuid()).thenAccept(lpUser -> {
            if (lpUser == null) {
                return;
            }
            String primaryGroup = lpUser.getPrimaryGroup();

            String discordRoleToAdd = null;
            List<String> rolesToRemove = new ArrayList<>();
            for (String string : discordModule.getDiscordConfig().roles.syncRoles.get()) {
                String mcRole = string.split(":")[0];
                String discordRole = string.split(":")[1];
                if (mcRole.equalsIgnoreCase(primaryGroup)) {
                    discordRoleToAdd = discordRole;
                } else {
                    rolesToRemove.add(discordRole);
                }
            }

            if (discordRoleToAdd != null) {
                Guild guild = discordModule.getDiscordBot().getGuild();
                Role primaryRole = guild.getRoleById(discordRoleToAdd);
                if (primaryRole == null) {
                    return;
                }
                if (!member.getRoles().contains(primaryRole)) {
                    guild.addRoleToMember(member, primaryRole).queue();
                }

                // only remove unnecessary roles if we actually found a role for them
                for (String roleId : rolesToRemove) {
                    Role roleToRemove = guild.getRoleById(roleId);
                    if (roleToRemove == null) {
                        continue;
                    }
                    guild.removeRoleFromMember(member, roleToRemove).queue();
                }
            }
        });
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
