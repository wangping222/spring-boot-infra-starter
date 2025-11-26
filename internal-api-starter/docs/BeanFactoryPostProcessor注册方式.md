# Spring Boot Starter 中注册 BeanFactoryPostProcessor 的方式

## 背景

在 Spring Boot 3.3.3 中，有多种方式可以在自定义 Starter 中注册 `BeanFactoryPostProcessor`。

## 方式 1：通过 @AutoConfiguration 类中的 @Bean 方法（✅ 推荐）

这是最常用、最简单、最符合 Spring Boot 规范的方式。

### 实现步骤

#### 1. 创建 BeanFactoryPostProcessor 实现类

```java
package com.qbit.framework.starter.merchant.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class FeignClientUrlPostProcessor implements BeanFactoryPostProcessor {
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 实现逻辑
    }
}
```

#### 2. 在 @AutoConfiguration 类中声明为 @Bean

```java
package com.qbit.framework.starter.merchant.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

@AutoConfiguration
public class FeignAutoConfiguration {
    
    /**
     * 注意：方法必须是 static，因为 BeanFactoryPostProcessor 
     * 需要在配置类实例化之前就被注册
     */
    @Bean
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "base-url")
    public static BeanFactoryPostProcessor feignClientsUrlPostProcessor() {
        return new FeignClientUrlPostProcessor();
    }
}
```

#### 3. 在 AutoConfiguration.imports 中注册配置类

文件路径：`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
com.qbit.framework.starter.merchant.config.FeignAutoConfiguration
```

### 关键点

- ✅ **方法必须是 `static`**：因为 `BeanFactoryPostProcessor` 在容器启动早期执行，需要在配置类实例化之前注册
- ✅ **可以使用条件注解**：如 `@ConditionalOnProperty`、`@ConditionalOnClass` 等
- ✅ **自动发现**：Spring Boot 会自动扫描并注册
- ✅ **符合规范**：这是 Spring Boot 官方推荐的方式

---

## 方式 2：直接在 @AutoConfiguration 类上使用 @Component

### 实现步骤

#### 1. 为 BeanFactoryPostProcessor 添加 @Component 注解

```java
package com.qbit.framework.starter.merchant.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "framework.feign.api", name = "base-url")
public class FeignClientUrlPostProcessor implements BeanFactoryPostProcessor {
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 实现逻辑
    }
}
```

#### 2. 在 AutoConfiguration.imports 中注册

```
com.qbit.framework.starter.merchant.config.FeignClientUrlPostProcessor
```

### 优缺点

- ✅ 代码简洁，不需要额外的 @Bean 方法
- ❌ 不够灵活，难以进行复杂的条件控制
- ❌ 不符合 Spring Boot Starter 的最佳实践（配置类和组件类应该分离）

---

## 方式 3：使用 @Import 导入

### 实现步骤

#### 1. 在 @AutoConfiguration 类中使用 @Import

```java
package com.qbit.framework.starter.merchant.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(FeignClientUrlPostProcessor.class)
public class FeignAutoConfiguration {
    // 其他配置
}
```

#### 2. BeanFactoryPostProcessor 类

```java
package com.qbit.framework.starter.merchant.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class FeignClientUrlPostProcessor implements BeanFactoryPostProcessor {
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 实现逻辑
    }
}
```

### 优缺点

- ✅ 明确的依赖关系
- ❌ 难以使用条件注解（需要配合 `@Conditional` 自定义条件）
- ❌ 不如方式 1 灵活

---

## 方式 4：实现 ImportBeanDefinitionRegistrar（高级用法）

### 实现步骤

#### 1. 创建 ImportBeanDefinitionRegistrar 实现类

```java
package com.qbit.framework.starter.merchant.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class FeignClientUrlPostProcessorRegistrar implements ImportBeanDefinitionRegistrar {
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(FeignClientUrlPostProcessor.class);
        registry.registerBeanDefinition("feignClientUrlPostProcessor", beanDefinition);
    }
}
```

#### 2. 在 @AutoConfiguration 类中导入

```java
@AutoConfiguration
@Import(FeignClientUrlPostProcessorRegistrar.class)
public class FeignAutoConfiguration {
    // 其他配置
}
```

### 优缺点

- ✅ 最灵活，可以动态注册 Bean
- ✅ 可以根据条件动态决定是否注册
- ❌ 代码复杂，不易维护
- ❌ 仅在需要动态注册时使用

---

## 当前项目使用的方式

**方式 1：通过 @AutoConfiguration 类中的 @Bean 方法**

### 文件结构

```
internal-api-starter/
├── src/main/java/
│   └── com/qbit/framework/starter/merchant/config/
│       ├── FeignAutoConfiguration.java          # 自动配置类
│       └── FeignClientUrlPostProcessor.java     # BeanFactoryPostProcessor 实现
└── src/main/resources/
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### 注册流程

1. **Spring Boot 启动** → 读取 `AutoConfiguration.imports`
2. **加载 `FeignAutoConfiguration`** → 扫描 `@Bean` 方法
3. **发现 `feignClientsUrlPostProcessor()`** → 检查 `@ConditionalOnProperty` 条件
4. **条件满足** → 创建 `FeignClientUrlPostProcessor` 实例
5. **注册到容器** → 作为 `BeanFactoryPostProcessor` 在容器启动早期执行

### 为什么使用 static 方法？

```java
public static BeanFactoryPostProcessor feignClientsUrlPostProcessor() {
    return new FeignClientUrlPostProcessor();
}
```

**原因**：
- `BeanFactoryPostProcessor` 在 Spring 容器启动的**最早期**执行
- 此时配置类（`FeignAutoConfiguration`）还没有实例化
- 使用 `static` 方法可以在不创建配置类实例的情况下调用
- 避免循环依赖和初始化顺序问题

---

## 总结

| 方式 | 推荐度 | 适用场景 |
|------|--------|----------|
| 方式 1：@Bean 方法 | ⭐⭐⭐⭐⭐ | **所有场景**，最推荐 |
| 方式 2：@Component | ⭐⭐⭐ | 简单场景，不需要复杂条件控制 |
| 方式 3：@Import | ⭐⭐ | 需要明确依赖关系时 |
| 方式 4：ImportBeanDefinitionRegistrar | ⭐ | 需要动态注册时 |

**当前项目使用方式 1，这是最佳实践！** ✅
