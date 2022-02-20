package com.froobworld.nabsuite.util;

import java.io.IOException;

public interface CheckedBiFunction<T, U, R> {

    R apply(T t, U u) throws IOException;

}
