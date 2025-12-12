package com.qbit.framework.core.api.model.toolkits.message;


import lombok.Getter;

/**
 * @author Qbit Framework
 */
@Getter
public final class MessagePlaceholder {

    /**
     * 消息占位符表达式
     */
    private final String pattern;

    /**
     * 占位符参数
     */
    private final Object[] args;

    private MessagePlaceholder(String pattern, Object[] args) {
        this.pattern = pattern;
        this.args = args;
    }

    public static MessagePlaceholder of(String pattern, Object... args) {
        return new MessagePlaceholder(pattern, args == null ? new Object[0] : args);
    }
}
