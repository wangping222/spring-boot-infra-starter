package com.qbit.framework.business.trace.starter;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TraceUtils {
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";

    private TraceUtils() {}

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static void set(String key, String value) {
        MDC.put(key, value);
    }

    public static String get(String key) {
        return MDC.get(key);
    }

    public static void setTrace(String traceId, String spanId) {
        if (traceId != null) MDC.put(TRACE_ID, traceId);
        if (spanId != null) MDC.put(SPAN_ID, spanId);
    }

    public static Map<String, String> current() {
        Map<String, String> m = new HashMap<>();
        String t = MDC.get(TRACE_ID);
        String s = MDC.get(SPAN_ID);
        if (t != null) m.put(TRACE_ID, t);
        if (s != null) m.put(SPAN_ID, s);
        return m;
    }
}

