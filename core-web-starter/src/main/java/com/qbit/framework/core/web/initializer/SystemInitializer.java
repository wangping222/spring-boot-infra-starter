package com.qbit.framework.core.web.initializer;

import org.springframework.context.ApplicationContext;

/**
 * 系统初始化接口
 * 用于定义系统启动时需要执行的初始化操作
 */
public interface SystemInitializer {

    /**
     * 判断是否需要执行初始化
     *
     * @return true表示需要初始化，false表示不需要初始化
     */
    boolean requireInitialize();

    /**
     * 执行初始化操作
     * 实现类需要在此方法中完成具体的初始化逻辑
     */
    void initialize(ApplicationContext context);

}
