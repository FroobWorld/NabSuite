package com.froobworld.nabsuite.modules.protect.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class FlagArgument<C> extends CommandArgument<C, String> {

    @SafeVarargs
    public FlagArgument(boolean required, @NonNull String name, ArgumentPredicate<C, String>... predicates) {
        super(required, name, new Parser<>(predicates), String.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, String> {
        private final ArgumentPredicate<C, String>[] predicates;

        @SafeVarargs
        private Parser(ArgumentPredicate<C, String>... predicates) {
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String flag = inputQueue.remove();
            if (!Flags.flags.contains(flag)) {
                return ArgumentParseResult.failure(new IllegalArgumentException("A flag by that name does not exist."));
            }
            return ArgumentPredicate.testAll(commandContext, flag, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (String flag : Flags.flags) {
                if (ArgumentPredicate.testAll(commandContext, flag, predicates).getFailure().isEmpty()) {
                    suggestions.add(flag);
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
