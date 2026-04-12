# 项目运行状态

## 当前状态

项目代码框架已基本完成，但需要安装 Maven 才能编译运行。

## 环境检查

| 环境 | 状态 | 说明 |
|------|------|------|
| JDK 21 | ✅ 已安装 | D:\Programming_Enviroment\jdk21\jdk-21.0.9 |
| Maven | ❌ 未安装 | 需要安装 Maven 3.9+ |

## 安装 Maven

### 方法1: 下载安装
1. 访问 https://maven.apache.org/download.cgi
2. 下载 `apache-maven-3.9.6-bin.zip`
3. 解压到 `D:\Programming_Enviroment\maven\apache-maven-3.9.6`
4. 添加环境变量:
   ```
   MAVEN_HOME=D:\Programming_Enviroment\maven\apache-maven-3.9.6
   Path添加: %MAVEN_HOME%\bin
   ```

### 方法2: 使用 Chocolatey (推荐)
```powershell
choco install maven
```

### 方法3: 使用 SDKMAN
```bash
sdk install maven 3.9.6
```

## 验证安装

```bash
mvn -version
```

预期输出:
```
Apache Maven 3.9.6
Maven home: D:\Programming_Enviroment\maven\apache-maven-3.9.6
Java version: 21.0.9
```

## 编译项目

```bash
cd D:\Programming_Project\claude\phytopo
mvn clean compile
```

## 运行项目

### 方式1: 使用 Maven 插件
```bash
mvn spring-boot:run
```

### 方式2: 打包后运行
```bash
mvn package -DskipTests
java -jar target/phytopo-1.0.0-SNAPSHOT.jar
```

## 项目文件统计

| 类型 | 数量 |
|------|------|
| Java 文件 | 50 |
| XML 文件 | 5 |
| 配置文件 | 2 |
| 文档文件 | 8 |

## 核心模块

```
com.topology.phytopo/
├── PhyTopoApplication.java     # 启动类
├── common/                      # 通用类
├── config/                      # 配置类
├── controller/                  # 控制器 (3个)
├── dto/                         # DTO (10个)
├── entity/                      # 实体 (7个)
├── kafka/                       # Kafka (2个)
├── mapper/                      # Mapper (6个)
└── service/                     # 服务 (7个)
```

## 待完成事项

1. **安装 Maven** - 必需
2. **配置数据库** - 可选 (先用 H2 内存数据库测试)
3. **配置 Kafka** - 可选 (可先禁用)

## 快速测试配置

如果暂时没有数据库和 Kafka，可以修改 `application.yml`:

```yaml
# 禁用数据库
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

# 禁用初始化
topology:
  init:
    enabled: false

# 禁用 Kafka
spring:
  kafka:
    enabled: false
```

## 下一步

1. 安装 Maven
2. 运行 `mvn clean compile` 检查编译错误
3. 修复编译错误 (如果有)
4. 运行 `mvn spring-boot:run` 启动服务
5. 访问 http://localhost:8080/api/topo/swagger-ui.html
