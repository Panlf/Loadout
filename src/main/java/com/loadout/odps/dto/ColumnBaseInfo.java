package com.loadout.odps.dto;

import lombok.Data;

/**
 * ODPS表字段基础信息
 * @author panlf
 * @date 2026/10/31
 */
@Data
public class ColumnBaseInfo {
    //字段名称
    private String columnName;
    //字段类型
    private String type;
    //字段注释
    private String comment;
}

