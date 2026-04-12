# 拓扑服务 API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api/topo`
- **Content-Type**: `application/json`
- **认证方式**: Bearer Token (可选)

---

## 1. 子网管理 API

### 1.1 创建子网

**POST** `/subnet`

创建新的子网。

#### 请求体

```json
{
  "name": "机房A",
  "displayName": "北京机房A",
  "parentDn": "DN=Root",
  "address": "北京市朝阳区xxx",
  "location": "北京市",
  "maintainer": "张三",
  "contact": "13800138000"
}
```

#### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 子网名称，最大256字符 |
| displayName | string | 否 | 显示名称 |
| parentDn | string | 否 | 父节点DN，为空则创建根子网 |
| address | string | 否 | 地址 |
| location | string | 否 | 位置 |
| maintainer | string | 否 | 维护人 |
| contact | string | 否 | 联系方式 |

#### 响应示例

```json
{
  "code": 200,
  "message": "创建成功",
  "data": null,
  "timestamp": 1712123456789
}
```

#### 错误响应

| 状态码 | 说明 |
|--------|------|
| 400 | 参数校验失败 |
| 500 | 服务器内部错误 |

---

### 1.2 更新子网

**PUT** `/subnet/{dn}`

更新指定子网信息。

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| dn | string | 子网DN (URL编码) |

#### 请求体

```json
{
  "name": "机房A-更新",
  "displayName": "北京机房A-更新",
  "address": "北京市海淀区xxx",
  "location": "北京市海淀区",
  "maintainer": "李四",
  "contact": "13900139000"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1712123456789
}
```

---

### 1.3 删除子网

**DELETE** `/subnet/{dn}`

删除指定子网。

> ⚠️ **注意**: 子网下存在设备或子网时无法删除

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| dn | string | 子网DN (URL编码) |

#### 响应示例

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1712123456789
}
```

#### 错误响应

| 状态码 | 说明 |
|--------|------|
| 400 | 子网不存在 |
| 400 | 子网下存在设备 |
| 400 | 子网下存在子网 |

---

### 1.4 获取子网详情

**GET** `/subnet/{dn}`

获取指定子网的详细信息。

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| dn | string | 子网DN (URL编码) |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dn": "DN=Subnet_001",
    "name": "机房A",
    "displayName": "北京机房A",
    "layer": 1,
    "isMergeGroup": false,
    "mergeType": null,
    "neCount": 15,
    "subnetCount": 3,
    "offlineCount": 2,
    "onlineCount": 13,
    "criticalCount": 1,
    "majorCount": 2,
    "minorCount": 3,
    "warningCount": 5,
    "address": "北京市朝阳区xxx",
    "location": "北京市",
    "maintainer": "张三",
    "contact": "13800138000"
  },
  "timestamp": 1712123456789
}
```

---

### 1.5 获取子网树

**GET** `/subnet/tree`

获取子网树结构。

#### 查询参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| rootDn | string | 否 | 根节点DN，为空则返回所有根子网 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "dn": "DN=Root",
      "name": "Root",
      "displayName": "根子网",
      "type": "SUBNET",
      "neType": null,
      "icon": "subnet",
      "isMergeGroup": false,
      "layer": 0,
      "status": null,
      "childCount": 5,
      "children": [
        {
          "dn": "DN=Subnet_001",
          "name": "机房A",
          "displayName": "北京机房A",
          "type": "SUBNET",
          "icon": "subnet",
          "isMergeGroup": false,
          "layer": 1,
          "childCount": 15,
          "children": null
        }
      ]
    }
  ],
  "timestamp": 1712123456789
}
```

---

### 1.6 批量移入设备

**POST** `/subnet/batch-move`

批量将设备移动到指定子网。

#### 请求体

```json
{
  "targetSubnetDn": "DN=Subnet_001",
  "neDnList": [
    "DN=NE_001",
    "DN=NE_002",
    "DN=NE_003"
  ]
}
```

#### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetSubnetDn | string | 是 | 目标子网DN |
| neDnList | array | 是 | 设备DN列表，不能为空 |

#### 响应示例

```json
{
  "code": 200,
  "message": "移入成功",
  "data": null,
  "timestamp": 1712123456789
}
```

#### 错误响应

| 状态码 | 说明 |
|--------|------|
| 400 | 目标子网不存在 |
| 400 | 设备类型不匹配(移入合并组时) |

---

## 2. 拓扑视图 API

### 2.1 获取拓扑视图

**GET** `/topo/view/{subnetDn}`

获取指定子网的拓扑视图，包含所有子元素及其坐标。

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| subnetDn | string | 子网DN |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "subnetDn": "DN=Root",
    "subnetName": "Root",
    "elements": [
      {
        "dn": "DN=Subnet_001",
        "name": "机房A",
        "type": "SUBNET",
        "neType": null,
        "icon": "subnet",
        "x": 100,
        "y": 100,
        "width": null,
        "height": null,
        "status": null,
        "isMergeGroup": false,
        "alarmStatus": 0,
        "childCount": 15
      },
      {
        "dn": "DN=NE_001",
        "name": "防火墙1",
        "type": "NE",
        "neType": "FIREWALL",
        "icon": "firewall",
        "x": 300,
        "y": 100,
        "width": null,
        "height": null,
        "status": 1,
        "isMergeGroup": false,
        "alarmStatus": 1,
        "childCount": null
      }
    ]
  },
  "timestamp": 1712123456789
}
```

