package io.datavines.server.api.dto.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CatalogTableDetailVO extends CatalogEntityBaseDetailVO {

    private String comment;

    private int columns;
}
