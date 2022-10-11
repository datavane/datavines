package io.datavines.server.api.dto.bo.job;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "JobCreateWithEntityUuid cannot be null")
public class JobCreateWithEntityUuid {

    @NotNull(message = "JobCreate cannot be null")
    private JobCreate jobCreate;

    @NotNull(message = "entity uuid cannot be null")
    private String entityUuid;
}
