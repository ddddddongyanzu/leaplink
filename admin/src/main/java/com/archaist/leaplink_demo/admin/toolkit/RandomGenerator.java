package com.archaist.leaplink_demo.admin.toolkit;

import java.util.Random;

/**
 * 分组ID随机生成器
 */
public final class RandomGenerator {

    // 字符库，包含所有大写字母、小写字母以及数字
    private static final String BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    // 私有构造方法，防止实例化
    private RandomGenerator() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }

    public static String generateRandom() {
        return generateRandom(6);
    }

    /**
     * 生成随机分组ID
     *
     * @param length 生成多少位
     * @return 分组ID
     */
    public static String generateRandom(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(BASE.length());
            sb.append(BASE.charAt(index));
        }
        return sb.toString();
    }
}
