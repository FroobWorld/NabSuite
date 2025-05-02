package com.froobworld.nabsuite.modules.admin.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyLevel;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DeputyLevelArgument<C> extends CommandArgument<C, DeputyLevel> {

    @SafeVarargs
    public DeputyLevelArgument(boolean required, @NonNull String name, DeputyManager deputyManager, ArgumentPredicate<C, DeputyLevel>... predicates) {
        super(required, name, new DeputyLevelArgument.Parser<>(deputyManager, predicates), DeputyLevel.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, DeputyLevel> {
        private final DeputyManager deputyManager;
        private final ArgumentPredicate<C, DeputyLevel>[] predicates;

        @SafeVarargs
        private Parser(DeputyManager deputyManager, ArgumentPredicate<C, DeputyLevel>... predicates) {
            this.deputyManager = deputyManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<DeputyLevel> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(DeputyLevelArgument.Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            for (DeputyLevel deputyLevel: deputyManager.getDeputyLevels()) {
                if (deputyLevel.getName().equalsIgnoreCase(input)) {
                    return ArgumentPredicate.testAll(commandContext, deputyLevel, predicates);
                }
            }
            return ArgumentParseResult.failure(new DeputyLevelNotFoundException(input));
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (DeputyLevel deputyLevel: deputyManager.getDeputyLevels()) {
                if (deputyLevel.getName().toLowerCase().startsWith(input.toLowerCase())) {
                    if (ArgumentPredicate.testAll(commandContext, deputyLevel, predicates).getFailure().isEmpty()) {
                        suggestions.add(deputyLevel.getName());
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

    public static class DeputyLevelNotFoundException extends IllegalArgumentException {

        public DeputyLevelNotFoundException(String input) {
            super("No deputy level found for input '" + input + "'");
        }

    }

}
