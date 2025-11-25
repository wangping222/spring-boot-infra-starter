package com.qbit.framework.starter.merchant.logging;

import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;

@Slf4j
public class SingleLineHttpLogger implements HttpLoggingInterceptor.Logger {

    private static final ThreadLocal<StringBuilder> BUFFER = new ThreadLocal<>();

    @Override
    public void log(String message) {
        StringBuilder buf = BUFFER.get();
        if (buf == null) {
            buf = new StringBuilder();
            BUFFER.set(buf);
        }
        if (!buf.isEmpty()) {
            buf.append('\n');
        }
        buf.append(message);

        if (message.startsWith("<-- END") || message.startsWith("--> END")) {
            log.info(buf.toString());
            BUFFER.remove();
        }
    }
}
