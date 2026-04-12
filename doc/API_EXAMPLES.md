# API 请求示例集合

本文提供各接口的完整请求/响应示例，可用于 Postman 导入测试。

## 导入 Postman

1. 打开 Postman
2. 点击 Import -> Raw text
3. 粘贴以下内容

---

## Postman Collection

```json
{
  "info": {
    "name": "拓扑服务 API",
    "description": "物理拓扑服务接口集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. 子网管理",
      "item": [
        {
          "name": "创建子网",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"机房A\",\n  \"displayName\": \"北京机房A\",\n  \"parentDn\": \"DN=Root\",\n  \"address\": \"北京市朝阳区xxx\",\n  \"location\": \"北京市\",\n  \"maintainer\": \"张三\",\n  \"contact\": \"13800138000\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/subnet"
            }
          },
          "response": []
        },
        {
          "name": "更新子网",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"机房A-更新\",\n  \"displayName\": \"北京机房A-更新\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/subnet/DN=Subnet_001"
            }
          },
          "response": []
        },
        {
          "name": "获取子网详情",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/subnet/DN=Subnet_001"
            }
          },
          "response": []
        },
        {
          "name": "获取子网树",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/subnet/tree"
            }
          },
          "response": []
        },
        {
          "name": "批量移入设备",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"targetSubnetDn\": \"DN=Subnet_001\",\n  \"neDnList\": [\"DN=NE_001\", \"DN=NE_002\", \"DN=NE_003\"]\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/subnet/batch-move"
            }
          },
          "response": []
        },
        {
          "name": "删除子网",
          "request": {
            "method": "DELETE",
            "url": {
              "raw": "{{baseUrl}}/subnet/DN=Subnet_001"
            }
          },
          "response": []
        }
      ]
    },
    {
      "name": "2. 拓扑视图",
      "item": [
        {
          "name": "获取拓扑视图",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/topo/view/DN=Subnet_001"
            }
          },
          "response": []
        },
        {
          "name": "保存坐标",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"subnetDn\": \"DN=Subnet_001\",\n  \"positions\": [\n    {\n      \"elementDn\": \"DN=NE_001\",\n      \"elementType\": \"NE\",\n      \"x\": 100,\n      \"y\": 200\n    },\n    {\n      \"elementDn\": \"DN=Subnet_002\",\n      \"elementType\": \"SUBNET\",\n      \"x\": 300,\n      \"y\": 100\n    }\n  ]\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/topo/position"
            }
          },
          "response": []
        },
        {
          "name": "自动布局",
          "request": {
            "method": "POST",
            "url": {
              "raw": "{{baseUrl}}/topo/auto-layout/DN=Subnet_001"
            }
          },
          "response": []
        }
      ]
    },
    {
      "name": "3. 合并配置",
      "item": [
        {
          "name": "获取合并配置",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/merge/config"
            }
          },
          "response": []
        },
        {
          "name": "更新合并配置",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"enabled\": true,\n  \"threshold\": 15,\n  \"mode\": \"THRESHOLD\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/merge/config"
            }
          },
          "response": []
        },
        {
          "name": "执行合并",
          "request": {
            "method": "POST",
            "url": {
              "raw": "{{baseUrl}}/merge/execute"
            }
          },
          "response": []
        },
        {
          "name": "取消合并",
          "request": {
            "method": "POST",
            "url": {
              "raw": "{{baseUrl}}/merge/disable"
            }
          },
          "response": []
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080/api/topo"
    }
  ]
}
```

---

## curl 命令示例

### 创建子网
```bash
curl -X POST "http://localhost:8080/api/topo/subnet" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "机房A",
    "displayName": "北京机房A",
    "parentDn": "DN=Root",
    "address": "北京市朝阳区xxx",
    "location": "北京市",
    "maintainer": "张三",
    "contact": "13800138000"
  }'
```

### 获取子网树
```bash
curl -X GET "http://localhost:8080/api/topo/subnet/tree"
```

### 获取拓扑视图
```bash
curl -X GET "http://localhost:8080/api/topo/topo/view/DN=Subnet_001"
```

### 保存坐标
```bash
curl -X POST "http://localhost:8080/api/topo/topo/position" \
  -H "Content-Type: application/json" \
  -d '{
    "subnetDn": "DN=Subnet_001",
    "positions": [
      {
        "elementDn": "DN=NE_001",
        "elementType": "NE",
        "x": 100,
        "y": 200
      }
    ]
  }'
```

### 更新合并配置
```bash
curl -X PUT "http://localhost:8080/api/topo/merge/config" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "threshold": 15,
    "mode": "THRESHOLD"
  }'
```

### 执行合并
```bash
curl -X POST "http://localhost:8080/api/topo/merge/execute"
```

### 批量移入设备
```bash
curl -X POST "http://localhost:8080/api/topo/subnet/batch-move" \
  -H "Content-Type: application/json" \
  -d '{
    "targetSubnetDn": "DN=Subnet_001",
    "neDnList": ["DN=NE_001", "DN=NE_002", "DN=NE_003"]
  }'
```

---

## WebSocket 实时推送

服务支持 WebSocket 实时推送拓扑变更。

### 连接地址
```
ws://localhost:8080/api/topo/ws
```

### 消息格式
```json
{
  "type": "TOPOLOGY_UPDATE",
  "data": {
    "subnetDn": "DN=Subnet_001",
    "changeType": "ELEMENT_ADDED",
    "element": {
      "dn": "DN=NE_New",
      "name": "新设备",
      "type": "NE"
    }
  },
  "timestamp": 1712123456789
}
```

### 消息类型
| type | 说明 |
|------|------|
| TOPOLOGY_UPDATE | 拓扑结构变更 |
| ALARM_UPDATE | 告警状态更新 |
| SYNC_COMPLETE | 同步完成通知 |
