package io.datavines.common.entity.job.builder;

import io.datavines.common.enums.JobType;

public class TaskParameterBuilderFactory {

    public static ParameterBuilder builder(JobType jobType){
        switch (jobType) {
            case DATA_QUALITY:
            case DATA_PROFILE:
                return new DataQualityTaskParameterBuilder();
            default:
                return new DataQualityTaskParameterBuilder();
        }
    }
}
