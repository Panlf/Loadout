package com.loadout.uuid;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.UUIDComparator;
import com.fasterxml.uuid.impl.*;

import java.util.UUID;

/**
 * UUID 工具类
 * 基于 java-uuid-generator (JUG) 5.1.1 实现
 * @author panlf
 * @date 2026/4/22
 */
public class UuidUtils {
    /**
     * 基于时间 + MAC地址
     */
    private static final TimeBasedGenerator TIME_BASED_GENERATOR =
            Generators.defaultTimeBasedGenerator();

    /**
     * 基于随机数
     */
    private static final RandomBasedGenerator RANDOM_BASED_GENERATOR =
            Generators.randomBasedGenerator();

    /**
     * 基于名称的UUID（命名空间 + 名字）
     * 使用 DNS 命名空间作为默认命名空间
     */
    private static final NameBasedGenerator NAME_BASED_GENERATOR =
            Generators.nameBasedGenerator();

    /**
     * 时间排序版本
     * 适用于数据库主键，可减少索引碎片
     */
    private static final TimeBasedReorderedGenerator TIME_BASED_REORDERED_GENERATOR =
            Generators.timeBasedReorderedGenerator();

    /**
     * 基于Unix时间戳 + 随机数
     */
    private static final TimeBasedEpochGenerator TIME_BASED_EPOCH_GENERATOR =
            Generators.timeBasedEpochGenerator();

    /**
     * 每次调用带随机值
     */
    private static final TimeBasedEpochRandomGenerator TIME_BASED_EPOCH_RANDOM_GENERATOR =
            Generators.timeBasedEpochRandomGenerator();

    // 私有构造函数，防止实例化
    private UuidUtils() {
        throw new UnsupportedOperationException("UuidUtils is a utility class and cannot be instantiated");
    }

    // ==================== UUID 生成方法 ====================

    /**
     * UUID（基于时间 + MAC地址）
     * 特点：全球唯一，包含时间戳和MAC地址信息
     * @return 版本1 UUID
     */
    public static UUID generateUuidV1() {
        return TIME_BASED_GENERATOR.generate();
    }

    /**
     * UUID（使用指定网卡接口的MAC地址）
     * @param networkInterfaceName 网卡接口名称（如 "eth0", "en0"）
     * @return UUID
     * @throws RuntimeException 如果无法获取指定网卡的MAC地址
     */
    public static UUID generateUuidV2(String networkInterfaceName) {
        try {
            java.net.NetworkInterface ni = java.net.NetworkInterface.getByName(networkInterfaceName);
            if (ni == null) {
                throw new RuntimeException("Network interface not found: " + networkInterfaceName);
            }
            com.fasterxml.uuid.EthernetAddress ethAddr =
                    com.fasterxml.uuid.EthernetAddress.fromInterface(ni);
            return Generators.timeBasedGenerator(ethAddr).generate();
        } catch (java.net.SocketException e) {
            throw new RuntimeException("Failed to get MAC address from interface: " + networkInterfaceName, e);
        }
    }

    /**
     * UUID（基于随机数）
     * 特点：概率上保证唯一性，最常用的UUID版本
     *
     * @return UUID
     */
    public static UUID generateUuidV3() {
        return RANDOM_BASED_GENERATOR.generate();
    }

    /**
     * UUID（基于名称的UUID）
     * 特点：相同的名称输入总是产生相同的输出
     *
     * @param name 用于生成UUID的字符串
     * @return UUID
     */
    public static UUID generateUuidV4(String name) {
        return NAME_BASED_GENERATOR.generate(name);
    }

    /**
     * UUID（指定命名空间）
     *
     * @param namespace 命名空间UUID
     * @param name 用于生成UUID的字符串
     * @return UUID
     */
    public static UUID generateUuidV5(UUID namespace, String name) {
        return Generators.nameBasedGenerator(namespace).generate(name);
    }

    /**
     * UUID（时间排序版本）
     * 特点：字典序可排序，适用于数据库主键，可减少索引碎片
     *
     * @return UUID
     */
    public static UUID generateUuidV6() {
        return TIME_BASED_REORDERED_GENERATOR.generate();
    }

    /**
     * UUID（基于Unix时间戳 + 随机数）
     * 特点：具有更好的时间排序特性，适用于需要时序性的场景
     *
     * @return UUID
     */
    public static UUID generateUuidV7() {
        return TIME_BASED_EPOCH_GENERATOR.generate();
    }

