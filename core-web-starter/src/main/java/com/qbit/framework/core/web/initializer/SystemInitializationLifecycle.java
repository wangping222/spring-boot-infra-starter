package com.qbit.framework.core.web.initializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.OrderComparator;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author muyue
 */
@Slf4j
public class SystemInitializationLifecycle implements SmartLifecycle, ApplicationContextAware {


    /**
     * 初始化状态标记
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 初始化是否已经执行过（防止极端情况下重复调用）
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * 初始化超时时间（秒）
     */
    private static final int INITIALIZATION_TIMEOUT_SECONDS = 300;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() {
        // SmartLifecycle 保证 start() 在单线程顺序调用
        // 这里的 CAS 更多是“防御式编程”
        if (!initialized.compareAndSet(false, true)) {
            log.warn("System initialization already executed, skipping");
            return;
        }

        log.info("System initialization started");

        try {
            execSystemInitializers(applicationContext);
            running.set(true);
            log.info("System initialization completed successfully");
        } catch (Exception e) {
            log.error("System initialization failed, application startup aborted", e);
            // 直接抛异常 → Spring Boot 启动失败 → 不会暴露 HTTP
            throw e;
        }
    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 关键点：
     * phase 必须早于 WebServerLifecycle
     * WebServer 的 phase = Integer.MAX_VALUE
     */
    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    /**
     * 默认 SmartLifecycle 是自动启动的
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }


    /**
     * 执行系统初始化器
     * 按照优先级顺序执行所有需要初始化的SystemInitializer，采用并行执行方式
     *
     * @param context Spring应用上下文
     */
    private void execSystemInitializers(ApplicationContext context) {
        log.info("Begin executing SystemInitializers");
        StopWatch totalWatch = new StopWatch("system-initialization");
        totalWatch.start();

        try {
            // 获取所有SystemInitializer类型的Bean
            Map<String, SystemInitializer> initializerMap = context.getBeansOfType(SystemInitializer.class);

            if (initializerMap.isEmpty()) {
                log.info("No SystemInitializer beans found");
                return;
            }

            List<SystemInitializer> initializers = new ArrayList<>(initializerMap.values());

            // 根据Order注解排序
            OrderComparator.sort(initializers);

            // 过滤需要初始化的初始化器
            List<SystemInitializer> initializersToRun = initializers.stream()
                    .filter(SystemInitializer::requireInitialize)
                    .toList();

            if (initializersToRun.isEmpty()) {
                log.info("No SystemInitializer requires initialization");
                return;
            }

            log.info("Found {} SystemInitializers to execute: {}",
                    initializersToRun.size(),
                    initializersToRun.stream()
                            .map(i -> i.getClass().getSimpleName())
                            .collect(Collectors.joining(", ")));

            // 并行执行初始化任务
            executeInitializersInParallel(context, initializersToRun);

        } catch (Exception e) {
            log.error("Fatal error during system initialization", e);
            throw new RuntimeException("System initialization failed", e);
        } finally {
            totalWatch.stop();
            log.info("SystemInitializers execution completed in {} seconds",
                    String.format("%.3f", totalWatch.getTotalTimeSeconds()));
        }
    }

    /**
     * 并行执行初始化器
     *
     * @param context Spring应用上下文
     * @param initializers 需要执行的初始化器列表
     */
    private void executeInitializersInParallel(ApplicationContext context,
                                               List<SystemInitializer> initializers) {
        // 使用虚拟线程池（JDK 21+）或ForkJoinPool
        ExecutorService executorService = createExecutorService(initializers.size());

        try {
            List<CompletableFuture<InitializationResult>> futures = initializers.stream()
                    .map(initializer -> CompletableFuture.supplyAsync(
                            () -> executeInitializer(context, initializer),
                            executorService
                    ))
                    .toList();

            // 等待所有任务完成，带超时控制
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            try {
                allOf.get(INITIALIZATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.error("System initialization timeout after {} seconds",
                        INITIALIZATION_TIMEOUT_SECONDS);
                throw new RuntimeException("Initialization timeout", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Initialization interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Initialization execution failed", e.getCause());
            }

            // 收集并处理结果
            processInitializationResults(futures);

        } finally {
            shutdownExecutorService(executorService);
        }
    }

    /**
     * 创建执行器服务
     */
    private ExecutorService createExecutorService(int initializerCount) {
        // JDK 21+ 推荐使用虚拟线程
        // return Executors.newVirtualThreadPerTaskExecutor();

        // JDK 17/11 使用 ForkJoinPool 或固定线程池
        int threadCount = Math.min(initializerCount, Runtime.getRuntime().availableProcessors());
        return Executors.newFixedThreadPool(
                threadCount,
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("system-initializer-" + counter.incrementAndGet());
                        thread.setDaemon(false); // 非守护线程，确保初始化完成
                        return thread;
                    }
                }
        );
    }

    /**
     * 执行单个初始化器
     */
    private InitializationResult executeInitializer(ApplicationContext context,
                                                    SystemInitializer initializer) {
        String initializerName = initializer.getClass().getSimpleName();
        log.info("Starting initialization: {}", initializerName);

        StopWatch watch = new StopWatch(initializerName);
        watch.start();

        try {
            initializer.initialize(context);
            watch.stop();

            log.info("Successfully completed initialization: {} in {} ms",
                    initializerName, watch.getTotalTimeMillis());

            return InitializationResult.success(initializerName, watch.getTotalTimeMillis());

        } catch (Exception e) {
            watch.stop();
            log.error("Failed to initialize: {} after {} ms",
                    initializerName, watch.getTotalTimeMillis(), e);

            return InitializationResult.failure(initializerName, watch.getTotalTimeMillis(), e);
        }
    }

    /**
     * 处理初始化结果
     */
    private void processInitializationResults(List<CompletableFuture<InitializationResult>> futures) {
        List<InitializationResult> results = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        log.error("Unexpected error getting initialization result", e);
                        return InitializationResult.failure("unknown", 0, e);
                    }
                })
                .toList();

        long successCount = results.stream().filter(InitializationResult::isSuccess).count();
        long failureCount = results.size() - successCount;

        if (failureCount > 0) {
            List<String> failedInitializers = results.stream()
                    .filter(r -> !r.isSuccess())
                    .map(InitializationResult::getName)
                    .toList();

            log.error("System initialization completed with {} failures: {}",
                    failureCount, String.join(", ", failedInitializers));

            throw new RuntimeException(
                    String.format("System initialization failed: %d/%d initializers failed",
                            failureCount, results.size())
            );
        }

        // 输出性能统计
        log.info("All {} SystemInitializers executed successfully", successCount);
        results.forEach(result ->
                log.info("  - {}: {} ms", result.getName(), result.getDurationMillis())
        );
    }

    /**
     * 优雅关闭执行器服务
     */
    private void shutdownExecutorService(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("ExecutorService did not terminate gracefully, forcing shutdown");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("ExecutorService shutdown interrupted", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


}