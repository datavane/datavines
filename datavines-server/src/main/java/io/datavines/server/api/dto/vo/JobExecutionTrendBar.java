package io.datavines.server.api.dto.vo;

import lombok.Data;

import java.util.List;

@Data
public class JobExecutionTrendBar {

    private List<String> dateList;

    private List<Integer> allList;

    private List<Integer> successList;

    private List<Integer> failureList;
}
