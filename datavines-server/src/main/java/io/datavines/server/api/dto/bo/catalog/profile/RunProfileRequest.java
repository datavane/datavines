package io.datavines.server.api.dto.bo.catalog.profile;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class RunProfileRequest {

    @NotBlank(message = "profile uuid cannot be empty")
    private String uuid;

    private boolean selectAll;

    private List<String> selectedColumnList;
}