    /**
     * UUID变体（每次调用带随机值）
     * @return UUID
     */
    public static UUID generateUuidV8() {
        return TIME_BASED_EPOCH_RANDOM_GENERATOR.generate();
    }


    // ==================== UUID 转换方法 ====================

    /**
     * 将UUID转换为字节数组（16字节）
     * 使用JUG优化的转换方法，比JDK原生方式更快
     *
     * @param uuid UUID对象
     * @return 16字节的字节数组
     * @throws NullPointerException 如果uuid为null
     */
    public static byte[] toByteArray(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException("UUID cannot be null");
        }
        return UUIDUtil.asByteArray(uuid);
    }

    /**
     * 将UUID写入指定的字节数组缓冲区
     *
     * @param uuid UUID对象
     * @param buffer 目标字节数组
     * @param offset 写入起始位置
     * @throws NullPointerException 如果uuid或buffer为null
     * @throws IndexOutOfBoundsException 如果buffer剩余空间不足16字节
     */
    public static void toByteArray(UUID uuid, byte[] buffer, int offset) {
        if (uuid == null) {
            throw new NullPointerException("UUID cannot be null");
        }
        if (buffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        if (offset < 0 || offset + 16 > buffer.length) {
            throw new IndexOutOfBoundsException("Buffer too small to write UUID at offset " + offset);
        }
        UUIDUtil.toByteArray(uuid, buffer, offset);
    }

    /**
     * 从字节数组恢复UUID对象
     * 使用JUG优化的转换方法
     *
     * @param bytes 16字节的字节数组
     * @return UUID对象
     * @throws NullPointerException 如果bytes为null
     * @throws IllegalArgumentException 如果bytes长度不是16
     */
    public static UUID fromByteArray(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("Byte array cannot be null");
        }
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Byte array must be exactly 16 bytes, got: " + bytes.length);
        }
        return UUIDUtil.uuid(bytes);
    }

    /**
     * 从字符串解析UUID（带连字符格式）
     * 使用JUG优化的解析方法，比JDK自带的UUID.fromString()更快
     *
     * @param uuidString UUID字符串（标准格式，如 "550e8400-e29b-41d4-a716-446655440000"）
     * @return UUID对象
     * @throws NullPointerException 如果uuidString为null
     * @throws IllegalArgumentException 如果字符串格式无效
     */
    public static UUID fromString(String uuidString) {
        if (uuidString == null) {
            throw new NullPointerException("UUID string cannot be null");
        }
        return UUIDUtil.uuid(uuidString);
    }

    /**
     * 将UUID转换为无连字符的字符串（32位）
     *
     * @param uuid UUID对象
     * @return 32位无连字符的UUID字符串
     * @throws NullPointerException 如果uuid为null
     */
    public static String toCompactString(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException("UUID cannot be null");
        }
        return uuid.toString().replaceAll("-", "");
    }

    /**
     * 将UUID转换为带连字符的标准字符串
     *
     * @param uuid UUID对象
     * @return 标准UUID字符串
     * @throws NullPointerException 如果uuid为null
     */
    public static String toStandardString(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException("UUID cannot be null");
        }
        return uuid.toString();
    }

    // ==================== UUID 比较方法 ====================

    /**
     * 比较两个UUID
     * JDK自带的UUID.compareTo()方法存在缺陷（有符号比较导致排序错误）
     * 本方法使用JUG提供的UUIDComparator进行正确的UUID排序
     *
     * @param first 第一个UUID
     * @param second 第二个UUID
     * @return 如果first小于second返回负数，相等返回0，大于返回正数
     * @throws NullPointerException 如果任一参数为null
     */
    public static int compare(UUID first, UUID second) {
        if (first == null || second == null) {
            throw new NullPointerException("UUIDs cannot be null");
        }
        return UUIDComparator.staticCompare(first, second);
    }

    /**
     * 获取UUID版本号
     *
     * @param uuid UUID对象
     * @return 版本号（1-7）
     * @throws NullPointerException 如果uuid为null
     */
    public static int getVersion(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException("UUID cannot be null");
        }
        return uuid.version();
    }

    /**
     * 获取UUID的变体
     *
     * @param uuid UUID对象
     * @return 变体值
     * @throws NullPointerException 如果uuid为null
     */
    public static int getVariant(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException("UUID cannot be null");
        }
        return uuid.variant();
    }
}
