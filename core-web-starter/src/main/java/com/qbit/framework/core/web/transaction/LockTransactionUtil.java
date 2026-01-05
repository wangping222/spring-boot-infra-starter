package com.qbit.framework.core.web.transaction;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁 + 事务执行工具类（基于 Redisson）
 * 确保事务提交后再释放锁，防止并发问题
 * <p>
 * 使用示例：
 * <pre>{@code
 * RLock lock = redissonClient.getLock("inventory:" + productId);
 * LockTransactionUtil.execute(lock, transactionTemplate, () -> {
 *     inventoryService.deduct(productId, quantity);
 *     return null;
 * });
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
     * 在分布式锁和事务保护下执行业务逻辑
     *
     * @param lock                Redisson 分布式锁
     * @param transactionTemplate 事务模板
     * @param action              业务逻辑
     * @param <T>                 返回值类型
     * @return 业务逻辑执行结果
     */
    public static <T> T execute(RLock lock, TransactionTemplate transactionTemplate, Supplier<T> action) {
        return execute(lock, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, transactionTemplate, action);
    }

    /**
     * 在分布式锁和事务保护下执行业务逻辑（带超时配置）
     *
     * @param lock                Redisson 分布式锁
     * @param waitTime            等待获取锁的最大时间
     * @param leaseTime           锁持有时间，-1 表示使用看门狗自动续期
     * @param timeUnit            时间单位
     * @param transactionTemplate 事务模板
     * @param action              业务逻辑
     * @param <T>                 返回值类型
     * @return 业务逻辑执行结果
     */
    public static <T> T execute(RLock lock, long waitTime, long leaseTime, TimeUnit timeUnit,
                                TransactionTemplate transactionTemplate, Supplier<T> action) {
        // 1. 获取分布式锁
        boolean acquired;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquireException("Lock acquisition interrupted", e);
        }

        if (!acquired) {
            throw new LockAcquireException("Failed to acquire lock: " + lock.getName());
        }

        log.debug("Lock acquired: {}", lock.getName());

        try {
            // 2. 在事务中执行，注册同步器确保事务完成后释放锁
            return transactionTemplate.execute(status -> {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        releaseLock(lock);
                    }
                });
                return action.get();
            });
        } catch (Exception e) {
            // 事务未启动时需手动释放锁
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                releaseLock(lock);
            }
            throw e;
        }
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
     * <p>
     * 注意：调用此方法时必须已经在事务中（如 @Transactional 方法内）
     */
    public static <T> T executeInTransaction(RLock lock, Supplier<T> action) {
        return executeInTransaction(lock, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, action);
    }

    /**
     * 在现有事务中执行带锁的操作（带超时配置）
     */
    public static <T> T executeInTransaction(RLock lock, long waitTime, long leaseTime, 
                                              TimeUnit timeUnit, Supplier<T> action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction. Use execute() instead.");
        }

        boolean acquired;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquireException("Lock acquisition interrupted", e);
        }

        if (!acquired) {
            throw new LockAcquireException("Failed to acquire lock: " + lock.getName());
        }

        log.debug("Lock acquired in transaction: {}", lock.getName());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                releaseLock(lock);
            }
        });

        return action.get();
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

}
