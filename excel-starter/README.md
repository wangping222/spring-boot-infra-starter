# Excel Starter

基于 EasyExcel 封装的 Spring Boot Starter，提供便捷的 Excel 导入导出功能。

## 特性

- **简单易用**: 只需注入 `ExcelClient` 即可使用。
- **灵活导出**: 支持基于注解 (`@ExcelProperty`) 和基于动态描述符 (`ExcelCellDescriptor`) 的导出方式。
- **Web 集成**: 专为 Web 环境优化，自动处理响应头和流。
- **自动配置**: 开箱即用。

## 依赖

在你的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.qbit.framework</groupId>
    <artifactId>excel-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

## 配置

可选配置：

```yaml
excel:
  default-sheet-name: Sheet1
```

## 使用指南

### 1. 注入 ExcelClient

```java
@Autowired
private ExcelClient excelClient;
```

### 2. 基于注解导出 (推荐)

定义你的数据模型：

```java
@Data
public class UserExportVO {
    @ExcelProperty("用户ID")
    private Long id;

    @ExcelProperty("用户名")
    private String username;
    
    @ExcelProperty("注册时间")
    private Date createTime;
}
```

在 Controller 中导出：

```java
@GetMapping("/export")
public void export(HttpServletResponse response) throws IOException {
    List<UserExportVO> data = userService.listExportData();
    excelClient.export(response, "用户列表", UserExportVO.class, data);
}
```

### 3. 动态表头导出 (Fluent API)
无需定义 VO 类，直接指定表头和字段提取器：

```java
@GetMapping("/export-dynamic")
public void exportDynamic(HttpServletResponse response) throws IOException {
    List<User> users = userService.listAll();
    
    excelClient.export(users)
        .fileName("用户动态列表")
        .sheetName("Sheet1")
        .column("ID", User::getId, id -> "USER_" + id)
        .column("姓名", User::getName)
        .column("邮箱", User::getEmail)
        .writeTo(response);
}
```

### 4. 导入 Excel

```java
@PostMapping("/import")
public void importExcel(@RequestParam("file") MultipartFile file) throws IOException {
    excelClient.read(file.getInputStream(), UserImportVO.class, new PageReadListener<UserImportVO>(dataList -> {
        userService.saveBatch(dataList);
    }));
}
```
