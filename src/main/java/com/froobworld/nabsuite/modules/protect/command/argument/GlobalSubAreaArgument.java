package com.froobworld.nabsuite.modules.protect.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.area.GlobalAreaManager;
import com.froobworld.nabsuite.modules.protect.area.GlobalSubArea;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class GlobalSubAreaArgument<C> extends CommandArgument<C, GlobalSubArea> {

    @SafeVarargs
    public GlobalSubAreaArgument(boolean required, @NonNull String name, GlobalAreaManager globalAreaManager, Function<CommandContext<C>, World> worldFunction, ArgumentPredicate<C, GlobalSubArea>... predicates) {
        super(required, name, new Parser<>(globalAreaManager, worldFunction, predicates), GlobalSubArea.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, GlobalSubArea> {
        private final GlobalAreaManager globalAreaManager;
        private final Function<CommandContext<C>, World> worldFunction;
        private final ArgumentPredicate<C, GlobalSubArea>[] predicates;

        @SafeVarargs
        private Parser(GlobalAreaManager globalAreaManager, Function<CommandContext<C>, World> worldFunction, ArgumentPredicate<C, GlobalSubArea>... predicates) {
            this.globalAreaManager = globalAreaManager;
            this.worldFunction = worldFunction;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<GlobalSubArea> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            World world = worldFunction.apply(commandContext);
            GlobalSubArea area = globalAreaManager.getGlobalSubArea(world, input);
            if (area == null) {
                return ArgumentParseResult.failure(new AreaNotFoundException(input));
            }
            return ArgumentPredicate.testAll(commandContext, area, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            World world = worldFunction.apply(commandContext);
            for (GlobalSubArea area : globalAreaManager.getGlobalArea(world).getSubAreas()) {
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
            super("No sub area found for input '" + input + "'");
        }

    }

}
