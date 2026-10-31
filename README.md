# Loadout

<div align="center">
    <p>一站式 Java 开发实用工具库</p>
    <p>解决日常开发高频问题，整合开源生态 + 自研工具</p>
    <img src="https://img.shields.io/badge/Java-8%2B-brightgreen.svg" alt="Java Version">
    <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
    <img src="https://img.shields.io/badge/Status-Active-brightgreen.svg" alt="Status">
</div>

## 📖 项目介绍
Loadout 是面向实际业务开发的 Java 工具集，旨在沉淀日常工作中验证过的、可复用的工具类，覆盖 SQL 解析、数据查询、中文处理、加密、重试、算法等常见场景。
- 整合主流开源框架能力，适配业务场景做轻量化封装
- 自研工具类聚焦解决实际开发痛点，兼顾易用性与扩展性
- 持续迭代，逐步覆盖更多开发场景

## 🛠 核心功能
### 当前实现功能
| 工具类                | 核心能力                                                                 | 依赖框架/组件             |
|-----------------------|----------------------------------------------------------------------|---------------------|
| `CalciteUtils`        | SQL 解析（AST）、语义校验、表名/字段/条件提取、SQL 血缘分析                                 | Apache Calcite      |
| `JdbcUtils`           | JDBC 连接构建、参数化查询（防注入，Map / Bean 两种结果形态）、DDL/DML 执行、库/表/字段注释元信息查询、资源静默关闭 | Java原生 + commons-lang3 + Jackson |
| `OdpsUtils`           | ODPS(MaxCompute) SQL 同步/异步执行、普通/Tunnel 大结果集查询（Map / Bean 两种形态）、表元信息与字段信息、表/视图 DDL 生成、对外授权信息解析、文件量统计 | odps-sdk-core + fastjson2 + commons-lang3 |
| `MapBeanUtils`        | Map 与 Bean 互转（单个/列表），下划线键名自动转驼峰映射，未知字段忽略                              | Jackson             |
| `HanLpUtils`          | 中文分词、关键词提取、摘要生成、简繁转换、拼音/首字母提取、停用词过滤                                  | HanLP（汉语言处理包）       |
| `PinyinUtil`          | 汉字拼音转换（带声调/无声调/数字声调）、首字母/缩写生成、拼音格式化                                  | houbb-pinyin        |
|`WordCloudUtils`| 中文分词、自定义尺寸、颜色、字体、圆形背景、输出词云PNG图片                                      | kumo-core           |
|`JsonUtils`| 常用的序列化、反序列化、泛型支持、日期格式化、JSON 校验、格式化输出等功能                              | fastjson2           |
|`JacksonUtils`| 基于 Jackson 的对象转JSON、JSON转对象/List/Map、格式化、空值过滤等                       | Jackson             |
|`UuidUtils`| 基于 java-uuid-generator (JUG) 实现UUID生成                                | java-uuid-generator |
|`OkHttpUtils`| 封装了 GET、POST（JSON/表单）、文件上传/下载等常用操作，支持连接池、超时配置、日志拦截器、HTTPS 忽略证书（测试用）等特性 | okhttp3             |
|`DateTimeUtils`| 提供日期转换、格式化、计算、农历、节气、节日等功能                                            | xk-time             |
|`FileMD5Util`| 提供文件 MD5 快速计算功能                                                      | Java原生              |
|`FileSearchUtils`| 递归查找目录下所有文件，文件搜索工具                                                   | Java原生              |
|`StringUtils`| 空判断、字符串常用操作的自研轻量封装                                                   | Java原生              |
|`BigDecimalUtils`| 任意对象（String/Number等）安全转换 BigDecimal、精确数值计算                            | Java原生              |
|`AesUtils`| AES 加密/解密，支持 CBC/ECB 模式、128/192/256 密钥长度、Base64/Hex 输出                | Java原生              |
|`RetryExecutor`| 重试执行器（同步 + 异步），配套等待策略（WaitStrategy）与重试监听器（RetryListener）             | Java原生              |
|`GithubUtils`| GitHub 仓库信息查询（最近 commit 等），集成 JsonUtils 解析                            | okhttp3 + fastjson2 |
|`TreeMenuUtil`| 平铺节点列表构建树形结构（菜单树等）                                                   | Java原生              |
|`Trie`| Trie 字典树（前缀匹配）                                                       | Java原生              |
|`LRUCache` / `LinkedLruCache`| LRU 缓存：手写双向链表线程安全版 + 基于 LinkedHashMap 的极简版                           | Java原生              |
|`ParseNestJson`| 递归解析复杂嵌套 JSON 字符串                                                    | Java原生              |
|`DeviationAlgorithm`| 基于 Z-Score（标准差）的数据离群点检测与过滤                                           | Java原生              |

