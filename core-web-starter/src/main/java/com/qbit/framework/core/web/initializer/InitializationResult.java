package com.qbit.framework.core.web.initializer;

import lombok.Getter;

/**
 * 初始化结果记录
 */
@Getter
class InitializationResult {
    private final String name;
    private final long durationMillis;
    private final boolean success;
    private final Exception exception;

    private InitializationResult(String name, long durationMillis,
                                 boolean success, Exception exception) {
        this.name = name;
        this.durationMillis = durationMillis;
        this.success = success;
        this.exception = exception;
    }

    static InitializationResult success(String name, long durationMillis) {
        return new InitializationResult(name, durationMillis, true, null);
    }

    static InitializationResult failure(String name, long durationMillis, Exception exception) {
        return new InitializationResult(name, durationMillis, false, exception);
    }

}
