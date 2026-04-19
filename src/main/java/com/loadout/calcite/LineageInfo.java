package com.loadout.calcite;


import lombok.Data;

import java.util.Set;

/**
 * 血缘分析结果：包含源表集合和目标表名
 * @author panlf
 * @date 2026/4/19
 */
@Data
public class LineageInfo {
    private final Set<String> sourceTables;   // 使用的表（源表）
    private final String targetTable;         // 结果表（目标表）
}
