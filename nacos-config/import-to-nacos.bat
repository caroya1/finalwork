@echo off
chcp 65001 >nul
echo ===================================
echo Nacos配置导入脚本 (Windows CMD)
echo ===================================

:: 默认配置
set "NACOS_ADDR=http://localhost:8848"
set "GROUP=DEFAULT_GROUP"
set "CONFIG_DIR=nacos-config"

:: 检查参数
if not "%~1"=="" set "NACOS_ADDR=%~1"

cls
echo ===================================
echo Nacos配置导入脚本
echo ===================================
echo Nacos地址: %NACOS_ADDR%
echo 分组: %GROUP%
echo 配置目录: %CONFIG_DIR%
echo ===================================
echo.

:: 检查配置目录是否存在
if not exist "%CONFIG_DIR%" (
    echo ❌ 错误: 配置目录 %CONFIG_DIR% 不存在
    echo 请确保在包含 %CONFIG_DIR% 目录的位置运行此脚本
    pause
    exit /b 1
)

:: 检查curl是否可用
curl --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: curl 未安装或未添加到环境变量
    echo 请安装 curl 或将其添加到 PATH 环境变量
    pause
    exit /b 1
)

:: 统计导入数量
set SUCCESS=0
set FAILED=0
set TOTAL=0

:: 遍历所有yml文件
for %%f in ("%CONFIG_DIR%\*.yml") do (
    set /a TOTAL+=1
    set "FILENAME=%%~nf"
    echo 📝 正在导入: %%~nf
    
    :: 使用curl导入配置
    curl -s -o nul -w "%%{http_code}" -X POST "%NACOS_ADDR%/nacos/v1/cs/configs" ^
        -d "dataId=%%~nf" ^
        -d "group=%GROUP%" ^
        -d "type=yaml" ^
        --data-urlencode "content@%%f" > temp_http_code.txt
    
    set /p HTTP_CODE=<temp_http_code.txt
    
    if "!HTTP_CODE!"=="200" (
        echo ✅ 导入成功: %%~nf
        set /a SUCCESS+=1
    ) else (
        echo ❌ 导入失败: %%~nf (HTTP !HTTP_CODE!)
        set /a FAILED+=1
    )
)

:: 清理临时文件
if exist temp_http_code.txt del temp_http_code.txt

echo.
echo ===================================
echo 导入完成!
echo ===================================
echo 成功: %SUCCESS%
echo 失败: %FAILED%
echo 总计: %TOTAL%
echo ===================================

if %FAILED%==0 (
    echo ✨ 所有配置导入成功！
    pause
    exit /b 0
) else (
    echo ⚠️  部分配置导入失败，请检查 Nacos 服务是否正常运行
    pause
    exit /b 1
)
