package com.froobworld.nabsuite.modules.protect.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.data.identity.PlayerIdentityManager;
import com.froobworld.nabsuite.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserArgument<C> extends CommandArgument<C, User> {
    private static final Pattern uuidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    @SafeVarargs
    public UserArgument(boolean required, @NonNull String name, NabSuite nabSuite, boolean prioritiseOnline, ArgumentPredicate<C, User>... predicates) {
        super(required, name, new Parser<>(nabSuite, prioritiseOnline, predicates), User.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, User> {
        private final NabSuite nabSuite;
        private final PlayerIdentityManager playerIdentityManager;
        private final boolean prioritiseOnline;
        private final ArgumentPredicate<C, User>[] predicates;

        @SafeVarargs
        private Parser(NabSuite nabSuite, boolean prioritiseOnline, ArgumentPredicate<C, User>... predicates) {
            this.nabSuite = nabSuite;
            this.playerIdentityManager = nabSuite.getPlayerIdentityManager();
            this.prioritiseOnline = prioritiseOnline;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<User> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            User user;
            if (input.toLowerCase().startsWith("group:")) {
                input = input.split(":", 2)[1];
                if (!nabSuite.getUserManager().getGroupUserManager().getAllowableGroups().contains(input.toLowerCase())) {
                    return ArgumentParseResult.failure(new IllegalArgumentException("Unknown group '" + input + "'"));
                }
                return ArgumentPredicate.testAll(commandContext, nabSuite.getUserManager().newGroupUser(input.toLowerCase()), predicates);
            } else {
                boolean friends = false;
                if (input.toLowerCase().startsWith("friends:")) {
                    friends = true;
                    input = input.split(":", 2)[1];
                }
                UUID uuid = uuidPattern.matcher(input).matches() ? UUID.fromString(input) : null;
                PlayerIdentity playerIdentity;
                if (uuid != null) {
                    playerIdentity = playerIdentityManager.getPlayerIdentity(uuid);
                } else {
                    Set<PlayerIdentity> playerIdentities = playerIdentityManager.getPlayerIdentity(input);
                    if (playerIdentities.isEmpty()) {
                        playerIdentity = null;
                    } else if (playerIdentities.size() > 1) {
                        return ArgumentParseResult.failure(new MultiplePlayersParseException(commandContext, playerIdentities));
                    } else {
                        playerIdentity = playerIdentities.stream().findAny().get();
                    }
                }
                if (playerIdentity == null) {
                    return ArgumentParseResult.failure(new cloud.commandframework.bukkit.parsers.PlayerArgument.PlayerParseException(input, commandContext));
                }
                user = friends ? nabSuite.getUserManager().newFriendsUser(playerIdentity.getUuid()) : nabSuite.getUserManager().newPlayerUser(playerIdentity.getUuid());
            }

            return ArgumentPredicate.testAll(commandContext, user, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            if (prioritiseOnline) {
                List<String> backupSuggestions = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input.toLowerCase())) {
                        PlayerIdentity playerIdentity = playerIdentityManager.getPlayerIdentity(player);
                        if (ArgumentPredicate.testAll(commandContext, nabSuite.getUserManager().newPlayerUser(playerIdentity.getUuid()), predicates).getFailure().isEmpty()) {
                            suggestions.add(player.getName());
                        }
                    }
                    if (("friends:" + player.getName().toLowerCase()).startsWith(input.toLowerCase())) {
                        PlayerIdentity playerIdentity = playerIdentityManager.getPlayerIdentity(player);
                        if (ArgumentPredicate.testAll(commandContext, nabSuite.getUserManager().newFriendsUser(playerIdentity.getUuid()), predicates).getFailure().isEmpty()) {
                            backupSuggestions.add("Friends:" + player.getName());
                        }
                    }
                }
                if (!suggestions.isEmpty()) {
                    return suggestions;
                } else if (!backupSuggestions.isEmpty()) {
                    return backupSuggestions;
                }
            }
            suggestions = playerIdentityManager.getAllPlayerIdentities().stream()
                    .filter(playerIdentity -> playerIdentity.getLastName().toLowerCase().startsWith(input.toLowerCase()))
                    .filter(playerIdentity -> ArgumentPredicate.testAll(commandContext, nabSuite.getUserManager().newPlayerUser(playerIdentity.getUuid()), predicates).getFailure().isEmpty())
                    .map(PlayerIdentity::getLastName)
                    .collect(Collectors.toList());
            if (suggestions.isEmpty()) {
                suggestions = playerIdentityManager.getAllPlayerIdentities().stream()
                        .filter(playerIdentity -> playerIdentity.getUuid().toString().toLowerCase().startsWith(input.toLowerCase()))
                        .filter(playerIdentity -> ArgumentPredicate.testAll(commandContext, nabSuite.getUserManager().newPlayerUser(playerIdentity.getUuid()), predicates).getFailure().isEmpty())
                        .map(playerIdentity -> playerIdentity.getUuid().toString())
                        .collect(Collectors.toList());
            }
            if (suggestions.isEmpty()) {
                suggestions = playerIdentityManager.getAllPlayerIdentities().stream()
                        .filter(playerIdentity -> ("friends:" + playerIdentity.getLastName().toLowerCase()).startsWith(input.toLowerCase()))
                        .filter(playerIdentity -> ArgumentPredicate.testAll(commandContext, nabSuite.getUserManager().newFriendsUser(playerIdentity.getUuid()), predicates).getFailure().isEmpty())
                        .map(playerIdentity -> "Friends:" + playerIdentity.getLastName())
                        .collect(Collectors.toList());
            }
            if (suggestions.isEmpty()) {
                suggestions = playerIdentityManager.getAllPlayerIdentities().stream()
                        .filter(playerIdentity -> ("friends:" + playerIdentity.getUuid().toString().toLowerCase()).startsWith(input.toLowerCase()))
                        .filter(playerIdentity -> ArgumentPredicate.testAll(commandContext, nabSuite.getUserManager().newFriendsUser(playerIdentity.getUuid()), predicates).getFailure().isEmpty())
                        .map(playerIdentity -> "Friends:" + playerIdentity.getUuid().toString())
                        .collect(Collectors.toList());
            }
            if (suggestions.isEmpty()) {
                suggestions = nabSuite.getUserManager().getGroupUserManager().getAllowableGroups().stream()
                        .filter(group -> ("group:" + group).startsWith(input.toLowerCase()))
                        .filter(group -> ArgumentPredicate.testAll(commandContext, nabSuite.getUserManager().newGroupUser(group), predicates).getFailure().isEmpty())
                        .map(group -> "Group:" + group)
                        .collect(Collectors.toList());
            }
            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class MultiplePlayersParseException extends IllegalStateException {

        protected MultiplePlayersParseException(@NonNull CommandContext<?> context, Set<PlayerIdentity> playerIdentities) {
            super("Multiple players previously seen with that name: \n" +
                    playerIdentities.stream()
                            .map(PlayerIdentity::getUuid)
                            .map(Objects::toString)
                            .collect(Collectors.joining("\n"))
            );
        }
    }

}
