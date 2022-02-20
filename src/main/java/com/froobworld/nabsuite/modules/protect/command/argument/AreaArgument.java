package com.froobworld.nabsuite.modules.protect.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class AreaArgument<C> extends CommandArgument<C, Area> {

    @SafeVarargs
    public AreaArgument(boolean required, @NonNull String name, AreaManager areaManager, ArgumentPredicate<C, Area>... predicates) {
        super(required, name, new Parser<>(areaManager, predicates), Area.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, Area> {
        private final AreaManager areaManager;
        private final ArgumentPredicate<C, Area>[] predicates;

        @SafeVarargs
        private Parser(AreaManager areaManager, ArgumentPredicate<C, Area>... predicates) {
            this.areaManager = areaManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<Area> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            Area area = areaManager.getArea(input);
            if (area == null) {
                return ArgumentParseResult.failure(new AreaNotFoundException(input));
            }
            return ArgumentPredicate.testAll(commandContext, area, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (Area area : areaManager.getAreas()) {
                if (ArgumentPredicate.testAll(commandContext, area, predicates).getFailure().isEmpty()) {
                    suggestions.add(area.getName());
                }
            }

            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class AreaNotFoundException extends IllegalArgumentException {

        public AreaNotFoundException(String input) {
            super("No area found for input '" + input + "'");
        }

    }

}
