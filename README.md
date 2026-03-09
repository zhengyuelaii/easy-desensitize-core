

# Easy Desensitize Core

![Build Status](https://github.com/zhengyuelaii/easy-desensitize-core/actions/workflows/main.yml/badge.svg)
![Maven Central](https://img.shields.io/maven-central/v/io.github.zhengyuelaii/easy-desensitize-core.svg)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)

🚀 **轻量级、高性能、可扩展的 Java 数据脱敏核心引擎**

`easy-desensitize-core` 是一个专注于数据脱敏核心逻辑的工具库。它不仅支持基于注解的声明式脱敏，还提供了强大的编程式 API，能够完美处理**复杂嵌套对象**、**泛型容器**（如 `List<T>`, `Map<K,V>`）以及**动态结构**的数据脱敏。

> 对于 Spring Boot 用户，可以查看 [easy-desensitize-spring-boot-starter](https://github.com/zhengyuelaii/easy-desensitize-spring-boot-starter) 以获得自动集成支持。

## 🔧 环境要求
* JDK 8+
* 无第三方依赖

## ✨ 核心特性

- **⚡ 极致性能**：采用 **"局部缓存 + 全局缓存"** 的二级缓存架构，反射元数据解析损耗降低 90% 以上。
- **🔍 深度递归**：完美支持 `List`、`Set`、`Map` 及多层嵌套 Bean 的递归脱敏，自动识别并处理复杂对象图。
- **🛡️ 泛型友好**：通过运行时结构探测机制，支持 `Result<T>`、`Page<T>` 等泛型包装类的自动递归脱敏。
- **🧩 高度扩展**：提供 `MaskingDataResolver` 接口，支持 Lambda 表达式快速提取复杂对象（如分页器）中的待脱敏数据。
- **🛠️ 丰富工具**：内置 `Masker` 工具类，提供常用脱敏方法的极简调用。

## 📦 快速开始

> 完整代码示例见：[easy-desensitize-samples](https://github.com/zhengyuelaii/easy-desensitize-samples)

### 1. 引入依赖

```XML
<dependency>
   <groupId>io.github.zhengyuelaii</groupId>
   <artifactId>easy-desensitize-core</artifactId>
   <version>${latest.version}</version>
</dependency>
```

### 2. 基础用法

在实体类的字段上添加 `@MaskingField` 注解，并指定脱敏处理器。

```Java
public class User {
   @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
   private String name;
   @MaskingField(typeHandler = FixedMaskHandler.class)
   private String password;
   // 省略 Getter/Setter/ToString
}
```
执行脱敏
```java
public class QuickStart {
   public static void main(String[] args) {
      User user = new User();
      user.setName("张小凡");
      user.setPassword("123456");
      
      EasyDesensitize.mask(user);
      System.out.println(user); 
      // 输出：User [name=张*凡, password=******]
   }
}
```
### 3. 脱敏处理器

**EasyDesensitize**框架默认提供以下几种处理器：

* **DefaultMaskingHandler**：不做任何处理，直接返回原值
* **KeepFirstAndLastHandler**：仅保留首尾字符，中间字符用"*"填充
* **FixedMaskHandler**：将字段替换为定长字符"\*\*\*\*\*\*"

如果默认处理器无法满足需求，可通过实现 `MaskingHandler` 接口轻松扩展。

```java
/**
 * 手机号脱敏处理器示例
 */
public class MobileMaskingHandler implements MaskingHandler {
   @Override
   public String getMaskingValue(String value) {
      // 使用内置 Masker 工具类隐藏中间 4 位
      return Masker.hide(value, 3, 7);
   }
}
```

为User增加Mobile字段

```java
public class User {
   ...
   @MaskingField(typeHandler = MobileMaskingHandler.class)
   private String mobile;
   ...
}
```

执行脱敏

```java
public class QuickStart {
   public static void main(String[] args) {
      User user = new User();
      user.setName("张小凡");
      user.setMobile("13700001234");
      user.setPassword("123456");
      
      EasyDesensitize.mask(user);
      System.out.println(user);
      // 输出：User [name=张*凡, mobile=137****1234, password=******]
   }
}
```

> 推荐搭配Hutools 的 [DesensitizedUtil](https://doc.hutool.cn/pages/DesensitizedUtil)使用
------

## 🔧 进阶用法

### 1. 复杂类型与递归支持

* List脱敏

```java
public class ListTypeDesensitize {
   
   public static void main(String[] args) {
      User user = new User();
      user.setName("李小鹏");
      user.setMobile("13700001234");
      user.setPassword("123456");
      
      // List脱敏
      List<User> list = Collections.singletonList(user);
      EasyDesensitize.mask(list);
      System.out.println(list);
      // 输出：[User [name=李*鹏, mobile=137****1234, password=******]]
   }
}
```

* Map脱敏

```java
public class MapTypeDesensitize {
	
   public static void main(String[] args) {
      User user1 = new User("李小鹏", "13700001234", "123456");
      User user2 = new User("张三", "13888880000", "456789");
      
      // Map脱敏
      Map<String, Object> map = new HashMap<>();
      map.put("code", 200);
      map.put("data", user1);
      map.put("list", Collections.singleton(user2));
      
      EasyDesensitize.mask(map);
      System.out.println(map);
      // 输出：{code=200, data=User [name=李*鹏, mobile=137****1234, password=******], list=[User [name=张*, mobile=138****0000, password=******]]}
   }
}
```

* 树形数据脱敏

```java
public class TreeVO {
   private String id;
   @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
   private String name;
   private List<TreeVO> children;
   ...
}

public class TreeTypeDesensitize {

   public static void main(String[] args) {
      TreeVO root = new TreeVO();
      root.setId("1");
      root.setName("XXX有限公司");
      
      TreeVO t1 = new TreeVO();
      t1.setId("1001");
      t1.setName("行政部");
      
      TreeVO t2 = new TreeVO();
      t2.setId("1001");
      t2.setName("研发部");
      
      root.setChildren(Arrays.asList(t1, t2));
      EasyDesensitize.mask(root);
      System.out.println(root);
      // 输出：TreeVO [id=1, name=X*****司, children=[TreeVO [id=1001, name=行*部, children=null], TreeVO [id=1001, name=研*部, children=null]]]
   }
}
```

### 2. 编程式脱敏（无侵入）

适用于**无法修改源码**（如第三方 SDK 的类）或需要根据业务逻辑**动态改变规则**的场景。

```Java
public class TestNode {
   private String name;
   private String mobile;
   ...
}
// 编程式脱敏
public class ProgrammaDesensitize {
   public static void main(String[] args) {
      // 准备数据
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("name", "张三");
      
      TestNode node = new TestNode("王小华", "13700001234");
      data.put("node", node);
      
      // 定义脱敏处理器
      Map<String, MaskingHandler> handler = new HashMap<>();
      // 基于key自动匹配Map、Bean同名字段进行脱敏
      handler.put("name", new KeepFirstAndLastHandler());
      // 采用 Lambda 方式定义处理器
      handler.put("mobile", value -> Masker.hide(value, 3, 7));
      
      // 执行脱敏
      EasyDesensitize.mask(data, handler);
      System.out.println(data);
      // 输出：{name=张*, node=TestNode [name=王*华, mobile=137****1234]}
   }
}
```
> ⚠️ 注意：当 `handlerMap` 中存在与待脱敏对象同名的字段时，会优先使用 `handlerMap` 中定义的处理器

### 3. 处理复杂对象（Resolver）

对于 `Page<T>`、`ResultWrapper<T>` 等复杂包装对象，无需编写复杂的反射逻辑，使用 `MaskingDataResolver` 接口即可一键提取。

```Java
// 定义实体类
public class Page<T> {
   private Integer pageNum;
   private Integer pageSize;
   private List<T> data;
   ...
}
// 定义Resolver
public class PageDataResolver implements MaskingDataResolver<Page<?>> {

   @Override
      public Iterator<?> resolve(Page<?> source) {
      return source.getData().iterator();
   }

}
// 实现脱敏
public class PageWrapperDesensitize {
   public static void main(String[] args) {
      Page<User> page = new Page<>();
      page.setPageNum(1);
      page.setPageSize(10);
      
      User user = new User();
      user.setName("张小凡");
      user.setMobile("13700001234");
      user.setPassword("123456");
      page.setData(Collections.singletonList(user));
      
      // 执行脱敏
      EasyDesensitize.mask(page, new PageDataResolver());
      System.out.println(page);
      // 输出：Page [pageNum=1, pageSize=10, data=[User [name=张*凡, mobile=137****1234, password=******]]]
   }
}
```
* 或者也可以使用 Lambda 表达式定义如何提取数据
```java
EasyDesensitize.mask(page, p -> p.getData().iterator());
```

> **💡 性能提示**：虽然框架具备自动扫描结构的能力，但对于已知结构的复杂对象，通过 `Resolver` 显式指定数据路径可大幅减少反射扫描，提升处理性能。

### 4. 全局缓存控制

框架默认开启全局缓存以提升性能。在内存极其敏感或动态类加载场景下，可手动关闭：

```Java
// 第三个参数 false 表示关闭全局缓存，仅使用单次任务级缓存
EasyDesensitize.mask(data, null, handlerMap, false);

// 手动清理全局缓存（如有需要）
EasyDesensitize.clearCache();
```

------

## 🛠️ 工具类 Masker

`Masker` 提供了语义化的静态方法，无需记忆复杂的索引计算。

```Java
public class MaskerUsageSample {

   public static void main(String[] args) {
      System.out.println("========= 1. 基础脱敏 (指定索引) =========");
      String raw = "1234567890";
      // 脱敏索引 3 到 7 (即：4567)
      String basic = Masker.hide(raw, 3, 7);
      System.out.println("原字符串: " + raw);
      System.out.println("脱敏结果: " + basic); // 123****890
      
      System.out.println("\n========= 2. 自定义掩码字符 =========");
      // 使用 '#' 代替 '*'
      String customChar = Masker.hide(raw, "#", 3, 7);
      System.out.println("自定义掩码 (#): " + customChar); // 123####890
      
      System.out.println("\n========= 3. 常见业务场景模拟 =========");
      
      // 手机号脱敏示例 (保留前3后4)
      String phone = "13812345678";
      String maskedPhone = Masker.hide(phone, 3, phone.length() - 4);
      System.out.println("手机号脱敏: " + maskedPhone); // 138****5678
      
      // 姓名脱敏示例 (保留第1位)
      String name = "张无忌";
      String maskedName = Masker.hide(name, 1, name.length());
      System.out.println("姓名脱敏: " + maskedName); // 张**
   }
}
```
输出：
```
========= 1. 基础脱敏 (指定索引) =========
原字符串: 1234567890
脱敏结果: 123****890

========= 2. 自定义掩码字符 =========
自定义掩码 (#): 123####890

========= 3. 常见业务场景模拟 =========
手机号脱敏: 138****5678
姓名脱敏: 张**
```

------

## ⚡ 性能表现

基于 **JMH (Java Microbenchmark Harness)** 在真实业务模型下的压测数据。

### 测试环境
* **CPU**: Apple M2
* **JDK**: 11.0.16.1, OpenJDK 64-Bit Server VM
* **数据模型**: 包含 15+ 字段的复杂嵌套对象（包含 `List` 嵌套、手机号/邮箱/地址等 6 处脱敏计算）

### 测试结果
| 批量数据量         | 平均总耗时 (ms) | 单条处理耗时 (ns) | 性能评价 |
|:--------------| :--- |:------------| :--- |
| **100 条**     | 0.048 ms | **~480 ns** | 极其轻量 |
| **1,000 条**   | 0.447 ms | **~447 ns** | 极致稳定 |
| **10,000 条**  | 5.513 ms | **~551 ns** | 高效处理 |
| **100,000 条** | 68.269 ms | **~682 ns** | 线性伸缩 |

### 性能分析
* 单条复杂嵌套对象处理耗时约 400–700ns
* 在 10 万级数据下保持线性增长
* 无明显 GC 抖动
* 缓存机制稳定

> ⚠ 性能主要取决于用户自定义 MaskingHandler 的实现复杂度。

## 🚀 性能与架构设计

### 二级缓存机制

为了解决反射带来的性能开销，本框架设计了独特的二级缓存：

1. **L1 局部缓存 (Local Cache)**：
   - **生命周期**：仅在单次 `mask()` 调用链中有效。
   - **作用**：保证在处理大型列表（如 `List<User>` 1000条）时，元数据只解析一次，且绝对线程安全。
2. **L2 全局缓存 (Global Cache)**：
   - **实现**：`ConcurrentHashMap<Class<?>, List<FieldMeta>>`
   - **作用**：跨请求复用解析结果，避免重复反射分析。

### 泛型安全

在处理 `Map` 和 `Bean` 属性赋值时，框架内置了严格的类型检查：

- **运行时探测**：递归前自动判断字段类型，防止泛型擦除导致的 `ClassCastException`。
- **安全赋值**：仅当字段实际值为 `String` 时才执行脱敏，避免误伤 `Integer/Long` 等同名字段。

### 线程安全
* EasyDesensitize.mask() 为无状态静态方法
* 全局缓存基于 ConcurrentHashMap
* 支持并发环境使用

------

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request！

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

------

## 📄 开源协议

本项目基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 协议开源。