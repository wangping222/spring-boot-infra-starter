package com.qbit.framework.core.toolkits.monitor;

/**
 * PerformanceMonitor 使用示例
 * 
 * @author auto-generated
 * @date 2025/12/15
 */
public class PerformanceMonitorExample {

    /**
     * 示例1: 简单监控 - 无返回值
     */
    public void example1_SimpleMonitor() {
        PerformanceMonitor.execute("数据库查询", () -> {
            // 执行数据库查询
            simulateWork(100);
        });
        // 输出: [数据库查询] 总耗时=100ms
    }

    /**
     * 示例2: 带返回值的监控
     */
    public String example2_MonitorWithResult() {
        return PerformanceMonitor.executeWithResult("API调用", () -> {
            simulateWork(50);
            return "API响应数据";
        });
        // 输出: [API调用] 总耗时=50ms
    }

    /**
     * 示例3: 链式调用 + 阈值告警
     */
    public void example3_ThresholdAlert() {
        PerformanceMonitor.start("业务处理")
                .threshold(100)  // 设置阈值100ms
                .context("RRN=123456789")  // 添加上下文信息
                .execute(() -> {
                    simulateWork(150);  // 模拟耗时150ms
                })
                .logIfExceedsThreshold();  // 仅超过阈值时记录
        // 输出: [性能告警] [业务处理] RRN=123456789 总耗时=150ms, 阈值=100ms [超阈值!]
    }

    /**
     * 示例4: 分段计时
     */
    public void example4_StageMonitor() {
        PerformanceMonitor monitor = PerformanceMonitor.start("授权请求处理");
        
        // 阶段1
        simulateWork(30);
        monitor.stage("业务入队");
        
        // 阶段2
        simulateWork(50);
        monitor.stage("业务处理");
        
        // 阶段3
        simulateWork(20);
        monitor.stage("写缓存区");
        
        monitor.finish();
        monitor.log();
        // 输出: [授权请求处理] 总耗时=100ms, 分段: [业务入队=30ms, 业务处理=50ms, 写缓存区=20ms]
    }

    /**
     * 示例5: 自定义告警回调
     */
    public void example5_CustomAlert() {
        PerformanceMonitor.start("支付处理")
                .threshold(200)
                .context("订单号=ORD123456")
                .onAlert(monitor -> {
                    // 自定义告警逻辑：发送机器人通知、记录到数据库等
                    String alertMsg = String.format(
                        "性能告警: %s, 耗时: %dms, 上下文: %s",
                        monitor.getName(),
                        monitor.getCostMillis(),
                        monitor.getContext()
                    );
                    sendRobotNotice(alertMsg);
                })
                .execute(() -> {
                    simulateWork(300);
                })
                .logIfExceedsThreshold();
    }

    /**
     * 示例6: 复杂场景 - 嵌套监控
     */
    public void example6_NestedMonitor() {
        PerformanceMonitor outerMonitor = PerformanceMonitor.start("整体流程");
        
        // 子任务1
        PerformanceMonitor.executeWithThreshold("子任务1", 50, () -> {
            simulateWork(30);
        });
        outerMonitor.stage("子任务1完成");
        
        // 子任务2
        PerformanceMonitor.executeWithThreshold("子任务2", 100, () -> {
            simulateWork(80);
        });
        outerMonitor.stage("子任务2完成");
        
        outerMonitor.finish();
        outerMonitor.log();
    }

    /**
     * 示例7: 在Authorization8583MessageHandler中的实际应用
     */
    public void example7_RealWorldUsage() {
        String rrn = "123456789012";
        
        // 方式1: 完整监控整个处理流程
        PerformanceMonitor monitor = PerformanceMonitor.start("授权请求处理")
                .threshold(10000)  // 10秒阈值
                .context("RRN=" + rrn)
                .onAlert(m -> {
                    // 发送超时告警通知
                    sendTimeoutNotice(m.getCostMillis(), rrn);
                });
        
        // 业务处理前
        monitor.stage("业务入队");
        
        // 业务处理
        simulateWork(100);
        monitor.stage("业务处理");
        
        // 写缓存区
        simulateWork(20);
        monitor.stage("写缓存区");
        
        // 网络传输
        simulateWork(30);
        monitor.stage("网络传输");
        
        monitor.finish();
        monitor.logIfExceedsThreshold();
        
        // 也可以手动获取各项指标
        long totalCost = monitor.getCostMillis();
        for (PerformanceMonitor.Stage stage : monitor.getStages()) {
            System.out.println(stage.getName() + ": " + stage.getDurationMillis() + "ms");
        }
    }

    // ==================== 辅助方法 ====================

    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendRobotNotice(String message) {
        // 模拟发送机器人通知
        System.out.println("发送机器人通知: " + message);
    }

    private void sendTimeoutNotice(long costMs, String rrn) {
        // 模拟发送超时通知
        System.out.println("超时告警 - RRN: " + rrn + ", 耗时: " + costMs + "ms");
    }
}
