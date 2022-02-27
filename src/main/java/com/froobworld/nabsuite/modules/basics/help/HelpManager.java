package com.froobworld.nabsuite.modules.basics.help;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class HelpManager {
    private final List<HelpObject> helpObjects = new ArrayList<>();

    private void makeHelpObjects() {
        Set<Command> processedCommands = new HashSet<>();
        helpObjects.clear();
        for (Command command : Bukkit.getCommandMap().getKnownCommands().values()) {
            if (processedCommands.contains(command)) {
                continue;
            }
            if (command.getDescription().isEmpty()) {
                continue;
            }
            helpObjects.add(new HelpObject(command));
            processedCommands.add(command);
        }
        helpObjects.sort(Comparator.comparing(helpObject -> helpObject.command.getName()));
    }

    public List<HelpObject> getHelpObjects(CommandSender sender) {
        if (helpObjects.isEmpty()) {
            makeHelpObjects();
        }
        return helpObjects.stream()
                .filter(helpObject -> helpObject.command.testPermissionSilent(sender))
                .collect(Collectors.toList());
    }

    public List<HelpObject> getHelpObjects(CommandSender sender, String filter) {
        return getHelpObjects(sender).stream()
                .filter(helpObject -> helpObject.matchesFilter(filter))
                .collect(Collectors.toList());
    }

    public static class HelpObject {
        private final Command command;

        private HelpObject(Command command) {
            this.command = command;
        }

        public Component shortDescription() {
            return Component.text("/" + command.getName(), NamedTextColor.RED)
                    .append(Component.text(": " + command.getDescription(), NamedTextColor.WHITE));
        }

        public Component longDescription() {
            return Component.text("Command", NamedTextColor.RED)
                    .append(Component.text(": " + command.getName(), NamedTextColor.WHITE))
                    .append(Component.text("Description", NamedTextColor.RED))
                    .append(Component.text(": " + command.getDescription(), NamedTextColor.WHITE))
                    .append(Component.text("Usage", NamedTextColor.RED))
                    .append(Component.text(": " + command.getUsage(), NamedTextColor.WHITE));
        }

        private boolean matchesFilter(String filter) {
            if (command.getName().contains(filter)) {
                return true;
            }
            if (command.getDescription().contains(filter)) {
                return true;
            }
            for (String alias : command.getAliases()) {
                if (alias.contains(filter)) {
                    return true;
                }
            }
            return false;
        }

    }

}
