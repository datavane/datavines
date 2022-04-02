package io.datavines.engine.spark.core;

import io.datavines.engine.core.DataVinesBootstrap;

public class SparkDataVinesBootstrap extends DataVinesBootstrap {

    public static void main(String[] args) {
        SparkDataVinesBootstrap bootstrap = new SparkDataVinesBootstrap();
        bootstrap.execute(args);
    }
}
