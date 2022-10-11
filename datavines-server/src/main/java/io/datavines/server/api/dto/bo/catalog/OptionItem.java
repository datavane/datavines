package io.datavines.server.api.dto.bo.catalog;

import lombok.Data;

import java.io.Serializable;

@Data
public class OptionItem implements Serializable {

    private String uuid;

    private String name;

    private String type;

    private String status;

}
