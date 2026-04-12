# 拓扑服务数据库设计文档

## 1. 概述

本文档描述拓扑服务的数据库设计，支持子网管理、网元管理、坐标存储、同类型合并、告警统计等功能。

## 2. ER 关系图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              拓扑服务数据模型                                     │
└─────────────────────────────────────────────────────────────────────────────────┘

                            ┌──────────────────────┐
                            │   T_TOPO_CONFIG      │
                            │   (拓扑配置)          │
                            │ ──────────────────── │
                            │ • MERGE_ENABLED      │
                            │ • MERGE_THRESHOLD    │
                            │ • MERGE_MODE         │
                            └──────────────────────┘

┌──────────────────────┐                            ┌──────────────────────┐
│  T_TOPO_NE_TYPE      │                            │ T_TOPO_SYNC_LOG      │
│  (网元类型枚举)       │                            │ (同步日志)            │
│ ──────────────────── │                            │ ──────────────────── │
│ • TYPE_CODE          │                            │ • SYNC_TYPE          │
│ • TYPE_NAME          │                            │ • STATUS             │
│ • ICON_NAME          │                            │ • TOTAL_COUNT        │
└──────────────────────┘                            └──────────────────────┘

┌──────────────────────────────────────┐
│           T_TOPO_SUBNET              │
│           (拓扑子网表)                 │
│ ──────────────────────────────────── │
│ PK: ID                               │
│ UK: DN                               │
│ ──────────────────────────────────── │
│ • NAME                               │
│ • PARENT_DN  ────────────────┐       │
│ • LAYER                      │       │
│ • IS_MERGE_GROUP             │       │
│ • MERGE_TYPE                 │       │
└──────────────────────────────┼───────┘
                               │
         ┌─────────────────────┴─────────────────────┐
         │ 自关联(父子关系)                            │
         ▼                                           │
┌──────────────────────────────────────┐            │
│           T_TOPO_SUBNET              │            │
│           (父/子子网)                  │            │
└──────────────────────────────────────┘            │
                                                    │
         ┌──────────────────────────────────────────┘
         │ 引用
         ▼
┌──────────────────────────────────────┐       ┌──────────────────────────────────────┐
│           T_TOPO_NE                  │       │       T_TOPO_MERGE_GROUP             │
│           (拓扑网元表)                 │       │       (合并组表)                      │
│ ──────────────────────────────────── │       │ ──────────────────────────────────── │
│ PK: ID                               │       │ PK: ID                               │
│ UK: DN                               │       │ UK: (PARENT_DN, NE_TYPE, GROUP_INDEX)│
│ FK: PARENT_DN → T_TOPO_SUBNET.DN     │───────│ FK: SUBNET_DN → T_TOPO_SUBNET.DN     │
│ ──────────────────────────────────── │       │ FK: PARENT_DN → T_TOPO_SUBNET.DN     │
│ • NAME                               │       │ ──────────────────────────────────── │
│ • NE_TYPE  ◄─────────────────────────│───────│ • NE_TYPE                            │
│ • STATUS                             │       │ • GROUP_INDEX                        │
│ • ROOT_SUBNET_DN                     │       │ • MEMBER_COUNT                       │
└──────────────────────────────────────┘       └──────────────────────────────────────┘
         │
         │
         ▼
