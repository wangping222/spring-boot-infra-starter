Excel Starter：封装 Apache Fesod(FastExcel) 的读写能力

### 依赖

```xml
<dependency>
  <groupId>com.qbit.framework</groupId>
  <artifactId>excel-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 简单使用

```java
import com.qbit.framework.business.excel.starter.ExcelClient;
import cn.idev.excel.annotation.ExcelProperty;

class Row {
  @ExcelProperty("字符串") String s;
}

// 写（显式 clazz）
excelClient.write("demo.xlsx", Row.class, List.of(new Row()), "Sheet1");

// 写（省略 clazz，自动从首元素推断）
excelClient.write("demo.xlsx", List.of(new Row()), "Sheet1");

// 读
List<Row> rows = excelClient.read("demo.xlsx", Row.class);
```

### 国际化与表头本地化

- 配置示例：

```yaml
excel:
  i18n-enabled: true
  default-locale: zh-CN
```

- 使用国际化的 sheet 名称与表头：

```java
// 使用消息码作为 sheet 名称，未命中时回退原值
excelClient.write("orders.xlsx", Order.class, data, "excel.sheet.orders");

// 动态表头国际化：按字段注解或默认消息码生成本地化标题
excelClient.writeWithI18nHeaders("orders.xlsx", Order.class, data, "excel.sheet.orders", java.util.Locale.CHINA);

class Order {
  // 优先使用注解中的 value[0] 作为消息码
  @ExcelProperty("excel.header.Order.id") Long id;
  // 未设置注解时，使用默认消息码 excel.header.Order.status
  @ExcelProperty("excel.header.Order.status") Integer status;
}
```

### 数据转换（写入前加工）

```java
// 在写入前对数据做加工（脱敏、格式化、枚举值本地化等）
excelClient.write(
    "users.xlsx",
    users,
    "excel.sheet.users",
    u -> {
      User v = new User();
      v.setName(u.getName());
      v.setEmail(mask(u.getEmail()));
      v.setStatus(u.getStatus());
      return v;
    }
);

// 带语言环境的重载
excelClient.write(
    "users.xlsx",
    users,
    "excel.sheet.users",
    java.util.Locale.US,
    u -> localize(u)
);
```
