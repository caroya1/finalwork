#!/bin/bash
# Nacos配置一键导入脚本 - 自动版
# Nacos地址: http://192.168.145.128:8848
# 使用方法: 将此脚本放在项目根目录，直接运行 ./import-nacos.sh

# 配置
NACOS_ADDR="http://192.168.145.128:8848"
NACOS_USERNAME="nacos"
NACOS_PASSWORD="nacos"
NAMESPACE=${1:-""}  # 空表示默认命名空间
GROUP="DEFAULT_GROUP"

# 查找配置目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -d "$SCRIPT_DIR/nacos-config" ]; then
    CONFIG_DIR="$SCRIPT_DIR/nacos-config"
elif [ -d "$SCRIPT_DIR/../nacos-config" ]; then
    CONFIG_DIR="$SCRIPT_DIR/../nacos-config"
else
    echo "❌ 错误: 找不到 nacos-config 目录"
    echo "请将此脚本放在项目根目录或与 nacos-config 同级的目录"
    exit 1
fi

echo "==================================="
echo "  Nacos配置一键导入脚本"
echo "==================================="
echo "Nacos地址: $NACOS_ADDR"
echo "配置目录: $CONFIG_DIR"
echo "命名空间: ${NAMESPACE:-默认}"
echo "分组: $GROUP"
echo "==================================="
echo ""

# 检查curl
if ! command -v curl &> /dev/null; then
    echo "❌ 错误: curl 未安装"
    echo "请安装 curl: yum install -y curl"
    exit 1
fi

# 测试Nacos连接
echo "🔍 正在连接Nacos..."
if ! curl -s --connect-timeout 5 "$NACOS_ADDR/v1/ns/operator/metrics" > /dev/null 2>&1; then
    echo "❌ 错误: 无法连接到Nacos ($NACOS_ADDR)"
    echo ""
    echo "请检查:"
    echo "  1. Nacos服务是否已启动"
    echo "  2. 网络连接是否正常 (ping 192.168.145.128)"
    echo "  3. 防火墙是否放行了8848端口"
    exit 1
fi

echo "✅ Nacos连接成功"
echo ""

# 导入配置
SUCCESS=0
FAILED=0
TOTAL=0

for file in "$CONFIG_DIR"/*.yml; do
    [ -e "$file" ] || continue
    TOTAL=$((TOTAL + 1))
    filename=$(basename "$file")
    data_id="${filename%.yml}"
    
    echo -n "📝 导入 [$TOTAL]: $data_id ... "
    
    # 读取文件内容
    content=$(cat "$file" 2>/dev/null)
    if [ $? -ne 0 ]; then
        echo "✗ (无法读取文件)"
        FAILED=$((FAILED + 1))
        continue
    fi
    
    # 构建URL
    url="$NACOS_ADDR/v1/cs/configs?username=$NACOS_USERNAME&password=$NACOS_PASSWORD"
    
    # 发送请求
    if [ -n "$NAMESPACE" ]; then
        response=$(curl -s -X POST "$url" \
            -d "tenant=$NAMESPACE" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content=$content" 2>&1)
    else
        response=$(curl -s -X POST "$url" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content=$content" 2>&1)
    fi
    
    if [ "$response" = "true" ]; then
        echo "✓"
        SUCCESS=$((SUCCESS + 1))
    else
        echo "✗ ($response)"
        FAILED=$((FAILED + 1))
    fi
done

echo ""
echo "==================================="
echo "导入完成! 成功: $SUCCESS, 失败: $FAILED"
echo "==================================="

[ $FAILED -eq 0 ] && exit 0 || exit 1
