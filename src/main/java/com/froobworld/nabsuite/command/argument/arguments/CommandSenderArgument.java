package com.froobworld.nabsuite.command.argument.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CommandSenderArgument<C> extends CommandArgument<C, CommandSender> {
    private static final String CONSOLE_IDENTIFIER = "(console)";

    @SafeVarargs
    public CommandSenderArgument(boolean required, @NonNull String name, ArgumentPredicate<C, CommandSender>... predicates) {
        super(required, name, new Parser<>(predicates), CommandSender.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, CommandSender> {
        private final ArgumentPredicate<C, CommandSender>[] predicates;

        @SafeVarargs
        private Parser(ArgumentPredicate<C, CommandSender>... predicates) {
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<CommandSender> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            CommandSender sender;
            String input = inputQueue.remove();
            if (input.equalsIgnoreCase(CONSOLE_IDENTIFIER)) {
                sender = Bukkit.getConsoleSender();
            } else {
                sender = Bukkit.getPlayer(input);
                if (sender == null) {
                    return ArgumentParseResult.failure(new PlayerArgument.PlayerParseException(input, commandContext));
                }
            }
            return ArgumentPredicate.testAll(commandContext, sender, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (ArgumentPredicate.testAll(commandContext, player, predicates).getFailure().isEmpty()) {
                    suggestions.add(player.getName());
                }
            }
            if (ArgumentPredicate.testAll(commandContext, Bukkit.getConsoleSender(), predicates).getFailure().isEmpty()) {
                suggestions.add(CONSOLE_IDENTIFIER);
            }

            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

}
