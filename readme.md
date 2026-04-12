# TopoConstruct - 物理拓扑管理服务

可视化物理网络拓扑管理平台，支持多层子网层级、网元管理、拓扑画布编辑和告警上卷展示。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 3.2 / MyBatis / MySQL 8.0 / Java 21 |
| 前端 | React 19 / TypeScript / ReactFlow / Ant Design 6 / Zustand |
| 构建 | Maven / Vite |

## 功能概览

### 子网与网元管理

- 多层级子网树形结构，支持无限层级嵌套
- 网元（NE）管理：防火墙、交换机、服务器、存储、网关、机框、机架等类型
- 树形导航，单击展开子网视图，双击进入子网下钻
- 子网/网元的创建、编辑、删除（右键菜单 + 弹窗）

### 拓扑画布

- 基于 ReactFlow 的交互式拓扑图
- 节点拖拽、画布缩放、小地图导航
- 坐标自动保存，进入子网自动加载历史布局
- 未放置元素自动网格排列
- 同类型合并组显示（可配置阈值）

### 告警上卷展示

- 子网告警聚合：递归收集所有子孙网元的告警，向上逐层汇聚
- 画布横幅：进入子网时顶部显示设备统计（NE数/子网数/在线离线）和分级告警汇总
- 节点角标：子网节点和网元节点右上角显示红色告警总数角标
- 详情面板：单击节点弹出 Drawer，展示 2×2 分级告警卡片（严重/主要/一般/警告）
- 树节点角标：树中每个节点旁显示子树告警总数

### 其他

- 拓扑视图 Toolbar：自动布局、刷新、全屏
- 右键上下文菜单
- 网元详情卡片（DN、IP、位置、维护人、告警状态）
- 子网详情面板（网元统计、在线/离线、聚合告警）

## 项目结构

```
phytopo/
├── src/main/java/com/topology/phytopo/
│   ├── controller/          # REST API
│   ├── service/             # 业务逻辑
│   ├── mapper/              # MyBatis Mapper
│   ├── entity/              # 数据实体
│   ├── dto/                 # 请求/响应 DTO
│   ├── config/              # 全局配置
│   ├── kafka/               # Kafka 消费者
│   └── common/              # 通用工具
├── src/main/resources/
│   ├── mapper/              # MyBatis XML
│   └── application.yml      # 应用配置
├── frontend/
│   ├── src/
│   │   ├── api/             # API 调用层
│   │   ├── components/
│   │   │   ├── canvas/      # 拓扑画布组件
│   │   │   ├── tree/        # 树形导航组件
│   │   │   ├── dialogs/     # 弹窗组件
│   │   │   └── layout/      # 布局组件
│   │   ├── stores/          # Zustand 状态管理
│   │   ├── types/           # TypeScript 类型定义
│   │   └── utils/           # 工具函数
│   └── package.json
├── doc/                     # 文档
└── pom.xml
```

## 快速开始

### 环境要求

- JDK 21+
- MySQL 8.0+
- Node.js 18+

### 数据库

创建数据库并执行建表脚本：

```sql
CREATE DATABASE topology DEFAULT CHARACTER SET utf8mb4;
```

导入 `doc/database/topo_schema.sql`。

### 后端

修改 `src/main/resources/application.yml` 中的数据库连接信息，然后：

```bash
mvn package -DskipTests
java -jar target/phytopo-1.0.0-SNAPSHOT.jar
```

后端启动在 `http://localhost:8081/api/topo`。

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`。

## API 概览

| 模块 | 接口 | 说明 |
|------|------|------|
| 拓扑视图 | `GET /topology/view?subnetDn=` | 获取子网拓扑（含聚合告警） |
| 子网 | `GET /subnet/tree` | 获取完整子网树 |
| 子网 | `GET /subnet/detail?dn=` | 子网详情（含告警统计） |
| 子网 | `POST /subnet/create` | 创建子网 |
| 网元 | `GET /ne/detail?dn=` | 网元详情（含告警统计） |
| 网元 | `POST /ne/create` | 创建网元 |
| 坐标 | `POST /topology/position/save` | 保存拓扑坐标 |
| 坐标 | `POST /topology/auto-layout` | 自动布局 |

## License

MIT
