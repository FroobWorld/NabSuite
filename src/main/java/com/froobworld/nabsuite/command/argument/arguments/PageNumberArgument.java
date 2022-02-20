package com.froobworld.nabsuite.command.argument.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.util.ListPaginator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class PageNumberArgument<C> extends CommandArgument<C, Integer> {

    public PageNumberArgument(boolean required, @NonNull String name, Function<CommandContext<C>, Integer> maxPageFunction) {
        super(required, name, new Parser<>(maxPageFunction), Integer.class);
    }

    public PageNumberArgument(boolean required, @NonNull String name, Function<CommandContext<C>, Integer> totalItemsFunction, int itemsPerPage) {
        super(required, name, new Parser<>(totalItemsFunction.andThen(totalItems -> ListPaginator.numberOfPages(totalItems, itemsPerPage))), Integer.class);
    }

    @Override
    public @NonNull String getDefaultValue() {
        return 1 + "";
    }

    private static final class Parser<C> implements ArgumentParser<C, Integer> {
        private final Function<CommandContext<C>, Integer> maxPageFunction;

        private Parser(Function<CommandContext<C>, Integer> maxPageFunction) {
            this.maxPageFunction = maxPageFunction;
        }

        @Override
        public @NonNull ArgumentParseResult<Integer> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            int maxPage = Math.max(maxPageFunction.apply(commandContext), 1);
            try {
                int page = Integer.parseInt(input);
                if (page < 1 || page > maxPage) {
                    return ArgumentParseResult.failure(new IntegerArgument.IntegerParseException(
                            input,
                            1,
                            maxPage,
                            commandContext
                    ));
                }
                return ArgumentParseResult.success(page);
            } catch (Exception ex) {
                return ArgumentParseResult.failure(new IntegerArgument.IntegerParseException(
                        input,
                        1,
                        maxPage,
                        commandContext
                ));
            }
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            int maxPage = Math.max(maxPageFunction.apply(commandContext), 1);
            ArrayList<String> suggestions = new ArrayList<>();
            for (int i = 1; i <= maxPage; i++) {
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
