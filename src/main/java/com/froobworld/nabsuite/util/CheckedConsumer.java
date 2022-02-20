package com.froobworld.nabsuite.util;

import java.io.IOException;

public interface CheckedConsumer<T> {

    void accept(T t) throws IOException;

}
