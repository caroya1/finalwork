#!/bin/bash
# Nacos配置清除并重新导入

NACOS_ADDR=${1:-"http://192.168.145.128:8848"}
NAMESPACE=${2:-""}
GROUP="DEFAULT_GROUP"
CONFIG_DIR="nacos-config"

USERNAME="nacos"
PASSWORD="nacos"

echo "==================================="
echo "Nacos配置清除并重新导入"
echo "==================================="
echo "Nacos地址: $NACOS_ADDR"
echo "命名空间: ${NAMESPACE:-默认}"
echo "==================================="
echo ""

# 获取Nacos accessToken
echo "🔑 正在获取Nacos访问令牌..."
token_response=$(curl -s -X POST "$NACOS_ADDR/v1/auth/users/login" \
    -d "username=$USERNAME" \
    -d "password=$PASSWORD")
access_token=$(echo "$token_response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$access_token" ]; then
    echo "❌ 获取访问令牌失败"
    echo "响应: $token_response"
    exit 1
fi

echo "✅ 获取访问令牌成功"
echo ""

# 构建认证参数
AUTH_PARAM="accessToken=$access_token"

# 清除现有配置
echo "🗑️  正在清除现有配置..."

# 获取配置列表并删除
if [ -n "$NAMESPACE" ]; then
    configs=$(curl -s "$NACOS_ADDR/v1/cs/configs?$AUTH_PARAM&dataId=&group=$GROUP&tenant=$NAMESPACE&pageNo=1&pageSize=100" | grep -o '"dataId":"[^"]*"' | cut -d'"' -f4)
else
    configs=$(curl -s "$NACOS_ADDR/v1/cs/configs?$AUTH_PARAM&dataId=&group=$GROUP&pageNo=1&pageSize=100" | grep -o '"dataId":"[^"]*"' | cut -d'"' -f4)
fi

for data_id in $configs; do
    echo "  删除: $data_id"
    if [ -n "$NAMESPACE" ]; then
        curl -s -X DELETE "$NACOS_ADDR/v1/cs/configs?$AUTH_PARAM&dataId=$data_id&group=$GROUP&tenant=$NAMESPACE" > /dev/null 2>&1
    else
        curl -s -X DELETE "$NACOS_ADDR/v1/cs/configs?$AUTH_PARAM&dataId=$data_id&group=$GROUP" > /dev/null 2>&1
    fi
done

echo "✅ 现有配置已清除"
echo ""

# 重新导入配置
echo "📥 正在重新导入配置..."
SUCCESS=0
FAILED=0

for file in "$CONFIG_DIR"/*.yml; do
    [ -e "$file" ] || continue
    
    filename=$(basename "$file")
    data_id="${filename%.yml}"
    
    echo "📝 导入: $data_id"
    
    if [ -n "$NAMESPACE" ]; then
        response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$NACOS_ADDR/v1/cs/configs" \
            -d "$AUTH_PARAM" \
            -d "tenant=$NAMESPACE" \
            -d "dataId=$data_id" \
            -d "group=$GROUP" \
            -d "type=yaml" \
            --data-urlencode "content@$file")
    else
        response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$NACOS_ADDR/v1/cs/configs" \
            -d "$AUTH_PARAM" \
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
