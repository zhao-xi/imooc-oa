package com.imooc.oa.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;

public class MD5Utils {
    public static String md5Digest(String source) {
        return DigestUtils.md5Hex(source);
    }

    /**
     * 对原始字符串加盐后生成MD5摘要
     * @param source 原始字符串
     * @param salt 盐值
     * @return MD5摘要
     */
    public static String md5Digest(String source, Integer salt) {
        char[] ca = source.toCharArray();
        for(int i = 0; i < ca.length; i++) {
            ca[i] = (char) (ca[i] + salt);
        }
        String salted = new String(ca);
        return md5Digest(salted);
    }
}
