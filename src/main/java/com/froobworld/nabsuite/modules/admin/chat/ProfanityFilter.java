package com.froobworld.nabsuite.modules.admin.chat;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ProfanityFilter implements Listener {
    private final List<TextReplacementConfig> replacementConfigs = new ArrayList<>();

    public ProfanityFilter(AdminModule adminModule) {
        for (String worldFilter : adminModule.getAdminConfig().wordFilters.get()) {
            String[] filterSplit = worldFilter.split(":", 2);
            String word = filterSplit[0];
            String replacement = filterSplit[1];
            replacementConfigs.add(
                    TextReplacementConfig.builder()
                            .match(Pattern.compile(expandPattern(word), Pattern.CASE_INSENSITIVE))
                            .replacement(replacement)
                            .build()
            );
        }
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerChat(AsyncChatEvent event) {
        event.message(filter(event.message()));
    }

    @EventHandler(ignoreCancelled = true)
    private void onSignChange(SignChangeEvent event) {
        for (int i = 0; i < 4; i++) {
            event.line(i, filter(event.line(i)));
        }
    }

    private Component filter(Component component) {
        Component filteredComponent = component;
        for (TextReplacementConfig replacementConfig : replacementConfigs) {
            component = component.replaceText(replacementConfig);
        }
        return component;
    }

    private static String expandPattern(String pattern) {
        StringBuilder newPatternBuilder = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char character = pattern.charAt(i);
            if (i == 0) {
                newPatternBuilder.append("[").append(character).append("]+");
            } else if (i == pattern.length() - 1) {
                newPatternBuilder.append("[").append(character).append("\\W").append("]*");
                newPatternBuilder.append("[").append(character).append("]+");
            } else {
                newPatternBuilder.append("[").append(character).append("\\W").append("]*");
                newPatternBuilder.append("[").append(character).append("]+");
                newPatternBuilder.append("[").append(character).append("\\W").append("]*");
            }
        }
        return newPatternBuilder.toString();
    }

}
