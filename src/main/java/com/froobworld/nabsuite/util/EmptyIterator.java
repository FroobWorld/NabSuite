package com.froobworld.nabsuite.util;

import java.util.Iterator;

public final class EmptyIterator {

    private EmptyIterator() {}

    public static  <E> Iterator<E> get() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public E next() {
                throw new IllegalStateException();
            }
        };
    }

}
