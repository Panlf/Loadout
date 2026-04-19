package com.loadout.calcite;

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.ddl.SqlCreateTable;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.tools.ValidationException;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.SqlDialect;

import java.util.*;

/**
 * Calcite的解析SQL
 * @author panlf
 * @date 2026/4/17
 */
@Slf4j
public class CalciteUtils {
    /**
     * 解析 SQL 为抽象语法树 (AST)
     * @param sql SQL 语句
     * @param lex SQL 方言配置（如 Lex.MYSQL、Lex.ORACLE 等）
     * @return SqlNode AST 根节点
     * @throws SqlParseException 解析失败
     */
    public static SqlNode parse(String sql, Lex lex) throws SqlParseException {
        Config config = SqlParser.config()
                .withLex(lex)
                .withCaseSensitive(false)
                .withConformance(SqlConformanceEnum.LENIENT);
                // 移除 .withQuoting(...), .withQuotedCasing(...), .withUnquotedCasing(...)
                // 因为这些已经由 Lex 参数决定
                //.withQuoting(Quoting.BACK_TICK)
                //.withQuotedCasing(Casing.TO_LOWER)
                //.withUnquotedCasing(Casing.TO_LOWER);

        try {
            return SqlParser.create(sql, config).parseQuery();
        } catch (SqlParseException e) {
            log.error("SQL 解析失败: {}", sql, e);
            throw e;
        }
    }

    /**
     * 校验 SQL 语句的语义正确性
     * @param sql SQL 语句
     * @param schema 数据模式
     * @param lex SQL 方言配置
     * @return 校验后的 SqlNode
     * @throws ValidationException 校验失败
     */
    public static SqlNode validate(String sql, SchemaPlus schema, Lex lex) throws ValidationException {
        Config config = SqlParser.config()
                .withLex(lex)
                .withConformance(SqlConformanceEnum.LENIENT)
                .withCaseSensitive(false);
                // 移除 .withQuoting(...), .withQuotedCasing(...), .withUnquotedCasing(...)
                // 因为这些已经由 Lex 参数决定
                //.withQuoting(Quoting.BACK_TICK)
                //.withQuotedCasing(Casing.TO_LOWER)
                //.withUnquotedCasing(Casing.TO_LOWER);

        try {
            SqlNode sqlNode = SqlParser.create(sql, config).parseQuery();
            Planner planner = Frameworks.getPlanner(Frameworks.newConfigBuilder()
                    .parserConfig(config)
                    .defaultSchema(schema)
                    .build());
            return planner.validate(sqlNode);
        } catch (Exception e) {
            log.error("SQL 校验失败: {}", sql, e);
            throw new ValidationException("SQL 校验失败", e);
        }
    }

    /**
     * 从 SqlNode 中提取所有表名（包括 JOIN、子查询中的表）
     *
     * @param sqlNode SqlNode（通常为 SELECT 语句）
     * @return 表名集合（保留原始引号格式，如 `user`）
     */
    public static Set<String> getTableNames(SqlNode sqlNode) {
        SqlSelect select = unwrapSelect(sqlNode);
        if (select == null) {
            log.warn("SqlNode 不是 SELECT 语句，无法提取表名");
            return Collections.emptySet();
        }
        Set<String> tableNames = new LinkedHashSet<>();
        extractTableNames(select.getFrom(), tableNames);
        return tableNames;
    }

    /**
     * 从 SqlNode 中提取 SELECT 字段列表（原始表达式字符串）
     *
     * @param sqlNode SqlNode
     * @return 字段表达式列表（如 "`u`.`id`", "`u`.`name`", "`d`.`dept_name`"）
     */
    public static List<String> getSelectFields(SqlNode sqlNode) {
        SqlSelect select = unwrapSelect(sqlNode);
        if (select == null) {
            log.warn("SqlNode 不是 SELECT 语句，无法提取字段");
            return Collections.emptyList();
        }
        List<String> fields = new ArrayList<>();
        for (SqlNode field : select.getSelectList()) {
            fields.add(field.toString());
        }
        return fields;
    }

    /**
     * 获取 WHERE 条件的 SqlNode（可能为 null）
     *
     * @param sqlNode SqlNode
     * @return WHERE 子句的 SqlNode，若无则返回 null
     */
    public static SqlNode getWhereCondition(SqlNode sqlNode) {
        SqlSelect select = unwrapSelect(sqlNode);
        return select != null ? select.getWhere() : null;
    }

