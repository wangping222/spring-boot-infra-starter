package com.qbit.framework.business.xxljob.starter.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;

public interface XxlJobExecutorCustomizer {
    void customize(XxlJobSpringExecutor executor);
}

