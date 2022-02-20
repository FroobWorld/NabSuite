package com.froobworld.nabsuite.util;

import java.io.IOException;

public interface CheckedBiConsumer<T, U> {

    void accept(T t, U u) throws IOException;

}
