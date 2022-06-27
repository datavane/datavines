package io.datavines.server.coordinator.api.dto.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode
@ToString
@Data
public class SlaJobCreate {
    @NotNull(message = "slaId must not null")
    private Long slaId;
    @NotNull(message = "jobId must not null")
    private Long jobId;
}
