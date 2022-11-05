package io.datavines.server.dqc.coordinator.quartz;

import io.datavines.server.enums.ScheduleJobType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleJobInfo {

    private ScheduleJobType type;

    private Long datasourceId;

    private Long id;

    private String cronExpression;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public ScheduleJobInfo(ScheduleJobType type,
                           Long datasourceId,
                           Long id,
                           String cronExpression,
                           LocalDateTime startTime,
                           LocalDateTime endTime) {
        this.type = type;
        this.datasourceId = datasourceId;
        this.id = id;
        this.cronExpression = cronExpression;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
