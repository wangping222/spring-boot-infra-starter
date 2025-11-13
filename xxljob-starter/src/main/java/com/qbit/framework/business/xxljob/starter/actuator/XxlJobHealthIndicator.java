package com.qbit.framework.business.xxljob.starter.actuator;

import com.qbit.framework.business.xxljob.starter.properties.XxlJobProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.net.HttpURLConnection;
import java.net.URL;

public class XxlJobHealthIndicator implements HealthIndicator {
    private final XxlJobProperties props;

    public XxlJobHealthIndicator(XxlJobProperties props) {
        this.props = props;
    }

    @Override
    public Health health() {
        if (props.getAdminAddresses() == null || props.getAdminAddresses().isBlank()) {
            return Health.unknown().withDetail("reason", "no adminAddresses").build();
        }
        String[] addrs = props.getAdminAddresses().split(",");
        for (String addr : addrs) {
            addr = addr.trim();
            if (addr.isEmpty()) continue;
            try {
                URL url = new URL(addr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                if (code >= 200 && code < 400) {
                    return Health.up().withDetail("admin", addr).build();
                }
            } catch (Exception e) {
                // try next
            }
        }
        return Health.down().withDetail("admin", props.getAdminAddresses()).build();
    }
}

