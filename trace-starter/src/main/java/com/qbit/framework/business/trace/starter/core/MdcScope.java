package com.qbit.framework.business.trace.starter.core;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MdcScope implements AutoCloseable {
    private final Map<String, String> previous = new HashMap<>();
    private final Set<String> keys = new HashSet<>();

    public static MdcScope with(String key, String value) {
        MdcScope s = new MdcScope();
        s.put(key, value);
        return s;
    }

    public static MdcScope with(Map<String, String> kv) {
        MdcScope s = new MdcScope();
        if (kv != null) {
            for (Map.Entry<String, String> e : kv.entrySet()) {
                s.put(e.getKey(), e.getValue());
            }
        }
        return s;
    }

    public MdcScope and(String key, String value) {
        put(key, value);
        return this;
    }

    private void put(String key, String value) {
        previous.put(key, MDC.get(key));
        MDC.put(key, value);
        keys.add(key);
    }

    @Override
    public void close() {
        for (String k : keys) {
            String prev = previous.get(k);
            if (prev == null) {
                MDC.remove(k);
            } else {
                MDC.put(k, prev);
            }
        }
    }
}

