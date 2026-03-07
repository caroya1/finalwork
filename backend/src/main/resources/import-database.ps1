# ============================================================
# 类大众点评系统 - 数据库导入脚本
# 用途：将所有 SQL 脚本导入到 MySQL 数据库
# ============================================================

# 配置参数（请根据实际情况修改）
$MYSQL_HOST = "localhost"
$MYSQL_PORT = "3306"
$MYSQL_USER = "root"
$MYSQL_PASSWORD = ""  # 如果有密码，请填写
$DATABASE_NAME = "dianping"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  类大众点评系统 - 数据库导入工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取当前脚本所在目录的 resources 文件夹路径
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ResourcesDir = Join-Path $ScriptDir "resources"

# 检查 resources 目录是否存在
if (-Not (Test-Path $ResourcesDir)) {
    Write-Host "错误：找不到 resources 目录：$ResourcesDir" -ForegroundColor Red
    exit 1
}

Write-Host "SQL 文件目录：$ResourcesDir" -ForegroundColor Yellow
Write-Host ""

# 定义 SQL 文件执行顺序（按依赖关系排序）
$SqlFiles = @(
    "schema.sql",              # 创建数据库和所有表结构
    "data-user.sql",           # 用户数据
    "data-merchant.sql",       # 商户数据
    "data-shop.sql",           # 店铺数据
    "data-coupon.sql",         # 优惠券数据
    "data-order.sql",          # 订单数据
    "data-post.sql",           # 帖子/笔记数据
    "data-recommendation.sql"  # 推荐日志数据
)

# 统计信息
$TotalFiles = 0
$SuccessFiles = 0
$FailedFiles = 0

foreach ($SqlFile in $SqlFiles) {
    $FilePath = Join-Path $ResourcesDir $SqlFile
    
    if (Test-Path $FilePath) {
        $TotalFiles++
        Write-Host "正在执行：$SqlFile ..." -ForegroundColor Green
        
        try {
            # 构建 MySQL 命令
            $mysqlCmd = "mysql"
            
            if ($MYSQL_PASSWORD -eq "") {
                & $mysqlCmd -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER $DATABASE_NAME < $FilePath
            } else {
                & $mysqlCmd -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD $DATABASE_NAME < $FilePath
            }
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  ✓ 成功执行：$SqlFile" -ForegroundColor Green
                $SuccessFiles++
            } else {
                Write-Host "  ✗ 执行失败：$SqlFile (错误代码：$LASTEXITCODE)" -ForegroundColor Red
                $FailedFiles++
            }
        }
        catch {
            Write-Host "  ✗ 执行异常：$SqlFile" -ForegroundColor Red
            Write-Host "    错误信息：$($_.Exception.Message)" -ForegroundColor Red
            $FailedFiles++
        }
        
        Write-Host ""
    }
    else {
        Write-Host "警告：文件不存在 - $SqlFile" -ForegroundColor Yellow
    }
}

# 输出总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  导入完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "总文件数：$TotalFiles" -ForegroundColor White
Write-Host "成功：$SuccessFiles" -ForegroundColor Green
Write-Host "失败：$FailedFiles" -ForegroundColor $(if ($FailedFiles -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($FailedFiles -gt 0) {
    Write-Host "请检查失败的 SQL 文件，确保 MySQL 服务正常运行且配置正确。" -ForegroundColor Yellow
    exit 1
}
else {
    Write-Host "所有 SQL 脚本已成功导入到数据库 '$DATABASE_NAME'！" -ForegroundColor Green
    Write-Host ""
    Write-Host "提示：可以使用以下命令验证：" -ForegroundColor Cyan
    Write-Host "  mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p $DATABASE_NAME" -ForegroundColor White
    Write-Host "  USE dianping;" -ForegroundColor White
    Write-Host "  SHOW TABLES;" -ForegroundColor White
    exit 0
}
