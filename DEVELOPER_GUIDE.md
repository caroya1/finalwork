# 开发者指南

## 环境搭建详细步骤

### 1. 安装JDK 17

下载并安装 JDK 17，配置环境变量：
```bash
# 验证安装
java -version
```

### 2. 安装Maven

下载 Maven 3.8+，配置环境变量：
```bash
# 验证安装
mvn -v
```

### 3. 安装Node.js

下载 Node.js 18+，包含npm：
```bash
# 验证安装
node -v
npm -v
```

### 4. 配置开发环境

#### Windows 开发环境

1. **安装IDE**: IntelliJ IDEA 2023.2+
2. **安装插件**: 
   - Lombok
   - Maven Helper
   - Vue.js

3. **配置Maven镜像** (settings.xml):
```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <name>阿里云公共仓库</name>
        <url>https://maven.aliyun.com/repository/public</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
</mirrors>
```

4. **配置npm镜像**:
```bash
npm config set registry https://registry.npmmirror.com
```

### 5. 虚拟机环境配置

本项目使用虚拟机（192.168.145.128）运行基础设施：

#### Docker Compose 配置（参考）

```yaml
version: '3.8'
services:
  nacos:
    image: nacos/nacos-server:v2.2.3
    ports:
      - "8848:8848"
      - "9848:9848"
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=123456
  
  redis:
    image: redis:6.2-alpine
    ports:
      - "6379:6379"
    command: redis-server --requirepass 123456
  
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=123456
      - MYSQL_DATABASE=dianping_user
    volumes:
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
```

## 服务开发指南

### 创建新服务

1. **复制模板**（如 `user-service`）
2. **修改 `pom.xml`**:
   - artifactId
   - name
   - description
3. **修改 `bootstrap.yml`**:
   - spring.application.name
   - 数据库名
   - 端口号
4. **创建启动类**
5. **创建Nacos配置** `nacos-config/{service-name}.yml`

### 添加FeignClient

```java
@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/internal/users/{id}")
    UserDTO getUser(@PathVariable("id") Long id);
}
```

### 数据库实体规范

```java
@Data
@TableName("dp_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String email;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### API接口规范

```java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUser(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }
    
    @PostMapping
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.create(request));
    }
}
```

## 调试技巧

### 1. 使用IDE调试

1. 在代码中设置断点
2. 右键服务模块 → Debug 'Application'
3. 使用Postman或浏览器发送请求

### 2. 查看服务日志

```bash
# 查看实时日志
tail -f logs/recommendation-service.log

# 查看错误日志
grep ERROR logs/*.log
```

### 3. 使用Actuator监控

```bash
# 健康检查
curl http://localhost:8098/actuator/health

# 查看指标
curl http://localhost:8098/actuator/metrics
```

### 4. Nacos服务列表

访问 http://192.168.145.128:8848/nacos 查看：
- 服务列表
- 配置列表
- 服务详情

## 性能优化

### 1. 数据库优化

- 添加索引
- 使用连接池（HikariCP已集成）
- 分页查询

### 2. 缓存策略

```java
@Cacheable(value = "user", key = "#id")
public UserDTO getUser(Long id) {
    return userMapper.selectById(id);
}
```

### 3. 异步处理

```java
@Async("appTaskExecutor")
public CompletableFuture<Void> asyncTask() {
    // 异步执行
}
```

## 测试指南

### 单元测试

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=UserServiceTest

# 跳过测试
mvn install -DskipTests
```

### 集成测试

使用Postman集合测试API接口。

## 部署指南

### 单机部署

```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/recommendation-service-1.0.0.jar
```

### Docker部署

```dockerfile
FROM openjdk:17-jdk-alpine
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t recommendation-service .
docker run -p 8098:8098 recommendation-service
```

## 故障排查

### 服务无法注册到Nacos

1. 检查Nacos是否启动
2. 检查网络连接
3. 检查配置中的Nacos地址

### 数据库连接超时

1. 检查MySQL是否启动
2. 检查连接池配置
3. 检查网络连接

### 内存溢出

调整JVM参数：
```bash
java -Xms512m -Xmx1024m -jar app.jar
```

## 代码提交规范

### 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 示例

```
feat(user): 添加用户关注功能

- 实现关注/取消关注接口
- 添加粉丝列表查询
- 添加关注列表查询

Closes #123
```

### Type说明

- **feat**: 新功能
- **fix**: 修复bug
- **docs**: 文档更新
- **style**: 代码格式（不影响功能）
- **refactor**: 重构
- **perf**: 性能优化
- **test**: 测试相关
- **chore**: 构建/工具相关

## 版本管理

### 版本号规则

遵循语义化版本：MAJOR.MINOR.PATCH

- MAJOR: 不兼容的API修改
- MINOR: 向下兼容的功能添加
- PATCH: 向下兼容的问题修复

### 发布流程

1. 更新版本号（pom.xml）
2. 更新CHANGELOG.md
3. 打标签
4. 发布Release

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## 参考资料

- [Spring Boot官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Alibaba文档](https://sca.aliyun.com/)
- [Vue3官方文档](https://vuejs.org/)
- [MyBatis-Plus文档](https://baomidou.com/)

## 联系我们

如有问题，请提交Issue或联系项目维护者。