# Loadout

<div align="center">
    <p>一站式 Java 开发实用工具库</p>
    <p>解决日常开发高频问题，整合开源生态 + 自研工具</p>
    <img src="https://img.shields.io/badge/Java-8%2B-brightgreen.svg" alt="Java Version">
    <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
    <img src="https://img.shields.io/badge/Status-Active-brightgreen.svg" alt="Status">
</div>

## 📖 项目介绍
Loadout 是面向实际业务开发的 Java 工具集，旨在沉淀日常工作中验证过的、可复用的工具类，覆盖 SQL 解析、中文处理、拼音转换等常见场景。
- 整合主流开源框架能力，适配业务场景做轻量化封装
- 自研工具类聚焦解决实际开发痛点，兼顾易用性与扩展性
- 持续迭代，逐步覆盖更多开发场景（后续将补充日期、加密、集合、网络等工具）

## 🛠 核心功能
### 已实现能力
| 工具类                | 核心能力                                                                 | 依赖框架/组件                     |
|-----------------------|--------------------------------------------------------------------------|-----------------------------------|
| `CalciteUtils`        | SQL 解析（AST）、语义校验、表名/字段/条件提取、SQL 血缘分析              | Apache Calcite                    |
| `HanLpUtils`          | 中文分词、关键词提取、摘要生成、简繁转换、拼音/首字母提取、停用词过滤     | HanLP（汉语言处理包）            |
| `PinyinUtil`          | 汉字拼音转换（带声调/无声调/数字声调）、首字母/缩写生成、拼音格式化       | houbb-pinyin                      |

### 规划中能力
- 日期时间处理（时区转换、格式化、周期计算）
- 加密解密（MD5/SHA/AES/RSA 封装）
- 集合/数组工具（高效遍历、转换、过滤）
- 网络工具（HTTP 请求、IP/域名解析）
- 反射工具（简化类/方法/字段操作）

