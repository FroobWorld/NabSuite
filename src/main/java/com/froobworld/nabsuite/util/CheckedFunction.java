package com.froobworld.nabsuite.util;

import java.io.IOException;

public interface CheckedFunction<T, R> {

    R apply(T t) throws IOException;

}
