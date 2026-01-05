package com.qbit.framework.core.toolkits.transaction;

import com.qbit.framework.core.toolkits.exception.SystemException;

/**
 * 分布式锁获取异常
 * @author Qbit Framework
 */
public class LockAcquireException extends SystemException {
    public LockAcquireException(String message) {
        super(message);
    }

    public LockAcquireException(String message, Throwable cause) {
        super(message, cause);
    }
}