┌──────────────────────────────────────┐       ┌──────────────────────────────────────┐
│       T_TOPO_ALARM_STATS             │       │       T_TOPO_POSITION                │
│       (告警统计表)                     │       │       (坐标位置表)                    │
│ ──────────────────────────────────── │       │ ──────────────────────────────────── │
│ PK: ID                               │       │ PK: ID                               │
│ UK: ELEMENT_DN                       │       │ UK: (SUBNET_DN, ELEMENT_DN)          │
│ ──────────────────────────────────── │       │ ──────────────────────────────────── │
│ • ELEMENT_DN ────────────────────────│───────│ • SUBNET_DN → T_TOPO_SUBNET.DN       │
│ • ELEMENT_TYPE (SUBNET/NE)           │       │ • ELEMENT_DN (子网或网元DN)           │
│ • CRITICAL_COUNT                     │       │ • ELEMENT_TYPE (SUBNET/NE)           │
│ • MAJOR_COUNT                        │       │ • POS_X, POS_Y                       │
│ • MINOR_COUNT                        │       │ • WIDTH, HEIGHT                      │
│ • WARNING_COUNT                      │       └──────────────────────────────────────┘
└──────────────────────────────────────┘
```

## 3. 表结构详细说明

### 3.1 T_TOPO_SUBNET (拓扑子网表)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ID | BIGSERIAL | Y | 主键 |
| DN | VARCHAR(255) | Y | 唯一标识，来自EAM |
| NAME | VARCHAR(256) | Y | 子网名称 |
| DISPLAY_NAME | VARCHAR(256) | N | 显示名称 |
| PARENT_DN | VARCHAR(255) | N | 父节点DN |
| PARENT_TYPE | VARCHAR(32) | N | 父节点类型: SUBNET/NE |
| LAYER | INT | N | 层级深度，默认0 |
| IS_MERGE_GROUP | BOOLEAN | N | 是否为同类型合并组 |
| MERGE_TYPE | VARCHAR(64) | N | 合并的设备类型 |
| ALARM_STATUS | INT | N | 告警状态 |

**索引**:
- `IDX_TOPO_SUBNET_DN`: DN唯一索引
- `IDX_TOPO_SUBNET_PARENT`: PARENT_DN索引
- `IDX_TOPO_SUBNET_LAYER`: LAYER索引

### 3.2 T_TOPO_NE (拓扑网元表)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ID | BIGSERIAL | Y | 主键 |
| DN | VARCHAR(255) | Y | 唯一标识，来自EAM |
| NAME | VARCHAR(256) | Y | 网元名称 |
| NE_TYPE | VARCHAR(64) | Y | 网元类型 |
| PARENT_DN | VARCHAR(255) | N | 父节点DN |
| ROOT_SUBNET_DN | VARCHAR(255) | N | 所属根子网DN |
| STATUS | INT | N | 状态: 0-离线 1-在线 |
| ALARM_STATUS | INT | N | 告警状态 |

**索引**:
- `IDX_TOPO_NE_DN`: DN唯一索引
- `IDX_TOPO_NE_TYPE`: NE_TYPE索引
- `IDX_TOPO_NE_ROOT`: ROOT_SUBNET_DN索引

### 3.3 T_TOPO_POSITION (拓扑坐标表)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ID | BIGSERIAL | Y | 主键 |
| SUBNET_DN | VARCHAR(255) | Y | 所属子网视图DN |
| ELEMENT_DN | VARCHAR(255) | Y | 元素DN |
| ELEMENT_TYPE | VARCHAR(32) | Y | 元素类型: SUBNET/NE |
| POS_X | INT | N | X坐标 |
| POS_Y | INT | N | Y坐标 |
| WIDTH | INT | N | 宽度 |
| HEIGHT | INT | N | 高度 |

**唯一约束**: (SUBNET_DN, ELEMENT_DN)

### 3.4 T_TOPO_MERGE_GROUP (合并组表)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ID | BIGSERIAL | Y | 主键 |
| SUBNET_DN | VARCHAR(255) | Y | 合并组子网DN |
| PARENT_DN | VARCHAR(255) | Y | 父子网DN |
| NE_TYPE | VARCHAR(64) | Y | 合并的网元类型 |
| GROUP_INDEX | INT | N | 组编号，默认1 |
| MEMBER_COUNT | INT | N | 成员数量 |

**唯一约束**: (PARENT_DN, NE_TYPE, GROUP_INDEX)

### 3.5 T_TOPO_CONFIG (拓扑配置表)

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| MERGE_ENABLED | false | 是否开启同类型合并 |
| MERGE_THRESHOLD | 20 | 合并阈值 |
| MERGE_MODE | THRESHOLD | 合并模式: THRESHOLD/ALL |

### 3.6 T_TOPO_ALARM_STATS (告警统计表)

| 字段 | 类型 | 说明 |
|------|------|------|
| ELEMENT_DN | VARCHAR(255) | 元素DN |
| ELEMENT_TYPE | VARCHAR(32) | 元素类型 |
| CRITICAL_COUNT | INT | 严重告警数 |
| MAJOR_COUNT | INT | 主要告警数 |
| MINOR_COUNT | INT | 次要告警数 |
| WARNING_COUNT | INT | 警告数 |
| TOTAL_COUNT | INT | 总告警数 |

## 4. 数据流说明

### 4.1 初始化同步流程

```
┌─────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  EAM    │────▶│   Kafka     │────▶│  拓扑服务    │────▶│  GaussDB    │
│ 服务    │     │  变更事件    │     │  消费者      │     │  数据库      │
└─────────┘     └─────────────┘     └─────────────┘     └─────────────┘
     │                                      │
     │  1. 全量查询Default分组               │
     │─────────────────────────────────────▶│
     │                                      │
     │  2. 返回子网和设备列表                 │
     │◀─────────────────────────────────────│
     │                                      │
     │                                      │ 3. 写入 T_TOPO_SUBNET
     │                                      │    写入 T_TOPO_NE
     │                                      │    记录 T_TOPO_SYNC_LOG
