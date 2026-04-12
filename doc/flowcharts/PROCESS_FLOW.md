# 拓扑服务流程图

## 1. 初始化同步流程

从 EAM 服务全量加载数据到拓扑服务。

```mermaid
flowchart TD
    Start([开始初始化]) --> A[从EAM查询Default分组]
    A --> B{查询结果?}
    B -->|成功| C[将子网和设备加入待查询队列]
    B -->|失败| Err[记录错误日志]
    Err --> End1([结束])

    C --> D[从待查询队列取出元素]
    D --> E{元素类型?}

    E -->|子网| F[查询子网下的子网和设备]
    E -->|设备| G[填充设备详细信息]

    F --> H{有子元素?}
    H -->|是| I[将子元素加入待查询队列]
    H -->|否| J[将当前子网加入待插入队列]

    I --> K{待查询队列是否为空?}
    J --> K
    G --> L[将设备加入待插入队列]
    L --> K

    K -->|否| D
    K -->|是| M[批量插入待插入队列数据]

    M --> N[更新同步日志]
    N --> End([初始化完成])

    style Start fill:#e1f5e1
    style End fill:#e1f5e1
    style End1 fill:#ffe1e1
    style Err fill:#ffe1e1
```

### 初始化流程说明

| 步骤 | 说明 |
|------|------|
| 1 | 调用 EAM 接口查询 Default 分组下所有子网和设备 |
| 2 | 将查询结果加入待查询队列 |
| 3 | 轮询待查询队列，递归查询子网层级结构 |
| 4 | 设备信息填充完成后加入待插入队列 |
| 5 | 队列清空后批量入库 |

---

## 2. Kafka 变更事件处理流程

监听 EAM 发送的 Kafka 变更事件。

```mermaid
flowchart TD
    Start([Kafka消息到达]) --> A[解析消息内容]
    A --> B[获取变更类型]

    B --> C{变更类型?}

    C -->|CREATE| D[创建事件处理]
    C -->|UPDATE| E[更新事件处理]
    C -->|DELETE| F[删除事件处理]

    D --> D1{对象类型?}
    D1 -->|子网| D2[插入T_TOPO_SUBNET]
    D1 -->|网元| D3[插入T_TOPO_NE]

    E --> E1{对象类型?}
    E1 -->|子网| E2[更新T_TOPO_SUBNET]
    E1 -->|网元| E3[更新T_TOPO_NE]

    F --> F1{对象类型?}
    F1 -->|子网| F2[检查子网下是否有设备]
    F1 -->|网元| F3[删除T_TOPO_NE记录]

    F2 --> F4{有设备?}
    F4 -->|是| F5[拒绝删除,返回错误]
    F4 -->|否| F6[递归删除子网及坐标]

    D2 --> G[处理同类型合并]
    D3 --> G
    E2 --> G
    E3 --> G

    G --> H[同步到CIE]
    H --> I[更新同步日志]
    I --> End([处理完成])

    F3 --> I
    F5 --> End
    F6 --> I

    style Start fill:#fff3e0
    style End fill:#e1f5e1
    style F5 fill:#ffe1e1
```

---

## 3. 同类型合并流程

开启同类型合并时的处理逻辑。

```mermaid
flowchart TD
    Start([触发合并检查]) --> A[读取合并配置]

    A --> B{MERGE_ENABLED?}
    B -->|false| End1([不处理合并])

    B -->|true| C[按父子网分组统计同类型网元]

    C --> D[遍历每个父子网+类型组合]
    D --> E{数量 >= MERGE_THRESHOLD?}

    E -->|否| D
    E -->|是| F[计算需要创建的组数]

    F --> G[组数 = ceil(数量 / 阈值)]

    G --> H[循环创建组]
    H --> H1[创建合并组子网]
    H1 --> H2[子网名称 = 类型名 + 编号]
    H2 --> H3[插入T_TOPO_SUBNET<br/>IS_MERGE_GROUP=true]

    H3 --> H4[插入T_TOPO_MERGE_GROUP]
    H4 --> H5[批量更新网元PARENT_DN]

    H5 --> I{还有组?}
    I -->|是| H
    I -->|否| J[保存坐标信息]

    J --> K[同步到CIE]
    K --> End([合并完成])

    style Start fill:#e3f2fd
    style End fill:#e1f5e1
    style End1 fill:#fff3e0
```

