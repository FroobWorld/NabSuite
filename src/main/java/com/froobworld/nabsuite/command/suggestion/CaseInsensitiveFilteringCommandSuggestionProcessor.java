package com.froobworld.nabsuite.command.suggestion;

import cloud.commandframework.execution.CommandSuggestionProcessor;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedList;
import java.util.List;

public class CaseInsensitiveFilteringCommandSuggestionProcessor <C> implements CommandSuggestionProcessor<C> {

    @Override
    public @NonNull List<@NonNull String> apply(
            final @NonNull CommandPreprocessingContext<C> context,
            final @NonNull List<@NonNull String> strings
    ) {
        final String input;
        if (context.getInputQueue().isEmpty()) {
            input = "";
        } else {
            input = context.getInputQueue().peek();
        }
        final List<String> suggestions = new LinkedList<>();
        for (final String suggestion : strings) {
            if (suggestion.toLowerCase().startsWith(input.toLowerCase())) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }

}
