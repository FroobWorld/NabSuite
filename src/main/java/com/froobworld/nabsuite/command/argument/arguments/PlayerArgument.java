package com.froobworld.nabsuite.command.argument.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class PlayerArgument<C> extends CommandArgument<C, Player> {

    @SafeVarargs
    public PlayerArgument(boolean required, @NonNull String name, ArgumentPredicate<C, Player>... predicates) {
        super(required, name, new Parser<>(predicates), Player.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, Player> {
        private final ArgumentPredicate<C, Player>[] predicates;

        @SafeVarargs
        private Parser(ArgumentPredicate<C, Player>... predicates) {
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<Player> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            Player player = Bukkit.getPlayer(input);
            if (player == null) {
                return ArgumentParseResult.failure(new cloud.commandframework.bukkit.parsers.PlayerArgument.PlayerParseException(input, commandContext));
            }
            return ArgumentPredicate.testAll(commandContext, player, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (ArgumentPredicate.testAll(commandContext, player, predicates).getFailure().isEmpty()) {
                    suggestions.add(player.getName());
                }
            }

            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

}
