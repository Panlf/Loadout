package com.loadout.odps.dto;

import lombok.Data;

/**
 * 表对外授权信息
 * @author panlf
 * @date 2026/10/31
 */
@Data
public class TableGrantInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 是否有对外授权
     */
    private Boolean hasExternalGrants;

    /**
     * 被授权主体（如 user/RAM$xxx:sx_sqyy_kf）
     */
    private String principal;

    /**
     * 主体类型（USER / ROLE / GROUP）
     */
    private String principalType;

    /**
     * 被授权主体名称（从 principal 中截取，如 sx_sqyy_kf）
     */
    private String principalName;

    /**
     * 权限（Select / Describe / Read / All 等）
     */
    private String privilege;

    public TableGrantInfo() {
    }

    public TableGrantInfo(String tableName, Boolean hasExternalGrants, String principal,
                          String principalType, String principalName, String privilege) {
        this.tableName = tableName;
        this.hasExternalGrants = hasExternalGrants;
        this.principal = principal;
        this.principalType = principalType;
        this.principalName = principalName;
        this.privilege = privilege;
    }

    @Override
    public String toString() {
        if (Boolean.FALSE.equals(hasExternalGrants)) {
            return tableName + "表 无对外授权";
        }
        return tableName + "表 对外授权给 " + principalName + "(" + principalType + ") 权限:" + privilege;
    }
}
