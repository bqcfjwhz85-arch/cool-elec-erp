# Cool-Admin-Java 后端项目规则文档

## 1. 项目结构

项目采用模块化组织方式，结构清晰明了：

```
main
├─ java
│  ├─ com.cool
│  ├─ core             核心包
│  │  ├─ annotation
│  │  └─ ...
│  ├─ modules          模块包
│  │  ├─ base          基础模块（系统用户、菜单、资源、角色）
│  │  │  ├─ controller
│  │  │  ├─ entity
│  │  │  ├─ mapper
│  │  │  └─ service
│  │  ├─ dict          字段模块
│  │  ├─ plugin        插件模块
│  │  ├─ recycle       数据回收站模块
│  │  ├─ space         文件管理模块
│  │  ├─ task          定时任务模块
│  │  └─ user          用户管理模块(c端用户)
│  └─ CoolApplication
└─ resources           资源文件
   ├─ cool
   │  └─ data
   │     └─ db         初始化数据json文件
   └─ mapper           mapper xml 文件
```

**规范**：
- 核心功能放在core包中
- 业务代码放在modules对应模块下
- 新增功能模块时，按照controller、entity、mapper、service的结构创建

## 2. 开发方式

### 2.1 AI全自动开发

零代码开发模式，通过微调大模型学习框架特有写法，实现从API接口到前端页面的一键生成。

### 2.2 半自动开发

三步完成开发：
1. 编写实体类，重启生成表
2. 运行代码生成器，生成Service、Controller等
3. 自动生成前端页面

**代码生成示例**：
```java
public class CoolCodeGeneratorTest {
    public static void main(String[] args) {
        CodeGenerator codeGenerator = new CodeGenerator();
        codeGenerator.init();

        CodeModel codeModel = new CodeModel();
        codeModel.setType(CodeTypeEnum.ADMIN);
        codeModel.setName("测试CURD");
        codeModel.setModule("demo");
        codeModel.setEntity(DemoEntity.class);

        codeGenerator.controller(codeModel);
        codeGenerator.mapper(codeModel);
        codeGenerator.service(codeModel);
    }
}
```

## 3. 控制器（Controller）规范

### 3.1 基础规范

- 所有Controller必须继承自`BaseController`
- 使用`@CoolRestController`注解替代`@RestController`
- 通过`@Tag`注解添加Swagger文档描述
- 配置需要的API方法集合

### 3.2 示例

```java
@Tag(name = "测试CURD", description = "测试CURD")
@CoolRestController(value = "/cool", api = {"add", "delete", "update", "page", "list", "info"})
public class AdminDemoInfoController extends BaseController<DemoService, DemoEntity> {
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 可以在这边实现列表数据排序规则,查询条件过滤，返回哪些字段等
    }
}
```

### 3.3 查询配置

- 使用`setPageOption`和`setListOption`配置查询
- 支持字段匹配、模糊查询、自定义查询等
- 支持连表查询和自定义查询模式

**查询配置示例**：
```java
@Override
protected void init(HttpServletRequest request, JSONObject requestParams) {
    setPageOption(createOp()
        .fieldEq("status")
        .keyWordLikeFields("name", "phone")
        .select("name", "phone", "age")
        .queryWrapper(QueryWrapper.create()));
    
    setListOption(createOp());
}
```

## 4. 服务层（Service）规范

### 4.1 基础规范

- 业务逻辑必须封装在服务层
- 继承`BaseService`获取基础CRUD功能
- 重写`modifyBefore`和`modifyAfter`方法处理数据修改前后的逻辑

### 4.2 示例

```java
// 修改前处理
@Override
public void modifyBefore(JSONObject requestParams, T t, ModifyEnum type) {
    // 数据修改前的处理逻辑
}

// 修改后处理
@Override
public void modifyAfter(JSONObject requestParams, T t, ModifyEnum type) {
    // 数据修改后的处理逻辑
}
```

## 5. 数据库规范

### 5.1 技术选型

- 使用MyBatis-Flex作为ORM框架
- 支持多种数据库（MySQL、Oracle、PostgreSQL等）

### 5.2 核心特性

- 支持关联查询、多表查询、多主键
- 支持逻辑删除、乐观锁更新、数据填充、数据脱敏
- 支持多租户、动态表名、动态Schema
- 高性能设计，无SQL解析，无MyBatis拦截器

