package io.datavines.engine.executor.core.executor;

import io.datavines.common.config.Configurations;
import io.datavines.common.entity.TaskRequest;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class ShellCommandProcess extends BaseCommandProcess {

    /**
     * For Unix-like, using sh
     */
    public static final String SH = "sh";

    public ShellCommandProcess(Consumer<List<String>> logHandler,
                               Logger logger,
                               TaskRequest taskRequest,
                               Configurations configurations){
        super(logHandler, logger, taskRequest, configurations);
    }

    @Override
    protected String buildCommandFilePath() {
        return String.format("%s/%s.command", taskRequest.getExecuteFilePath(), taskRequest.getTaskId());
    }

    @Override
    protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
        logger.info("tenant {},job dir:{}" , taskRequest.getTenantCode(), taskRequest.getExecuteFilePath());

        if(Files.exists(Paths.get(commandFile))){
            Files.delete(Paths.get(commandFile));
        }

        logger.info("create command file:{}",commandFile);

        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/sh\n");
        sb.append("BASEDIR=$(cd `dirname $0`; pwd)\n");
        sb.append("cd $BASEDIR\n");

        if (taskRequest.getEnv() != null) {
            sb.append(taskRequest.getEnv()).append("\n");
        }

        sb.append("\n");
        sb.append(execCommand);
        logger.info("command : {}",sb.toString());

        // write data to file
        FileUtils.writeStringToFile(new File(commandFile), sb.toString(), StandardCharsets.UTF_8);
    }

    @Override
    protected String commandInterpreter() {
        return SH;
    }
}
