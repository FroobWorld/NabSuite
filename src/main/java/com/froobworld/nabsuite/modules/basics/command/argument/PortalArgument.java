package com.froobworld.nabsuite.modules.basics.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.teleport.portal.Portal;
import com.froobworld.nabsuite.modules.basics.teleport.portal.PortalManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class PortalArgument<C> extends CommandArgument<C, Portal> {

    @SafeVarargs
    public PortalArgument(boolean required, @NonNull String name, PortalManager portalManager, ArgumentPredicate<C, Portal>... predicates) {
        super(required, name, new Parser<>(portalManager, predicates), Portal.class);
    }

    private static final class Parser<C> implements ArgumentParser<C, Portal> {
        private final PortalManager portalManager;
        private final ArgumentPredicate<C, Portal>[] predicates;

        @SafeVarargs
        private Parser(PortalManager portalManager, ArgumentPredicate<C, Portal>... predicates) {
            this.portalManager = portalManager;
            this.predicates = predicates;
        }

        @Override
        public @NonNull ArgumentParseResult<Portal> parse(@NonNull CommandContext<C> commandContext, @NonNull Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
            }
            String input = inputQueue.remove();
            Portal portal = portalManager.getPortal(input);
            if (portal == null) {
                return ArgumentParseResult.failure(new PortalNotFoundException(input));
            }
            return ArgumentPredicate.testAll(commandContext, portal, predicates);
        }

        @Override
        public @NonNull List<String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            List<String> suggestions = new ArrayList<>();
            for (Portal portal : portalManager.getPortals()) {
                if (ArgumentPredicate.testAll(commandContext, portal, predicates).getFailure().isEmpty()) {
                    suggestions.add(portal.getName());
                }
            }

            return suggestions;
        }

        @Override
        public boolean isContextFree() {
            return ArgumentPredicate.allContextFree(predicates);
        }

    }

    public static class PortalNotFoundException extends IllegalArgumentException {

        public PortalNotFoundException(String input) {
            super("No portal found for input '" + input + "'");
        }

    }

}
