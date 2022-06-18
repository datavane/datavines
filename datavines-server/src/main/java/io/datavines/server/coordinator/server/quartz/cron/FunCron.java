package io.datavines.server.coordinator.server.quartz.cron;

import io.datavines.server.coordinator.repository.entity.JobSchedule;
import org.springframework.beans.factory.InitializingBean;

public interface FunCron extends InitializingBean {
    public String funcDeal(JobSchedule jobschedule);

    public String getFuncName();
}
