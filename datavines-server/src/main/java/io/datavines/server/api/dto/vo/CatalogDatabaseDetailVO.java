package io.datavines.server.api.dto.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CatalogDatabaseDetailVO extends CatalogEntityBaseDetailVO {

    private int tables;
}
