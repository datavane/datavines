package io.datavines.server.api.dto.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CatalogColumnDetailVO extends CatalogEntityBaseDetailVO {

    private String comment;
}
