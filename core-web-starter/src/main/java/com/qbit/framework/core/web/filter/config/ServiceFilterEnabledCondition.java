package com.qbit.framework.core.web.filter.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Qbit Framework
 */
public class ServiceFilterEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        Object nameObj = metadata.getAnnotationAttributes(ConditionalOnServiceFilter.class.getName()).get("value");
        String name = String.valueOf(nameObj);

        boolean globalEnabled = env.getProperty("service.filters.enabled", Boolean.class, true);
        if (!globalEnabled) {
            return false;
        }

        Set<String> include = parseSet(env.getProperty("service.filters.include", String.class));
        if (!include.isEmpty()) {
            return include.contains(name);
        }

        Set<String> exclude = parseSet(env.getProperty("service.filters.exclude", String.class));
        if (!exclude.isEmpty()) {
            return !exclude.contains(name);
        }

        Boolean specific = env.getProperty("service.filters." + name + ".enabled", Boolean.class);
        return specific == null || specific;
    }

    private Set<String> parseSet(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptySet();
        }
        String[] arr = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        return new HashSet<>(Arrays.asList(arr));
    }
}