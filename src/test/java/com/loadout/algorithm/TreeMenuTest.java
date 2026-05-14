package com.loadout.algorithm;


import com.loadout.algorithm.tree.TreeMenuNode;
import com.loadout.algorithm.tree.TreeMenuUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author panlf
 * @date 2026/5/14
 */
public class TreeMenuTest {
    List<TreeMenuNode> originList = new ArrayList<>();

    @BeforeEach
    public void buildTreeMenu(){
        originList.add(new TreeMenuNode(1,0,"系统管理"));
        originList.add(new TreeMenuNode(2,1,"用户管理"));
        originList.add(new TreeMenuNode(4,2,"新增用户"));
        originList.add(new TreeMenuNode(3,1,"角色管理"));
    }

    @Test
    public void testBuildTree(){
        List<TreeMenuNode> tree = TreeMenuUtil.buildTree(originList);
        System.out.println(tree);
    }

    @Test
    public void testBuildTreeAndSort(){
        List<TreeMenuNode> sortTree = TreeMenuUtil.buildTreeAndSort(originList);
        System.out.println(sortTree);
    }

    @Test
    public void testSortBySelf(){
        // 1. 先构建树（默认根节点 parentId = 0）
        List<TreeMenuNode> treeList = TreeMenuUtil.buildTree(originList);

        // 2. 再对树形结构进行降序排序
        TreeMenuUtil.sortTreeById(treeList, false);

        // 3. 输出树形结构（注意：需要重写 TreeMenuNode 的 toString 方法，递归打印 children）
        System.out.println(treeList);
    }
}
