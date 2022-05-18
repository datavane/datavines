package io.datavines.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dv_slas_job")
public class SlasJob {

    private static final long serialVersionUID = -1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "work_space_id")
    private Long workSpaceId;

    @TableField(value = "slas_id")
    private Long slasId;

    @TableField(value = "job_id")
    private Long jobId;

    @TableField(value = "create_by")
    private Long createBy;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_by")
    private Long updateBy;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
