package com.qbit.framework.core.api.model.toolkits.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工具类
 * 提供常用的线程池创建和管理功能
 */
@Slf4j
public final class ThreadPoolUtils {

    private ThreadPoolUtils() {
        throw new AssertionError();
    }

    /**
     * 创建固定大小的线程池
     *
     * @param poolSize  线程池大小
     * @param threadName 线程名称前缀
     * @return ExecutorService
     */
    public static ExecutorService newFixedThreadPool(int poolSize, String threadName) {
        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(threadName)
        );
    }

    /**
     * 创建固定大小的线程池（带队列容量限制）
     *
     * @param poolSize      线程池大小
     * @param queueCapacity 队列容量
     * @param threadName    线程名称前缀
     * @return ExecutorService
     */
    public static ExecutorService newFixedThreadPool(int poolSize, int queueCapacity, String threadName) {
        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new NamedThreadFactory(threadName),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建缓存线程池
     *
     * @param threadName 线程名称前缀
     * @return ExecutorService
     */
    public static ExecutorService newCachedThreadPool(String threadName) {
        return new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new NamedThreadFactory(threadName)
        );
    }

    /**
     * 创建单线程池
     *
     * @param threadName 线程名称
     * @return ExecutorService
     */
    public static ExecutorService newSingleThreadExecutor(String threadName) {
        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(threadName)
        );
    }

    /**
     * 创建可调度的线程池
     *
     * @param corePoolSize 核心线程数
     * @param threadName   线程名称前缀
     * @return ScheduledExecutorService
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String threadName) {
        return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory(threadName));
    }

    /**
     * 创建自定义线程池
     *
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   空闲线程存活时间
     * @param timeUnit        时间单位
     * @param queueCapacity   队列容量
     * @param threadName      线程名称前缀
     * @return ExecutorService
     */
    public static ExecutorService newCustomThreadPool(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueCapacity,
            String threadName) {
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                timeUnit,
                new LinkedBlockingQueue<>(queueCapacity),
                new NamedThreadFactory(threadName),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 优雅关闭线程池
     *
     * @param executor 线程池
     */
    public static void shutdownGracefully(ExecutorService executor) {
        shutdownGracefully(executor, 60, TimeUnit.SECONDS);
    }

    /**
     * 优雅关闭线程池
     *
     * @param executor 线程池
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     */
    public static void shutdownGracefully(ExecutorService executor, long timeout, TimeUnit timeUnit) {
        if (executor == null || executor.isShutdown()) {
            return;
        }

        try {
            executor.shutdown();
            if (!executor.awaitTermination(timeout, timeUnit)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(timeout, timeUnit)) {
                    log.warn("线程池未能正常关闭");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 自定义线程工厂，支持线程命名
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final boolean daemon;

        public NamedThreadFactory(String namePrefix) {
            this(namePrefix, false);
        }

        public NamedThreadFactory(String namePrefix, boolean daemon) {
            this.namePrefix = namePrefix + "-";
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(daemon);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
