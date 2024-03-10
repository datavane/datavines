/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.common.enums;

import java.util.HashMap;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * running status for workflow and task nodes
 * 
 */
public enum ExecutionStatus {

    /**
     * status：
     * 0 submit success
     * 1 running
     * 2 ready pause
     * 3 pause
     * 4 ready stop
     * 5 stop
     * 6 failure
     * 7 success
     * 8 need fault tolerance
     * 9 kill
     * 10 waiting thread
     * 11 waiting depend node complete
     */
    SUBMITTED_SUCCESS(0, "submitted", "已提交"),
    RUNNING_EXECUTION(1, "running", "执行中"),
    READY_PAUSE(2, "ready pause", "准备暂停"),
    PAUSE(3, "pause", "暂停"),
    READY_STOP(4, "ready stop", "准备停止"),
    STOP(5, "stop", "停止"),
    FAILURE(6, "failure", "失败"),
    SUCCESS(7, "success", "成功"),
    NEED_FAULT_TOLERANCE(8, "need fault tolerance","需要容错"),
    KILL(9, "kill", "强制终止"),
    WAITING_THREAD(10, "waiting thread", "等待线程"),
    WAITING_DEPEND(11, "waiting depend node complete","");

    ExecutionStatus(int code, String description,String zhDescription){
        this.code = code;
        this.description = description;
        this.zhDescription = zhDescription;
    }

    @Getter
    @EnumValue
    private final int code;
    @Getter
    private final String description;
    @Getter
    private final String zhDescription;

    private static final HashMap<Integer, ExecutionStatus> EXECUTION_STATUS_MAP = new HashMap<>();

    static {
       for (ExecutionStatus executionStatus: ExecutionStatus.values()){
           EXECUTION_STATUS_MAP.put(executionStatus.code,executionStatus);
       }
    }

    /**
    * status is success
    * @return status
    */
    public boolean typeIsSuccess(){
     return this == SUCCESS;
   }

    /**
    * status is failure
    * @return status
    */
    public boolean typeIsFailure(){
     return this == FAILURE || this == NEED_FAULT_TOLERANCE || this == KILL;
   }

    /**
    * status is finished
    * @return status
    */
    public boolean typeIsFinished(){

        return typeIsSuccess() || typeIsFailure() || typeIsCancel() || typeIsPause()
               || typeIsStop();
    }

    /**
     * status is waiting thread
     * @return status
     */
    public boolean typeIsWaitingThread(){
       return this == WAITING_THREAD;
   }

    /**
     * status is pause
     * @return status
     */
    public boolean typeIsPause(){
       return this == PAUSE;
   }
    /**
     * status is pause
     * @return status
     */
    public boolean typeIsStop(){
        return this == STOP;
    }

    /**
     * status is running
     * @return status
     */
    public boolean typeIsRunning(){
       return this == RUNNING_EXECUTION || this == WAITING_DEPEND;
   }

    /**
     * status is cancel
     * @return status
     */
    public boolean typeIsCancel(){
        return this == KILL || this == STOP ;
    }

    public static ExecutionStatus of(int status){
       if(EXECUTION_STATUS_MAP.containsKey(status)){
           return EXECUTION_STATUS_MAP.get(status);
       }
       throw new IllegalArgumentException("invalid status : " + status);
    }

    public boolean canPause(){
        return this == SUBMITTED_SUCCESS || this == READY_PAUSE;
    }

}
