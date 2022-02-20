package com.froobworld.nabsuite.command.argument.predicate;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;

import java.util.function.BiPredicate;

public class ArgumentPredicate<C, A> {
    private final BiPredicate<CommandContext<C>, A> predicate;
    private final boolean contextFree;
    private final String failureString;

    public ArgumentPredicate(boolean contextFree, BiPredicate<CommandContext<C>, A> predicate, String failureString) {
        this.contextFree = contextFree;
        this.predicate = predicate;
        this.failureString = failureString;
    }

    public boolean test(CommandContext<C> context, A argument) {
        return predicate.test(context, argument);
    }

    public boolean isContextFree() {
        return contextFree;
    }

    public String getFailureString() {
        return failureString;
    }

    @SafeVarargs
    public static <A,C> ArgumentParseResult<A> testAll(CommandContext<C> context, A argument, ArgumentPredicate<C, A>... predicates) {
        for (ArgumentPredicate<C, A> predicate : predicates) {
            if (!predicate.test(context, argument)) {
                return ArgumentParseResult.failure(new ArgumentPredicateFailureException(predicate));
            }
        }
        return ArgumentParseResult.success(argument);
    }

    public static boolean allContextFree(ArgumentPredicate<?, ?>... predicates) {
        for (ArgumentPredicate<?, ?> predicate : predicates) {
            if (!predicate.isContextFree()) {
                return false;
            }
        }
        return true;
    }

    public static class ArgumentPredicateFailureException extends IllegalArgumentException {

        public ArgumentPredicateFailureException(ArgumentPredicate<?,?> failedPredicate) {
            super(failedPredicate.failureString);
        }

    }

}
