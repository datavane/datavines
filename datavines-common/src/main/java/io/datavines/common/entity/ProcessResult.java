package io.datavines.common.entity;

import io.datavines.common.enums.ExecutionStatus;

/**
 * 
 */
public class ProcessResult {

    private Integer exitStatusCode;

    private String applicationId;

    private Integer processId;

    public ProcessResult(){
        this.exitStatusCode = ExecutionStatus.FAILURE.getCode();
        this.processId = -1;
    }

    public ProcessResult(Integer exitStatusCode){
        this.exitStatusCode = exitStatusCode;
        this.processId = -1;
    }

    public Integer getExitStatusCode() {
        return exitStatusCode;
    }

    public void setExitStatusCode(Integer exitStatusCode) {
        this.exitStatusCode = exitStatusCode;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }
}
