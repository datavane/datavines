package io.datavines.server.coordinator.api.dto.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public class SlasReceiverCreate {

    private String type;

    private String name;

    private Long workSpaceId;

    private String config;
}
