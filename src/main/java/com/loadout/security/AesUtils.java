package com.loadout.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 加密/解密工具类
 * <p>
 * 支持算法：AES/CBC/PKCS5Padding、AES/ECB/PKCS5Padding
 * 支持密钥长度：128、192、256（需 JCE 无限制策略）
 * 支持输出格式：Base64、Hex
 * </p>
 * @author panlf
 * @date 2026/5/18
 */
public class AesUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORM_CBC = "AES/CBC/PKCS5Padding";
    private static final String TRANSFORM_ECB = "AES/ECB/PKCS5Padding";

    // 默认迭代次数（用于从密码派生密钥）
    private static final int DEFAULT_ITERATION_COUNT = 10000;
    // 密钥长度（位）
    private static final int DEFAULT_KEY_SIZE = 128;
    // 盐长度（字节）
    private static final int SALT_LENGTH = 16;
    // IV 长度（字节，CBC模式固定16）
    private static final int IV_LENGTH = 16;

    private AesUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 检查当前环境是否支持指定的 AES 密钥长度
     *
     * @param keySize 密钥长度（128、192、256）
     * @return true 支持，false 不支持
     */
    public static boolean isKeySizeSupported(int keySize) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
            kg.init(keySize);
            kg.generateKey();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 随机生成密钥 ====================

    /**
     * 生成随机 AES 密钥（默认 128 位）
     *
     * @return Base64 编码的密钥字符串
     */
    public static String generateRandomKey() {
        return generateRandomKey(DEFAULT_KEY_SIZE);
    }

    /**
     * 生成随机 AES 密钥
     *
     * @param keySize 密钥长度（128/192/256）
     * @return Base64 编码的密钥字符串
     * @throws RuntimeException 如果不支持指定的密钥长度
     */
    public static String generateRandomKey(int keySize) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
            kg.init(keySize, new SecureRandom());
            SecretKey secretKey = kg.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成随机密钥失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从密码派生密钥（使用 PBKDF2 算法）
     *
     * @param password 密码字符串
     * @param salt     盐（Base64 格式，可为 null，自动生成）
     * @return 包含密钥(Base64)和盐(Base64)的数组 [key, salt]
     */
    public static String[] deriveKeyFromPassword(String password, String salt) {
        return deriveKeyFromPassword(password, salt, DEFAULT_ITERATION_COUNT, DEFAULT_KEY_SIZE);
    }

    /**
     * 从密码派生密钥（使用 PBKDF2 算法）
     *
     * @param password     密码字符串
     * @param salt         盐（Base64 格式，可为 null，自动生成）
     * @param iteration    迭代次数
     * @param keySize      密钥长度（位）
     * @return 包含密钥(Base64)和盐(Base64)的数组 [key, salt]
     */
    public static String[] deriveKeyFromPassword(String password, String salt, int iteration, int keySize) {
        try {
            byte[] saltBytes;
            if (salt == null || salt.isEmpty()) {
                saltBytes = new byte[SALT_LENGTH];
                new SecureRandom().nextBytes(saltBytes);
            } else {
                saltBytes = Base64.getDecoder().decode(salt);
            }

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iteration, keySize);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            String keyBase64 = Base64.getEncoder().encodeToString(keyBytes);
            String saltBase64 = Base64.getEncoder().encodeToString(saltBytes);
            return new String[]{keyBase64, saltBase64};
        } catch (Exception e) {
            throw new RuntimeException("从密码派生密钥失败: " + e.getMessage(), e);
        }
    }

    // ==================== CBC 模式加密/解密（推荐） ====================

    /**
     * AES/CBC/PKCS5Padding 加密
     *
     * @param plainText 明文
     * @param keyBase64 Base64 编码的密钥
     * @return 密文（Base64 格式，IV 拼接在密文前面）
     */
    public static String encryptCbc(String plainText, String keyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // 生成随机 IV
            byte[] ivBytes = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(ivBytes);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORM_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 将 IV 和密文拼接：IV + 密文
            byte[] result = new byte[ivBytes.length + cipherText.length];
            System.arraycopy(ivBytes, 0, result, 0, ivBytes.length);
            System.arraycopy(cipherText, 0, result, ivBytes.length, cipherText.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("CBC 加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * AES/CBC/PKCS5Padding 解密
     *
     * @param cipherTextBase64 Base64 编码的密文（IV+密文）
     * @param keyBase64        Base64 编码的密钥
     * @return 明文字符串
     */
    public static String decryptCbc(String cipherTextBase64, String keyBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(cipherTextBase64);
            if (combined.length < IV_LENGTH) {
                throw new IllegalArgumentException("密文长度过短，无法提取 IV");
            }

            // 提取 IV 和密文
            byte[] ivBytes = new byte[IV_LENGTH];
            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, ivBytes, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);

            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORM_CBC);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("CBC 解密失败: " + e.getMessage(), e);
        }
    }

    // ==================== ECB 模式加密/解密（不推荐，仅用于兼容旧系统） ====================

    /**
     * AES/ECB/PKCS5Padding 加密（不推荐，ECB 模式不安全）
     *
     * @param plainText 明文
     * @param keyBase64 Base64 编码的密钥
     * @return Base64 编码的密文
     */
    public static String encryptEcb(String plainText, String keyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORM_ECB);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("ECB 加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * AES/ECB/PKCS5Padding 解密
     *
     * @param cipherTextBase64 Base64 编码的密文
     * @param keyBase64        Base64 编码的密钥
     * @return 明文字符串
     */
    public static String decryptEcb(String cipherTextBase64, String keyBase64) {
        try {
            byte[] cipherText = Base64.getDecoder().decode(cipherTextBase64);
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORM_ECB);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("ECB 解密失败: " + e.getMessage(), e);
        }
    }

    // ==================== Hex 编码支持（可选） ====================

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex 字符串长度必须是偶数");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * CBC 加密，输出 Hex 格式
     */
    public static String encryptCbcToHex(String plainText, String keyBase64) {
        String base64Result = encryptCbc(plainText, keyBase64);
        byte[] data = Base64.getDecoder().decode(base64Result);
        return bytesToHex(data);
    }

    /**
     * CBC 解密，输入 Hex 格式
     */
    public static String decryptCbcFromHex(String cipherTextHex, String keyBase64) {
        byte[] data = hexToBytes(cipherTextHex);
        String base64Cipher = Base64.getEncoder().encodeToString(data);
        return decryptCbc(base64Cipher, keyBase64);
    }
}
