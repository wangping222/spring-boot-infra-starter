package com.qbit.framework.business.xxljob.starter.support;

import com.xxl.job.core.handler.IJobHandler;

import java.lang.reflect.Field;
import java.util.Map;

public class XxlJobTrigger {

    public void trigger(String jobName) {
        IJobHandler handler = get(jobName);
        if (handler == null) {
            throw new IllegalArgumentException("unknown job: " + jobName);
        }
        try {
            try {
                handler.execute();
            } catch (NoSuchMethodError err) {
                handler.getClass().getMethod("execute", String.class).invoke(handler, (Object) null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void trigger(String jobName, String param) {
        IJobHandler handler = get(jobName);
        if (handler == null) {
            throw new IllegalArgumentException("unknown job: " + jobName);
        }
        try {
            try {
                handler.execute();
            } catch (NoSuchMethodError err) {
                handler.getClass().getMethod("execute", String.class).invoke(handler, param);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private IJobHandler get(String name) {
        try {
            Class<?> clazz = Class.forName("com.xxl.job.core.executor.XxlJobExecutor");
            Field f = clazz.getDeclaredField("jobHandlerRepository");
            f.setAccessible(true);
            Map<String, IJobHandler> repo = (Map<String, IJobHandler>) f.get(null);
            return repo.get(name);
        } catch (Exception e) {
            throw new IllegalStateException("fetch xxl-job handler repository failed", e);
        }
    }
}
