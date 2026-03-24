# Nacos 关闭认证配置

## 方法：修改 Nacos 配置文件

在 CentOS 服务器上执行：

```bash
# 进入 Nacos 配置目录
cd /path/to/nacos/conf

# 编辑 application.properties
vi application.properties

# 找到并修改以下配置：
nacos.core.auth.enabled=false
nacos.core.auth.server.identity.key=
nacos.core.auth.server.identity.value=
nacos.core.auth.plugin.nacos.token.secret.key=
```

然后重启 Nacos：
```bash
# 停止 Nacos
sh shutdown.sh

# 启动 Nacos（单机模式）
sh startup.sh -m standalone
```

## 验证

重启后，Nacos 控制台应该可以无认证访问：
http://192.168.145.128:8848/nacos

## 注意

关闭认证仅适用于开发和测试环境！
生产环境应该保持认证开启。
