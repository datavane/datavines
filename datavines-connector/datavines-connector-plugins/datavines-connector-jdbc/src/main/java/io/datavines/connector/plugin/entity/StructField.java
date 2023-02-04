package io.datavines.connector.plugin.entity;

import io.datavines.common.enums.DataType;
import lombok.Data;

@Data
public class StructField {

    private String name;

    private DataType dataType;

    private boolean nullable;

    private String comment;
}
