Excel Starter：封装 Apache Fesod(FastExcel) 的读写能力

### 依赖

```xml
<dependency>
  <groupId>com.qbit.framework</groupId>
  <artifactId>excel-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 简单使用（自定义表头 + 列表写入）

```java
import com.qbit.framework.business.excel.starter.ExcelClient;
import com.qbit.framework.business.excel.starter.ExcelCellDescriptor;

List<ExcelCellDescriptor<UserKycDTO>> heads = List.of(
  ExcelCellDescriptor.of("法人代表姓名", UserKycDTO::getLegalPerson),
  ExcelCellDescriptor.of("手机号", UserKycDTO::getPhone),
  ExcelCellDescriptor.of("企业名称", UserKycDTO::getCompanyName)
);

List<UserKycDTO> data = userKycService.listAll();
excelClient.write("kyc.xlsx", heads, data, "excel.sheet.kyc", java.util.Locale.CHINA, null);
```

### 国际化与表头本地化

- 配置示例：

```yaml
excel:
  i18n-enabled: true
  default-locale: zh-CN
```

```java
// 标题与工作表名支持消息码，未命中时回退原值
List<ExcelCellDescriptor<Order>> heads = List.of(
  ExcelCellDescriptor.of("excel.header.order.id", Order::getId),
  ExcelCellDescriptor.of("excel.header.order.status", Order::getStatus)
);
excelClient.write("orders.xlsx", heads, orders, "excel.sheet.orders", java.util.Locale.CHINA, null);
```

> 说明：当 `excel.i18n-enabled=true` 时，标题与 `sheetName` 将通过 `MessageSource` 做本地化；传入普通中文标题亦可，效果同上。

### 数据转换（写入前加工）

```java
List<ExcelCellDescriptor<User>> heads = List.of(
  ExcelCellDescriptor.of("用户名称", User::getName),
  ExcelCellDescriptor.of("邮箱", User::getEmail),
  ExcelCellDescriptor.of("状态", User::getStatus)
);

java.util.function.UnaryOperator<User> converter = u -> {
  User v = new User();
  v.setName(u.getName());
  v.setEmail(mask(u.getEmail()));
  v.setStatus(localizeStatus(u.getStatus()));
  return v;
};

excelClient.write("users.xlsx", heads, users, "excel.sheet.users", java.util.Locale.US, converter);
```

### 分页写入（按页拉取）

```java
List<ExcelCellDescriptor<UserKycDTO>> heads = List.of(
  ExcelCellDescriptor.of("法人代表姓名", UserKycDTO.Fields.legalPerson),
  ExcelCellDescriptor.of("手机号", UserKycDTO.Fields.phone),
  ExcelCellDescriptor.of("企业名称", UserKycDTO.Fields.companyName)
);

java.util.function.BiFunction<Integer, Integer, List<UserKycDTO>> fetch =
    (pageNo, pageSize) -> userKycRepository.fetchPage(pageNo, pageSize);

excelClient.write("kyc.xlsx", heads, fetch, "excel.sheet.kyc", java.util.Locale.CHINA, 1000, null);
```