### 5.3 实体类规范

- 使用`@Table`注解定义表信息
- 使用`@Column`注解定义字段信息
- 使用`@RelationOneToMany`等注解定义关联关系

**关联关系示例**：
```java
@Table(value = "order_info", comment = "订单信息")
public class OrderInfoEntity extends BaseEntity<OrderInfoEntity> {
    @RelationOneToMany(selfField = "id", targetField = "orderId")
    private List<OrderGoodsEntity> goodsList;
}
```

## 6. 异常处理规范

### 6.1 统一异常捕获

框架提供了统一的异常处理器`CoolExceptionHandler`，处理各类异常并返回统一格式。

### 6.2 业务校验

使用`CoolPreconditions.check`方法进行业务校验，简化代码：

```java
public Long add(JSONObject requestParams, BaseSysUserEntity entity) {
    BaseSysUserEntity check = getOne(
            Wrappers.<BaseSysUserEntity>lambdaQuery().eq(BaseSysUserEntity::getUsername, entity.getUsername()));
    CoolPreconditions.check(check != null, "用户名已存在");
    entity.setPassword(MD5.create().digestHex(entity.getPassword()));
    super.add(requestParams, entity);
    return entity.getId();
}
```

## 7. 安全与权限

### 7.1 技术选型

- 使用Spring Security与JWT实现权限控制
- 支持token过期和刷新机制

### 7.2 配置

```yaml
cool:
  # token 相关配置
  token:
    # 过期时间 单位：秒 半小时
    expire: 1800
    # 刷新token过期时间 单位：秒 7天
    refreshExpire: 604800
```

### 7.3 权限校验流程

1. 系统启动时加载所有权限设置
2. 用户登录时查询并缓存用户权限
3. 请求时验证用户是否具有对应权限

## 8. 缓存规范

### 8.1 缓存类型

框架支持两种缓存实现：
- Caffeine：内存缓存，无需额外安装
- Redis：分布式缓存，需安装Redis服务

### 8.2 配置

```yaml
spring:
  # caffeine 缓存
  cache:
    type: caffeine
    file: cache

  #redis 缓存
#  cache:
#    type: redis
#  data:
#    redis:
#      host: 127.0.0.1
#      port: 6379
#      database: 0
#      password:
```

### 8.3 使用方式

- Spring Cache注解方式：`@Cacheable`、`@CacheEvict`等
- 框架工具类方式：`CoolCache`

**CoolCache使用示例**：
```java
@Service
public class DemoCacheServiceImpl implements DemoCacheService {
    @Resource
    CoolCache coolCache;

    @Override
    public Object test(String id) {
        coolCache.set("a", 1);
        return coolCache.get("a", Integer.class);
    }
}
```

## 9. 国际化多语言

### 9.1 实现方式

- 基于大模型翻译的多语言支持
- 自动扫描并翻译菜单、字典、异常消息
- 支持40+种语言

### 9.2 使用方法

1. 引入国际化多语言插件
2. 自动生成翻译文件到`assets/i18n`目录
3. 无需修改原有代码

## 10. 部署规范

### 10.1 部署流程

1. 拉取前后端代码
2. 本地配置数据库并初始化
3. 配置生产环境数据库连接
4. 服务器环境准备（安装必要软件）
5. 上传代码并安装依赖
6. 启动服务并配置Nginx反向代理

### 10.2 服务运行

推荐使用PM2管理后端服务：

```bash
# 启动服务
pm install pm2 -g
pm start

# 集群方式启动
pm2 start ./bootstrap.js -i max --name cool-admin
```

### 10.3 Nginx配置

配置反向代理，将API请求转发到后端服务。

## 11. 其他规范

### 11.1 代码生成
- 使用框架提供的代码生成器快速创建基础代码
- 遵循框架的命名规范和代码风格

### 11.2 插件开发
- 遵循框架的插件开发规范
- 使用插件机制扩展功能

### 11.3 性能优化
- 使用缓存减少数据库查询
- 合理设计数据结构和索引
- 避免N+1查询问题

以上规则涵盖了Cool-Admin-Java后端项目的核心开发规范，遵循这些规范可以保证项目的可维护性、可扩展性和代码质量。
        