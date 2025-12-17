package com.qbit.framework.core.web.initializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
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
public class ApplicationStartedListener implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 初始化状态标记，用于确保初始化只执行一次
     */
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    /**
     * 初始化超时时间（秒）
     */
    private static final int INITIALIZATION_TIMEOUT_SECONDS = 300;

    /**
     * 应用启动事件处理方法
     * 使用 ApplicationReadyEvent 确保在应用接收请求前完成初始化
     *
     * @param event Spring应用就绪事件
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (INITIALIZED.compareAndSet(false, true)) {
            try {
                execSystemInitializers(event.getApplicationContext());
            } catch (Exception e) {
                log.error("System initialization failed, application may not function correctly", e);
                // 根据业务需求决定是否需要终止应用启动
                // System.exit(1);
            }
        } else {
            log.warn("System initialization already executed, skipping duplicate execution");
        }
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