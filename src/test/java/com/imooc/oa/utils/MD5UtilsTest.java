package com.imooc.oa.utils;

import junit.framework.TestCase;

public class MD5UtilsTest extends TestCase {

    public void testMd5Digest() {
        System.out.println(MD5Utils.md5Digest("test", 196));
        System.out.println(MD5Utils.md5Digest("test", 197));
    }
}