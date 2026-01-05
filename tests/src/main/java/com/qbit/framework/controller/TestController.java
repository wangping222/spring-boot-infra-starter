package com.qbit.framework.controller;

import com.qbit.framework.core.toolkits.exception.code.DefaultExceptionCode;
import com.qbit.framework.core.toolkits.exception.factory.CustomerExceptionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        // 测试手机号脱敏
        log.info("用户手机号: 13812345678");

        // 测试身份证脱敏
        log.info("身份证号: 110101199001011234");

        // 测试邮箱脱敏
        log.info("邮箱地址: zhangsan@qq.com");

        // 测试密码脱敏
        log.info("用户登录信息: username=zhangsan, password=123456");

        // 测试银行卡脱敏
        log.info("银行卡号: 6222021234567891234");

        return "日志已输出,请查看控制台";
    }

    @PostMapping("post-log")
    public String testPostLog(@RequestBody UserDTO userDTO) {

        return "hello";
    }

    @PostMapping("exception")
    public String testException() {
        throw CustomerExceptionFactory.of(DefaultExceptionCode.BAD_REQUEST);
    }


    @PostMapping("long-time")
    public String longTimeWork() {
        try {
            // 告警阈值200ms
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }
}