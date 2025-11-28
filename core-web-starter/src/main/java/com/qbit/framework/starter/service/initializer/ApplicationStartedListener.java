package com.qbit.framework.starter.service.initializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 应用启动监听器
 * 应用已经启动, 但未完全初始化完成(没到健康检查阶段)
 * 用于在应用启动时执行系统初始化任务
 */
@Slf4j
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
    /**
     * 初始化状态标记，用于确保初始化只执行一次
     */
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    /**
     * 应用启动事件处理方法
     *
     * @param event Spring应用启动事件
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (!INITIALIZED.get()) {
            INITIALIZED.set(true);
            this.execSystemInitializers(event.getApplicationContext());
        }
    }

    /**
     * 执行系统初始化器
     * 按照优先级顺序执行所有需要初始化的SystemInitializer
     *
     * @param context Spring应用上下文
     */
    private void execSystemInitializers(ApplicationContext context) {
        log.info("begin execute SystemInitializer");
        StopWatch watch = new StopWatch();
        watch.start("system-initialization-task");

        try {
            // 获取所有SystemInitializer类型的Bean
            List<SystemInitializer> initializers = new ArrayList<>(context.getBeansOfType(SystemInitializer.class).values());
            // 根据Order注解排序
            OrderComparator.sort(initializers);
            // 过滤需要初始化的初始化器并执行初始化
            initializers.stream()
                    .filter(SystemInitializer::requireInitialize)
                    .forEach(bean -> bean.initialize(context));
        } catch (Exception exception) {
            log.error("execute SystemInitializer error", exception);
        }
        watch.stop();
        log.info("SystemInitializer execute end, use times = {} seconds", watch.getTotalTimeSeconds());
    }
}
