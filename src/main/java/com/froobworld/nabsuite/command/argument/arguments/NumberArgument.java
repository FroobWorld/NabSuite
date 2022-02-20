package com.froobworld.nabsuite.command.argument.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class NumberArgument<C> extends CommandArgument<C, Integer> {

    public NumberArgument(boolean required, @NonNull String name, Function<CommandContext<C>, Integer> minValueFunction, Function<CommandContext<C>, Integer> maxValueFunction) {
        super(required, name, new Parser<>(minValueFunction, maxValueFunction), Integer.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, Integer> {
        private final Function<CommandContext<C>, Integer> minValueFunction;
        private final Function<CommandContext<C>, Integer> maxValueFunction;

        public Parser(Function<CommandContext<C>, Integer> minValueFunction, Function<CommandContext<C>, Integer> maxValueFunction) {
            this.minValueFunction = minValueFunction;
            this.maxValueFunction = maxValueFunction;
        }

        @Override
        public @NonNull ArgumentParseResult<Integer> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            int min = minValueFunction.apply(commandContext);
            int max = maxValueFunction.apply(commandContext);
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    return ArgumentParseResult.failure(new IntegerArgument.IntegerParseException(
                            input,
                            min,
                            max,
                            commandContext
                    ));
                }
                return ArgumentParseResult.success(value);
            } catch (Exception ex) {
                return ArgumentParseResult.failure(new IntegerArgument.IntegerParseException(
                        input,
                        min,
                        max,
                        commandContext
                ));
            }
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            int min = minValueFunction.apply(commandContext);
            int max = maxValueFunction.apply(commandContext);
            ArrayList<String> suggestions = new ArrayList<>();
            for (int i = min; i <= max; i++) {
                suggestions.add(i + "");
            }
            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return false;
        }

    }

}
