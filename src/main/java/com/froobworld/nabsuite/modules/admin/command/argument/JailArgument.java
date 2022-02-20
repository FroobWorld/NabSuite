package com.froobworld.nabsuite.modules.admin.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.jail.Jail;
import com.froobworld.nabsuite.modules.admin.jail.JailManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class JailArgument<C> extends CommandArgument<C, Jail> {

    @SafeVarargs
    public JailArgument(boolean required, @NonNull String name, JailManager jailManager, ArgumentPredicate<C, Jail>... predicates) {
        super(required, name, new Parser<>(jailManager, predicates), Jail.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, Jail> {
        private final JailManager jailManager;
        private final ArgumentPredicate<C, Jail>[] predicates;

        @SafeVarargs
        private Parser(JailManager jailManager, ArgumentPredicate<C, Jail>... predicates) {
            this.jailManager = jailManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<Jail> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            Jail jail = jailManager.getJail(input);
            if (jail == null) {
                return ArgumentParseResult.failure(new JailNotFoundException(input));
            }
            return ArgumentPredicate.testAll(commandContext, jail, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (Jail jail : jailManager.getJails()) {
                if (ArgumentPredicate.testAll(commandContext, jail, predicates).getFailure().isEmpty()) {
                    suggestions.add(jail.getName());
                }
            }

            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class JailNotFoundException extends IllegalArgumentException {

        public JailNotFoundException(String input) {
            super("No jail found for input '" + input + "'");
        }

    }

}