### 合并示例

```
假设阈值 = 10, 防火墙数量 = 25

分组结果:
├── 防火墙组1 (10个)  → 新建子网 "防火墙1"
├── 防火墙组2 (10个)  → 新建子网 "防火墙2"
└── 防火墙组3 (5个)   → 不满足阈值,保留原位置

最终:
├── Default子网
│   ├── 防火墙1 (合并组子网)
│   │   └── [10个防火墙设备]
│   ├── 防火墙2 (合并组子网)
│   │   └── [10个防火墙设备]
│   └── [5个防火墙设备] (未合并)
```

---

## 4. 同类型消除流程

从开启合并切换到关闭合并时的处理。

```mermaid
flowchart TD
    Start([关闭合并配置]) --> A[查询所有合并组子网]

    A --> B[遍历合并组]
    B --> B1[获取合并组下的所有网元]

    B1 --> C[获取合并组的父节点]
    C --> D[批量更新网元PARENT_DN<br/>指向原父节点]

    D --> E[删除T_TOPO_MERGE_GROUP记录]
    E --> F[删除合并组子网记录]
    F --> G[删除相关坐标记录]

    G --> H{还有合并组?}
    H -->|是| B
    H -->|否| I[同步到CIE]

    I --> End([消除完成])

    style Start fill:#fce4ec
    style End fill:#e1f5e1
```

---

## 5. 子网管理流程

### 5.1 创建子网

```mermaid
flowchart TD
    Start([创建子网请求]) --> A[验证参数]

    A --> B{名称有效?}
    B -->|否| Err1([返回错误: 名称无效])

    B -->|是| C[生成DN]
    C --> D[计算层级LAYER]

    D --> E[插入T_TOPO_SUBNET]
    E --> F[调用EAM创建接口]

    F --> G{EAM返回?}
    G -->|成功| H[同步到CIE]
    G -->|失败| Err2([回滚本地数据])

    H --> End([创建成功])

    style Start fill:#e3f2fd
    style End fill:#e1f5e1
    style Err1 fill:#ffe1e1
    style Err2 fill:#ffe1e1
```

### 5.2 删除子网

```mermaid
flowchart TD
    Start([删除子网请求]) --> A[查询子网信息]

    A --> B{子网存在?}
    B -->|否| Err1([返回错误: 子网不存在])

    B -->|是| C{是合并组子网?}
    C -->|是| D[允许直接删除]
    C -->|否| E[查询子网下的设备]

    E --> F{有设备?}
    F -->|是| Err2([返回错误: 子网下存在设备])

    F -->|否| G[查询子网下的子网]
    G --> H{有子网?}
    H -->|是| Err3([返回错误: 子网下存在子网])
    H -->|否| D

    D --> I[递归删除坐标数据]
    I --> J[删除T_TOPO_SUBNET记录]
    J --> K[调用EAM删除接口]
    K --> L[同步到CIE]
    L --> End([删除成功])

    style Start fill:#e3f2fd
    style End fill:#e1f5e1
    style Err1 fill:#ffe1e1
    style Err2 fill:#ffe1e1
    style Err3 fill:#ffe1e1
```

### 5.3 批量移入设备

```mermaid
flowchart TD
    Start([批量移入设备请求]) --> A[验证目标子网]

    A --> B{子网存在?}
    B -->|否| Err1([返回错误: 子网不存在])

    B -->|是| C{是合并组子网?}
    C -->|是| D[检查设备类型是否匹配]
    D --> E{类型匹配?}
    E -->|否| Err2([返回错误: 类型不匹配])

    E -->|是| F[获取设备列表]
    C -->|否| F

    F --> G[批量更新PARENT_DN]
    G --> H[批量调用EAM更新接口]
    H --> I[同步到CIE]
    I --> End([移入成功])

    style Start fill:#e3f2fd
    style End fill:#e1f5e1
    style Err1 fill:#ffe1e1
    style Err2 fill:#ffe1e1
```

