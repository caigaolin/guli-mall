package com.muke.gulimall.auth.utils;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * 随机数工具类
 * @author 木可
 * @version 1.0
 * @date 2021/1/3 20:13
 */
public class RandomUtil {

    private static final Random RANDOM = new Random();

    private static final DecimalFormat FOUR_DF = new DecimalFormat("0000");

    private static final DecimalFormat SIX_DF = new DecimalFormat("000000");

    /**
     * 获取四位数的随机数
     * @return
     */
    public static String getFourBitRandom() {
        return FOUR_DF.format(RANDOM.nextInt(10000));
    }

    /**
     * 获取六位数的随机数
     * @return
     */
    public static String getSixBitRandom() {
        return SIX_DF.format(RANDOM.nextInt(1000000));
    }
}
