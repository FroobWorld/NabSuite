package com.froobworld.nabsuite.command.argument.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.util.DurationParser;
import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;

public class DurationArgument<C> extends CommandArgument<C, Long> {

    @SafeVarargs
    public DurationArgument(boolean required, @NonNull String name, ArgumentPredicate<C, Long>... predicates) {
        super(required, name, new Parser<>(predicates), Long.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, Long> {
        private final ArgumentPredicate<C, Long>[] predicates;

        @SafeVarargs
        private Parser(ArgumentPredicate<C, Long>... predicates) {
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<Long> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            long duration;
            try {
                duration = DurationParser.fromString(inputQueue.remove());
            } catch (Exception e) {
                return ArgumentParseResult.failure(e);
            }
            return ArgumentPredicate.testAll(commandContext, duration, predicates);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            return Lists.newArrayList("30m", "1h", "1d", "7d", "14d", "30d");
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }
}
