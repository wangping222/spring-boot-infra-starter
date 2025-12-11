package com.qbit.framework.core.api.model.toolkits.message;


import java.text.MessageFormat;

public interface MessageFormatter {
    static MessageFormatter none() {
        return (pattern, args) -> pattern;
    }

    static MessageFormatter java() {
        return MessageFormat::format;
    }

    static MessageFormatter simple() {
        return new SimpleMessageFormatter();
    }

    String format(String pattern, Object... args);
}

