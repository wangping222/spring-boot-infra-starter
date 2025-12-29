package com.qbit.framework.core.toolkits.tracing.context;

/**
 * 基于 MDC 的作用域化 Trace 上下文工具
 * 用法：try (MdcTraceContext ignored = MdcTraceContext.put("X-TraceId", traceId)) { ... }
 * 特性：
 * - 进入作用域时保存当前线程的 MDC 快照，退出时自动恢复或清空
 * - 同一线程内嵌套使用安全；不同线程不自动传播 MDC
 * 注意：
 * - put 与 close 必须在同一线程调用
 * - 异步任务需自行传播 MDC（例如使用 TaskDecorator）
 *
 * @author Qbit Framework
 */

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;
import java.util.concurrent.Callable;

public final class MdcTraceContext implements AutoCloseable {

    private final Map<String, String> previous;

    private MdcTraceContext(Map<String, String> previous) {
        this.previous = previous;
    }

    public static MdcTraceContext put(String key, String value) {
        Map<String, String> prev = MDC.getCopyOfContextMap();
        if (value == null || value.isEmpty()) {
            MDC.remove(key);
        } else {
            MDC.put(key, value);
        }
        return new MdcTraceContext(prev);
    }

    public static MdcTraceContext setAll(Map<String, String> values) {
        Map<String, String> prev = MDC.getCopyOfContextMap();
        if (values == null || values.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(values);
        }
        return new MdcTraceContext(prev);
    }

    public static Runnable wrap(Runnable task) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> prev = MDC.getCopyOfContextMap();
            if (context == null || context.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                task.run();
            } finally {
                if (prev == null || prev.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(prev);
                }
            }
        };
    }

    public static <T> Callable<T> wrap(Callable<T> task) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> prev = MDC.getCopyOfContextMap();
            if (context == null || context.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                return task.call();
            } finally {
                if (prev == null || prev.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(prev);
                }
            }
        };
    }

    public static TaskDecorator decorator() {
        return MdcTraceContext::wrap;
    }

    @Override
    public void close() {
        if (previous == null || previous.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(previous);
        }
    }
}
