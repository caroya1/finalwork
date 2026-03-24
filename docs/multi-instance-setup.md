# 微服务多实例集群配置指南

## IDEA中启动多个实例的方法

### 方法一：修改启动配置（推荐）

1. 点击顶部运行配置下拉框（显示当前服务名的地方）
2. 选择 `Edit Configurations...`
3. 找到要启动多实例的服务（如PostServiceApplication）
4. 点击右上角 `Modify options` → `Add VM options`
5. 在 `VM options` 中添加：
   ```
   -Dserver.port=8098
   ```
6. 点击 `Apply`，然后复制这个配置，修改端口为8099、8100等
7. 分别启动这些配置

### 方法二：使用不同的配置文件

为每个实例创建不同的application.yml：

**application-8098.yml:**
```yaml
server:
  port: 8098
```

**application-8099.yml:**
```yaml
server:
  port: 8099
```

启动时指定profile：
- 实例1: `--spring.profiles.active=8098`
- 实例2: `--spring.profiles.active=8099`

### 方法三：Environment Variables

在IDEA启动配置中设置环境变量：
```
SERVER_PORT=8098
```

## 验证集群

启动多个实例后，在Nacos控制台可以看到：
- 同一个服务名下有多个健康实例
- 每个实例有不同的IP:端口

## 负载均衡

Gateway会自动从Nacos获取所有实例，进行负载均衡。
