package com.loadout.security;


/**
 *
 * @author panlf
 * @date 2026/5/18
 */
public class AesUtilsTest {
    public static void main(String[] args) {
        // 1. 随机生成密钥（128位）
        String key = AesUtils.generateRandomKey();
        System.out.println("随机密钥(Base64): " + key);

        // 2. 从密码派生密钥（带盐）
        String password = "mySecretPassword";
        String[] keyAndSalt = AesUtils.deriveKeyFromPassword(password, null);
        String derivedKey = keyAndSalt[0];
        String salt = keyAndSalt[1];
        System.out.println("派生密钥(Base64): " + derivedKey);
        System.out.println("盐(Base64): " + salt);

        // 3. CBC 加密/解密
        String plainText = "Hello,  AES 加密！";
        String encrypted = AesUtils.encryptCbc(plainText, derivedKey);
        System.out.println("CBC 加密结果(Base64): " + encrypted);
        String decrypted = AesUtils.decryptCbc(encrypted, derivedKey);
        System.out.println("CBC 解密结果: " + decrypted);

        // 4. ECB 加密/解密（示例）
        String ecbEncrypted = AesUtils.encryptEcb(plainText, derivedKey);
        System.out.println("ECB 加密结果(Base64): " + ecbEncrypted);
        String ecbDecrypted = AesUtils.decryptEcb(ecbEncrypted, derivedKey);
        System.out.println("ECB 解密结果: " + ecbDecrypted);

        // 5. Hex 输出
        String hexEncrypted = AesUtils.encryptCbcToHex(plainText, derivedKey);
        System.out.println("Hex 加密结果: " + hexEncrypted);
        String hexDecrypted = AesUtils.decryptCbcFromHex(hexEncrypted, derivedKey);
        System.out.println("Hex 解密结果: " + hexDecrypted);
    }
}