---

### 2.2 保存坐标

**POST** `/topo/position`

批量保存元素坐标。

#### 请求体

```json
{
  "subnetDn": "DN=Root",
  "positions": [
    {
      "elementDn": "DN=Subnet_001",
      "elementType": "SUBNET",
      "x": 100,
      "y": 100,
      "width": 200,
      "height": 150
    },
    {
      "elementDn": "DN=NE_001",
      "elementType": "NE",
      "x": 300,
      "y": 200,
      "width": null,
      "height": null
    }
  ]
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "保存成功",
  "data": null,
  "timestamp": 1712123456789
}
```

---

### 2.3 自动布局

**POST** `/topo/auto-layout/{subnetDn}`

对指定子网下的元素执行自动布局。

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| subnetDn | string | 子网DN |

#### 响应示例

返回更新后的拓扑视图，布局算法会将元素按网格排列。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "subnetDn": "DN=Root",
    "subnetName": "Root",
    "elements": [
      {
        "dn": "DN=Subnet_001",
        "name": "机房A",
        "type": "SUBNET",
        "x": 50,
        "y": 50,
        ...
      }
    ]
  },
  "timestamp": 1712123456789
}
```

---

## 3. 合并配置 API

### 3.1 获取合并配置

**GET** `/merge/config`

获取当前合并配置。

#### 响应示例

```json
{
  "enabled": false,
  "threshold": 20,
  "mode": "THRESHOLD"
}
```

---

### 3.2 更新合并配置

**PUT** `/merge/config`

更新合并配置。

#### 请求体

```json
{
  "enabled": true,
  "threshold": 15,
  "mode": "THRESHOLD"
}
```

#### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| enabled | boolean | 否 | 是否开启合并 |
| threshold | integer | 否 | 合并阈值，最小为1 |
| mode | string | 否 | 合并模式: THRESHOLD / ALL |

#### 响应

成功: HTTP 200

---

### 3.3 执行合并

**POST** `/merge/execute`

对现有数据执行合并操作。

> 需要先开启合并配置

#### 响应

成功: HTTP 200

---

### 3.4 取消合并

**POST** `/merge/disable`

取消所有合并组，将设备恢复到原位置。

#### 响应

成功: HTTP 200

---

## 4. 网元管理 API

### 4.1 获取网元详情

**GET** `/ne/{dn}`

获取指定网元的详细信息。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dn": "DN=NE_001",
    "name": "防火墙1",
    "displayName": "核心防火墙1",
    "neType": "FIREWALL",
    "icon": "firewall",
    "parentDn": "DN=Subnet_001",
    "rootSubnetDn": "DN=Root",
    "status": 1,
    "statusDesc": "在线",
    "criticalCount": 0,
    "majorCount": 1,
    "minorCount": 2,
    "warningCount": 3,
    "address": "192.168.1.1",
    "location": "机房A-机柜1",
    "maintainer": "张三",
    "contact": "13800138000"
  },
  "timestamp": 1712123456789
}
```

---

## 5. 枚举值参考

### 5.1 元素类型 (ElementType)

| 值 | 说明 |
|------|------|
| SUBNET | 子网 |
| NE | 网元 |

### 5.2 网元类型 (NeType)

| 值 | 说明 | 图标 |
|------|------|------|
| SUBNET | 子网 | subnet |
| FIREWALL | 防火墙 | firewall |
| SWITCH | 交换机 | switch |
| SERVER | 服务器 | server |
| STORAGE | 存储设备 | storage |
| GATEWAY | 网关 | gateway |
| CHASSIS | 机框 | chassis |
| RACK | 机架 | rack |
| DEFAULT | 通用设备 | default |

### 5.3 合并模式 (MergeMode)

| 值 | 说明 |
|------|------|
| THRESHOLD | 超过阈值时合并 |
| ALL | 全部合并(数量>1时) |

### 5.4 设备状态 (Status)

| 值 | 说明 |
|------|------|
| 0 | 离线 |
| 1 | 在线 |

---

## 6. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 7. Swagger 访问

启动服务后访问:

- **Swagger UI**: http://localhost:8080/api/topo/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/topo/v3/api-docs
