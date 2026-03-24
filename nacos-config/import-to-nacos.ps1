# Nacos配置一键导入脚本 (PowerShell)
# 使用方法: .\import-to-nacos.ps1 [nacos地址] [命名空间ID]

# 默认配置
param(
    [string]$NacosAddr = "http://localhost:8848",
    [string]$Namespace = ""  # 空表示默认命名空间
)

$ConfigDir = "nacos-config"
$Group = "DEFAULT_GROUP"

Write-Host "===================================" -ForegroundColor Cyan
Write-Host "Nacos配置导入脚本" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "Nacos地址: $NacosAddr"
Write-Host "命名空间: $(if ($Namespace) { $Namespace } else { '默认' })"
Write-Host "配置目录: $ConfigDir"
Write-Host "分组: $Group"
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""

# 检查配置目录是否存在
if (-not (Test-Path $ConfigDir)) {
    Write-Host "❌ 错误: 配置目录 $ConfigDir 不存在" -ForegroundColor Red
    Write-Host "请确保在包含 $ConfigDir 目录的位置运行此脚本" -ForegroundColor Yellow
    exit 1
}

# 检查PowerShell版本（需要支持Invoke-RestMethod）
if ($PSVersionTable.PSVersion.Major -lt 5) {
    Write-Host "❌ 错误: 需要 PowerShell 5.0 或更高版本" -ForegroundColor Red
    exit 1
}

# 统计导入数量
$Success = 0
$Failed = 0

# 获取所有yml文件
$ymlFiles = Get-ChildItem -Path $ConfigDir -Filter "*.yml"

if ($ymlFiles.Count -eq 0) {
    Write-Host "⚠️  警告: 在 $ConfigDir 目录中未找到 .yml 文件" -ForegroundColor Yellow
    exit 1
}

foreach ($file in $ymlFiles) {
    # 获取文件名（不含扩展名）作为Data ID
    $dataId = $file.BaseName
    $filePath = $file.FullName
    
    Write-Host "📝 正在导入: $dataId" -NoNewline
    
    try {
        # 读取文件内容
        $content = Get-Content -Path $filePath -Raw -Encoding UTF8
        
        # 构建请求参数
        $body = @{
            dataId = $dataId
            group = $Group
            type = "yaml"
            content = $content
        }
        
        # 如果指定了命名空间，添加tenant参数
        if ($Namespace) {
            $body['tenant'] = $Namespace
        }
        
        # 发送请求
        $uri = "$NacosAddr/nacos/v1/cs/configs"
        $response = Invoke-RestMethod -Uri $uri -Method Post -Body $body -ContentType "application/x-www-form-urlencoded" -ErrorAction Stop
        
        if ($response -eq $true -or $response -eq "true") {
            Write-Host "`r✅ 导入成功: $dataId" -ForegroundColor Green
            $Success++
        } else {
            Write-Host "`r❌ 导入失败: $dataId (响应: $response)" -ForegroundColor Red
            $Failed++
        }
    }
    catch {
        Write-Host "`r❌ 导入失败: $dataId (错误: $($_.Exception.Message))" -ForegroundColor Red
        $Failed++
    }
}

Write-Host ""
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "导入完成!" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "成功: $Success" -ForegroundColor Green
Write-Host "失败: $Failed" -ForegroundColor Red
Write-Host "总计: $($Success + $Failed)"
Write-Host "===================================" -ForegroundColor Cyan

if ($Failed -eq 0) {
    Write-Host "✨ 所有配置导入成功！" -ForegroundColor Green
    exit 0
} else {
    Write-Host "⚠️  部分配置导入失败，请检查：" -ForegroundColor Yellow
    Write-Host "   1. Nacos 服务是否正常运行 ($NacosAddr)" -ForegroundColor Yellow
    Write-Host "   2. 网络连接是否正常" -ForegroundColor Yellow
    Write-Host "   3. 命名空间ID是否正确" -ForegroundColor Yellow
    exit 1
}
