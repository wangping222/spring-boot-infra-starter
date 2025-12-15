package com.qbit.framework.core.api.model.toolkits.monitor;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 性能监控工具类
 * 用于方法执行耗时监控，支持分段计时、阈值告警、自动日志记录
 * 
 * <p>使用示例:</p>
 * <pre>
 * // 1. 简单监控
 * PerformanceMonitor.execute("业务处理", () -> {
 *     // 业务代码
 * });
 * 
 * // 2. 带返回值的监控
 * String result = PerformanceMonitor.executeWithResult("查询操作", () -> {
 *     return queryData();
 * });
 * 
 * // 3. 链式调用 + 阈值告警
 * PerformanceMonitor.start("授权请求")
 *     .threshold(100)
 *     .execute(() -> doAuth())
 *     .logIfExceedsThreshold();
 * 
 * // 4. 分段计时
 * PerformanceMonitor monitor = PerformanceMonitor.start("复杂业务");
 * monitor.stage("阶段1");
 * // ... 业务代码
 * monitor.stage("阶段2");
 * // ... 业务代码
 * monitor.finish(); // 自动输出各阶段耗时
 * </pre>
 *
 * @author auto-generated
 * @date 2025/12/15
 */
@Slf4j
public class PerformanceMonitor {
    
    /** 监控名称 */
    private final String name;
    
    /** 开始时间(纳秒) */
    private final long startNanos;
    
    /** 结束时间(纳秒) */
    private long endNanos;
    
    /** 阈值(毫秒)，超过此值将告警 */
    private long thresholdMs = -1;
    
    /** 是否已完成 */
    private boolean finished = false;
    
    /** 分段计时记录 */
    private final List<Stage> stages = new ArrayList<>();
    
    /** 上一个阶段的时间戳 */
    private long lastStageNanos;
    
    /** 附加上下文信息 */
    private String context;
    
    /** 自定义告警回调 */
    private Consumer<PerformanceMonitor> alertCallback;

    /**
     * 私有构造函数
     */
    private PerformanceMonitor(String name) {
        this.name = name;
        this.startNanos = System.nanoTime();
        this.lastStageNanos = this.startNanos;
    }

    /**
     * 开始性能监控
     *
     * @param name 监控名称
     * @return PerformanceMonitor实例
     */
    public static PerformanceMonitor start(String name) {
        return new PerformanceMonitor(name);
    }

    /**
     * 设置耗时阈值(毫秒)
     *
     * @param thresholdMs 阈值，超过此值将告警
     * @return this
     */
    public PerformanceMonitor threshold(long thresholdMs) {
        this.thresholdMs = thresholdMs;
        return this;
    }

    /**
     * 设置附加上下文信息
     *
     * @param context 上下文信息（如RRN、订单号等）
     * @return this
     */
    public PerformanceMonitor context(String context) {
        this.context = context;
        return this;
    }

    /**
     * 设置自定义告警回调
     *
     * @param callback 告警回调函数
     * @return this
     */
    public PerformanceMonitor onAlert(Consumer<PerformanceMonitor> callback) {
        this.alertCallback = callback;
        return this;
    }

    /**
     * 记录一个阶段
     *
     * @param stageName 阶段名称
     * @return this
     */
    public PerformanceMonitor stage(String stageName) {
        long now = System.nanoTime();
        long duration = now - lastStageNanos;
        stages.add(new Stage(stageName, duration));
        lastStageNanos = now;
        return this;
    }

    /**
     * 执行无返回值的操作并监控
     *
     * @param runnable 待执行的操作
     * @return this
     */
    public PerformanceMonitor execute(Runnable runnable) {
        try {
            runnable.run();
        } finally {
            finish();
        }
        return this;
    }

    /**
     * 执行有返回值的操作并监控
     *
     * @param supplier 待执行的操作
     * @param <T> 返回值类型
     * @return 操作返回值
     */
    public <T> T executeWithResult(Supplier<T> supplier) {
        try {
            return supplier.get();
        } finally {
            finish();
        }
    }

    /**
     * 完成监控
     *
     * @return 总耗时(毫秒)
     */
    public long finish() {
        if (!finished) {
            this.endNanos = System.nanoTime();
            this.finished = true;
        }
        return getCostMillis();
    }

    /**
     * 获取总耗时(毫秒)
     *
     * @return 耗时(毫秒)
     */
    public long getCostMillis() {
        long endTime = finished ? endNanos : System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(endTime - startNanos);
    }

