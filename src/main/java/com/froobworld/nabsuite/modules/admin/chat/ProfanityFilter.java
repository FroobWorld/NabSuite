package com.froobworld.nabsuite.modules.admin.chat;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.Punishments;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ProfanityFilter implements Listener {
    private final AdminModule adminModule;
    private final List<TextReplacementConfig> replacementConfigs = new ArrayList<>();
    private final List<Pattern> highlyOffensiveWords = new ArrayList<>();

    public ProfanityFilter(AdminModule adminModule) {
        this.adminModule = adminModule;
        for (String worldFilter : adminModule.getAdminConfig().wordFilters.get()) {
            String[] filterSplit = worldFilter.split(":", 2);
            String word = filterSplit[0];
            boolean standalone = false;

            // "standalone" to only filter words that don't appear within other words
            if (word.contains(";")) {
                String[] wordSplit = word.split(";");
                if (wordSplit[0].equalsIgnoreCase("standalone")) {
                    word = wordSplit[1];
                    standalone = true;
                }
            }

            String replacement = filterSplit[1];
            replacementConfigs.add(
                    TextReplacementConfig.builder()
                            .match(Pattern.compile(expandPattern(word, standalone), Pattern.CASE_INSENSITIVE))
                            .replacement(replacement)
                            .build()
            );
        }
        for (String offensiveWord : adminModule.getAdminConfig().highlyOffensiveWords.get()) {
            boolean standalone = false;
            if (offensiveWord.contains(";")) {
                String[] wordSplit = offensiveWord.split(";");
                if (wordSplit[0].equalsIgnoreCase("standalone")) {
                    offensiveWord = wordSplit[1];
                    standalone = true;
                }
            }
            highlyOffensiveWords.add(Pattern.compile(expandPattern(offensiveWord, standalone), Pattern.CASE_INSENSITIVE));
        }
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
        adminModule.getTicketManager().registerTicketType("profanity", (ticket, subject) -> Component
                        .text("Player ")
                        .append(subject.displayName())
                        .append(Component.text(" - Highly offensive language"))
        );
    }

    public Component filter(Component component) {
        for (TextReplacementConfig replacementConfig : replacementConfigs) {
            component = component.replaceText(replacementConfig);
        }
        return component;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onPlayerChat(AsyncChatEvent event) {
        String plainTextMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        for (Pattern pattern : highlyOffensiveWords) {
            if (pattern.matcher(plainTextMessage).find()) {
                Punishments punishments = adminModule.getPunishmentManager().getPunishments(event.getPlayer().getUniqueId());
                if (punishments.getMutePunishment() == null) {
                    adminModule.getPunishmentManager().getMuteEnforcer().mute(
                            adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(event.getPlayer()),
                            Bukkit.getConsoleSender(),
                            "Highly offensive language",
                            -1,
                            true
                    );
                    adminModule.getTicketManager().createSystemTicket(
                            event.getPlayer().getLocation(),
                            event.getPlayer().getUniqueId(),
                            "profanity",
                            String.format("Player %s was automatically muted for highly offensive language. Message was \"%s\".", event.getPlayer().getName(), plainTextMessage)
                    );
                }
            }
        }
        event.message(filter(event.message()));
    }

    @EventHandler(ignoreCancelled = true)
    private void onSignChange(SignChangeEvent event) {
        for (int i = 0; i < 4; i++) {
            event.line(i, filter(event.line(i)));
        }
    }

    private static String expandPattern(String pattern, boolean standalone) {
        StringBuilder newPatternBuilder = new StringBuilder();

        if (standalone) {
            newPatternBuilder.append("\\b");
        }

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

        if (standalone) {
            newPatternBuilder.append("\\b");
        }

        return newPatternBuilder.toString();
    }

}
