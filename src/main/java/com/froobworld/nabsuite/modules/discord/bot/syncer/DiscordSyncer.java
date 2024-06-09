package com.froobworld.nabsuite.modules.discord.bot.syncer;

import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.froobworld.nabsuite.modules.discord.bot.linking.LinkedAccountData;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.track.UserTrackEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

public class DiscordSyncer extends ListenerAdapter implements Listener {
    private final DiscordModule discordModule;
    private final NicknameSyncer nicknameSyncer;
    private final RoleSyncer roleSyncer;
    private final Queue<UUID> uuidsToSync = new ArrayDeque<>();

    public DiscordSyncer(DiscordModule discordModule) {
        this.discordModule = discordModule;
        this.nicknameSyncer = new NicknameSyncer(discordModule);
        this.roleSyncer = new RoleSyncer(discordModule);
        discordModule.getDiscordBot().getJda().addEventListener(this);
        Bukkit.getPluginManager().registerEvents(this, discordModule.getPlugin());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(discordModule.getPlugin(), this::updateLoop, 600, 600); // run every 30 seconds
        LuckPerms luckPerms = discordModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            luckPerms.getEventBus().subscribe(UserTrackEvent.class, this::onTrackChangePosition);
        }
    }

    private void syncPlayer(UUID uuid) {
        nicknameSyncer.syncPlayer(uuid);
        roleSyncer.syncPlayer(uuid);
    }

    private void syncMember(Member member) {
        nicknameSyncer.syncMember(member);
        roleSyncer.syncMember(member);
    }

    private void updateLoop() {
        if (uuidsToSync.isEmpty()) {
            discordModule.getDiscordBot().getAccountLinkManager().getAllLinkedAccounts()
                    .stream()
                    .map(LinkedAccountData::getMinecraftUuid)
                    .forEach(uuidsToSync::add);
        }
        UUID nextUuid = uuidsToSync.poll();
        if (nextUuid != null) {
            syncPlayer(nextUuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        syncPlayer(event.getPlayer().getUniqueId());
    }

    private void onTrackChangePosition(UserTrackEvent event) {
        syncPlayer(event.getUser().getUniqueId());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        syncMember(event.getMember());
    }
}
