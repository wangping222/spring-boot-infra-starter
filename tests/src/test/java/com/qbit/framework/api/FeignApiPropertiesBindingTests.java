package com.qbit.framework.api;

import com.qbit.framework.starter.merchant.properties.FeignApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FeignApiPropertiesBindingTests.Config.class)
@TestPropertySource(locations = "classpath:application.yml")
class FeignApiPropertiesBindingTests {

    @Configuration
    @EnableConfigurationProperties(FeignApiProperties.class)
    static class Config { }

    @Autowired
    FeignApiProperties props;

    @Test
    void bindsFromApplicationYaml() {
        assertNotNull(props.getBaseUrl());
        assertNotNull(props.getSecret());
    }



    @Test
    void testExpression(){
        StandardEvaluationContext context = new StandardEvaluationContext();

        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression("3*5");
        Object value = expression.getValue(context);
        System.out.println(value);
    }
}

