package com.froobworld.nabsuite.hook;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;

public class SparkHook {

    public Spark getSpark() {
        return SparkProvider.get();
    }

}
