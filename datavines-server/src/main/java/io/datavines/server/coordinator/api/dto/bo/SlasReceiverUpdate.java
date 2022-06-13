package io.datavines.server.coordinator.api.dto.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SlasReceiverUpdate extends SlasReceiverCreate{

    private Long id;
}
