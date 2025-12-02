package com.qbit.framework.starter.service.context;

import com.qbit.framework.starter.service.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SpringApplicationContextUtils implements ApplicationContextAware {

    private static final AtomicReference<ApplicationContext> CONTEXT_HOLDER = new AtomicReference<>();

    public static String getProperty(String key) {
        return requireApplicationContext().getEnvironment().getProperty(key, String.class);
    }

    public static void publishEvent(ApplicationEvent event) {
        requireApplicationContext().publishEvent(event);
    }

    static ApplicationContext requireApplicationContext() {
        ApplicationContext result = CONTEXT_HOLDER.get();
        AssertUtils.notNull(result, "spring application context not init");
        return result;
    }

    /**
     * 根据类型获取bean实例
     */
    public static <T> T getBean(Class<T> requiredType) {
        return requireApplicationContext().getBean(requiredType);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        AssertUtils.notNull(applicationContext, "argument applicationContext must not null");
        CONTEXT_HOLDER.set(applicationContext);
    }
}
