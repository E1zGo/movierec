# MovieRec · 电影推荐后端

基于 **Java 17、Spring Boot 3.2.0 和 MySQL** 的电影推荐项目后端，围绕电影发现与用户互动，提供搜索筛选、评分收藏、评论回复、推荐列表和 AI 客服等 REST 接口。

> 本仓库包含 Java 后端，不包含前端页面、数据库初始化 SQL 或电影数据集。运行前需要准备与实体映射相符的数据库表和业务数据。

## 功能概览

| 模块 | 已实现的能力 |
| --- | --- |
| 电影发现 | 分页电影列表、详情、热门电影、按类型浏览、类型与年代筛选 |
| 搜索 | 片名／导演／演员／类型检索，完整与轻量搜索结果，搜索历史、热门词与词云数据 |
| 用户互动 | 评分与修改评分、收藏与取消收藏、分享计数、评论回复与点赞、举报 |
| 用户资料 | 资料查询与修改、头像上传、密码修改、收藏列表 |
| 推荐列表 | 读取用户推荐记录、最新推荐、同类型电影推荐、无记录时返回热门电影 |
| 使用帮助 | 常见问题、DeepSeek 客服问答、外部调用失败时返回预设回复 |

### 推荐逻辑

- **用户推荐**：读取 `RecommendationLog` 中已有的数据，按推荐分数、推荐时间排序，并按电影 ID 去重。
- **最新推荐**：按推荐时间读取记录，再转换为电影信息。
- **热门兜底**：无推荐记录时返回热门电影；当前热门排序依据为平均评分和评分人数，且要求评分人数大于零。
- **相关推荐**：寻找与当前电影有共同类型的影片，排除当前电影，再按评分相关字段排序。

当前代码未包含推荐模型训练或推荐分数生成流程。DeepSeek 用于客服问答，与推荐记录读取是独立功能。推荐日志先限制查询数量、后去重，因此返回的电影数可能小于请求的 `limit`。

## 技术栈

| 技术 | 用途 |
| --- | --- |
| Java 17 | 开发与运行环境 |
| Spring Boot 3.2.0 / Spring Web | 应用启动与 HTTP 接口 |
| Spring Data JPA / Hibernate | 实体映射与数据访问 |
| MySQL / MySQL Connector/J | 关系型数据存储与连接 |
| Lombok | 减少实体和 DTO 样板代码 |
| Maven Wrapper | 构建与依赖管理，当前分发版本为 Maven 3.9.11 |
| RestTemplate / Jackson | 外部 AI 请求与 JSON 处理 |

依赖与版本以 [pom.xml](pom.xml) 为准。

## 项目结构

```text
movierec/
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/wrapper/
├── src/main/java/com/example/movierec/
│   ├── MovieRecommendationApplication.java
│   ├── config/          # 跨域和上传文件访问配置
│   ├── controller/      # HTTP 接口
│   ├── service/         # 业务逻辑
│   ├── repository/      # JPA 数据访问
│   ├── entity/          # 数据库实体
│   ├── dto/             # 请求与响应对象
│   └── exception/       # 异常类型
├── src/main/resources/
│   └── application.properties
└── src/test/java/com/example/movierec/
    └── MovierecApplicationTests.java
```

请求主要按照 `Controller → Service → Repository → MySQL` 的路径处理。电影与演员、类型存在多对多关联，评分、收藏、评论和推荐日志分别使用独立实体记录。

## 本地运行

### 1. 准备环境

- 安装 JDK 17，确认 `java -version` 指向正确版本，并配置 `JAVA_HOME`。
- 准备可连接的 MySQL 数据库与具有相应权限的数据库账号。
- 首次使用 Maven Wrapper 时需要联网下载 Maven 和项目依赖。
- 仅使用在线 AI 客服时需要有效的 DeepSeek API Key；其他电影功能不依赖 AI 服务。

```bash
git clone https://github.com/E1zGo/movierec.git
cd movierec
```

### 2. 准备数据库

项目默认连接本机 `3306` 端口的 `movie` 数据库，且默认 `spring.jpa.hibernate.ddl-auto=none`，**不会自动建表**。

