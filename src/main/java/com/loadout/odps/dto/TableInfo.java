package com.loadout.odps.dto;

import lombok.Data;

import java.util.List;

/**
 * ODPS表基础信息
 * @author panlf
 * @date 2026/10/31
 */
@Data
public class TableInfo {
    private String tableName;
    private String tableComment;
    private Integer type; // 真实表 1 视图 10
    private String project;
    private List<ColumnBaseInfo> columnBaseInfoList;
}