```

### 4.2 同类型合并流程

```
1. 检查配置 MERGE_ENABLED = true
2. 统计同层级同类型网元数量
3. 如果数量 > MERGE_THRESHOLD:
   a. 创建合并组子网 → T_TOPO_SUBNET (IS_MERGE_GROUP=true)
   b. 记录合并组 → T_TOPO_MERGE_GROUP
   c. 更新网元父节点 → T_TOPO_NE.PARENT_DN
4. 如果 MERGE_ENABLED 从 true → false:
   a. 查询所有合并组
   b. 将子网元挂载到上层
   c. 删除合并组子网
```

## 5. 视图说明

### 5.1 V_TOPO_SUBNET_STATS (子网统计视图)

提供子网的汇总统计信息:
- 子网下的网元数量
- 子网下的子网数量
- 离线设备数量
- 各级别告警数量

### 5.2 V_TOPO_TYPE_STATS (网元类型统计视图)

按网元类型统计:
- 各类型总数
- 在线/离线数量
- 按父子网分组

## 6. 网元类型枚举

| 类型编码 | 类型名称 | 图标 |
|----------|----------|------|
| SUBNET | 子网 | subnet |
| FIREWALL | 防火墙 | firewall |
| SWITCH | 交换机 | switch |
| SERVER | 服务器 | server |
| STORAGE | 存储设备 | storage |
| GATEWAY | 网关 | gateway |
| CHASSIS | 机框 | chassis |
| RACK | 机架 | rack |
| DEFAULT | 通用设备 | default |

## 7. 与 EAM 的映射关系

```
EAM.TBLMANAGEDOBJECT          拓扑服务
─────────────────────────────────────────────
DN                      ───▶  T_TOPO_SUBNET.DN / T_TOPO_NE.DN
NAME                    ───▶  NAME
DISPLAYNAME             ───▶  DISPLAY_NAME
PARENT                  ───▶  PARENT_DN
MEDNODE                 ───▶  MED_NODE
ADDRESS                 ───▶  ADDRESS
LOCATION                ───▶  LOCATION
MAINTAINER              ───▶  MAINTAINER
CONTACT                 ───▶  CONTACT
CREATEDTIME             ───▶  CREATED_TIME
SEQUENCENO              ───▶  SEQUENCE_NO
ALARMSTATUS             ───▶  ALARM_STATUS
VERSION                 ───▶  VERSION
```

## 8. 性能优化建议

1. **索引策略**
   - DN 字段建立唯一索引
   - PARENT_DN 建立普通索引支持树查询
   - 复合查询考虑建立组合索引

2. **分区策略** (数据量大时)
   - T_TOPO_NE 可按 NE_TYPE 分区
   - T_TOPO_SYNC_LOG 可按时间分区

3. **缓存策略**
   - 配置信息可缓存到内存
   - 告警统计数据可定期刷新
