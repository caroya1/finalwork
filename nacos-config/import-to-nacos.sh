#!/bin/bash
# Nacos配置一键导入脚本
# 使用方法: ./import-to-nacos.sh [nacos地址] [命名空间ID]

# 默认配置
NACOS_ADDR=${1:-"http://localhost:8848"}
NAMESPACE=${2:-""}  # 空表示默认命名空间
CONFIG_DIR="nacos-config"
GROUP="DEFAULT_GROUP"

echo "==================================="
echo "Nacos配置导入脚本"
echo "==================================="
echo "Nacos地址: $NACOS_ADDR"
echo "命名空间: ${NAMESPACE:-默认}"
echo "配置目录: $CONFIG_DIR"
echo "分组: $GROUP"
echo "==================================="
echo ""

# 检查配置目录是否存在
if [ ! -d "$CONFIG_DIR" ]; then
    echo "❌ 错误: 配置目录 $CONFIG_DIR 不存在"
    echo "请确保在包含 $CONFIG_DIR 目录的位置运行此脚本"
    exit 1
fi

# 检查curl是否安装
if ! command -v curl &> /dev/null; then
    echo "❌ 错误: curl 未安装，请先安装 curl"
    exit 1
fi

# 统计导入数量
SUCCESS=0
FAILED=0
SKIPPED=0

# 遍历所有yml文件
for file in "$CONFIG_DIR"/*.yml; do
    # 检查文件是否存在（处理目录为空的情况）
    [ -e "$file" ] || continue
    
    # 获取文件名（不含扩展名）作为Data ID
    filename=$(basename "$file")
    data_id="${filename%.yml}"
    
    echo "📝 正在导入: $data_id"
    
    # 读取文件内容并转义
    content=$(cat "$file" | sed 's/"/\\"/g' | sed ':a;N;$!ba;s/\n/\\n/g')
    
    # 构建curl命令
    if [ -n "$NAMESPACE" ]; then
        # 指定命名空间
        response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$NACOS_ADDR/nacos/v1/cs/configs" \
            -d "tenant=$NAMESPACE" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content@$file")
    else
        # 默认命名空间
        response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$NACOS_ADDR/nacos/v1/cs/configs" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content@$file")
    fi
    
    if [ "$response" = "200" ]; then
        echo "✅ 导入成功: $data_id"
        ((SUCCESS++))
    else
        echo "❌ 导入失败: $data_id (HTTP $response)"
        ((FAILED++))
    fi
done

echo ""
echo "==================================="
echo "导入完成!"
echo "==================================="
echo "成功: $SUCCESS"
echo "失败: $FAILED"
echo "总计: $((SUCCESS + FAILED))"
echo "==================================="

if [ $FAILED -eq 0 ]; then
    echo "✨ 所有配置导入成功！"
    exit 0
else
    echo "⚠️  部分配置导入失败，请检查 Nacos 服务是否正常运行"
    exit 1
fi
