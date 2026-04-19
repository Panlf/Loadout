package com.loadout.calcite;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;

/**
 * @author panlf
 * @date 2026/4/17
 */
public class CalciteTest {
    public static void main(String[] args) throws SqlParseException {
        String sql = "SELECT u.id, u.name, d.dept_name " +
            "FROM `user` u " +
            "LEFT JOIN dept d ON u.dept_id = d.id " +
            "WHERE u.age > 18";

        SqlNode sqlNode = CalciteUtils.parse(sql,Lex.MYSQL);

        // 提取where条件
        System.out.println("查询where字段：" + CalciteUtils.getWhereConditionSql(sqlNode));

        // 提取表名
        System.out.println("涉及表名：" + CalciteUtils.getTableNames(sqlNode));

        //提取字段
        System.out.println("查询字段：" + CalciteUtils.getSelectFields(sqlNode));


        sql = "INSERT INTO rrr SELECT a.id, b.name FROM aaa LEFT JOIN bbb ON aaa.id = bbb.aid";
        sqlNode = CalciteUtils.parse(sql,Lex.MYSQL);
        LineageInfo lineage = CalciteUtils.getLineage(sqlNode);
        System.out.println("结果："+lineage);

        //不支持
        sql = "INSERT OVERWRITE TABLE rrr SELECT a.id, b.name FROM aaa LEFT JOIN bbb ON aaa.id = bbb.aid";
        sqlNode = CalciteUtils.parse(sql,Lex.BIG_QUERY);
        lineage = CalciteUtils.getLineage(sqlNode);
        System.out.println("结果："+lineage);
    }

}
