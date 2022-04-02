package io.datavines.server.executor.cache;

import java.util.concurrent.ConcurrentHashMap;

public class TaskExecutionCache {

    private final ConcurrentHashMap<Long, TaskExecutionContext> cache = new ConcurrentHashMap<>();

    private TaskExecutionCache(){}

    private static class Singleton{
        static TaskExecutionCache instance = new TaskExecutionCache();
    }

    public static TaskExecutionCache getInstance(){
        return Singleton.instance;
    }

    public TaskExecutionContext getById(Long taskId){
        return cache.get(taskId);
    }

    public void cache(TaskExecutionContext taskExecutionContext){
        cache.put(taskExecutionContext.getTaskRequest().getTaskId(), taskExecutionContext);
    }

    public void remove(Long taskId){
        cache.remove(taskId);
    }
}
