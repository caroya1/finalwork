# 配置阿里云镜像加速器
# 在Docker Desktop中设置：
# Settings -> Docker Engine -> 添加 registry-mirrors

# 或者在Linux/Mac上创建 /etc/docker/daemon.json：
# {
#   "registry-mirrors": ["https://your-id.mirror.aliyuncs.com"]
# }

# Windows PowerShell 配置命令（以管理员身份运行）：
# $config = @'
# {
#   "registry-mirrors": ["https://your-id.mirror.aliyuncs.com"]
# }
# '@
# $config | Out-File -FilePath "$env:USERPROFILE\.docker\daemon.json" -Encoding utf8

# 注意：需要将 your-id 替换为您的阿里云镜像加速器ID
# 获取地址：https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors
