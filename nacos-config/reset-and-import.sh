#!/bin/bash
# 清除并重新导入Nacos配置

NACOS_ADDR=${1:-"http://192.168.145.128:8848"}
NAMESPACE=${2:-""}
GROUP="DEFAULT_GROUP"
CONFIG_DIR="nacos-config"

# 用户名密码
USERNAME="nacos"
PASSWORD="nacos"

echo "==================================="
echo "清除并重新导入Nacos配置"
echo "==================================="
echo "Nacos地址: $NACOS_ADDR"
echo "命名空间: ${NAMESPACE:-默认}"
echo "==================================="
echo ""

# 1. 获取所有配置并删除
echo "🗑️  正在清除现有配置..."

# 获取配置列表
if [ -n "$NAMESPACE" ]; then
    CONFIG_LIST=$(curl -s -u "$USERNAME:$PASSWORD" "$NACOS_ADDR/nacos/v1/cs/configs?dataId=&group=$GROUP&tenant=$NAMESPACE&pageNo=1&pageSize=100" | grep -o '"dataId":"[^"]*"' | cut -d'"' -f4)
else
    CONFIG_LIST=$(curl -s -u "$USERNAME:$PASSWORD" "$NACOS_ADDR/nacos/v1/cs/configs?dataId=&group=$GROUP&pageNo=1&pageSize=100" | grep -o '"dataId":"[^"]*"' | cut -d'"' -f4)
fi

# 删除每个配置
for data_id in $CONFIG_LIST; do
    echo "  删除: $data_id"
    if [ -n "$NAMESPACE" ]; then
        curl -s -u "$USERNAME:$PASSWORD" -X DELETE "$NACOS_ADDR/nacos/v1/cs/configs?dataId=$data_id&group=$GROUP&tenant=$NAMESPACE" > /dev/null
    else
        curl -s -u "$USERNAME:$PASSWORD" -X DELETE "$NACOS_ADDR/nacos/v1/cs/configs?dataId=$data_id&group=$GROUP" > /dev/null
    fi
done

echo "✅ 现有配置已清除"
echo ""

# 2. 重新导入配置
echo "📥 正在重新导入配置..."
SUCCESS=0
FAILED=0

for file in "$CONFIG_DIR"/*.yml; do
    [ -e "$file" ] || continue
    
    filename=$(basename "$file")
    data_id="${filename%.yml}"
    
    echo "📝 导入: $data_id"
    
    if [ -n "$NAMESPACE" ]; then
        response=$(curl -s -o /dev/null -w "%{http_code}" -u "$USERNAME:$PASSWORD" -X POST "$NACOS_ADDR/nacos/v1/cs/configs" \
            -d "tenant=$NAMESPACE" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content@$file")
    else
        response=$(curl -s -o /dev/null -w "%{http_code}" -u "$USERNAME:$PASSWORD" -X POST "$NACOS_ADDR/nacos/v1/cs/configs" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content@$file")
    fi
    
    if [ "$response" = "200" ]; then
        echo "  ✅ 成功"
        ((SUCCESS++))
    else
        echo "  ❌ 失败 (HTTP $response)"
        ((FAILED++))
    fi
done

echo ""
echo "==================================="
echo "操作完成!"
echo "==================================="
echo "成功: $SUCCESS"
echo "失败: $FAILED"
echo "==================================="

if [ $FAILED -eq 0 ]; then
    echo "✨ 所有配置导入成功！"
else
    echo "⚠️  部分配置导入失败"
fi