请先准备数据库，并根据 [entity 目录](src/main/java/com/example/movierec/entity) 中的表名、字段与关联关系建立表结构，再导入电影、类型、演员等基础数据。涉及用户的评分、收藏或资料接口，还需要存在相应用户记录。

仓库暂未提供建表脚本、迁移文件或数据导入工具。只有一个空数据库不能完成正常业务验证；没有电影数据时列表也不会出现演示内容。

### 3. 配置本地参数

在仓库**外部**新建同级目录 `movierec-local`，其中创建 `application.properties`：

```text
工作目录/
├── movierec/                         # 本仓库
└── movierec-local/
    └── application.properties       # 自己的本地配置
```

配置示例：

```properties
server.port=8080

# 仅用于本地开发的连接示例；请替换为自己的数据库账号
spring.datasource.url=jdbc:mysql://localhost:3306/movie?allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=YOUR_DATABASE_USER
spring.datasource.password=YOUR_DATABASE_PASSWORD
spring.jpa.hibernate.ddl-auto=none

# 使用当前电脑上可写的绝对路径，Windows 建议使用正斜杠
file.upload-dir=C:/movierec-data/avatars

# 暂不使用在线客服时保留为空；不要填写或复用仓库中的凭据
deepseek.api.key=
deepseek.api.url=https://api.deepseek.com/v1/chat/completions
```

Linux / macOS 下请将 `file.upload-dir` 改为实际可写目录，例如 `/home/your-user/movierec-data/avatars`。上传后的文件通过 `/uploads/**` 路径访问。

启动时显式加载这份外部配置，覆盖仓库中的开发环境设置。不要把真实密码和 API Key 提交到 Git；部署时也可以通过环境变量覆盖，例如 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`FILE_UPLOAD_DIR`、`DEEPSEEK_API_KEY`。

### 4. 启动服务

在 `movierec` 仓库根目录执行。

**Windows PowerShell：**

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.config.additional-location=file:../movierec-local/"
```

**Linux / macOS：**

```bash
sh ./mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.config.additional-location=file:../movierec-local/"
```

默认服务地址为 `http://localhost:8080`。这是后端 API 服务，访问根路径不代表会出现网站首页。

数据库表和数据准备完成后，可在浏览器访问：

```text
http://localhost:8080/api/movies?page=0&size=20
http://localhost:8080/api/movies/hot?limit=10
```

### 5. 前端联调

当前仓库的跨域配置分散在 [CorsConfig.java](src/main/java/com/example/movierec/config/CorsConfig.java) 和控制器上的 `@CrossOrigin`，包含 `http://localhost:8081`、`http://localhost:8082` 等开发地址。

接入自己的前端时，请统一这些位置的允许来源。仅修改 `application.properties` 中的 `spring.web.cors.*` 字段，不能替代上述代码配置。

## 常用 API

默认地址：`http://localhost:8080`。表中 `{movieId}`、`{userId}` 需要替换成数据库中实际存在的 ID。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/movies` | 电影分页列表；`page=0`、`size=20` |
| GET | `/api/movies/{movieId}` | 电影详情 |
| GET | `/api/movies/hot` | 热门电影；默认 `limit=10` |
| GET | `/api/movies/search` | 完整搜索；`query` 或 `keyword`，默认 `size=20` |
| GET | `/api/movies/search-lite` | 轻量搜索；`query` 或 `keyword`，默认 `size=12` |
| GET | `/api/movies/filter` | `genres` 为逗号分隔类型，`year` 可传 `2010s`；默认 `page=0`、`size=100` |
| GET | `/api/movies/genre/{genreName}` | 按类型分页查询 |
| POST | `/api/movies/{movieId}/rating` | 提交或更新评分；JSON 包含 `userId`、`score` |
| POST | `/api/movies/{movieId}/favorite` | 切换收藏状态；JSON 包含 `userId` |
| GET | `/api/movies/{movieId}/comments` | 获取电影评论 |
| GET | `/api/movies/{movieId}/related` | 同类型相关推荐 |
| GET | `/api/recommendations/user/{userId}` | 用户推荐；默认 `limit=25` |
| GET | `/api/recommendations/latest/{userId}` | 最新推荐；默认 `limit=10` |
| GET | `/api/search/hot-words` | 热门搜索词 |
| GET / POST / DELETE | `/api/search/history/{userId}` | 获取、保存、清空搜索历史；POST JSON 包含 `query` |
| GET / PUT | `/api/users/{userId}/profile` | 查询、更新用户资料 |
| GET | `/api/users/{userId}/favorites` | 用户收藏列表 |
| POST | `/api/users/{userId}/avatar` | 上传头像；`multipart/form-data`，文件字段为 `avatar` |
| GET | `/api/support/faqs` | 常见问题 |
| POST | `/api/support/ai-chat` | 客服问答；JSON 包含 `query` |

完整路由和请求字段请查看 [controller 目录](src/main/java/com/example/movierec/controller) 与 [dto 目录](src/main/java/com/example/movierec/dto)。

### 请求示例

以下为 Bash / 常规 curl 示例：

```bash
curl "http://localhost:8080/api/movies?page=0&size=20"

