package com.froobworld.nabsuite.modules.admin.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.modules.admin.ticket.TicketManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class TicketArgument<C> extends CommandArgument<C, Ticket> {

    @SafeVarargs
    public TicketArgument(boolean required, @NonNull String name, TicketManager ticketManager, ArgumentPredicate<C, Ticket>... predicates) {
        super(required, name, new Parser<>(ticketManager, predicates), Ticket.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, Ticket> {
        private final TicketManager ticketManager;
        private final ArgumentPredicate<C, Ticket>[] predicates;

        @SafeVarargs
        private Parser(TicketManager ticketManager, ArgumentPredicate<C, Ticket>... predicates) {
            this.ticketManager = ticketManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<Ticket> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                return ArgumentParseResult.failure(new IllegalArgumentException("Ticket ids must be integers"));
            }
            Ticket ticket = ticketManager.getTicket(id);
            if (ticket == null) {
                return ArgumentParseResult.failure(new TicketNotFoundException(input));
            }
            return ArgumentPredicate.testAll(commandContext, ticket, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (Ticket ticket : ticketManager.getTickets()) {
                if (ArgumentPredicate.testAll(commandContext, ticket, predicates).getFailure().isEmpty()) {
                    suggestions.add(ticket.getId() + "");
                }
            }

            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class TicketNotFoundException extends IllegalArgumentException {

        public TicketNotFoundException(String input) {
            super("No ticket found for input '" + input + "'");
        }

    }

}
