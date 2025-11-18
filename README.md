# spring-boot-infra-starter

基于 Spring Boot 的基础设施 Starter 集合，统一封装常用中间件与工程能力，帮助业务工程以依赖即用的方式快速集成。

## 环境与版本

- JDK: 17
- Spring Boot: 3.3.3
- 版本：`0.0.1-SNAPSHOT`（根 `pom.xml` 的 `revision` 属性）

## 模块总览

- `openapi-starter`：对接 qbit OpenAPI v3，统一鉴权与令牌管理
- `merchant-api-starter`：通过内部接口对接 qbit 业务接口
- `oss-starter`：封装阿里云 OSS 与 AWS S3，提供一致 API 与多实例支持
- `trace-starter`：提供 MDC 封装、Trace ID/Span ID 工具与 Web 自动传播
- `xxljob-starter`：对 XXL-Job 的 Spring Boot 自动装配与增强
- `excel-starter`：基于 FastExcel 的 Excel 读写能力与国际化支持
- `service-starter`：Web/Jackson/校验/Caffeine 等通用基础能力

## 快速开始

1. 在项目根目录安装到本地仓库：

   ```bash
   mvn clean install
   ```

2. 在业务工程按需引入模块依赖（示例以 `oss-starter` 为例）：

   ```xml
   <dependency>
     <groupId>com.qbit.framework</groupId>
     <artifactId>oss-starter</artifactId>
     <version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```

3. 配置与使用请参考各模块 README：

   - `openapi-starter/README.md`
   - `merchant-api-starter/README.md`
   - `oss-starter/README.md`
   - `trace-starter/README.md`
   - `xxljob-starter/README.md`
   - `excel-starter/README.md`

## 统一版本管理

所有依赖版本通过根 `pom.xml` 的 `dependencyManagement` 统一控制，业务工程只需声明坐标即可。Java 版本与常用库（如 `fastjson2`、`lombok`、`springdoc`、`redisson`、`easyexcel`、`itextpdf`、`mybatis-flex`、`okhttp` 等）均已在父工程中集中定义。

## 目录结构

```text
spring-boot-infra-starter/
├─ excel-starter/
├─ merchant-api-starter/
├─ openapi-starter/
├─ oss-starter/
├─ service-starter/
├─ trace-starter/
├─ xxljob-starter/
└─ pom.xml
```

## 说明

- 所有模块均遵循 Spring Boot Starter 约定，提供自动装配并尽量零配置开箱可用。
- 部分模块存在前置依赖（如 `openapi-starter` 需 Redis）；具体以模块文档为准。