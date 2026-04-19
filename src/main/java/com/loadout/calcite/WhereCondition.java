package com.loadout.calcite;

import lombok.Data;

/**
 * WHERE 条件结构化信息
 * @author panlf
 * @date 2026/4/17
 */
@Data
public class WhereCondition {
    private final String operator;
    private final String left;
    private final String right;

    public WhereCondition(String operator, String left, String right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }
}
