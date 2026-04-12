# 项目运行指南

## 壋需条件

- **JDK 21+**
- **Maven 3.9+**
- **GaussDB / PostgreSQL** (可选，用于完整功能)
- **Kafka** (可选，用于 EAM 事件监听)

## 安装 Maven

### Windows
1. 下载 Maven: https://maven.apache.org/download.cgi
2. 解压到指定目录 (如 `D:\Programming_Enviroment\maven`)
3. 添加环境变量:
   ```
   MAVEN_HOME=D:\Programming_Enviroment\maven\apache-maven-3.9.6
   Path=%MAVEN_HOME%\bin
   ```

### 验证安装
```bash
mvn -version
```

## 运行步骤

### 1. 编译项目
```bash
cd D:\Programming_Project\claude\phytopo
mvn clean compile
```

### 2. 运行测试
```bash
mvn test
```

### 3. 打包
```bash
mvn package -DskipTests
```

### 4. 运行服务
```bash
java -jar target/phytopo-1.0.0-SNAPSHOT.jar
```

或者使用 Maven 插件:
```bash
mvn spring-boot:run
```

## 配置说明

### 数据库配置
在 `application.yml` 中配置数据库连接:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/topology
    username: topology
    password: topology123
```

### Kafka 配置
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### 合并配置
```yaml
topology:
  merge:
    enabled: false
    threshold: 20
    mode: THRESHOLD
```

## 访问服务

- **API 地址**: http://localhost:8080/api/topo
- **Swagger UI**: http://localhost:8080/api/topo/swagger-ui.html
- **健康检查**: http://localhost:8080/api/topo/actuator/health

## 快速测试 (无数据库)

如果暂时没有数据库，可以禁用数据库自动配置:
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

或使用 H2 内存数据库进行测试
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
```

## 常见问题

### Q: 端口被占用
修改 `application.yml` 中的端口
```yaml
server:
  port: 8081
```

### Q: 数据库连接失败
1. 检查数据库是否启动
2. 检查用户名密码是否正确
3. 检查数据库是否已创建

### Q: Kafka 连接失败
1. 检查 Kafka 是否启动
2. 检查 bootstrap-servers 配置