    /**
     * 获取 WHERE 条件的 SQL 字符串（无 "WHERE" 关键字）
     *
     * @param sqlNode SqlNode
     * @return WHERE 条件字符串，若无则返回空字符串
     */
    public static String getWhereConditionSql(SqlNode sqlNode) {
        SqlNode where = getWhereCondition(sqlNode);
        if (where == null) {
            return "";
        }
        // 使用 Calcite 默认方言
        SqlDialect dialect = SqlDialect.DatabaseProduct.CALCITE.getDialect();
        return where.toSqlString(dialect).getSql();
    }

    /**
     * 获取 ORDER BY 的 SqlNode（可能为 null）
     *
     * @param sqlNode SqlNode
     * @return ORDER BY 子句的 SqlNode，若无则返回 null
     */
    public static SqlNode getOrderBy(SqlNode sqlNode) {
        SqlSelect select = unwrapSelect(sqlNode);
        if (select == null) {
            return null;
        }
        // 注意：SqlSelect.getOrderList() 返回 SqlNodeList，不是 SqlNode
        // 为保持 API 一致，此处返回 SqlNodeList（它实现了 SqlNode）
        return select.getOrderList();
    }

    /**
     * 获取 ORDER BY 的 SQL 字符串（无 "ORDER BY" 关键字）
     *
     * @param sqlNode SqlNode
     * @return ORDER BY 字符串，若无则返回空字符串
     */
    public static String getOrderBySql(SqlNode sqlNode) {
        SqlNode orderBy = getOrderBy(sqlNode);
        if (orderBy instanceof SqlNodeList) {
            SqlNodeList list = (SqlNodeList) orderBy;
            if (list.isEmpty()) {
                return "";
            }
            SqlDialect dialect = SqlDialect.DatabaseProduct.CALCITE.getDialect();
            return list.toSqlString(dialect).getSql();
        }
        return "";
    }

