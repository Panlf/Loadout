package com.loadout.jdbc;

import lombok.Getter;

/**
 * Jdbc 的数据库类型枚举
 * @author panlf
 * @date 2026/10/31
 */
@Getter
public enum JdbcType {

    /** MySQL 驱动 */
    MYSQL("com.mysql.cj.jdbc.Driver", "MySQL驱动"),

    /** PostgreSQL 驱动 */
    POSTGRESQL("org.postgresql.Driver", "PostgreSQL驱动");

    /** 驱动类全限定名 */
    private final String type;

    /** 描述 */
    private final String msg;

    JdbcType(String type, String msg) {
        this.type = type;
        this.msg = msg;
    }

}
