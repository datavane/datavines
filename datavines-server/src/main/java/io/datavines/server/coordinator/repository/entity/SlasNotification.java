package io.datavines.server.coordinator.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@ToString
@TableName("dv_slas_notification")
public class SlasNotification {
    private static final long serialVersionUID = -1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "type")
    private String type;

    @TableField(value = "work_space_id")
    private Long workSpaceId;

    @TableField(value = "slasId")
    private Long slasId;

    @TableField(value = "sender_id")
    private Long senderId;

    @TableField(value = "receiver_id")
    private Long receiverId;

    @TableField(value = "create_by")
    private Long createBy;

    @TableField(value = "create_time")
    @JsonFormat(pattern = "uuuu-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(value = "update_by")
    private Long updateBy;

    @TableField(value = "update_time")
    @JsonFormat(pattern = "uuuu-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
