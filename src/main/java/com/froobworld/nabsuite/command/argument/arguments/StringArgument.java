package com.froobworld.nabsuite.command.argument.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class StringArgument<C> extends CommandArgument<C, String> {
    private final String defaultValue;
    private final List<String> suggestions;

    @SafeVarargs
    public StringArgument(boolean required, @NonNull String name, String defaultValue, boolean greedy, List<String> suggestions, ArgumentPredicate<C, String>... predicates) {
        super(required, name, greedy ? new GreedyParser<>(suggestions, predicates) : new Parser<>(suggestions, predicates), String.class);
        this.defaultValue = defaultValue;
        this.suggestions = suggestions;
    }

    @SafeVarargs
    public StringArgument(boolean required, @NonNull String name, String defaultValue, boolean greedy, ArgumentPredicate<C, String>... predicates) {
        this(required, name, defaultValue, greedy, Collections.emptyList(), predicates);
    }

    @SafeVarargs
    public StringArgument(boolean required, @NonNull String name, boolean greedy, ArgumentPredicate<C, String>... predicates) {
        this(required, name, "", greedy, predicates);
    }

    @SafeVarargs
    public StringArgument(boolean required, @NonNull String name, boolean greedy, List<String> suggestions, ArgumentPredicate<C, String>... predicates) {
        this(required, name, "", greedy, suggestions, predicates);
    }

    @Override
    public @NonNull String getDefaultValue() {
        return defaultValue;
    }

    private static final class Parser<C> implements ArgumentParser<C, String> {
        private final List<String> suggestions;
        private final ArgumentPredicate<C, String>[] predicates;

        @SafeVarargs
        private Parser(List<String> suggestions, ArgumentPredicate<C, String>... predicates) {
            this.suggestions = suggestions;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            return ArgumentPredicate.testAll(commandContext, inputQueue.remove(), predicates);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            return suggestions.stream()
                    .filter(string -> ArgumentPredicate.testAll(commandContext, string, predicates).getFailure().isEmpty())
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    private static final class GreedyParser<C> implements ArgumentParser<C, String> {
        private final List<String> suggestions;
        private final ArgumentPredicate<C, String>[] predicates;

        @SafeVarargs
        private GreedyParser(List<String> suggestions, ArgumentPredicate<C, String>... predicates) {
            this.suggestions = suggestions;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String value = null;
            while (!inputQueue.isEmpty()) {
                String nextString = inputQueue.peek();
                if (nextString.startsWith("--")) {
                    break;
                }
                nextString = nextString.startsWith("\\--") ? inputQueue.remove().replaceFirst("\\\\", "") : inputQueue.remove();
                value = value == null ? nextString : (value + " " + nextString);
            }

            return ArgumentPredicate.testAll(commandContext, value, predicates);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            return suggestions.stream()
                    .filter(string -> ArgumentPredicate.testAll(commandContext, string, predicates).getFailure().isEmpty())
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

}
