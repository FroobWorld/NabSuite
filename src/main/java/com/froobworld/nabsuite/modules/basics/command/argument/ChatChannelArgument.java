package com.froobworld.nabsuite.modules.basics.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannel;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannelManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ChatChannelArgument<C> extends CommandArgument<C, ChatChannel> {

    @SafeVarargs
    public ChatChannelArgument(boolean required, @NonNull String name, ChatChannelManager chatChannelManager, ArgumentPredicate<C, ChatChannel>... predicates) {
        super(required, name, new Parser<>(chatChannelManager, predicates), ChatChannel.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, ChatChannel> {
        private final ChatChannelManager chatChannelManager;
        private final ArgumentPredicate<C, ChatChannel>[] predicates;

        @SafeVarargs
        private Parser(ChatChannelManager chatChannelManager, ArgumentPredicate<C, ChatChannel>... predicates) {
            this.chatChannelManager = chatChannelManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<ChatChannel> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            ChatChannel channel = chatChannelManager.getChannel(input);
            if (channel == null) {
                return ArgumentParseResult.failure(new ChatChannelNotFoundException(input));
            }
            return ArgumentPredicate.testAll(commandContext, channel, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (ChatChannel channel : chatChannelManager.getChannels()) {
                if (ArgumentPredicate.testAll(commandContext, channel, predicates).getFailure().isEmpty()) {
                    suggestions.add(channel.getName());
                }
            }

            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class ChatChannelNotFoundException extends IllegalArgumentException {

        public ChatChannelNotFoundException(String input) {
            super("No chat channel found for input '" + input + "'");
        }

    }

}