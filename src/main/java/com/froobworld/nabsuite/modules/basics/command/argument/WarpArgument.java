package com.froobworld.nabsuite.modules.basics.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.teleport.warp.Warp;
import com.froobworld.nabsuite.modules.basics.teleport.warp.WarpManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class WarpArgument<C> extends CommandArgument<C, Warp> {

    @SafeVarargs
    public WarpArgument(boolean required, @NonNull String name, WarpManager warpManager, ArgumentPredicate<C, Warp>... predicates) {
        super(required, name, new Parser<>(warpManager, predicates), Warp.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, Warp> {
        private final WarpManager warpManager;
        private final ArgumentPredicate<C, Warp>[] predicates;

        @SafeVarargs
        private Parser(WarpManager warpManager, ArgumentPredicate<C, Warp>... predicates) {
            this.warpManager = warpManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<Warp> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            Warp warp = warpManager.getWarp(input);
            if (warp == null) {
                return ArgumentParseResult.failure(new WarpNotFoundException(input));
            }
            return ArgumentPredicate.testAll(commandContext, warp, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (Warp warp : warpManager.getWarps()) {
                if (ArgumentPredicate.testAll(commandContext, warp, predicates).getFailure().isEmpty()) {
                    suggestions.add(warp.getName());
                }
            }

            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class WarpNotFoundException extends IllegalArgumentException {

        public WarpNotFoundException(String input) {
            super("No warp found for input '" + input + "'");
        }

    }

}