curl --get "http://localhost:8080/api/movies/search-lite" \
  --data-urlencode "query=科幻" \
  --data "page=0" \
  --data "size=12"

curl -X POST "http://localhost:8080/api/support/ai-chat" \
  -H "Content-Type: application/json" \
  -d '{"query":"如何收藏电影？"}'
```

配置有效的 AI Key 时，最后一个请求会调用外部 DeepSeek 服务；未配置或调用失败时返回预设回复，并标注离线模式。

### 响应格式

接口主要使用 [ApiResponse](src/main/java/com/example/movierec/dto/ApiResponse.java) 包装数据：

```json
{
  "success": true,
  "message": "操作成功",
  "data": [],
  "code": 200
}
```

这是返回列表的结构示例，实际 `data` 随接口变化：分页接口通常返回分页对象，而筛选接口返回当前页数组。页码从 **0** 开始。

响应体中的 `code` 是业务字段，不一定等于 HTTP 状态码；前端需要同时检查 HTTP 状态、`success` 与 `message`。

## 构建与测试

Windows 使用 `.\mvnw.cmd`，Linux / macOS 使用 `sh ./mvnw`。

```powershell
# 编译并打包，跳过测试执行
.\mvnw.cmd clean package -DskipTests

# 加载外部配置并执行测试
.\mvnw.cmd test "-Dspring.config.additional-location=file:../movierec-local/"

# 在仓库根目录运行打包产物
java -jar target/movie-recommendation-0.0.1-SNAPSHOT.jar --spring.config.additional-location=file:../movierec-local/
```

目前测试目录包含一个 `@SpringBootTest` 上下文加载测试，需要可用的数据库连接与配置，尚未覆盖各业务流程。跳过测试打包成功也不代表数据库、接口或推荐效果已经验证。

## 常见问题

- **启动时数据库连接失败**：检查 MySQL 是否运行，数据库是否存在，账号权限、地址和端口是否正确，并确认外部配置目录路径正确。
- **查询时报表不存在**：默认 `ddl-auto=none` 不会建表，需要先准备与实体映射匹配的表结构。
- **电影或推荐列表为空**：检查电影基础数据与推荐日志；热门兜底也依赖已有评分数据。
- **头像上传或访问失败**：检查 `file.upload-dir` 是否为当前系统的可写绝对路径，前端访问图片时是否指向后端地址；当前配置的单文件和单请求大小上限均为 10 MB。
- **浏览器跨域错误**：统一控制器和 `CorsConfig` 中的前端来源地址。
- **AI 客服显示离线模式**：检查本地 Key、网络连接和服务地址；预设回复是失败兜底，不代表在线模型调用成功。

## 后续完善

- 补充数据库迁移、示例数据和自动化初始化流程。
- 明确推荐日志的生成、更新与效果评估流程，处理去重后数量不足的情况。
- 完善身份认证与权限校验、密码存储和上传文件校验。当前退出接口仅返回确认结果，不能视为完整的认证体系。
- 统一参数验证、异常与分页响应，优化批量查询和并发评分统计。
- 为外部 AI 请求补充超时控制，完善核心业务测试。

---

由 [E1zGo](https://github.com/E1zGo) 维护。
