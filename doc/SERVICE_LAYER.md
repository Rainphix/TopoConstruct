# Service 层说明文档

## 已完善的服务类

### 1. TopologyService
**路径**: `com.topology.phytopo.service.TopologyService`

**功能**:
| 方法 | 说明 |
|------|------|
| `getView(String subnetDn)` | 获取拓扑视图，含子网、子元素、坐标 |
| `savePosition(PositionSaveRequest request)` | 批量保存坐标 |
| `autoLayout(String subnetDn)` | 自动布局（网格算法） |

### 2. MergeService
**路径**: `com.topology.phytopo.service.MergeService`

**功能**:
| 方法 | 说明 |
|------|------|
| `checkAndMerge(String parentDn)` | 检查并执行合并 |
| `processTypeMerge(...)` | 处理单个类型合并 |
| `createMergeGroup(...)` | 创建合并组子网 |
| `disableMerge()` | 取消所有合并 |
| `enableMerge()` | 开启合并（对现有数据） |
| `updateConfig(MergeConfigRequest request)` | 更新合并配置 |
| `getConfig()` | 获取当前配置 |

### 3. NeService
**路径**: `com.topology.phytopo.service.NeService`

**功能**:
| 方法 | 说明 |
|------|------|
| `getByDn(String dn)` | 根据DN获取网元 |
| `getDetail(String dn)` | 获取网元详情(含告警) |
| `batchUpdateParentDn(...)` | 批量更新父节点 |
| `delete(String dn)` | 删除网元 |

| `batchDelete(List<String> dnList)` | 批量删除网元 |

### 4. SubnetService
**路径**: `com.topology.phytopo.service.SubnetService`

**功能** =
| 方法 | 说明 |
|------|------|
| `create(SubnetCreateRequest request)` | 创建子网 |
| `update(String dn, SubnetUpdateRequest request)` | 更新子网 |
| `delete(String dn)` | 删除子网(含校验) |
| `getDetail(String dn)` | 获取子网详情 |
| `getTree(String rootDn)` | 获取子网树 |
| `getChildren(String parentDn)` | 获取子节点 |
| `batchMove(BatchMoveRequest request)` | 批量移入设备 |

### 5. SyncService
**路径**: `com.topology.phytopo.service.SyncService`

**功能** =
| 方法 | 说明 |
|------|------|
| `handleChange(EamChangeMessage message)` | 处理Kafka变更消息 |
| `handleCreate(...)` | 处理创建消息 |
| `handleUpdate(...)` | 处理更新消息 |
| `handleDelete(...)` | 处理删除消息 |
| `fullSync()` | 全量同步 |

### 6. ConfigService
**路径**: `com.topology.phytopo.service.ConfigService`

**功能** =
| 方法 | 说明 |
|------|------|
| `getByKey(String key)` | 获取配置 |
| `setConfig(String key, String value)` | 设置配置 |
| `updateMergeConfig(...)` | 更新合并配置 |

### 7. InitService
**路径**: `com.topology.phytopo.service.InitService`

**功能** =
| 方法 | 说明 |
|------|------|
| `onApplicationReady(...)` | 应用启动时执行初始化 |
| `doInit()` | 执行初始化同步 |
| `processPendingQueue()` | 处理待查询队列 |
| `batchInsert()` | 批量入库 |

---

## 新增文件

```
src/main/java/com/topology/phytopo/
├── service/
│   ├── TopologyService.java      # 拓扑视图服务 ✅
│   ├── MergeService.java          # 合并服务 ✅
│   ├── NeService.java            # 网元服务 ✅
│   ├── SubnetService.java        # 子网服务 (已更新) ✅
│   ├── SyncService.java          # 同步服务 ✅
│   ├── ConfigService.java        # 配置服务 ✅
│   └── InitService.java          # 初始化服务 ✅
```

---

下一步需要我做什么？
1. **EAM/CIE 客户端** - 外部服务调用封装
2. **单元测试** - 编写测试用例
3. **Docker 部署** - 容器化配置
4. **前端接口对接** - API 对接文档
