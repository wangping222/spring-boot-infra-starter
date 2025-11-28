package com.qbit.framework.starter.service.message;


import org.apache.commons.lang3.ObjectUtils;

public class SimpleMessageFormatter implements MessageFormatter {
    static final String DELIM_STR = "{}";

    public SimpleMessageFormatter() {
    }

    @Override
    public String format(String pattern, Object... args) {
        if (ObjectUtils.isEmpty(args)) {
            return pattern;
        } else {
            StringBuilder result = new StringBuilder(pattern);

            for (int i = 0; i < args.length; ++i) {
                int index = result.indexOf("{}", i);
                if (index >= 0) {
                    result.replace(index, index + "{}".length(), String.valueOf(args[i]));
                }
            }

            return result.toString();
        }
    }
}
