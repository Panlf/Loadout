package com.loadout.math;


/**
 *
 * @author panlf
 * @date 2026/5/18
 */
public class TestBigDecimalUtils {
    public static void main(String[] args) {
        // 1. 测试修复后的除法（原版本有 Bug）
        String v1 = "10.0";
        String v2 = "3.0";
        System.out.println(BigDecimalUtils.divide(v1, v2, 2));


        // 2. 精度不丢失的加法
        float f1 = 0.05f;
        float f2 = 0.01f;
        System.out.println(BigDecimalUtils.add(f1, f2));
        // 输出: 0.06 (正确)

        // 3. 比较
        System.out.println(BigDecimalUtils.greaterThan("2.5", "2.50")); // false
        System.out.println(BigDecimalUtils.equal("2.5", "2.50"));       // true

        // 4. 字符串输出（适用于数据库存储）
        String sum = BigDecimalUtils.addAsString("99.99", "0.01");
        System.out.println(sum); // 100.00

        // 5. 空值安全
        System.out.println(BigDecimalUtils.add(null, "100")); // 100
        System.out.println(BigDecimalUtils.add(null, null));  // 0

        // 6. 取余
        System.out.println(BigDecimalUtils.remainder("10", "3", 0)); // 1
    }
}
