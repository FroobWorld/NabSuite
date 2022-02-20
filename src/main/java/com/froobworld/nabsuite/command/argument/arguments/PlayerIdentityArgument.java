package com.froobworld.nabsuite.command.argument.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.data.identity.PlayerIdentityManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlayerIdentityArgument<C> extends CommandArgument<C, PlayerIdentity> {
    private static final Pattern uuidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    @SafeVarargs
    public PlayerIdentityArgument(boolean required, @NonNull String name, PlayerIdentityManager playerIdentityManager, boolean prioritiseOnline, ArgumentPredicate<C, PlayerIdentity>... predicates) {
        super(required, name, new Parser<>(playerIdentityManager, prioritiseOnline, predicates), PlayerIdentity.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, PlayerIdentity> {
        private final PlayerIdentityManager playerIdentityManager;
        private final boolean prioritiseOnline;
        private final ArgumentPredicate<C, PlayerIdentity>[] predicates;

        @SafeVarargs
        private Parser(PlayerIdentityManager playerIdentityManager, boolean prioritiseOnline, ArgumentPredicate<C, PlayerIdentity>... predicates) {
            this.playerIdentityManager = playerIdentityManager;
            this.prioritiseOnline = prioritiseOnline;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<PlayerIdentity> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
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

            return ArgumentPredicate.testAll(commandContext, playerIdentity, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            if (prioritiseOnline) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input.toLowerCase())) {
                        PlayerIdentity playerIdentity = playerIdentityManager.getPlayerIdentity(player);
                        if (ArgumentPredicate.testAll(commandContext, playerIdentity, predicates).getFailure().isEmpty()) {
                            suggestions.add(player.getName());
                        }
                    }
                }
                if (!suggestions.isEmpty()) {
                    return suggestions;
                }
            }
            suggestions = playerIdentityManager.getAllPlayerIdentities().stream()
                    .filter(playerIdentity -> playerIdentity.getLastName().toLowerCase().startsWith(input.toLowerCase()))
                    .filter(playerIdentity -> ArgumentPredicate.testAll(commandContext, playerIdentity, predicates).getFailure().isEmpty())
                    .map(PlayerIdentity::getLastName)
                    .collect(Collectors.toList());
            if (suggestions.isEmpty()) {
                suggestions = playerIdentityManager.getAllPlayerIdentities().stream()
                        .filter(playerIdentity -> playerIdentity.getUuid().toString().toLowerCase().startsWith(input.toLowerCase()))
                        .filter(playerIdentity -> ArgumentPredicate.testAll(commandContext, playerIdentity, predicates).getFailure().isEmpty())
                        .map(playerIdentity -> playerIdentity.getUuid().toString())
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
