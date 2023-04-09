package com.froobworld.nabsuite.modules.basics.motd;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.util.PlayerList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.stream.Collectors;


public class MotdManager implements Listener {
    private final BasicsModule basicsModule;

    public MotdManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
    }

    public List<Component> getMotd() {
        return basicsModule.getConfig().messages.motd.get().stream()
                .map(string -> MiniMessage.miniMessage().deserialize(
                        string,
                        TagResolver.resolver("player_list", Tag.inserting(PlayerList.getPlayerListDecorated(basicsModule)))
                ))
                .collect(Collectors.toList());
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            basicsModule.getPlugin().getHookManager().getSchedulerHook().runTaskDelayed(() -> {
                Bukkit.broadcast(event.getPlayer().displayName()
                        .append(Component.text(" just joined for the first time.", NamedTextColor.LIGHT_PURPLE))
                );
            }, 5);
        }
        basicsModule.getPlugin().getHookManager().getSchedulerHook().runTaskDelayed(() -> getMotd().forEach(event.getPlayer()::sendMessage), 10);
    }

}
