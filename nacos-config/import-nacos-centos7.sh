#!/bin/bash
# Nacos配置一键导入脚本 - CentOS 7 版本
# Nacos地址: http://192.168.145.128:8848
# 使用方法: ./import-nacos-centos7.sh [命名空间ID]

# 配置
NACOS_ADDR="http://192.168.145.128:8848"
NAMESPACE=${1:-""}  # 空表示默认命名空间
CONFIG_DIR="nacos-config"
GROUP="DEFAULT_GROUP"

echo "==================================="
echo "  Nacos配置一键导入脚本"
echo "==================================="
echo "Nacos地址: $NACOS_ADDR"
echo "命名空间: ${NAMESPACE:-默认}"
echo "配置目录: $CONFIG_DIR"
echo "分组: $GROUP"
echo "==================================="
echo ""

# 检查配置目录是否存在
if [ ! -d "$CONFIG_DIR" ]; then
    # 尝试在父目录查找
    if [ -d "../nacos-config" ]; then
        CONFIG_DIR="../nacos-config"
        echo "📁 在父目录找到配置目录"
    else
        echo "❌ 错误: 配置目录不存在"
        echo "请确保在包含 nacos-config 目录的位置运行此脚本"
        exit 1
    fi
fi

# 检查curl是否安装
if ! command -v curl &> /dev/null; then
    echo "📦 curl 未安装，正在安装..."
    yum install -y curl || {
        echo "❌ 安装 curl 失败，请手动安装"
        exit 1
    }
fi

# 测试Nacos连接
echo "🔍 正在测试Nacos连接..."
if ! curl -s --connect-timeout 5 "$NACOS_ADDR/nacos/v1/ns/operator/metrics" > /dev/null 2>&1; then
    echo "⚠️  警告: 无法连接到Nacos ($NACOS_ADDR)"
    echo "请确保Nacos服务已启动且网络可达"
    read -p "是否继续尝试? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 统计导入数量
SUCCESS=0
FAILED=0
TOTAL=0

# 定义颜色（如果终端支持）
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 遍历所有yml文件
for file in "$CONFIG_DIR"/*.yml; do
    [ -e "$file" ] || continue
    
    TOTAL=$((TOTAL + 1))
    filename=$(basename "$file")
    data_id="${filename%.yml}"
    
    printf "📝 正在导入 [%2d/%2d]: %-30s" "$TOTAL" "$(ls $CONFIG_DIR/*.yml 2>/dev/null | wc -l)" "$data_id"
    
    # 构建curl命令
    if [ -n "$NAMESPACE" ]; then
        response=$(curl -s -o /dev/null -w "%{http_code}" \
            -X POST "$NACOS_ADDR/nacos/v1/cs/configs" \
            -d "tenant=$NAMESPACE" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content@$file" 2>/dev/null)
    else
        response=$(curl -s -o /dev/null -w "%{http_code}" \
            -X POST "$NACOS_ADDR/nacos/v1/cs/configs" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content@$file" 2>/dev/null)
    fi
    
    if [ "$response" = "200" ]; then
        printf " ${GREEN}✓ 成功${NC}\n"
        SUCCESS=$((SUCCESS + 1))
    else
        printf " ${RED}✗ 失败 (HTTP $response)${NC}\n"
        FAILED=$((FAILED + 1))
    fi
done

echo ""
echo "==================================="
echo "        导入完成!"
echo "==================================="
printf "  ${GREEN}成功: %d${NC}\n" "$SUCCESS"
printf "  ${RED}失败: %d${NC}\n" "$FAILED"
echo "  总计: $TOTAL"
echo "==================================="

if [ $FAILED -eq 0 ]; then
    echo ""
    echo "✨ 所有配置导入成功！"
    echo ""
    echo "配置列表:"
    for file in "$CONFIG_DIR"/*.yml; do
        [ -e "$file" ] || continue
        data_id=$(basename "$file" .yml)
        echo "  • $data_id"
    done
    exit 0
else
    echo ""
    echo "⚠️  部分配置导入失败"
    echo ""
    echo "请检查:"
    echo "  1. Nacos服务是否正常运行: $NACOS_ADDR"
    echo "  2. 网络连接是否正常"
    echo "  3. 命名空间ID是否正确 (当前: ${NAMESPACE:-默认})"
    exit 1
fi
