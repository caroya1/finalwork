#!/bin/bash
# ELK Stack 部署脚本 for CentOS 7
# 使用方法: ./deploy-elk-centos7.sh

set -e

echo "========================================"
echo "ELK Stack 部署脚本 (CentOS 7)"
echo "========================================"

# 检查是否以root运行
if [ "$EUID" -ne 0 ]; then 
    echo "请使用 root 权限运行此脚本"
    exit 1
fi

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "正在安装 Docker..."
    yum install -y docker
    systemctl start docker
    systemctl enable docker
    echo "Docker 安装完成"
else
    echo "Docker 已安装"
    systemctl start docker
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "正在安装 Docker Compose..."
    curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    echo "Docker Compose 安装完成"
else
    echo "Docker Compose 已安装"
fi

# 创建ELK目录
mkdir -p /opt/elk/logstash/pipeline
cd /opt/elk

# 创建Logstash配置文件
cat > logstash/pipeline/logstash.conf << 'EOF'
input {
  tcp {
    port => 5000
    codec => json_lines
  }
}

filter {
  date {
    match => [ "timestamp", "ISO8601" ]
    target => "@timestamp"
  }
}

output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "logs-%{+YYYY.MM.dd}"
  }
}
EOF

# 创建docker-compose.yml
cat > docker-compose.yml << 'EOF'
version: '3.3'

services:
  elasticsearch:
    image: elasticsearch:7.17.15
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - bootstrap.memory_lock=true
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - "9200:9200"
    volumes:
      - es_data:/usr/share/elasticsearch/data
    networks:
      - elk_network
    restart: always

  logstash:
    image: logstash:7.17.15
    container_name: logstash
    environment:
      - "LS_JAVA_OPTS=-Xms256m -Xmx256m"
    ports:
      - "5000:5000/tcp"
      - "5044:5044"
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline:ro
    networks:
      - elk_network
    depends_on:
      - elasticsearch
    restart: always

  kibana:
    image: kibana:7.17.15
    container_name: kibana
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "5601:5601"
    networks:
      - elk_network
    depends_on:
      - elasticsearch
    restart: always

volumes:
  es_data:

networks:
  elk_network:
    driver: bridge
EOF

echo ""
echo "正在启动 ELK Stack..."
docker-compose down -v 2>/dev/null || true
docker-compose up -d

echo ""
echo "========================================"
echo "ELK Stack 部署完成！"
echo "========================================"
echo "Elasticsearch: http://localhost:9200"
echo "Kibana:        http://localhost:5601"
echo "Logstash:      tcp://localhost:5000"
echo ""
echo "查看日志: docker-compose logs -f"
echo "停止服务: docker-compose down"
echo "========================================"
