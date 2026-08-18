package com.loadout.algorithm;


import com.loadout.algorithm.zscore.BaseCompute;
import com.loadout.algorithm.zscore.DeviationAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 *
 * @author panlf
 * @date 2026/8/18
 */
public class DeviationAlgorithmTest {

    @Test
    public void test001(){
        DeviationAlgorithm algorithm = new DeviationAlgorithm();

        List<BaseCompute> list = Arrays.asList(
                new BaseCompute("A", 10.0),
                new BaseCompute("B", 11.0),
                new BaseCompute("C", 12.0),
                new BaseCompute("D", 12.0),
                new BaseCompute("E", 13.0),
                new BaseCompute("F", 100.0)
        );

        // 统计信息
        DoubleSummaryStatistics stats = algorithm.getSummaryStatistics(list);
        System.out.println("计数: " + stats.getCount());
        System.out.println("平均值: " + stats.getAverage());
        System.out.println("中位数: " + algorithm.calculateMedian(list));
        System.out.println("总体标准差: " + algorithm.calculateStdDev(list));
        System.out.println("总体方差: " + algorithm.calculateVariance(list));
        System.out.println("样本标准差: " + algorithm.calculateSampleStdDev(list));

        // 过滤离群点（阈值 2.0）
        List<BaseCompute> outliers = algorithm.filterOutliers(list, 2.0);
        System.out.println("偏离 2 个标准差的对象: " + outliers);
        // 输出：[BaseCompute{type='F', value=100.0}]
    }
}
