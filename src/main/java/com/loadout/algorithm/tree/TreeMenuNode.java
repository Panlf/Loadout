package com.loadout.algorithm.tree;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author panlf
 * @date 2026/5/14
 */
@Data
public class TreeMenuNode {
    /** 节点ID */
    private Integer id;
    /** 父节点ID：顶级节点为0 */
    private Integer parentId;
    /** 节点名称 */
    private String label;
    /** 子节点 直接初始化 */
    private List<TreeMenuNode> children = new ArrayList<>();

    public TreeMenuNode(Integer id, Integer parentId, String label) {
        this.id = id;
        this.parentId = parentId;
        this.label = label;
    }
}