---

## 6. 坐标保存流程

```mermaid
flowchart TD
    Start([保存坐标请求]) --> A[获取当前子网DN]

    A --> B[获取坐标数据列表]
    B --> C[遍历坐标数据]

    C --> D{坐标记录存在?}
    D -->|是| E[更新T_TOPO_POSITION]
    D -->|否| F[插入T_TOPO_POSITION]

    E --> G{还有更多坐标?}
    F --> G

    G -->|是| C
    G -->|否| End([保存完成])

    style Start fill:#e8f5e9
    style End fill:#e1f5e1
```

**坐标数据结构示例**:
```json
{
  "subnetDn": "DN=Default",
  "positions": [
    { "elementDn": "DN=Subnet1", "elementType": "SUBNET", "x": 100, "y": 200 },
    { "elementDn": "DN=Firewall1", "elementType": "NE", "x": 300, "y": 150 }
  ]
}
```

---

## 7. 告警统计更新流程

```mermaid
flowchart TD
    Start([告警变更事件]) --> A[解析告警信息]

    A --> B[获取关联的网元DN]
    B --> C[更新网元告警统计]

    C --> D[递归更新父节点统计]
    D --> D1[查询父节点DN]
    D1 --> D2[聚合子节点告警数]
    D2 --> D3[更新T_TOPO_ALARM_STATS]

    D3 --> E{还有父节点?}
    E -->|是| D
    E -->|否| F[推送WebSocket通知]

    F --> End([统计更新完成])

    style Start fill:#fff8e1
    style End fill:#e1f5e1
```

---

## 8. 自动布局流程

```mermaid
flowchart TD
    Start([触发自动布局]) --> A[获取当前子网下所有元素]

    A --> B[按类型分组]
    B --> C[计算布局网格]

    C --> D[分配坐标位置]
    D --> E[保存坐标到数据库]

    E --> F[返回新坐标给前端]
    F --> End([布局完成])

    style Start fill:#f3e5f5
    style End fill:#e1f5e1
```

---

## 9. 整体架构流程图

```mermaid
flowchart TB
    subgraph EAM["EAM服务"]
        E1[(EAM数据库)]
        E2[Kafka生产者]
    end

    subgraph TOPO["拓扑服务"]
        T1[Kafka消费者]
        T2[同步处理器]
        T3[合并处理器]
        T4[业务服务层]
        T5[(GaussDB)]
    end

    subgraph CIE["CIE服务"]
        C1[CIE接口]
    end

    subgraph FRONT["前端"]
        F1[拓扑视图]
        F2[左侧树]
        F3[右侧面板]
    end

    E1 -->|变更| E2
    E2 -->|事件| T1
    T1 --> T2
    T2 --> T3
    T3 --> T4
    T4 --> T5

    T4 -->|同步| C1
    T4 -->|API| F1
    T4 -->|API| F2
    T4 -->|API| F3

    F1 -->|用户操作| T4
    F2 -->|用户操作| T4
```

---

## 10. 状态机 - 合并配置状态

```mermaid
stateDiagram-v2
    [*] --> Disabled: 初始状态

    Disabled --> Enabled: 开启合并
    Enabled --> Disabled: 关闭合并

    state Enabled {
        [*] --> Checking
        Checking --> Merging: 超过阈值
        Checking --> Idle: 未超过阈值
        Merging --> Idle: 合并完成
        Idle --> Checking: 新设备加入
    }

    state Disabled {
        [*] --> Normal
        Normal --> Restoring: 从开启切换
        Restoring --> Normal: 恢复完成
    }

    note right of Enabled
        合并组子网:
        - IS_MERGE_GROUP = true
        - 自动管理
    end note

    note right of Disabled
        正常模式:
        - 无合并组
        - 手动管理
    end note
```