    /**
     * 解析 WHERE 条件为结构化列表（每个条件包含操作符、左右操作数）
     *
     * @param sqlNode SqlNode
     * @return 条件列表，每个元素为 Pair<操作符, 条件字符串> 或自定义结构
     */
    public static List<WhereCondition> parseWhereConditions(SqlNode sqlNode) {
        SqlNode where = getWhereCondition(sqlNode);
        if (where == null) {
            return Collections.emptyList();
        }
        List<WhereCondition> conditions = new ArrayList<>();
        where.accept(new WhereConditionVisitor(conditions));
        return conditions;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 从 SqlNode 中解包出 SqlSelect（处理外层 SqlOrderBy 等情况）
     *
     * @param sqlNode 原始节点
     * @return SqlSelect 或 null
     */
    private static SqlSelect unwrapSelect(SqlNode sqlNode) {
        if (sqlNode == null) {
            return null;
        }
        if (sqlNode instanceof SqlSelect) {
            return (SqlSelect) sqlNode;
        }
        if (sqlNode instanceof SqlOrderBy) {
            SqlNode query = ((SqlOrderBy) sqlNode).query;
            if (query instanceof SqlSelect) {
                return (SqlSelect) query;
            }
        }
        // 可扩展处理 SqlUnion、SqlWith 等，但本工具聚焦简单 SELECT
        return null;
    }

    /**
     * 递归提取表名
     *
     * @param fromNode FROM 子句节点
     * @param outSet   存储表名的集合
     */
    private static void extractTableNames(SqlNode fromNode, Set<String> outSet) {
        if (fromNode == null) {
            return;
        }
        if (fromNode instanceof SqlIdentifier) {
            // 单表：user 或 db.user
            outSet.add(fromNode.toString());
        } else if (fromNode instanceof SqlJoin) {
            SqlJoin join = (SqlJoin) fromNode;
            extractTableNames(join.getLeft(), outSet);
            extractTableNames(join.getRight(), outSet);
        } else if (fromNode instanceof SqlBasicCall) {
            SqlBasicCall call = (SqlBasicCall) fromNode;
            if (call.getOperator() instanceof SqlAsOperator) {
                // AS 别名：左侧是真实表或子查询
                SqlNode left = call.getOperandList().get(0);
                extractTableNames(left, outSet);
            } else {
                // 其他情况，遍历所有操作数
                for (SqlNode operand : call.getOperandList()) {
                    extractTableNames(operand, outSet);
                }
            }
        } else if (fromNode instanceof SqlSelect) {
            // 子查询：递归提取子查询中的表名
            extractTableNames(((SqlSelect) fromNode).getFrom(), outSet);
        }
        // 其他类型（如 SqlSnapshot、SqlUnnest）可根据需要扩展
    }

    /**
     * 访问 WHERE 树，收集简单比较条件
     */
    private static class WhereConditionVisitor extends SqlBasicVisitor<Void> {
        private final List<WhereCondition> conditions;

        WhereConditionVisitor(List<WhereCondition> conditions) {
            this.conditions = conditions;
        }

        @Override
        public Void visit(SqlCall call) {
            SqlOperator op = call.getOperator();
            // 处理 AND/OR：继续向下遍历
            if (op == SqlStdOperatorTable.AND || op == SqlStdOperatorTable.OR) {
                for (SqlNode operand : call.getOperandList()) {
                    operand.accept(this);
                }
            } else if (isComparisonOperator(op)) {
                List<SqlNode> operands = call.getOperandList();
                if (operands.size() >= 2) {
                    String left = operands.get(0).toString();
                    String right = operands.get(1).toString();
                    conditions.add(new WhereCondition(op.getName(), left, right));
                } else if (op == SqlStdOperatorTable.IS_NULL || op == SqlStdOperatorTable.IS_NOT_NULL) {
                    // IS NULL / IS NOT NULL 只有左操作数
                    String left = operands.get(0).toString();
                    conditions.add(new WhereCondition(op.getName(), left, "NULL"));
                }
            } else {
                // 其他复杂表达式（函数、IN 等）统一作为字符串保存
                conditions.add(new WhereCondition("COMPLEX", call.toString(), ""));
            }
            return null;
        }

        private boolean isComparisonOperator(SqlOperator op) {
            return op == SqlStdOperatorTable.EQUALS ||
                    op == SqlStdOperatorTable.NOT_EQUALS ||
                    op == SqlStdOperatorTable.GREATER_THAN ||
                    op == SqlStdOperatorTable.GREATER_THAN_OR_EQUAL ||
                    op == SqlStdOperatorTable.LESS_THAN ||
                    op == SqlStdOperatorTable.LESS_THAN_OR_EQUAL ||
                    op == SqlStdOperatorTable.LIKE ||
                    op == SqlStdOperatorTable.IS_NULL ||
                    op == SqlStdOperatorTable.IS_NOT_NULL;
        }
    }

    /**
     * 血缘分析：提取 SQL 中的源表（使用的表）和目标表（结果表）
     * 支持 INSERT、CREATE TABLE AS SELECT、SELECT（仅源表，无目标表）
     *
     * @param sql SQL 语句
     * @param lex SQL 方言配置
     * @return 血缘分析结果，若无目标表则 targetTable 为 null
     * @throws SqlParseException 解析失败
     */
    public static LineageInfo getLineage(String sql, Lex lex) throws SqlParseException {
        SqlNode sqlNode = parse(sql, lex);
        // 解包可能的 SqlOrderBy
        sqlNode = unwrap(sqlNode);
        return getLineage(sqlNode);
    }

    /**
     * 血缘分析：从 SqlNode 中提取源表和目标表
     *
     * @param sqlNode SqlNode 节点
     * @return 血缘分析结果
     */
    public static LineageInfo getLineage(SqlNode sqlNode) {
        Set<String> sourceTables = new LinkedHashSet<>();
        String targetTable = null;

        if (sqlNode instanceof SqlInsert) {
            SqlInsert insert = (SqlInsert) sqlNode;
            // 目标表
            SqlNode target = insert.getTargetTable();
            if (target instanceof SqlIdentifier) {
                targetTable = target.toString();
            }
            // 源表
            SqlNode source = insert.getSource();
            if (source != null) {
                sourceTables.addAll(getTableNames(source));
            }
        }
        else if (sqlNode instanceof SqlCreateTable) {
            SqlCreateTable createTable = (SqlCreateTable) sqlNode;
            // 直接访问 public final 字段
            SqlNode query = createTable.query;
            if (query != null) {
                // 目标表名
                SqlIdentifier name = createTable.name;
                if (name != null) {
                    targetTable = name.toString();
                }
                // 源表：从 query 中提取
                sourceTables.addAll(getTableNames(query));
            }
        }
        else if (sqlNode instanceof SqlSelect) {
            sourceTables.addAll(getTableNames(sqlNode));
        }
        else {
            log.warn("不支持的 SQL 类型：{}，无法进行血缘分析", sqlNode.getClass().getSimpleName());
        }

        return new LineageInfo(sourceTables, targetTable);
    }

    /**
     * 递归解包 SqlOrderBy / SqlWith 等，获取真正的查询节点
     */
    private static SqlNode unwrap(SqlNode node) {
        if (node instanceof SqlOrderBy) {
            return unwrap(((SqlOrderBy) node).query);
        }
        if (node instanceof SqlWith) {
            return unwrap(((SqlWith) node).body);
        }
        return node;
    }
}
