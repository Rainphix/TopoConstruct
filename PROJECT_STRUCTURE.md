# 拓扑服务项目结构说明

## 项目结构

```
phytopo/
├── pom.xml                              # Maven 配置
├── doc/
│   ├── database/
│   │   ├── topo_schema.sql          # 数据库建表脚本
│   │   └── DESIGN.md               # 数据库设计文档
│   └── flowcharts/
│       ├── PROCESS_FLOW.md           # Mermaid 流程图
│       └── PROCESS_FLOW_ASCII.md    # ASCII 流程图
├── src/
│   ├── main/
│   │   ├── java/com/topology/phytopo/
│   │   │   ├── PhyTopoApplication.java    # 启动类
│   │   │   ├── common/
│   │   │   │   ├── Result.java             # 统一响应
│   │   │   │   ├── PageResult.java        # 分页响应
│   │   │   │   └── enums/
│   │   │   │       ├── ElementType.java    # 元素类型枚举
│   │   │   │       ├── NeTypeEnum.java      # 网元类型枚举
│   │   │   │       └── MergeModeEnum.java   # 合并模式枚举
│   │   │   ├── config/
│   │   │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   │   │   ├── controller/
│   │   │   │   ├── SubnetController.java       # 子网控制器
│   │   │   │   └── TopologyController.java    # 拓扑视图控制器
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── SubnetCreateRequest.java   # 子网创建请求
│   │   │   │   │   ├── SubnetUpdateRequest.java   # 子网更新请求
│   │   │   │   │   ├── PositionSaveRequest.java  # 坐标保存请求
│   │   │   │   │   ├── BatchMoveRequest.java    # 批量移入请求
│   │   │   │   │   └── MergeConfigRequest.java  # 合并配置请求
│   │   │   │   └── response/
│   │   │   │       ├── TreeNodeVO.java       # 树节点VO
│   │   │   │       ├── SubnetDetailVO.java   # 子网详情VO
│   │   │   │       ├── NeDetailVO.java       # 网元详情VO
│   │   │   │       └── TopologyViewVO.java   # 拓扑视图VO
│   │   │   ├── entity/
│   │   │   │   ├── TopoSubnet.java        # 子网实体
│   │   │   │   ├── TopoNe.java            # 网元实体
│   │   │   │   ├── TopoPosition.java      # 坐标实体
│   │   │   │   ├── TopoMergeGroup.java    # 合并组实体
│   │   │   │   ├── TopoConfig.java        # 配置实体
│   │   │   │   └── TopoAlarmStats.java    # 告警统计实体
│   │   │   ├── kafka/
│   │   │   │   ├── EamChangeMessage.java   # EAM变更消息
│   │   │   │   └── EamChangeConsumer.java  # Kafka消费者
│   │   │   ├── mapper/
│   │   │   │   ├── TopoSubnetMapper.java      # 子网Mapper
│   │   │   │   ├── TopoNeMapper.java          # 网元Mapper
│   │   │   │   ├── TopoPositionMapper.java    # 坐标Mapper
│   │   │   │   └── TopoMergeGroupMapper.java  # 合并组Mapper
│   │   │   └── service/
│   │   │       ├── SubnetService.java      # 子网服务
│   │   │       ├── TopologyService.java    # 拓扑服务
│   │   │       ├── SyncService.java        # 同步服务
│   │   │       └── MergeService.java       # 合并服务
│   │   └── resources/
│   │       ├── application.yml            # 应用配置
│   │       └── mapper/
│   │           ├── TopoSubnetMapper.xml    # 子网SQL
│   │           ├── TopoNeMapper.xml        # 网元SQL
│   │           ├── TopoPositionMapper.xml  # 坐标SQL
│   │           └── TopoMergeGroupMapper.xml # 合并组SQL
│   └── test/
│       └── java/com/topology/phytopo/
│           └── (测试类)
└── logs/                               # 日志目录
```

## 模块说明

### 1. Controller 层
- `SubnetController`: 子网增删改查、批量移入设备
- `TopologyController`: 拓扑视图、坐标保存、自动布局

- `MergeController`: 合并配置管理

### 2. Service 层
- `SubnetService`: 子网业务逻辑
- `TopologyService`: 拓扑视图、坐标业务
- `SyncService`: EAM数据同步
- `MergeService`: 同类型合并处理
- `NeService`: 网元业务逻辑

- `AlarmService`: 告警统计

### 3. Mapper 层
- `TopoSubnetMapper`: 子网数据访问
- `TopoNeMapper`: 网元数据访问
- `TopoPositionMapper`: 坐标数据访问
- `TopoMergeGroupMapper`: 合并组数据访问
- `TopoConfigMapper`: 配置数据访问
- `TopoAlarmStatsMapper`: 告警统计数据访问

### 4. Kafka 消费者
- `EamChangeConsumer`: 监听EAM变更事件

### 5. 配置
- `application.yml`: 主配置文件
- `GlobalExceptionHandler`: 全局异常处理

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 21 | Java 21 |
| Spring Boot | 3.2.0 | 基础框架 |
| MyBatis | 3.0.3 | ORM框架 |
| GaussDB | - | 数据库 (PostgreSQL驱动) |
| Kafka | - | 消息队列 |
| Lombok | 1.18.30 | 简化代码 |
| MapStruct | 1.5.5 | 对象映射 |
| Hutool | 5.8.24 | 工具类库 |

## API 端点

### 子网管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /subnet | 创建子网 |
| PUT | /subnet/{dn} | 更新子网 |
| DELETE | /subnet/{dn} | 删除子网 |
| GET | /subnet/{dn} | 获取子网详情 |
| GET | /subnet/tree | 获取子网树 |
| POST | /subnet/batch-move | 批量移入设备 |

### 拓扑视图
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /topo/view/{subnetDn} | 获取拓扑视图 |
| POST | /topo/position | 保存坐标 |
| POST | /topo/auto-layout/{subnetDn} | 自动布局 |

### 合并配置
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /merge/config | 获取合并配置 |
| PUT | /merge/config | 更新合并配置 |
| POST | /merge/execute | 执行合并 |
| POST | /merge/disable | 取消合并 |

## 启动方式

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/phytopo-1.0.0-SNAPSHOT.jar

# 或使用 Maven
mvn spring-boot:run
```

## 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| server.port | 8080 | 服务端口 |
| spring.datasource.url | localhost:5432/topology | 数据库连接 |
| spring.kafka.bootstrap-servers | localhost:9092 | Kafka地址 |
| topology.merge.enabled | false | 是否开启合并 |
| topology.merge.threshold | 20 | 合并阈值 |
| eam.base-url | localhost:8081 | EAM服务地址 |
| cie.base-url | localhost:8082 | CIE服务地址 |
