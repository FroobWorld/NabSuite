package com.froobworld.nabsuite.modules.admin.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyManager;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DeputyPlayerArgument<C> extends CommandArgument<C, DeputyPlayer> {

    @SafeVarargs
    public DeputyPlayerArgument(boolean required, @NonNull String name, DeputyManager deputyManager, ArgumentPredicate<C, DeputyPlayer>... predicates) {
        super(required, name, new DeputyPlayerArgument.Parser<>(deputyManager, predicates), DeputyPlayer.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, DeputyPlayer> {
        private final DeputyManager deputyManager;
        private final ArgumentPredicate<C, DeputyPlayer>[] predicates;

        @SafeVarargs
        private Parser(DeputyManager deputyManager, ArgumentPredicate<C, DeputyPlayer>... predicates) {
            this.deputyManager = deputyManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<DeputyPlayer> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(DeputyPlayerArgument.Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            for (DeputyPlayer deputyPlayer: deputyManager.getDeputies()) {
                if (deputyPlayer.getPlayerIdentity().getLastName().equalsIgnoreCase(input)) {
                    return ArgumentPredicate.testAll(commandContext, deputyPlayer, predicates);
                }
            }
            return ArgumentParseResult.failure(new DeputyPlayerArgument.DeputyNotFoundException(input));
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (DeputyPlayer deputyPlayer: deputyManager.getDeputies()) {
                if (deputyPlayer.getPlayerIdentity().getLastName().toLowerCase().startsWith(input.toLowerCase())) {
                    if (ArgumentPredicate.testAll(commandContext, deputyPlayer, predicates).getFailure().isEmpty()) {
                        suggestions.add(deputyPlayer.getPlayerIdentity().getLastName());
                    }
                }
            }
            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class DeputyNotFoundException extends IllegalArgumentException {

        public DeputyNotFoundException(String input) {
            super("No deputy found for input '" + input + "'");
        }

    }

}
