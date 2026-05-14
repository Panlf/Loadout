package com.loadout.algorithm.tree;

import java.util.*;

/**
 * 树形结构工具类
 * @author panlf
 * @date 2026/5/14
 */
public class TreeMenuUtil {
    /**
     * 构建树形结构 默认根节点parentId=0
     * @param nodeList 原始平铺节点列表
     * @return 树形结构列表
     */
    public static List<TreeMenuNode> buildTree(List<TreeMenuNode> nodeList) {
        return buildTree(nodeList, 0);
    }

    /**
     * 构建树形结构 自定义根节点parentId
     * @param nodeList 原始列表
     * @param rootParentId 根节点父ID
     * @return 树形列表
     */
    public static List<TreeMenuNode> buildTree(List<TreeMenuNode> nodeList, Integer rootParentId) {
        if (nodeList == null || nodeList.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 清空或初始化每个节点的children，避免数据累积
        for (TreeMenuNode node : nodeList) {
            if (node != null) {
                List<TreeMenuNode> children = node.getChildren();
                if (children == null) {
                    node.setChildren(new ArrayList<>());
                } else {
                    children.clear();
                }
            }
        }

        // 2. id -> 节点 映射
        Map<Integer, TreeMenuNode> nodeMap = new HashMap<>(nodeList.size());
        List<TreeMenuNode> rootList = new ArrayList<>();

        for (TreeMenuNode node : nodeList) {
            if (node == null || node.getId() == null) {
                continue;
            }
            nodeMap.put(node.getId(), node);
            if (Objects.equals(node.getParentId(), rootParentId)) {
                rootList.add(node);
            }
        }

        // 3. 绑定父子关系
        for (TreeMenuNode node : nodeList) {
            if (node == null || node.getParentId() == null) {
                continue;
            }
            TreeMenuNode parent = nodeMap.get(node.getParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            }
        }
        return rootList;
    }

    /**
     * 构建树并 按ID升序排序
     */
    public static List<TreeMenuNode> buildTreeAndSort(List<TreeMenuNode> nodeList) {
        List<TreeMenuNode> treeList = buildTree(nodeList);
        sortTreeById(treeList);
        return treeList;
    }

    /**
     * 递归树形所有节点按ID升序排序
     */
    public static void sortTreeById(List<TreeMenuNode> treeList) {
        sortTreeById(treeList, true);
    }

    /**
     * 树形结构 转 平铺列表（递归）
     */
    public static List<TreeMenuNode> tree2Flat(List<TreeMenuNode> treeList) {
        List<TreeMenuNode> flatList = new ArrayList<>();
        if (treeList == null || treeList.isEmpty()) {
            return flatList;
        }
        for (TreeMenuNode node : treeList) {
            flatList.add(node);
            flatList.addAll(tree2Flat(node.getChildren()));
        }
        return flatList;
    }

    /**
     * 过滤树：只保留符合条件的节点及上级链路（原地修改原树，保留节点所有属性）
     * @param treeList 原始树形列表（会被修改）
     * @param keepIdSet 需要保留的节点ID集合
     * @return 过滤后的树形列表（与原列表共享节点对象）
     */
    public static List<TreeMenuNode> filterTree(List<TreeMenuNode> treeList, Set<Integer> keepIdSet) {
        if (treeList == null || treeList.isEmpty() || keepIdSet == null) {
            return new ArrayList<>();
        }
        List<TreeMenuNode> result = new ArrayList<>();
        for (TreeMenuNode node : treeList) {
            if (filterNodeInPlace(node, keepIdSet)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 原地递归过滤节点，返回当前节点是否应被保留
     */
    private static boolean filterNodeInPlace(TreeMenuNode node, Set<Integer> keepIdSet) {
        if (node == null) {
            return false;
        }
        List<TreeMenuNode> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            children.removeIf(child -> !filterNodeInPlace(child, keepIdSet));
        }
        // 当前节点命中 或 存在保留的子节点
        return keepIdSet.contains(node.getId()) || (node.getChildren() != null && !node.getChildren().isEmpty());
    }

    /**
     * 递归树形所有节点按ID排序（可指定顺序）
     * @param treeList  树形节点列表
     * @param ascending true=升序，false=降序
     */
    public static void sortTreeById(List<TreeMenuNode> treeList, boolean ascending) {
        if (treeList == null || treeList.isEmpty()) {
            return;
        }
        Comparator<TreeMenuNode> comparator = Comparator.comparingInt(TreeMenuNode::getId);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        treeList.sort(comparator);
        for (TreeMenuNode node : treeList) {
            sortTreeById(node.getChildren(), ascending);
        }
    }

    /**
     * 递归树形结构按自定义比较器排序（可指定升序/降序）
     * @param treeList   树形节点列表（会直接修改内部顺序）
     * @param comparator 比较器，用于定义节点间的排序规则
     * @param ascending  true=按照比较器自然顺序，false=反转比较器顺序
     */
    public static void sortTree(List<TreeMenuNode> treeList,
                                Comparator<TreeMenuNode> comparator,
                                boolean ascending) {
        if (treeList == null || treeList.isEmpty() || comparator == null) {
            return;
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }
        treeList.sort(comparator);
        for (TreeMenuNode node : treeList) {
            sortTree(node.getChildren(), comparator, ascending);
        }
    }
}
