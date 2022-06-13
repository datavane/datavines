package io.datavines.server.coordinator.api.dto.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SlasSenderUpdate extends SlasSenderCreate{
    private Long id;
}
