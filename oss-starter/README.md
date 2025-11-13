OSS Starter：统一封装阿里云 OSS 与 AWS S3，对外提供一致 API

### 依赖

```xml
<dependency>
    <groupId>com.qbit.framework</groupId>
    <artifactId>oss-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 配置

阿里云（单实例示例）：

```yaml
oss:
  provider: ALIYUN
  endpoint: https://oss-cn-hangzhou.aliyuncs.com
  accessKeyId: xxx
  accessKeySecret: xxx
  bucketName: your-bucket
```

AWS（单实例示例）：

```yaml
oss:
  provider: AWS
  region: ap-southeast-1
  accessKeyId: xxx
  accessKeySecret: xxx
  bucketName: your-bucket
  endpoint: https://s3.ap-southeast-1.amazonaws.com
```

多实例示例（同时配置阿里云与 AWS）：

```yaml
oss:
  clients:
    aliyunA:
      provider: ALIYUN
      endpoint: https://oss-cn-hangzhou.aliyuncs.com
      accessKeyId: xxx
      accessKeySecret: xxx
      bucketName: bucket-aliyun
    awsA:
      provider: AWS
      region: ap-southeast-1
      accessKeyId: xxx
      accessKeySecret: xxx
      bucketName: bucket-aws
```

### 使用示例

```java
import com.qbit.framework.business.oss.starter.core.OssTemplate;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.Duration;
import java.net.URL;

@Service
public class FileService {
    private final OssTemplate ossTemplate; // 默认模板（来自 default 或首个配置）
    private final com.qbit.framework.business.oss.starter.core.OssFactory ossFactory; // 多实例工厂

    public FileService(OssTemplate ossTemplate, com.qbit.framework.business.oss.starter.core.OssFactory ossFactory) {
        this.ossTemplate = ossTemplate;
        this.ossFactory = ossFactory;
    }

    public void upload() {
        ossTemplate.putObject("path/file.txt", new File("file.txt"));
    }

    public void uploadToAws() {
        OssTemplate aws = ossFactory.get("awsA");
        aws.putObject("path/file.txt", new File("file.txt"));
    }

    public URL presigned() {
        return ossTemplate.generatePresignedUrl("path/file.txt", Duration.ofHours(1));
    }
}
```