    /**
     * 获取总耗时(纳秒)
     *
     * @return 耗时(纳秒)
     */
    public long getCostNanos() {
        long endTime = finished ? endNanos : System.nanoTime();
        return endTime - startNanos;
    }

    /**
     * 是否超过阈值
     *
     * @return true=超过阈值
     */
    public boolean exceedsThreshold() {
        return thresholdMs > 0 && getCostMillis() > thresholdMs;
    }

    /**
     * 记录普通日志
     *
     * @return this
     */
    public PerformanceMonitor log() {
        finish();
        String msg = buildLogMessage();
        log.info(msg);
        return this;
    }

    /**
     * 仅当超过阈值时记录日志
     *
     * @return this
     */
    public PerformanceMonitor logIfExceedsThreshold() {
        finish();
        if (exceedsThreshold()) {
            String msg = buildLogMessage();
            log.warn("[性能告警] {}", msg);
            
            // 触发自定义告警回调
            if (alertCallback != null) {
                try {
                    alertCallback.accept(this);
                } catch (Exception e) {
                    log.error("告警回调执行失败", e);
                }
            }
        }
        return this;
    }

    /**
     * 构建日志消息
     */
    private String buildLogMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(name).append("]");
        
        if (context != null && !context.isEmpty()) {
            sb.append(" ").append(context);
        }
        
        sb.append(" 总耗时=").append(getCostMillis()).append("ms");
        
        if (!stages.isEmpty()) {
            sb.append(", 分段: [");
            for (int i = 0; i < stages.size(); i++) {
                Stage stage = stages.get(i);
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(stage.name).append("=").append(stage.getDurationMillis()).append("ms");
            }
            sb.append("]");
        }
        
        if (thresholdMs > 0) {
            sb.append(", 阈值=").append(thresholdMs).append("ms");
            if (exceedsThreshold()) {
                sb.append(" [超阈值!]");
            }
        }
        
        return sb.toString();
    }

    /**
     * 获取监控名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取上下文信息
     */
    public String getContext() {
        return context;
    }

    /**
     * 获取所有阶段信息
     */
    public List<Stage> getStages() {
        return new ArrayList<>(stages);
    }

    // ==================== 静态便捷方法 ====================

    /**
     * 执行并监控（无返回值）
     *
     * @param name 监控名称
     * @param runnable 待执行操作
     */
    public static void execute(String name, Runnable runnable) {
        PerformanceMonitor.start(name).execute(runnable).log();
    }

    /**
     * 执行并监控（带返回值）
     *
     * @param name 监控名称
     * @param supplier 待执行操作
     * @param <T> 返回值类型
     * @return 操作返回值
     */
    public static <T> T executeWithResult(String name, Supplier<T> supplier) {
        PerformanceMonitor monitor = PerformanceMonitor.start(name);
        T result = monitor.executeWithResult(supplier);
        monitor.log();
        return result;
    }

    /**
     * 执行并监控，带阈值告警（无返回值）
     *
     * @param name 监控名称
     * @param thresholdMs 阈值(毫秒)
     * @param runnable 待执行操作
     */
    public static void executeWithThreshold(String name, long thresholdMs, Runnable runnable) {
        PerformanceMonitor.start(name)
                .threshold(thresholdMs)
                .execute(runnable)
                .logIfExceedsThreshold();
    }

    /**
     * 执行并监控，带阈值告警（带返回值）
     *
     * @param name 监控名称
     * @param thresholdMs 阈值(毫秒)
     * @param supplier 待执行操作
     * @param <T> 返回值类型
     * @return 操作返回值
     */
    public static <T> T executeWithThreshold(String name, long thresholdMs, Supplier<T> supplier) {
        PerformanceMonitor monitor = PerformanceMonitor.start(name).threshold(thresholdMs);
        T result = monitor.executeWithResult(supplier);
        monitor.logIfExceedsThreshold();
        return result;
    }

    // ==================== 内部类 ====================

    /**
     * 阶段信息
     */
    public static class Stage {
        private final String name;
        private final long durationNanos;

        public Stage(String name, long durationNanos) {
            this.name = name;
            this.durationNanos = durationNanos;
        }

        public String getName() {
            return name;
        }

        public long getDurationNanos() {
            return durationNanos;
        }

        public long getDurationMillis() {
            return TimeUnit.NANOSECONDS.toMillis(durationNanos);
        }

        @Override
        public String toString() {
            return name + "=" + getDurationMillis() + "ms";
        }
    }
}
