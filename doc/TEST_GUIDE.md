# 单元测试指南

## 测试概述

本项目使用 JUnit 5 + Mockito + Spring Boot Test 进行单元测试和集成测试。

## 测试结构

```
src/test/java/com/topology/phytopo/
├── service/
│   ├── SubnetServiceTest.java      # 子网服务测试
│   ├── MergeServiceTest.java      # 合并服务测试
│   └── TopologyServiceTest.java    # 拓扑服务测试
├── controller/
│   ├── SubnetControllerTest.java  # 子网控制器测试
│   └── MergeControllerTest.java  # 合并控制器测试
└── kafka/
    └── EamChangeConsumerTest.java  # Kafka消费者测试
```

## 运行测试

### 运行所有测试
```bash
mvn test
```

### 运行单个测试类
```bash
mvn test -Dtest=SubnetServiceTest
```

### 运行特定测试方法
```bash
mvn test -Dtest=SubnetServiceTest#testCreateSubnet_Success
```

### 跳过测试打包
```bash
mvn package -DskipTests
```

## 测试覆盖率

### 生成覆盖率报告
```bash
mvn test jacoco:report
```

### 覆盖率目标
| 模块 | 目标覆盖率 |
|------|----------|
| Service | ≥ 80% |
| Controller | ≥ 70% |
| Mapper | ≥ 60% |

## 测试用例说明

### 1. SubnetServiceTest

| 测试方法 | 测试场景 |
|---------|---------|
| testCreateSubnet_Success | 创建子网成功 |
| testUpdateSubnet_Success | 更新子网成功 |
| testUpdateSubnet_NotFound | 更新不存在的子网 |
| testDeleteSubnet_Success | 删除子网成功 |
| testDeleteSubnet_HasDevices | 删除有设备的子网 |
| testDeleteSubnet_HasSubnets | 删除有子网的子网 |
| testGetDetail_Success | 获取子网详情 |
| testGetTree_Root | 获取根子网树 |
| testGetTree_WithRootDn | 获取指定根的树 |
| testBatchMove_Success | 批量移入设备 |

### 2. MergeServiceTest

| 测试方法 | 测试场景 |
|---------|---------|
| testCheckAndMerge_NotEnabled | 合并功能未开启 |
| testCheckAndMerge_BelowThreshold | 数量不满足阈值 |
| testCheckAndMerge_AboveThreshold | 满足阈值创建合并组 |
| testDisableMerge_Success | 取消合并成功 |
| testDisableMerge_NoGroups | 无合并组可取消 |
| testUpdateConfig_EnableMerge | 从关闭切换到开启 |
| testUpdateConfig_DisableMerge | 从开启切换到关闭 |
| testGetConfig | 获取配置 |

### 3. TopologyServiceTest

| 测试方法 | 测试场景 |
|---------|---------|
| testGetView_Success | 获取拓扑视图 |
| testGetView_WithCoordinates | 获取含坐标的视图 |
| testSavePosition_Success | 保存坐标成功 |
| testSavePosition_UpdateExisting | 更新已有坐标 |
| testAutoLayout_Success | 自动布局成功 |
| testAutoLayout_EmptyView | 空视图自动布局 |

### 4. Controller 测试

使用 `@WebMvcTest` 进行控制器层测试，自动配置 MockMvc。

### 5. Kafka 消费者测试

测试 Kafka 消息消费和异常处理。

## Mock 配置

### Service 层 Mock
```java
@Mock
private TopoSubnetMapper subnetMapper;

@InjectMocks
private SubnetService subnetService;
```

### Controller 层 Mock
```java
@WebMvcTest(SubnetController.class)
class SubnetControllerTest {
    @MockitoBean
    private SubnetService subnetService;
}
```

## 测试数据构建

### 创建测试子网
```java
private TopoSubnet createTestSubnet() {
    TopoSubnet subnet = new TopoSubnet();
    subnet.setDn("DN=TestSubnet");
    subnet.setName("TestSubnet");
    subnet.setLayer(0);
    subnet.setIsMergeGroup(false);
    return subnet;
}
```

### 创建测试网元列表
```java
private List<TopoNe> createNeList(int count, String neType) {
    return IntStream.range(0, count)
        .mapToObj(i -> {
            TopoNe ne = new TopoNe();
            ne.setDn("DN=NE_" + i);
            ne.setNeType(neType);
            return ne;
        })
        .toList();
}
```

## 最佳实践

1. **测试命名**: `test<MethodName>_<Scenario>`
2. **使用 @DisplayName**: 提供清晰的测试描述
3. **BeforeEach 初始化**: 每个测试前准备测试数据
4. **验证交互**: 使用 verify() 验证方法调用
5. **断言结果**: 使用 assertEquals, assertNotNull 等
6. **异常测试**: 使用 assertThrows
7. **隔离测试**: 每个 test 应该独立运行

## 常见问题

### Q: Mock 不生效?
确保使用 `@InjectMocks` 注入被测对象，`@Mock` 注入依赖。

### Q: Controller 测试 404?
检查 `@WebMvcTest` 指定的 Controller 类是否正确。

### Q: 测试数据库?
Service 层测试使用 Mock，Mapper 层测试使用 `@MybatisTest` (需要数据库)。
