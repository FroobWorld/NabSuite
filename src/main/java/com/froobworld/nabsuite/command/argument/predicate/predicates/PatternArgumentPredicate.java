package com.froobworld.nabsuite.command.argument.predicate.predicates;

import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;

import java.util.regex.Pattern;

public class PatternArgumentPredicate<C> extends ArgumentPredicate<C, String> {

    public PatternArgumentPredicate(Pattern pattern, String failureString) {
        super(true, (context, string) -> pattern.matcher(string).matches(), failureString);
    }

}
