
## Always response in Chinese.



# AGENTS.md - Talkkia Backend

本指南帮助智能编码代理理解构建/测试命令和代码约定。


## 构建命令

### 构建
```bash
mvn clean compile              # 编译项目
mvn clean package              # 构建JAR（运行测试）
mvn clean package -DskipTests  # 构建JAR（跳过测试）
java -jar target/talkkia-0.0.1-SNAPSHOT.jar  # 运行应用
```

### 测试
```bash
mvn test                                    # 运行所有测试
mvn test -Dtest=TalkkiaApplicationTests    # 运行单个测试类
mvn test -Dtest=ClassName#methodName        # 运行单个测试方法
mvn clean test                              # 清理后运行测试
```

### Docker
```bash
docker build -t talkkia-backend:latest .
docker compose -f /home/compose.yaml up -d talkkia
```

## 代码风格指南

### 命名约定
- **类名**: PascalCase (User, UserService, UserController)
- **接口名**: PascalCase (UserService, UserRepository)
- **方法名**: camelCase，动词开头 (signUp, verifyOTP, getUserById, createPost, updatePost, deletePost)
- **变量名**: camelCase (userId, mobile, emailAddress, fileName)
- **常量名**: UPPER_SNAKE_CASE (CLIENT_TOKEN, EXCLUDE_URL)
- **包名**: 全小写 (com.talkkia.api.controller)
- **REST端点**: 小写+斜杠 (/api/user/signup, /api/post/create)

### 导入顺序
推荐顺序：标准Java库 → 第三方库 → Spring → 项目内部。避免使用通配符导入 (import java.util.*)

### 类型使用
- 实体字段和API参数使用包装类型 (Long, Integer, Boolean)
- 局部变量和内部逻辑使用基本类型 (long, int, boolean)
- 泛型集合使用完整类型声明 (Map<String, Object>, List<User>)

### Entity和数据库注解
- Neo4j节点使用 `@Node`
- 主键使用 `@Id @GeneratedValue`
- 关系使用 `@Relationship(type = "RELATIONSHIP_NAME")`
- 关系属性使用 `@RelationshipProperties`
- 使用Lombok: `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`
- 关系集合使用 `@ToString.Exclude`, `@EqualsAndHashCode.Exclude`

### Controller层
- 使用 `@RestController` 和 `@RequestMapping("/api/...")`
- 返回 `ResponseEntity<Object>` 包装响应
- 使用 `@RequestHeader` 获取headers: client-token, token, userid
- 使用 `@RequestBody` 接收JSON数据
- 使用 `@RequestParam` 和 `@RequestPart` 处理文件上传
- 响应格式统一:
```java
Map<String, Object> response = new HashMap<>();
response.put("success", true/false);
response.put("status", "ua01");  // 状态码: ua=用户auth, cr=联系人, se=系统错误等
response.put("message", "...");
response.put("data", ...);  // 可选
return ResponseEntity.status(HttpStatus.OK).body(response);
```

### Service层
- 接口定义在 `XxxService.java`，实现在 `XxxServiceImpl.java`
- 使用 `@Service` 注解
- 使用 `@Autowired` 注入依赖
- 方法命名约定:
  - 查询: getXxx, findXxx, checkXxx, existsXxx
  - 创建: createXxx, addXxx, signUp, register
  - 更新: updateXxx, editXxx
  - 删除: deleteXxx, removeXxx
  - 验证: verifyXxx, authenticateXxx, validateXxx
- 返回值:
  - Boolean: 验证、删除、添加操作
  - Map<String, Object>: 复杂响应（如验证OTP）
  - List<Xxx> / Xxx: 查询操作

### Repository层
- 继承 `Neo4jRepository<Entity, ID>`
- 命名查询: `existsByMobile`, `findByEmail`, `findOneById`
- 自定义Cypher查询使用 `@Query`:
```java
@Query("MATCH(:User {id:$userId1})-[c:CONTACT]-(:User {id:$userId2}) DELETE c")
void deleteContact(@Param("userId1") long userId1, @Param("userId2") long userId2);
```

### View/DTO层
- View接口: 用于投影返回特定字段 (UserView, PostView)
- DTO类: 数据传输对象，完整POJO (UserDTO, PostDTO)
- Model类: 业务模型 (NearbySearchFilterModel, SearchRequest)

### 错误处理
- 使用try-catch捕获异常
- 使用 `log.error` 记录错误（需要 `@Slf4j` 注解）
- 返回统一的错误响应:
```java
try {
    // 业务逻辑
} catch (Exception e) {
    log.error("Error in xxx", e);
    response.put("success", false);
    response.put("status", "se00");  // 通用系统错误码
    response.put("message", "System error!");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
}
```

### 注解使用
- Spring注解: `@Service`, `@Component`, `@Configuration`, `@RestController`
- 依赖注入: `@Autowired` 或构造函数注入
- 配置值注入: `@Value("${property.name}")`
- 可空参数: 使用 `org.springframework.lang.@Nullable`
- 日志: `@Slf4j` (Lombok)

### 日志
- 使用Slf4j: 添加 `@Slf4j` 注解
- 使用参数占位符: `log.info("User {} signed up with email: {}", userId, email);`
- 记录关键业务流程和错误

### 代码组织
- 遵循标准Maven/Gradle项目结构
- 清晰分层: controller → service → repository
- 简洁注释，仅记录非显而易见的逻辑
- 发布前移除测试代码和注释代码

### API认证
所有受保护的API需要在请求头包含:
- `client-token`: 应用验证token (常量CLIENT_TOKEN)
- `token`: 用户认证token (SHA256生成)
- `userid`: 用户ID (Long)

### 配置文件
- `application.properties`: 生产环境配置
- `application-dev.properties`: 开发环境配置（SSL禁用）
- 使用环境变量: `${REDIS_HOST:localhost}`

### 文件上传
- 最大文件大小: 50MB
- 最大请求大小: 100MB
- 使用 `MultipartFile` 处理上传
- 存储路径: `upload-dir/`

### WebSocket
- STOMP端点: `/websocket`
- 前缀: `/app` (客户端发送), `/topic` (广播), `/queue` (点对点), `/user` (会话消息)
- 消息大小限制: 文本512KB，二进制1000KB

### 测试
- 使用JUnit 5 (`import org.junit.jupiter.api.Test`)
- 使用 `@SpringBootTest` 注解
- 默认web环境: `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
- 当前测试覆盖率低，建议为新功能添加测试

### Git提交
- 简洁的提交信息: "fix: email using production (gmail)"
- 使用"fix:", "add:", "update:", "refactor:"等前缀
- 避免提交敏感信息 (.env, credentials.json, talkkia.p12)

### 环境变量
生产环境使用以下环境变量:
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
