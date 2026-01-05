package com.qbit.framework.core.web.transaction;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 分布式锁 + 事务执行工具类（基于 Redisson）
 * 确保事务提交后再释放锁，防止并发问题
 * <p>
 * 使用示例：
 * <pre>{@code
 * RLock lock = redissonClient.getLock("inventory:" + productId);
 * 
 * // 基础用法
 * LockTransactionUtil.execute(lock, transactionTemplate, () -> {
 *     inventoryService.deduct(productId, quantity);
 *     return orderId;
 * });
 * 
 * // 事务提交后发送 webhook
 * Order order = LockTransactionUtil.builder(lock, transactionTemplate)
 *     .action(() -> orderService.create(dto))
 *     .afterCommit(result -> webhookService.send(result))
 *     .afterRollback(() -> log.warn("Order creation failed"))
 *     .execute();
 * }</pre>
 *
 * @author Qbit Framework
 */
@Slf4j
public class LockTransactionUtil {

    /** 默认锁等待时间（秒） */
    private static final long DEFAULT_WAIT_TIME = 10;
    /** 默认锁持有时间（秒），-1 表示使用看门狗自动续期 */
    private static final long DEFAULT_LEASE_TIME = -1;

    private LockTransactionUtil() {
    }

    /**
     * 创建执行构建器，支持链式配置回调
     */
    public static <T> ExecutionBuilder<T> builder(RLock lock, TransactionTemplate transactionTemplate) {
        return new ExecutionBuilder<>(lock, transactionTemplate);
    }

    /**
     * 在分布式锁和事务保护下执行业务逻辑
     */
    public static <T> T execute(RLock lock, TransactionTemplate transactionTemplate, Supplier<T> action) {
        return new ExecutionBuilder<T>(lock, transactionTemplate)
                .action(action)
                .execute();
    }

    /**
     * 在分布式锁和事务保护下执行业务逻辑（带超时配置）
     */
    public static <T> T execute(RLock lock, long waitTime, long leaseTime, TimeUnit timeUnit,
                                TransactionTemplate transactionTemplate, Supplier<T> action) {
        return new ExecutionBuilder<T>(lock, transactionTemplate)
                .waitTime(waitTime)
                .leaseTime(leaseTime)
                .timeUnit(timeUnit)
                .action(action)
                .execute();
    }

    /**
     * 在分布式锁和事务保护下执行无返回值的业务逻辑
     */
    public static void execute(RLock lock, TransactionTemplate transactionTemplate, Runnable action) {
        execute(lock, transactionTemplate, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 在现有事务中执行带锁的操作
     */
    public static <T> T executeInTransaction(RLock lock, Supplier<T> action) {
        return executeInTransaction(lock, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, action, null, null);
    }

    /**
     * 在现有事务中执行带锁的操作（带回调）
     */
    public static <T> T executeInTransaction(RLock lock, Supplier<T> action,
                                              Consumer<T> afterCommit, Runnable afterRollback) {
        return executeInTransaction(lock, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS,
                action, afterCommit, afterRollback);
    }

    /**
     * 在现有事务中执行带锁的操作（带超时配置和回调）
     */
    public static <T> T executeInTransaction(RLock lock, long waitTime, long leaseTime,
                                              TimeUnit timeUnit, Supplier<T> action,
                                              Consumer<T> afterCommit, Runnable afterRollback) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction. Use execute() instead.");
        }

        acquireLock(lock, waitTime, leaseTime, timeUnit);

        // 先注册同步器，确保无论 action 是否成功，锁都能被释放
        ResultHolder<T> holder = new ResultHolder<>();
        registerSynchronization(lock, holder, afterCommit, afterRollback);

        // 执行业务逻辑
        T result = action.get();
        holder.result = result;

        return result;
    }

    /**
     * 获取分布式锁
     */
    private static void acquireLock(RLock lock, long waitTime, long leaseTime, TimeUnit timeUnit) {
        boolean acquired;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquireException("Lock acquisition interrupted: " + lock.getName(), e);
        }

        if (!acquired) {
            throw new LockAcquireException("Failed to acquire lock: " + lock.getName());
        }

        log.debug("Lock acquired: {}", lock.getName());
    }

    /**
     * 注册事务同步器，处理回调和锁释放
     */
    private static <T> void registerSynchronization(RLock lock, ResultHolder<T> holder,
                                                     Consumer<T> afterCommit, Runnable afterRollback) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                try {
                    if (status == STATUS_COMMITTED && afterCommit != null) {
                        log.debug("Transaction committed, executing afterCommit callback");
                        afterCommit.accept(holder.result);
                    } else if (status == STATUS_ROLLED_BACK && afterRollback != null) {
                        log.debug("Transaction rolled back, executing afterRollback callback");
                        afterRollback.run();
                    }
                } catch (Exception e) {
                    log.error("Callback execution failed", e);
                } finally {
                    releaseLock(lock);
                }
            }
        });
    }

    private static void releaseLock(RLock lock) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released: {}", lock.getName());
            }
        } catch (Exception e) {
            log.error("Failed to release lock: {}", lock.getName(), e);
        }
    }

    /**
     * 结果持有器，用于在注册同步器后设置结果
     */
    private static class ResultHolder<T> {
        T result;
    }

    /**
     * 执行构建器，支持链式配置
     */
    public static class ExecutionBuilder<T> {
        private final RLock lock;
        private final TransactionTemplate transactionTemplate;
        private long waitTime = DEFAULT_WAIT_TIME;
        private long leaseTime = DEFAULT_LEASE_TIME;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private Supplier<T> action;
        private Consumer<T> afterCommit;
        private Runnable afterRollback;

        ExecutionBuilder(RLock lock, TransactionTemplate transactionTemplate) {
            this.lock = lock;
            this.transactionTemplate = transactionTemplate;
        }

        public ExecutionBuilder<T> waitTime(long waitTime) {
            this.waitTime = waitTime;
            return this;
        }

        public ExecutionBuilder<T> leaseTime(long leaseTime) {
            this.leaseTime = leaseTime;
            return this;
        }

        public ExecutionBuilder<T> timeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public ExecutionBuilder<T> action(Supplier<T> action) {
            this.action = action;
            return this;
        }

        /**
         * 事务提交后执行（可获取业务逻辑返回值）
         */
        public ExecutionBuilder<T> afterCommit(Consumer<T> afterCommit) {
            this.afterCommit = afterCommit;
            return this;
        }

        /**
         * 事务提交后执行（无参数版本）
         */
        public ExecutionBuilder<T> afterCommit(Runnable afterCommit) {
            this.afterCommit = result -> afterCommit.run();
            return this;
        }

        /**
         * 事务回滚后执行
         */
        public ExecutionBuilder<T> afterRollback(Runnable afterRollback) {
            this.afterRollback = afterRollback;
            return this;
        }

        public T execute() {
            if (action == null) {
                throw new IllegalStateException("Action must be set");
            }

            acquireLock(lock, waitTime, leaseTime, timeUnit);

            try {
                return transactionTemplate.execute(status -> {
                    // 先注册同步器，确保锁释放
                    ResultHolder<T> holder = new ResultHolder<>();
                    registerSynchronization(lock, holder, afterCommit, afterRollback);

                    // 执行业务逻辑
                    T result = action.get();
                    holder.result = result;

                    return result;
                });
            } catch (Exception e) {
                // 如果事务还未开始（同步器未激活），需手动释放锁
                if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                    releaseLock(lock);
                }
                throw e;
            }
        }
    }

    public static class LockAcquireException extends RuntimeException {
        public LockAcquireException(String message) {
            super(message);
        }

        public LockAcquireException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
