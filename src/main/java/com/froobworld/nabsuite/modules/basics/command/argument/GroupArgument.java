package com.froobworld.nabsuite.modules.basics.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.user.GroupUserManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class GroupArgument<C> extends CommandArgument<C, String> {

    @SafeVarargs
    public GroupArgument(boolean required, @NonNull String name, GroupUserManager groupUserManager, ArgumentPredicate<C, String>... predicates) {
        super(required, name, new GroupArgument.Parser<>(groupUserManager, predicates), String.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, String> {
        private final GroupUserManager groupUserManager;
        private final ArgumentPredicate<C, String>[] predicates;

        @SafeVarargs
        private Parser(GroupUserManager groupUserManager, ArgumentPredicate<C, String>... predicates) {
            this.groupUserManager = groupUserManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(GroupArgument.Parser.class, commandContext));
            }

            String input = inputQueue.remove();
            for (String group: groupUserManager.getAllowableGroups()) {
                if (input.equalsIgnoreCase(group)) {
                    return ArgumentPredicate.testAll(commandContext, group, predicates);
                }
            }

            return ArgumentParseResult.failure(new GroupNotFoundException(input));
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            return groupUserManager.getAllowableGroups().stream()
                    .filter(group -> group.toLowerCase().startsWith(input.toLowerCase()))
                    .filter(group -> ArgumentPredicate.testAll(commandContext, group, predicates).getFailure().isEmpty())
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class GroupNotFoundException extends IllegalArgumentException {

        public GroupNotFoundException(String input) {
            super("No group found for input '" + input + "'");
        }

    }
}
