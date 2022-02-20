package com.froobworld.nabsuite.util;

public final class NumberDisplayer {

    private NumberDisplayer() {}

    public static String numberToString(int number, boolean capitalise) {
        if (number == 0) {
            return capitalise ? "Zero" : "zero";
        } else if (number == 1) {
            return capitalise ? "One" : "one";
        } else if (number == 2) {
            return capitalise ? "Two" : "two";
        } else if (number == 3) {
            return capitalise ? "Three" : "three";
        } else if (number == 4) {
            return capitalise ? "Four" : "four";
        } else if (number == 5) {
            return capitalise ? "Five" : "five";
        } else if (number == 6) {
            return capitalise ? "Six" : "six";
        } else if (number == 7) {
            return capitalise ? "Seven" : "seven";
        } else if (number == 8) {
            return capitalise ? "Eight" : "eight";
        } else if (number == 9) {
            return capitalise ? "Nine" : "nine";
        }
        return number + "";
    }

    public static String toStringWithModifier(int number, String singularModifier, String pluralModifier, boolean capitalise) {
        return numberToString(number, capitalise) + (number == 1 ? singularModifier : pluralModifier);
    }

    public static String toStringWithModifierAndPrefix(int number, String singularModifier, String pluralModifier, String singularPrefix, String pluralPrefix) {
        return (number == 1 ? singularPrefix : pluralPrefix) + numberToString(number, false) + (number == 1 ? singularModifier : pluralModifier);
    }

}
