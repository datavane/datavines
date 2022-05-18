package io.datavines.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("dv_slas_sender")
public class SlasSender {
    private static final long serialVersionUID = -1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "slas_id")
    private Long slasId;

    @TableField(value = "name")
    private String name;

    @TableField(value = "config")
    private String config;

    @TableField(value = "create_by")
    private Long createBy;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_by")
    private Long updateBy;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
