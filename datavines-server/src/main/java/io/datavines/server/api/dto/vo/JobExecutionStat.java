package io.datavines.server.api.dto.vo;


import lombok.Data;

@Data
public class JobExecutionStat {

    private Long jobId;

    /**
     * 总执行次数
     */
    private Integer totalCount;

    /**
     * 执行成功次数
     */
    private Integer successCount;

    /**
     * 执行失败次数
     */
    private Integer failCount;

    /**
     * 首次执行时间
     */
    private String firstJobExecutionTime;

    /**
     * 最近一次执行时间
     */
    private String lastJobExecutionTime;


}
