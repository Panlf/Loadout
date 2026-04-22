package com.loadout.uuid;


/**
 *
 * @author panlf
 * @date 2026/4/22
 */
public class UuidUtilsTest {
    public static void main(String[] args) {
        System.out.println(
                UuidUtils.generateUuidV1()
        );

        System.out.println(
                UuidUtils.generateUuidV6()
        );
    }
}
