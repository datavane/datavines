package io.datavines.server.coordinator.api.dto.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SlasUpdate extends SlasCreate {
    private Long id;
}
