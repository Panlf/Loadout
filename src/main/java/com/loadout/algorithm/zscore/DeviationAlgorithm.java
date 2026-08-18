package com.loadout.algorithm.zscore;


import java.util.*;
import java.util.stream.Collectors;

/**
 * 算法工具类：基于 Z-Score（标准差）的离群点检测
 * @author panlf
 * @date 2026/8/18
 */
public class DeviationAlgorithm {

    /**
     * 过滤出偏离阈值（Z-Score > threshold）的对象
     *
     * @param list     输入数据列表（包含 BaseCompute 或其子类）
     * @param threshold 阈值（几倍标准差），通常传入 2.0 或 3.0
     * @return 偏离阈值的对象列表（保留原类型）
     */
    public List<BaseCompute> filterOutliers(List<? extends BaseCompute> list, double threshold) {
        // 1. 提取有效数值
        List<Double> validValues = extractValidValues(list);
        if (validValues.size() <= 1) {
            return new ArrayList<>(); // 数据不足，无法计算有意义的偏离
        }

        // 2. 计算均值和标准差（使用私有方法，避免重载冲突）
        double mean = meanOfDoubles(validValues);
        double stdDev = stdDevOfDoubles(validValues, mean);

        // 3. 如果标准差为 0，所有值相等，无偏离
        if (stdDev == 0) {
            return new ArrayList<>();
        }

        // 4. 利用 Stream 过滤原始对象（保留引用）
        return list.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getValue() != null)
                .filter(item -> Math.abs(item.getValue() - mean) > threshold * stdDev)
                .collect(Collectors.toList());
    }

    /**
     * 计算有效数值的平均值（入参为对象列表）
     */
    public double calculateMean(List<? extends BaseCompute> list) {
        List<Double> values = extractValidValues(list);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("有效数据为空，无法计算平均值");
        }
        return meanOfDoubles(values);
    }

    /**
     * 计算总体标准差（除以 N）
     */
    public double calculateStdDev(List<? extends BaseCompute> list) {
        List<Double> values = extractValidValues(list);
        if (values.size() <= 1) {
            return 0.0;
        }
        double mean = meanOfDoubles(values);
        return stdDevOfDoubles(values, mean);
    }

    /**
     * 计算总体方差（除以 N）
     */
    public double calculateVariance(List<? extends BaseCompute> list) {
        List<Double> values = extractValidValues(list);
        if (values.size() <= 1) {
            return 0.0;
        }
        double mean = meanOfDoubles(values);
        return values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average() // 除以 N（总体方差）
                .orElse(0.0);
    }

    /**
     * 计算样本标准差（除以 N-1），适用于抽样数据
     */
    public double calculateSampleStdDev(List<? extends BaseCompute> list) {
        List<Double> values = extractValidValues(list);
        if (values.size() <= 1) {
            return 0.0;
        }
        double mean = meanOfDoubles(values);
        double sumSquaredDiff = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum();
        return Math.sqrt(sumSquaredDiff / (values.size() - 1));
    }

    /**
     * 利用 DoubleSummaryStatistics 一次性获取：计数、平均值、最大值、最小值、总和
     */
    public DoubleSummaryStatistics getSummaryStatistics(List<? extends BaseCompute> list) {
        List<Double> values = extractValidValues(list);
        if (values.isEmpty()) {
            return new DoubleSummaryStatistics();
        }
        return values.stream().mapToDouble(Double::doubleValue).summaryStatistics();
    }

    /**
     * 计算中位数（Median）
     */
    public double calculateMedian(List<? extends BaseCompute> list) {
        List<Double> values = extractValidValues(list);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("有效数据为空，无法计算中位数");
        }
        Collections.sort(values);
        int size = values.size();
        if (size % 2 == 1) {
            return values.get(size / 2);
        } else {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        }
    }

    /**
     * 提取所有非空、value 非 null 的有效 Double 值
     */
    private List<Double> extractValidValues(List<? extends BaseCompute> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(BaseCompute::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 私有方法：计算 List<Double> 的平均值
     */
    private double meanOfDoubles(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    /**
     * 私有方法：基于已知均值计算总体标准差
     */
    private double stdDevOfDoubles(List<Double> values, double mean) {
        if (values.size() <= 1) {
            return 0.0;
        }
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average() // 总体方差
                .orElse(0.0);
        return Math.sqrt(variance);
    }
}
