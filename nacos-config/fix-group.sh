#!/bin/bash
# 修复Nacos配置Group问题

NACOS_ADDR=${1:-"http://192.168.145.128:8848"}
USERNAME="nacos"
PASSWORD="nacos"

echo "==================================="
echo "修复Nacos配置Group问题"
echo "==================================="
echo "Nacos地址: $NACOS_ADDR"
echo "==================================="
echo ""

# 获取Token
echo "🔑 获取访问令牌..."
token_response=$(curl -s -X POST "$NACOS_ADDR/v1/auth/users/login" \
    -d "username=$USERNAME" \
    -d "password=$PASSWORD")
access_token=$(echo "$token_response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$access_token" ]; then
    echo "❌ 获取访问令牌失败"
    exit 1
fi

echo "✅ 获取令牌成功"
echo ""

# 1. 删除错误Group的配置
echo "🗑️  删除错误Group(nacos-config)的配置..."
wrong_configs=$(curl -s "$NACOS_ADDR/v1/cs/configs?accessToken=$access_token&dataId=&group=nacos-config&pageNo=1&pageSize=100" | grep -o '"dataId":"[^"]*"' | cut -d'"' -f4)

for data_id in $wrong_configs; do
    echo "  删除: $data_id (Group: nacos-config)"
    curl -s -X DELETE "$NACOS_ADDR/v1/cs/configs?accessToken=$access_token&dataId=$data_id&group=nacos-config" > /dev/null 2>&1
done
echo "✅ 错误配置已删除"
echo ""

# 2. 重新导入到正确的Group
echo "📥 重新导入到DEFAULT_GROUP..."
SUCCESS=0
FAILED=0

for file in nacos-config/*.yml; do
    [ -e "$file" ] || continue
    
    filename=$(basename "$file")
    data_id="${filename%.yml}"
    
    echo "📝 导入: $data_id (Group: DEFAULT_GROUP)"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$NACOS_ADDR/v1/cs/configs" \
        -d "accessToken=$access_token" \
        -d "dataId=$data_id" \
        -d "group=DEFAULT_GROUP" \
        -d "type=yaml" \
        --data-urlencode "content@$file")
    
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
echo "修复完成!"
echo "==================================="
echo "成功: $SUCCESS"
echo "失败: $FAILED"
echo "==================================="
